package fr.sorbonne_universite.ldc.model.workers;

import javax.swing.SwingWorker;

import fr.sorbonne_universite.ldc.model.AnalysisSettings;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;

/**
 * {@link SwingWorker} that take care of processing measurements and returning them.
 * <p>
 * Its {@code doInBackground} method generates the results table by setting the measurements of the Analyzer according 
 * to those chosen by the user and then starting the particles analyzer. 
 * </p>
 */
public class MeasuresProcessingWorker extends SwingWorker<ResultsTable, Void>{
    
	private AnalysisSettings settings;
    private ImagePlus img;
    
    /**
     * Creates a {@code MeasuresProcessingWorker}.
     * @param settings						The plugin settings.
     * @param img 							The current image to consider.
     */
    public MeasuresProcessingWorker(AnalysisSettings settings, ImagePlus img) {
    	this.settings = settings;
    	this.img = img;
    }

	@Override
	protected ResultsTable doInBackground() throws Exception {
       	// set measurements
    	int measurements = 0;
    	measurements += Measurements.CENTROID; // center of the particle (x,y)
    	measurements += Measurements.STACK_POSITION; // image position in stack (z)  	
    	if (settings.showAreaEnabled()) measurements += Measurements.AREA;
    	if (settings.showMeanEnabled()) measurements += Measurements.MEAN;
    	if (settings.showMedianEnabled()) measurements += Measurements.MEDIAN;
    	if (settings.showIntegratedDensityEnabled()) measurements += Measurements.INTEGRATED_DENSITY;
    	if (settings.showDiameterEnabled()) measurements += Measurements.ELLIPSE;
    	
    	// measure circularity even if the show circularity is disabled, to tell if a particle is isolated
    	measurements += Measurements.CIRCULARITY;
    	
    	// add BX, BY, Width and Height to the result table, used to define if the particle is on the edge
    	measurements += Measurements.RECT;
    	
    	ResultsTable rt = new ResultsTable();
    	
    	// set options for Particles Analyzer
    	int options = 0;
    	if (settings.analyseExcludeOnEdgesEnabled()) options += ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;

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
    	
    	ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, pxMinSize, pxMaxSize, 
    			settings.getAnalyseMinCircularity(), settings.getAnalyseMaxCircularity());
    	
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
            	// if the checkbox to keep the default calibration is unchecked or the image don't have a default calibration
            	if (!settings.showDefaultCalibrationEnabled() || img.getCalibration() == null) {            		
            		//  force a neutral calibration 1 pixel / 1 pixel
            		Calibration pixelCal = new ij.measure.Calibration();
            		pixelCal.pixelWidth = 1.0;
            		pixelCal.pixelHeight = 1.0;
            		pixelCal.setUnit("pixel");
            		img.setCalibration(pixelCal);
            	}
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
    	    			if (rt.getValue("Circ.", row) >= settings.getAnalyseCircularityThreshold()) isIsolated = 1;
    	    		}
    	    		
    	    		// add the attribute to the particle
    	    		rt.setValue("is_isolated", row, isIsolated);
    	    	}
        	}
        	
    		// get the unit of the calibration
    		String unit = "px";
    		if (settings.isCalibrated() && calibration != null) unit = calibration.getUnit();
    		if (settings.showDefaultCalibrationEnabled() && img.getCalibration() != null) unit = calibration.getUnit();
    		
    		// add the unit in the area column
    		if (settings.showAreaEnabled() && rt.columnExists("Area")) {
    			rt.renameColumn("Area", "Area(" + unit + "²)");
    		}
        	
        	// compute the diameters
        	if (settings.showDiameterEnabled() && rt.columnExists("Major") && rt.columnExists("Minor")) {
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
            if (!settings.showCircularityEnabled()) rt.deleteColumn("Circ."); // remove if the circularity parameter isn't activated
    		
    	} finally {
    		// ensure that the original calibration is restored in the end. 
    		if (backupCal != null) {
    			img.setCalibration(backupCal);
    		}
    	}
    	
    	return rt;
	}
	
}
