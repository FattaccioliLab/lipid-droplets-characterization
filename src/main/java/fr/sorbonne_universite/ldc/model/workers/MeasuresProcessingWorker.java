package fr.sorbonne_universite.ldc.model.workers;

import javax.swing.SwingWorker;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
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
    
	private boolean isCalibrated;
	private Calibration calibration;
	private double minSize;
	private double maxSize;
	private double minCircularity;
	private double maxCircularity;
    private boolean excludeOnEdgesEnabled;
    private double circularityThreshold;
    private boolean showAreaEnabled;
    private boolean showDiameterEnabled;
    private boolean showMeanEnabled;
    private boolean showMedianEnabled;
    private boolean showIntegratedDensityEnabled;
    private boolean showCircularityEnabled;
    private ImagePlus img;
    
    /**
     * Creates a {@code MeasuresProcessingWorker}.
     * @param isCalibrated					Boolean that tell if the image there is a calibration given for the image
     * @param Calibration					Given calibration.
     * @param minSize 						Minimum particle size (unit² if IsCalibrated is true, otherwise px²).
     * @param maxSize 						Maximum particle size (unit² if IsCalibrated is true, otherwise px²).
     * @param minCircularity 				Minimum particle circularity.
     * @param maxCircularity 				Maximum particle circularity.
     * @param excludeOnEdgesEnabled 		Particle Analyzer option.
     * @param circularityThreshold 			Threshold on particle's circularity to define if they are isolated. 
     * @param showAreaEnabled 				If True then the 'Area' column must be shown in the results.
     * @param showDiameterEnabled			If True then the 'Diameter' column must be shown in the results.
     * @param showMedianEnabled 			If True then the 'Median' column must be shown in the results.
     * @param showMeanEnabled 				If True then the 'Mean' column must be shown in the results.
     * @param showIntegratedDensityEnabled 	If True then the 'IntegratedDensity' column must be shown in the results.
     * @param showCircularityEnabled 		If True then the 'Circularity' column is shown must be the results.
     * @param img 							The current image to consider.
     */
    public MeasuresProcessingWorker(
    		boolean isCalibrated,
    		Calibration calibration,
    		double minSize, 
    		double maxSize,
    		double minCircularity,
    		double maxCircularity,
    		boolean excludeOnEdgesEnabled,
    		double circularityThreshold,
    		boolean showAreaEnabled,
    		boolean showDiameterEnabled,
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
    	this.circularityThreshold = circularityThreshold;
    	this.showAreaEnabled = showAreaEnabled;
    	this.showDiameterEnabled = showDiameterEnabled;
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
    	if (showDiameterEnabled) measurements += Measurements.ELLIPSE;
    	
    	// measure circularity even if the show circularity is disabled, to tell if a particle is isolated
    	measurements += Measurements.CIRCULARITY;
    	
    	// add BX, BY, Width and Height to the result table, used to define if the particle is on the edge
    	measurements += Measurements.RECT;
    	
    	// set particlesAnalyzer
    	ResultsTable rt = ResultsTable.getResultsTable();
    	
    	// set options for Particles Analyzer
    	int options = 0;
    	if (excludeOnEdgesEnabled) options += ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;

    	// get current image
    	if (img == null) {
    		IJ.showMessage("Please open an image first (File > Open)");
    		return null;
    	}
    	
    	double pxMinSize = minSize;
    	double pxMaxSize = maxSize;
    	
    	if (isCalibrated && calibration != null && calibration.scaled()) {
    		// convert the unit² in px²
    		double pixelArea = calibration.pixelWidth * calibration.pixelHeight;
    		pxMinSize = minSize / pixelArea;
    		if (maxSize != Double.MAX_VALUE) {
    			pxMaxSize = maxSize / pixelArea;
    		}
        }
    	
    	ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, pxMinSize, pxMaxSize, minCircularity, maxCircularity);
    	
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
        	
        	if (rt.columnExists("Circ.")) { 
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
        	}
        	
    		// get the unit of the calibration
    		String unit = "px";
    		if (isCalibrated && calibration != null) unit = calibration.getUnit();
    		
    		// add the unit in the area column
    		if (showAreaEnabled && rt.columnExists("Area")) {
    			rt.renameColumn("Area", "Area(" + unit + "²)");
    		}
        	
        	// compute the diameters
        	if (showDiameterEnabled && rt.columnExists("Major") && rt.columnExists("Minor")) {
        		int rowCount = rt.getCounter();
        	    
        		// get the values of big and small axes of the particles
        		double[] majorAxis = rt.getColumnAsDoubles(rt.getColumnIndex("Major"));
        		double[] minorAxis = rt.getColumnAsDoubles(rt.getColumnIndex("Minor"));
        		
        		// create the Diameter column with the correct unit
        		int diameterIndex = rt.getFreeColumn("Diameter(" + unit + ")");
        	    
        		// compute and add the diameter for each particle
        		for (int i = 0; i < rowCount; i++) {
        			double diameter = (majorAxis[i] + minorAxis[i]) / 2.0;	// average of the two
        			rt.setValue(diameterIndex, i, diameter);
        		}
        	}

        	// cleaning the results table : remove unused columns
        	if (rt.columnExists("BX")) rt.deleteColumn("BX");
            if (rt.columnExists("BY")) rt.deleteColumn("BY");
            if (rt.columnExists("Width")) rt.deleteColumn("Width");
            if (rt.columnExists("Height")) rt.deleteColumn("Height");
            if (rt.columnExists("Major")) rt.deleteColumn("Major");
            if (rt.columnExists("Minor")) rt.deleteColumn("Minor");
            if (rt.columnExists("Angle")) rt.deleteColumn("Angle");
            if (rt.columnExists("AR")) rt.deleteColumn("AR");
            if (rt.columnExists("Round")) rt.deleteColumn("Round");
            if (rt.columnExists("Solidity")) rt.deleteColumn("Solidity");
            if (!showCircularityEnabled) rt.deleteColumn("Circ."); // remove if the circularity parameter isn't activated
            
    		// close the ROI manager window that appear with the ParticlesAnalyzer WIP
        	RoiManager rm = RoiManager.getInstance();
            if (rm != null) {
            	rm.setVisible(false);
            	rm.reset();
            	rm.close();
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
