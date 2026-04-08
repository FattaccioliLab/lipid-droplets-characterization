package fr.sorbonne_universite.ldc.model.workers;

import java.io.File;

import javax.swing.SwingWorker;

import fr.sorbonne_universite.ldc.model.AnalysisSettings;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

/**
 * Created by a {@link BatchWorker}, applies the whole workflow to a specific input file, returns the corresponding
 * {@link ResultsTable} after doing the particle analysis on the file.
 */
public class BatchFileWorker extends SwingWorker<ResultsTable,Void>{

	private AnalysisSettings settings;
	private File inputDirectory;
	private File inputFile;
	
    // threshold for the circularity, to define if a particle is isolated or not, tmp
    private double circularityThreshold = 0.5;
	
	/**
	 * Creates a worker applying the workflow on the given {@code inputFile}.
	 * @param settings			Particle analysis settings.
	 * @param inputDirectory 	The input directory root.
	 * @param inputFile			The file on which apply the analysis.
	 */
	public BatchFileWorker(AnalysisSettings settings, File inputDirectory, File inputFile) {
		this.settings = settings;
		this.inputDirectory = inputDirectory;
		this.inputFile = inputFile;
	}
	
	@Override
	protected ResultsTable doInBackground() throws Exception {
		
		// Opens the image to treat
		ImagePlus image = IJ.openImage(inputFile.getAbsolutePath());
        if (image == null || isCancelled()) return null;
        
        // PREPROCESSING
        
        ContrastEnhancer ce = new ContrastEnhancer();
        if (settings.enhanceContrastEnabled()) {
        	ce.stretchHistogram(image.getProcessor(), settings.getEnhanceSaturatedPercent());
        }
        
        if (settings.medianFilterEnabled()) {
        	ImageStack stack = image.getImageStack();
            final RankFilters rf = new RankFilters();
            final int n = stack.getSize();

            for (int z = 1; z <= n; z++) {
            	if (isCancelled()) return null;   
            	rf.rank(stack.getProcessor(z), settings.getMedianRadius(), RankFilters.MEDIAN);
            }
        }
        
        // THRESHOLDING
		if (isCancelled()) return null;
        
        String thrsMethod = settings.getThresholdMethod();
        ImageProcessor ip = image.getProcessor();
        if (thrsMethod.equals("Manual")) {
        	ip.setThreshold(settings.getThresholdMinValue(), settings.getThresholdMaxValue(), ImageProcessor.RED_LUT);
        } else {
            ip.setAutoThreshold(thrsMethod, settings.thresholdDarkBackgroundEnabled(), ImageProcessor.RED_LUT);
        }
        
        // 1. Get threshold from the image
        double min = ip.getMinThreshold();
        double max = ip.getMaxThreshold();
        
        // If the image is thresholded (normally always the case)
        if (min != ImageProcessor.NO_THRESHOLD) {
            ImageStack thrsStack = image.getImageStack(); // getStack ?
            int width = thrsStack.getWidth();
            int height = thrsStack.getHeight();
            int nSlices = thrsStack.getSize();
            
            // 2. Create a NEW empty stack for the 8-bit binary mask
            ImageStack binaryStack = new ImageStack(width, height);
            
            // 3. Process each slice
            for (int i = 1; i <= nSlices; i++) {
        		if (isCancelled()) return null;
                ImageProcessor thrsIp = thrsStack.getProcessor(i);
                thrsIp.setThreshold(min, max, ImageProcessor.NO_LUT_UPDATE); // Temporarily apply threshold to the slice mathematically
                ImageProcessor maskIp = thrsIp.createMask();  // Create an 8-bit mask (ByteProcessor) from the thresholded slice
                binaryStack.addSlice(thrsStack.getSliceLabel(i), maskIp); // Add the 8-bit mask to our new stack
                thrsIp.resetThreshold(); // Reset threshold on original slice to avoid messing up the original image
            }
            
            // 4. Create a new ImagePlus with the 8-bit stack
            ImagePlus binaryImg = new ImagePlus(image.getShortTitle() + "_Binary", binaryStack);
            image.setDimensions(
            		image.getNChannels(),
            		image.getNSlices(),
            		image.getNFrames()
                );
            
            // Copy calibration (pixel size, mm/px, etc.)
            binaryImg.setCalibration(image.getCalibration());
            
            // Restore the red preview overlay on the original image's current slice
            image.getProcessor().setThreshold(min, max, ImageProcessor.RED_LUT);
        }
        
        // PARTICLE ANALYSIS
		if (isCancelled()) return null;
        
       	// Set measurements
    	int measurements = 0;
    	measurements += Measurements.CENTROID; // center of the particle (x,y)
    	measurements += Measurements.STACK_POSITION; // image position in stack (z)  	
    	if (settings.showAreaEnabled()) measurements += Measurements.AREA;
    	if (settings.showMeanEnabled()) measurements += Measurements.MEAN;
    	if (settings.showMedianEnabled()) measurements += Measurements.MEDIAN;
    	if (settings.showIntegratedDensityEnabled()) measurements += Measurements.INTEGRATED_DENSITY;
    	if (settings.showCircularityEnabled()) measurements += Measurements.CIRCULARITY;
		
    	ResultsTable rt = new ResultsTable();
    	int options = 0;
    	if (settings.analyseExcludeOnEdgesEnabled()) options += ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
    	
    	ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, 
    			settings.getAnalyseMinSize(), settings.getAnalyseMaxSize(), 
    			settings.getAnalyseMinCircularity(), settings.getAnalyseMaxCircularity());
    	
