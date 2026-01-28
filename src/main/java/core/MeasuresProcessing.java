package core;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import model.AnalysisSettings;

/**
 * Class that take care of processing measurements 
 * */
public class MeasuresProcessing {
	
	AnalysisSettings selectedSettings;
	
	public MeasuresProcessing(AnalysisSettings selectedSettings) {
		this.selectedSettings = selectedSettings;
	}
	
	/**
    *  method that generate the results table by setting the measurements of the Analyzer according to those chosen by the user 
    *  and then starting the particles analyzer. 
    */
    public void generateMeasures() {
    	
       	// set measurements
    	int measurements = 0;
    	measurements += Measurements.CENTROID; // center of the particle (x,y)
    	measurements += Measurements.STACK_POSITION; // image position in stack (z)  	
    	if (selectedSettings.isArea()) {
    		measurements += Measurements.AREA;
    	}
    	if (selectedSettings.isEquivalentDiameter()) {
    		measurements += Measurements.SHAPE_DESCRIPTORS + Measurements.FERET;
    	}
    	if (selectedSettings.isMean()) {
    		measurements += Measurements.MEAN;
    	}
    	if (selectedSettings.isIntegratedDensity()) {
    		measurements += Measurements.INTEGRATED_DENSITY;
    	}
    	if (selectedSettings.isCircularity()) {
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
    	if (selectedSettings.isExcludeOnEdges()) {
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
