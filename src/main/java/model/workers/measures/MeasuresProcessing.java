package model.workers.measures;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;

/**
 * Class that take care of processing measurements 
 * */
public class MeasuresProcessing {
	
	/**
    *  method that generate the results table by setting the measurements of the Analyzer according to those chosen by the user 
    *  and then starting the particles analyzer. 
    */
    public void generateMeasures(boolean showAreaEnabled, boolean showMeanEnabled, boolean showEquivalentDiameterEnabled,
    		boolean showIntegratedDensityEnabled, boolean showCircularityEnabled, boolean excludeOnEdgesEnabled) {
    	
       	// set measurements
    	int measurements = 0;
    	measurements += Measurements.CENTROID; // center of the particle (x,y)
    	measurements += Measurements.STACK_POSITION; // image position in stack (z)  	
    	if (showAreaEnabled) {
    		measurements += Measurements.AREA;
    	}
    	if (showEquivalentDiameterEnabled) {
    		measurements += Measurements.SHAPE_DESCRIPTORS + Measurements.FERET;
    	}
    	if (showMeanEnabled) {
    		measurements += Measurements.MEAN;
    	}
    	if (showIntegratedDensityEnabled) {
    		measurements += Measurements.INTEGRATED_DENSITY;
    	}
    	if (showCircularityEnabled) {
    		measurements += Measurements.CIRCULARITY;
    	}
    	
    	// set particlesAnalyzer
    	ResultsTable rt = ResultsTable.getResultsTable();
    	
    	// set options for Particles Analyzer
    	int options = 0;
    	options += ParticleAnalyzer.ELLIPSE;  // show overlay of detected particles
    	// options += ParticleAnalyzer.OVERLAY; // crash the program
    	// options += ParticleAnalyzer.SHOW_OUTLINES; // show outlines of every particles in each images of the stack
    	options += ParticleAnalyzer.DISPLAY_SUMMARY; // show statistics
    	if (excludeOnEdgesEnabled) {
    		options += ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
    	}
    	
    	double minCirc = 0.8; 
    	double maxCirc = 1.0; 
    	ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, 100.0, Double.POSITIVE_INFINITY, minCirc, maxCirc);

    	// get current image
    	ImagePlus img = WindowManager.getCurrentImage();
    	if (img == null) {
    		IJ.showMessage("Please open an image first (File > Open)");
    		return;
    	}
    	
    	// analyze each images of the stack
    	boolean success = false;
    	int stackSize = img.getStackSize();
    	for (int i = 1; i <= stackSize; i++) {
    	    img.setSlice(i);
    	    success = pa.analyze(img);
    	    if (!success) {
    	    	break;
    	    }
    	}
    	
    	if (success) {
    	 	rt.show("Results");
    	}
    }
	
}