    	// analyze each image of the stack
    	boolean success = true;
    	int stackSize = image.getStackSize();
    	for (int i = 1; i <= stackSize; i++) {
    		if (isCancelled()) return null;
    		image.setSlice(i);
    	    success = pa.analyze(image);
    	    if (!success) return null;
    	}
    	
    	// detect for each particle if it is isolated
    	if (rt.columnExists("Circ.")) { 
	    	for (int row = 0; row < rt.getCounter(); row++) {
	    		int isIsolated = 0;
	    		
	    		// check if the particle touches the edge of the image
	    		boolean touchesEdge = false;
	    		if (rt.columnExists("BX") && rt.columnExists("BY") && rt.columnExists("Width") && rt.columnExists("Height")) {
	    			double bx = rt.getValue("BX", row);
	    			double by = rt.getValue("BY", row);
	    			double width = rt.getValue("Width", row);
	    			double height = rt.getValue("Height", row);
	    			if (bx <= 0 || by <= 0 || (bx + width) >= image.getWidth() || (by + height) >= image.getHeight()) {
	    	            touchesEdge = true;
	    	        }
	    		}
	    		
	    		// check if the particle circularity is greater than the threshold, then it is isolated
	    		if (!touchesEdge && rt.columnExists("Circ.")) {
	    			if (rt.getValue("Circ.", row) >= circularityThreshold) isIsolated = 1;
	    		}
	    		
	    		// add the attribute to the particle
	    		rt.setValue("is_isolated", row, isIsolated);
	    	}
    	}

    	// cleaning the results table : remove unused columns
    	if (rt.columnExists("BX")) rt.deleteColumn("BX");
        if (rt.columnExists("BY")) rt.deleteColumn("BY");
        if (rt.columnExists("Width")) rt.deleteColumn("Width");
        if (rt.columnExists("Height")) rt.deleteColumn("Height");
        if (!settings.showCircularityEnabled()){
        	rt.deleteColumn("Circ."); // remove if the circularity parameter isn't activated
        	rt.deleteColumn("AR");
        	rt.deleteColumn("Round");
        	rt.deleteColumn("Solidity");
        }
        
        // Adds the relative path of the file (or just filename if in root directory)
        String relativeFilePath = getRelativeFilePath();
        for (int row = 0; row < rt.size(); row++) {
        	rt.setLabel("", row); // Empty label for normal rows
        	rt.setValue("Filename", row, relativeFilePath);
        }
    	
		return rt;
	}

	/**
	 * Gets the relative path of a file from the input directory.
	 * 
	 * @return The relative path (e.g., "file.tif" or "subfolder/file.tif")
	 */
	private String getRelativeFilePath() {
		try {
			// Get absolute paths
			String rootPath = inputDirectory.getAbsolutePath();
			String filePath = inputFile.getAbsolutePath();

			// Check if file is inside the input directory
			if (filePath.startsWith(rootPath)) {
				// Remove the root path and leading separator
				String relativePath = filePath.substring(rootPath.length());

				// Remove leading slash or backslash
				if (relativePath.startsWith(File.separator)) {
					relativePath = relativePath.substring(1);
				}

				return relativePath;
			} else {
				// Fallback: just return filename if not in expected directory
				return inputFile.getName();
			}
		} catch (Exception e) {
			// In case of error, return just the filename
			return inputFile.getName();
		}
	}
	
}
