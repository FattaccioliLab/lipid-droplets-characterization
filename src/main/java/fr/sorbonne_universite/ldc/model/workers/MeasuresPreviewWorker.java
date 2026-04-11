package fr.sorbonne_universite.ldc.model.workers;

import javax.swing.SwingWorker;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.plugin.filter.ParticleAnalyzer;

/**
 * {@link SwingWorker} that take care of processing measurements and showing them.
 * <p>
 * Its {@code doInBackground} method generates the results table by setting the measurements of the Analyzer according 
 * to those chosen by the user and then starting the particles analyzer. 
 * </p>
 */
public class MeasuresPreviewWorker extends SwingWorker<Void, Void>{
    
	private boolean isCalibrated;
	private Calibration calibration;
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
    
    /**
     * Creates a {@code MeasuresPreviewWorker}.
     * @param isCalibrated					Boolean that tell if the image there is a calibration given for the image
     * @param Calibration					Given calibration.
     * @param minSize 						Minimum particle size (μm² IsCalibrated is true, otherwise px²).
     * @param maxSize 						Maximum particle size (μm² IsCalibrated is true, otherwise px²).
     * @param minCircularity 					Minimum particle circularity.
     * @param maxCircularity 					Maximum particle circularity.
     * @param excludeOnEdgesEnabled 			Particle Analyzer option.
     * @param showAreaEnabled 					True if the 'Area' column must be shown in the results.
     * @param showMedianEnabled 				True if the 'Median' column must be shown in the results.
     * @param showMeanEnabled 					True if the 'Mean' column must be shown in the results.
     * @param showIntegratedDensityEnabled 		True if the 'IntegratedDensity' column must be shown in the results.
     * @param showCircularityEnabled 			True if the 'Circularity' column is shown must be the results.
     * @param img 								The current image to consider.
     */
    public MeasuresPreviewWorker(
    		boolean isCalibrated,
    		Calibration calibration,
    		double minSize,
    		double maxSize,
    		double minCircularity,
    		double maxCircularity,
    		boolean excludeOnEdgesEnabled,
    		boolean showAreaEnabled,
    		boolean showMedianEnabled,
    		boolean showMeanEnabled,
    		boolean showIntegratedDensityEnabled,
    		boolean showCircularityEnabled,
    		ImagePlus img) {
    	this.isCalibrated = isCalibrated;
    	this.calibration = calibration;
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
    	
    	// set options for Particles Analyzer
    	int options = 0;
    	options += ParticleAnalyzer.SHOW_OUTLINES; // show outlines of every particles in each images of the stack
    	if (excludeOnEdgesEnabled) {
    		options += ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
    	}
    	
    	// get current image
    	if (img == null) {
    		IJ.showMessage("Please open an image first (File > Open)");
    		return null;
    	}
    	
    	double pxMinSize = minSize;
    	double pxMaxSize = maxSize;
    	
    	if (isCalibrated && calibration != null && calibration.scaled()) {
    		// convert the µm² in px²
    		double pixelArea = calibration.pixelWidth * calibration.pixelHeight;
    		pxMinSize = minSize / pixelArea;
    		if (maxSize != Double.MAX_VALUE) {
    			pxMaxSize = maxSize / pixelArea;
    		}
    	}
    	
    	ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, null, pxMinSize, pxMaxSize, minCircularity, maxCircularity);
    	pa.setHideOutputImage(true);
    	
    	// save the original calibration
    	Calibration backupCal = img.getCalibration();
    	if (backupCal != null) {
    		backupCal = backupCal.copy();	// deep copy of the original calibration
    	}
    	
    	try {
    		
    		// apply the given calibration
    		if (isCalibrated && calibration != null) {		
                img.setCalibration(calibration);
            } else {
            	//  force a neutral calibration 1 pixel / 1 pixel
                Calibration pixelCal = new ij.measure.Calibration();
                pixelCal.pixelWidth = 1.0;
                pixelCal.pixelHeight = 1.0;
                pixelCal.setUnit("pixel");
                img.setCalibration(pixelCal);
            }
    		
    		// create an empty stack with the same dimensions as the original one, to keep in it the outlines images
        	ImageStack outlinesStack = new ImageStack(img.getWidth(), img.getHeight());
        	
        	// analyze each image of the stack
        	boolean success = true;
        	int stackSize = img.getStackSize();
        	for (int i = 1; i <= stackSize; i++) {
        	    img.setSlice(i);
        	    success = pa.analyze(img);
        	    if (!success) {
        	    	break;
        	    }
        	    
        	    // adding the outlines to the stack
        	    ImagePlus outImg = pa.getOutputImage();
                if (outImg != null) {
                    outlinesStack.addSlice(img.getStack().getSliceLabel(i), outImg.getProcessor());
                }
        	}
        	
        	// show the preview
        	if (outlinesStack.getSize() > 0) {
                ImagePlus combinedOutlines = new ImagePlus("Outlines of " + img.getTitle(), outlinesStack);
                combinedOutlines.show();
            }
    		
    	} finally {
    		// ensure that the original calibration is restored in the end. 
    		if (backupCal != null) {
    			img.setCalibration(backupCal);
    		}
    	}
    	
    	return null;
	}
}
