package fr.sorbonne_universite.ldc.model;

import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import fr.sorbonne_universite.ldc.model.leftpanel.MorphologyManager;
import fr.sorbonne_universite.ldc.model.leftpanel.PreprocessingManager;
import fr.sorbonne_universite.ldc.model.leftpanel.ThresholdingManager;
import fr.sorbonne_universite.ldc.model.rightpanel.MeasurementsManager;
import fr.sorbonne_universite.ldc.model.workers.PreprocessingPreviewMedianWorker;
import fr.sorbonne_universite.ldc.ui.rightpanel.BatchWindow;
import fr.sorbonne_universite.ldc.model.workers.BatchWorker;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

/**
 * The implementation of the {@link LDCService}. It delegates its operations to inner attributes.
 */
@Plugin(type = Service.class)
public class LDCServiceImpl extends AbstractService implements LDCService{

	// Object holding all LDC parameters (pure state)
	private AnalysisSettings settings;
	
	// For the LeftPanel's sub-panels operations.
	private PreprocessingManager preprocessingManager;
	private ThresholdingManager thresholdingManager;
	private MorphologyManager morphologyManager;
  
	// For the RightPanel's sub-panels operations.
	private MeasurementsManager measurementsManager;
	
	@Override
	public void initialize() { 
		settings = new AnalysisSettings(); 
		preprocessingManager = new PreprocessingManager();
		thresholdingManager = new ThresholdingManager(this);
		measurementsManager = new MeasurementsManager();
		morphologyManager = new MorphologyManager();
	}
	
    // =========================================================================
    // GETTERS / SETTERS for the LDC state management.
    // =========================================================================

	// =================
    // Preprocessing
    // =================

    @Override public boolean enhanceContrastEnabled() { return settings.enhanceContrastEnabled(); }
    @Override public void setEnhanceContrast(boolean enhanceContrast) { settings.setEnhanceContrast(enhanceContrast); }

    @Override public double getEnhanceSaturatedPercent() { return settings.getEnhanceSaturatedPercent(); }
    @Override public void setEnhanceSaturatedPercent(double enhanceSaturatedPercent) { settings.setEnhanceSaturatedPercent(enhanceSaturatedPercent); }

    @Override public boolean medianFilterEnabled() { return settings.medianFilterEnabled(); }
    @Override public void setMedianFilter(boolean medianFilter) { settings.setMedianFilter(medianFilter); }

    @Override public double getMedianRadius() { return settings.getMedianRadius(); }
    @Override public void setMedianRadius(double medianRadius) { settings.setMedianRadius(medianRadius); }

    @Override public boolean gausianFilterEnabled() { return settings.gausianFilterEnabled(); }
    @Override public void setGausianFilter(boolean gausianFilter) { settings.setGausianFilter(gausianFilter); }

    @Override public double getGaussianRadius() { return settings.getGaussianRadius(); }
    @Override public void setGaussianRadius(double gaussianRadius) { settings.setGaussianRadius(gaussianRadius); }

    // ===========================
    // Segmentation / thresholding
    // ===========================

    @Override public List<String> getThresholdMethodsList() { return settings.getThresholdMethodsList(); }
    @Override public String getThresholdMethod() { return settings.getThresholdMethod(); }
    @Override public void setThresholdMethod(String method) { settings.setThresholdMethod(method); }

    @Override public int getThresholdMinValue() { return settings.getThresholdMinValue(); }
    @Override public int getThresholdMaxValue() { return settings.getThresholdMaxValue(); }
    @Override public void setThresholdMinValue(int value) { settings.setThresholdMinValue(value); }
    @Override public void setThresholdMaxValue(int value) { settings.setThresholdMaxValue(value); }

    @Override public boolean thresholdDarkBackgroundEnabled() { return settings.thresholdDarkBackgroundEnabled(); }
    @Override public void setThresholdDarkBackground(boolean thresholdDarkBackground) { settings.setThresholdDarkBackground(thresholdDarkBackground); }

    @Override public boolean getIndependentThreshold() { return settings.getIndpendentThreshold(); }
    @Override public void setIndependentThreshold(boolean thresholdGlobal) { settings.setIndpendentThreshold(thresholdGlobal); }

    // ====================================
    // Binary mask morphological operations
    // ====================================

