package model.workers.measures;

import javax.swing.SwingWorker;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;

/**
 * {@link SwingWorker} that take care of processing measurements and showing them.
 * <p>
 * Its {@code doInBackground} method generates the results table by setting the measurements of the Analyzer according 
 * to those chosen by the user and then starting the particles analyzer. 
 * </p>
 */
public class MeasuresProcessingWorker extends SwingWorker<Void, Void>{
    
    private boolean showAreaEnabled;
    private boolean showMeanEnabled;
    private boolean showMedianEnabled;
    private boolean showIntegratedDensityEnabled;
    private boolean showCircularityEnabled;
    private boolean excludeOnEdgesEnabled;
    
    /**
     * Creates a {@code MeasuresProcessingWorker}.
     * @param showAreaEnabled True if the 'Area' column must be shown in the results.
     * @param showMedianEnabled True if the 'Median' column must be shown in the results.
     * @param showMeanEnabled True if the 'Mean' column must be shown in the results.
     * @param showIntegratedDensityEnabled True if the 'IntegratedDensity' column must be shown in the results.
     * @param showCircularityEnabled True if the 'Circularity' column is shown must be the results.
     * @param excludeOnEdgesEnabled Particle Analyzer option.
     */
    public MeasuresProcessingWorker(boolean showAreaEnabled, boolean showMedianEnabled, boolean showMeanEnabled,
    		boolean showIntegratedDensityEnabled, boolean showCircularityEnabled, boolean excludeOnEdgesEnabled) {
    	this.showAreaEnabled = showAreaEnabled;
    	this.showMedianEnabled = showMedianEnabled;
    	this.showMeanEnabled = showMeanEnabled;
    	this.showIntegratedDensityEnabled = showIntegratedDensityEnabled;
    	this.showCircularityEnabled = showCircularityEnabled;
    	this.excludeOnEdgesEnabled = excludeOnEdgesEnabled;
    }

	@Override
	protected Void doInBackground() throws Exception {
       	// set measurements
    	int measurements = 0;
    	measurements += Measurements.CENTROID; // center of the particle (x,y)
    	measurements += Measurements.STACK_POSITION; // image position in stack (z)  	
    	if (showAreaEnabled) {
    		measurements += Measurements.AREA;
    	}
    	if (showMeanEnabled) {
    		measurements += Measurements.MEAN;
    	}
    	if (showMedianEnabled) {
    		measurements += Measurements.MEDIAN;
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
    		return null;
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
    	return null;
	}
	
}
