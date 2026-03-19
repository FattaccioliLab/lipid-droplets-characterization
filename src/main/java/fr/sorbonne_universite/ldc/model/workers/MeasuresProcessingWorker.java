package fr.sorbonne_universite.ldc.model.workers;

import javax.swing.SwingWorker;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;

/**
 * {@link SwingWorker} that take care of processing measurements and showing them.
 * <p>
 * Its {@code doInBackground} method generates the results table by setting the measurements of the Analyzer according 
 * to those chosen by the user and then starting the particles analyzer. 
 * </p>
 */
public class MeasuresProcessingWorker extends SwingWorker<Void, Void>{
    
	private double minSize;
	private double maxSize;
	private double minCircularity;
	private double maxCircularity;
    private boolean excludeOnEdgesEnabled;
    private boolean showAreaEnabled;
    private boolean showMeanEnabled;
    private boolean showMedianEnabled;
    private boolean showIntegratedDensityEnabled;
    private boolean showCircularityEnabled;
    private ImagePlus img;
    
    // threshold for the circularity, to define if a particle is isolated or not
    private double circularityThreshold = 0.5;
    
    /**
     * Creates a {@code MeasuresProcessingWorker}.
     * @param minSize minimum particle size (px²).
     * @param maxSize maximum particle size (px²).
     * @param minCircularity minimum particle circularity.
     * @param maxCircularity maximum particle circularity.
     * @param excludeOnEdgesEnabled Particle Analyzer option.
     * @param showAreaEnabled True if the 'Area' column must be shown in the results.
     * @param showMedianEnabled True if the 'Median' column must be shown in the results.
     * @param showMeanEnabled True if the 'Mean' column must be shown in the results.
     * @param showIntegratedDensityEnabled True if the 'IntegratedDensity' column must be shown in the results.
     * @param showCircularityEnabled True if the 'Circularity' column is shown must be the results.
     * @param img The current image to consider.
     */
    public MeasuresProcessingWorker(double minSize, double maxSize, double minCircularity, double maxCircularity, boolean excludeOnEdgesEnabled, 
    		boolean showAreaEnabled, boolean showMedianEnabled, boolean showMeanEnabled, boolean showIntegratedDensityEnabled, boolean showCircularityEnabled,
    		ImagePlus img) {
    	this.minSize = minSize;
    	this.maxSize = maxSize;
    	this.minCircularity = minCircularity;
    	this.maxCircularity = maxCircularity;
    	this.excludeOnEdgesEnabled = excludeOnEdgesEnabled;
    	this.showAreaEnabled = showAreaEnabled;
    	this.showMedianEnabled = showMedianEnabled;
    	this.showMeanEnabled = showMeanEnabled;
    	this.showIntegratedDensityEnabled = showIntegratedDensityEnabled;
    	this.showCircularityEnabled = showCircularityEnabled;
    	this.img = img;
    }

	@Override
	protected Void doInBackground() throws Exception {
       	// set measurements
    	int measurements = 0;
    	measurements += Measurements.CENTROID; // center of the particle (x,y)
    	measurements += Measurements.STACK_POSITION; // image position in stack (z)  	
    	if (showAreaEnabled) measurements += Measurements.AREA;
    	if (showMeanEnabled) measurements += Measurements.MEAN;
    	if (showMedianEnabled) measurements += Measurements.MEDIAN;
    	if (showIntegratedDensityEnabled) measurements += Measurements.INTEGRATED_DENSITY;
    	if (showCircularityEnabled) measurements += Measurements.CIRCULARITY;
    	
    	// add BX, BY, Width and Height to the result table, used to define if the particle is on the edge
    	measurements += Measurements.RECT;
    	
    	// set particlesAnalyzer
    	ResultsTable rt = ResultsTable.getResultsTable();
    	
    	// set options for Particles Analyzer
    	int options = 0;
    	// options += ParticleAnalyzer.ELLIPSE;  // show overlay of detected particles
    	// options += ParticleAnalyzer.OVERLAY; // crash the program
    	// options += ParticleAnalyzer.SHOW_OUTLINES; // show outlines of every particles in each images of the stack
    	// options += ParticleAnalyzer.DISPLAY_SUMMARY; // show statistics
    	if (excludeOnEdgesEnabled) {
    		options += ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
    	}
    	
    	ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, minSize, maxSize, minCircularity, maxCircularity);

    	// get current image
    	if (img == null) {
    		IJ.showMessage("Please open an image first (File > Open)");
    		return null;
    	}
    	
    	// analyze each image of the stack
    	boolean success = true;
    	int stackSize = img.getStackSize();
    	for (int i = 1; i <= stackSize; i++) {
    	    img.setSlice(i);
    	    success = pa.analyze(img);
    	    if (!success) {
    	    	break;
    	    }
    	}
    	
    	// detect for each particle if it is isolated
    	for (int row = 0; row < rt.getCounter(); row++) {
    		int isIsolated = 0;
    		
    		// check if the particle touches the edge of the image
    		boolean touchesEdge = false;
    		if (rt.columnExists("BX") && rt.columnExists("BY") && rt.columnExists("Width") && rt.columnExists("Height")) {
    			double bx = rt.getValue("BX", row);
    			double by = rt.getValue("BY", row);
    			double width = rt.getValue("Width", row);
    			double height = rt.getValue("Height", row);
    			if (bx <= 0 || by <= 0 || (bx + width) >= img.getWidth() || (by + height) >= img.getHeight()) {
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
    	
    	// cleaning the results table : remove unused columns
    	if (rt.columnExists("BX")) rt.deleteColumn("BX");
        if (rt.columnExists("BY")) rt.deleteColumn("BY");
        if (rt.columnExists("Width")) rt.deleteColumn("Width");
        if (rt.columnExists("Height")) rt.deleteColumn("Height");
    	
		// close the ROI manager window that appear with the ParticlesAnalyzer WIP
    	RoiManager rm = RoiManager.getInstance();
        if (rm != null) {
        	rm.setVisible(false);
        	rm.reset();
        	rm.close();
        }
    	
    	return null;
	}
	
}