    @Override public boolean erosionEnabled() { return settings.erosionEnabled(); }
    @Override public void setErosion(boolean erosion) { settings.setErosion(erosion); }

    @Override public boolean dilationEnabled() { return settings.dilationEnabled(); }
    @Override public void setDilation(boolean dilation) { settings.setDilation(dilation); }

    @Override public boolean openingEnabled() { return settings.openingEnabled(); }
    @Override public void setOpening(boolean opening) { settings.setOpening(opening); }

    @Override public boolean closingEnabled() { return settings.closingEnabled(); }
    @Override public void setClosing(boolean closing) { settings.setClosing(closing); }

    @Override public boolean watershedEnabled() { return settings.watershedEnabled(); }
    @Override public void setWathershed(boolean watershed) { settings.setWathershed(watershed); }

    // =================
    // Analyse particles
    // =================

    @Override public double getAnalyseMinSize() { return settings.getAnalyseMinSize(); }
    @Override public void setAnalyseMinSize(double analyseMinSize) { settings.setAnalyseMinSize(analyseMinSize); }

    @Override public double getAnalyseMaxSize() { return settings.getAnalyseMaxSize(); }
    @Override public void setAnalyseMaxSize(double analyseMaxSize) { settings.setAnalyseMaxSize(analyseMaxSize); }

    @Override public double getAnalyseMinCircularity() { return settings.getAnalyseMinCircularity(); }
    @Override public void setAnalyseMinCircularity(double analyseMinCircularity) { settings.setAnalyseMinCircularity(analyseMinCircularity); }

    @Override public double getAnalyseMaxCircularity() { return settings.getAnalyseMaxCircularity(); }
    @Override public void setAnalyseMaxCircularity(double analyseMaxCircularity) { settings.setAnalyseMaxCircularity(analyseMaxCircularity); }

    @Override public boolean analyseExcludeOnEdgesEnabled() { return settings.analyseExcludeOnEdgesEnabled(); }
    @Override public void setAnalyseExcludeOnEdges(boolean analyseExcludeOnEdges) { settings.setAnalyseExcludeOnEdges(analyseExcludeOnEdges); }

    @Override public double getAnalyseCircularityThreshold() { return settings.getAnalyseCircularityThreshold(); }
    @Override public void setAnalyseCircularityThreshold(double analyseCircularityThreshold) { settings.setAnalyseCircularityThreshold(analyseCircularityThreshold); }
    
	@Override public boolean isCalibrated() {	return settings.isCalibrated(); }
	@Override public void setIsCalibrated(boolean isCalibrated) { settings.setIsCalibrated(isCalibrated); }

	@Override public Calibration getCalibration() { return settings.getCalibration(); }
	@Override public void setCalibration(Calibration calibration) { settings.setCalibration(calibration); }
    
    // ============================
    // Measurements showing options
    // ============================
    
	@Override public boolean showAreaEnabled() { return settings.showAreaEnabled(); }
	@Override public void setShowArea(boolean showArea) { settings.setShowArea(showArea); }
	
	@Override public boolean showDiameterEnabled() { return settings.showDiameterEnabled(); }
	@Override public void setShowDiameter(boolean showDiameter) { settings.setShowDiameter(showDiameter); }

	@Override public boolean showMedianEnabled() { return settings.showMedianEnabled(); }
	@Override public void setShowMedian(boolean showMedian) { settings.setShowMedian(showMedian); }

	@Override public boolean showMeanEnabled() { return settings.showMeanEnabled(); }
	@Override public void setShowMean(boolean showMean) { settings.setShowMean(showMean); }

	@Override public boolean showIntegratedDensityEnabled() { return settings.showIntegratedDensityEnabled(); }
	@Override public void setShowIntegratedDensity(boolean showIntegratedDensity) { settings.setShowIntegratedDensity(showIntegratedDensity); }

	@Override public boolean showCircularityEnabled() { return settings.showCircularityEnabled(); }
	@Override public void setShowCircularity(boolean showCircularity) { settings.setShowCircularity(showCircularity); }    
    
    // =========================================================================
    // OPERATIONS
    // =========================================================================
	
	// =================
    // Preprocessing
    // =================
	
	/** @see PreprocessingManager#applyEnhanceContrast(ImageProcessor, double) */
	@Override public void applyEnhanceContrast(ImageProcessor ip) { preprocessingManager.applyEnhanceContrast(ip, enhanceContrastEnabled(), getEnhanceSaturatedPercent()); }

