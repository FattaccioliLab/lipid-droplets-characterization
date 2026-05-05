package fr.sorbonne_universite.ldc.model.workers;

import javax.swing.SwingWorker;

import fr.sorbonne_universite.ldc.model.AnalysisSettings;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.filter.ParticleAnalyzer;

/**
 * {@link SwingWorker} that take care of processing measurements and showing them.
 * <p>
 * Its {@code doInBackground} method generates the results table by setting the measurements of the Analyzer according 
 * to those chosen by the user and then starting the particles analyzer. 
 * </p>
 */
public class MeasuresPreviewWorker extends SwingWorker<Void, Void>{
    
	private AnalysisSettings settings;
    private ImagePlus img;
    
    /**
     * Creates a {@code MeasuresPreviewWorker}.
     * @param settings						The plugin settings.
     * @param img 							The current image to consider.
     */
    public MeasuresPreviewWorker(AnalysisSettings settings, ImagePlus img) {
    	this.settings = settings;
    	this.img = img;
    }

	@Override
	protected Void doInBackground() throws Exception {
    	
    	// set options for Particles Analyzer
    	int options = 0;
    	options += ParticleAnalyzer.SHOW_OUTLINES; // show outlines of every particles in each images of the stack
    	if (settings.analyseExcludeOnEdgesEnabled()) options += ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
    	if (settings.analyseIncludeHolesEnabled()) options += ParticleAnalyzer.INCLUDE_HOLES;
    	
    	// get current image
    	if (img == null) {
    		IJ.showMessage("Please open an image first (File > Open)");
    		return null;
    	}
    	
    	double pxMinSize = settings.getAnalyseMinSize();
    	double pxMaxSize = settings.getAnalyseMaxSize();
    	
    	Calibration calibration = settings.getCalibration();
    	if (settings.isCalibrated() && calibration != null && calibration.scaled()) {
    		// convert the unit² in px²
    		double pixelArea = calibration.pixelWidth * calibration.pixelHeight;
    		pxMinSize = settings.getAnalyseMinSize() / pixelArea;
    		if (settings.getAnalyseMaxSize() != Double.MAX_VALUE) {
    			pxMaxSize = settings.getAnalyseMaxSize() / pixelArea;
    		}
    	}
    	
    	ParticleAnalyzer pa = new ParticleAnalyzer(options, 0, null, pxMinSize, pxMaxSize, 
    			settings.getAnalyseMinCircularity(), settings.getAnalyseMaxCircularity());
    	pa.setHideOutputImage(true);
    	
    	// save the original calibration
    	Calibration backupCal = img.getCalibration();
    	if (backupCal != null) {
    		backupCal = backupCal.copy();	// deep copy of the original calibration
    	}
    	
    	try {
    		
    		// apply the given calibration
    		if (settings.isCalibrated() && calibration != null) {		
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