	/** @see PreprocessingPreviewMedianWorker */
	@Override public SwingWorker<Void, Void> createPreviewMedianWorker(ImageProcessor ip) {
		return preprocessingManager.createPreviewMedianWorker(ip, getMedianRadius());
	}
	
	/** @see PreprocessingApplyMedianWorker */
	@Override public SwingWorker<Void, Void> createApplyMedianWorker(ImageStack stack) {  
		return preprocessingManager.createApplyMedianWorker(medianFilterEnabled(), getMedianRadius(), stack);
	}
	
    // ===========================
    // Segmentation / thresholding
    // ===========================
	
    @Override public void previewManualThreshold(ImagePlus imp) {
        thresholdingManager.setManualThreshold(imp, settings.getThresholdMinValue(), settings.getThresholdMaxValue());
    }

    @Override public double[] previewAutoThreshold(ImagePlus imp) {
        return thresholdingManager.setAutoThreshold(imp, settings.getThresholdMethod(), settings.thresholdDarkBackgroundEnabled());
    }

    @Override public ImagePlus applyThreshold(ImagePlus imp) { return thresholdingManager.applyThreshold(imp); }
    
    @Override public boolean resetThreshold(ImagePlus imp) { return thresholdingManager.resetThreshold(imp); }
    
    
    // ====================================
    // Morphology
    // ====================================
    public void captureMorphologySnapshot(ImagePlus imp) {
    	morphologyManager.captureSnapshot(imp);
    }
    public void previewMorphology(ImagePlus imp) {
    	morphologyManager.previewMorphology(
    						imp, 
    						settings.erosionEnabled(), 
    						settings.dilationEnabled(), 
    						settings.openingEnabled(), 
    						settings.closingEnabled(), 
    						settings.watershedEnabled());
    }
    public boolean resetMorphologyPreview(ImagePlus imp) {
    	return morphologyManager.resetPreview(imp);
    }
    
    public boolean applyMorphology(ImagePlus imp) {
    	return morphologyManager.applyMorphology(
				imp, 
				settings.erosionEnabled(), 
				settings.dilationEnabled(), 
				settings.openingEnabled(), 
				settings.closingEnabled(), 
				settings.watershedEnabled());
    }
	
    // ============================
    // Measurements showing options
    // ============================

	/** @see MeasuresPreviewWorker */
    @Override public SwingWorker<Void, Void> createMeasuresPreviewWorker(ImagePlus img){
		return measurementsManager.createMeasuresPreviewWorker(
				isCalibrated(),
				getCalibration(),
				getAnalyseMinSize(),
				getAnalyseMaxSize(),
				getAnalyseMinCircularity(),
				getAnalyseMaxCircularity(),
				analyseExcludeOnEdgesEnabled(),
				img);
    }
    
	/** @see MeasuresProcessingWorker */
	@Override public SwingWorker<ResultsTable, Void> createMeasuresProcessingWorker(ImagePlus img) {
		return measurementsManager.createMeasuresProcessingWorker(
				isCalibrated(),
				getCalibration(),
				getAnalyseMinSize(),
				getAnalyseMaxSize(),
				getAnalyseMinCircularity(),
				getAnalyseMaxCircularity(),
				analyseExcludeOnEdgesEnabled(),
				getAnalyseCircularityThreshold(),
				showAreaEnabled(),
				showDiameterEnabled(),
				showMedianEnabled(),
				showMeanEnabled(),
				showIntegratedDensityEnabled(),
				showCircularityEnabled(),
				img);
	}

	@Override public void exportResultsTable(ResultsTable rt, String path) {
		measurementsManager.exportResultsTable(rt, path);
	}
	
	@Override public ResultsTable calculateSummaryTable(ResultsTable rt, Calibration cal, double imgWidth, double imgHeight) {
		return measurementsManager.calculateSummaryTable(rt, cal, imgWidth, imgHeight);
	}
	
	@Override public List<ImagePlus> generateHistograms(ResultsTable rt) {
		return measurementsManager.generateHistograms(rt);
	}
	
	// ============
    // Batch mode
    // ============
	
	@Override public SwingWorker<Void,Void> createBatchWorker(File inputDirectory, File outputFile, BatchWindow bw){
		return new BatchWorker(settings.clone(), inputDirectory, outputFile, bw);
	}

}
