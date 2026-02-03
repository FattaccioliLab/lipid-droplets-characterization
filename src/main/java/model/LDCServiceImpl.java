package model;

import java.awt.Component;
import java.util.List;

import javax.swing.SwingWorker;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.ImageProcessor;
import model.leftpanel.ImageSourceManager;
import model.leftpanel.PreprocessingManager;
import model.rightpanel.MeasurementsManager;
import model.workers.preprocessing.PreprocessingPreviewMedianWorker;

/**
 * The implementation of the {@link LDCService}. It delegates its operations to inner attributes.
 */
@Plugin(type = Service.class)
public class LDCServiceImpl extends AbstractService implements LDCService{

	// Object holding all LDC parameters (pure state)
	private AnalysisSettings settings;
	
	// For the LeftPanel's sub-panels operations.
	private ImageSourceManager imageSourceManager;
	private PreprocessingManager preprocessingManager;
	
	// For the RightPanel's sub-panels operations.
	private MeasurementsManager measurementsManager;
	
	@Override
	public void initialize() { 
		settings = new AnalysisSettings(); 
		imageSourceManager = new ImageSourceManager();
		preprocessingManager = new PreprocessingManager();
		measurementsManager = new MeasurementsManager();
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

    @Override public int getThresholdMinValue() { return settings.getThresholdMinValue(); }
    @Override public int getThresholdMaxValue() { return settings.getThresholdMaxValue(); }

    @Override public boolean thresholdDarkBackgroundEnabled() { return settings.thresholdDarkBackgroundEnabled(); }
    @Override public void setThresholdDarkBackground(boolean thresholdDarkBackground) { settings.setThresholdDarkBackground(thresholdDarkBackground); }

    @Override public boolean thresholdGlobalEnabled() { return settings.thresholdGlobalEnabled(); }
    @Override public void setThresholdGlobal(boolean thresholdGlobal) { settings.setThresholdGlobal(thresholdGlobal); }

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

    // ============================
    // Measurements showing options
    // ============================
    
	@Override public boolean showAreaEnabled() { return settings.showAreaEnabled(); }
	@Override public void setShowArea(boolean showArea) { settings.setShowArea(showArea); }

	@Override public boolean showEquivalentDiameterEnabled() { return settings.showEquivalentDiameterEnabled(); }
	@Override public void setShowEquivalentDiameter(boolean showEquivalentDiameter) { settings.setShowEquivalentDiameter(showEquivalentDiameter); }

	@Override public boolean showMeanEnabled() { return settings.showMeanEnabled(); }
	@Override public void setShowMean(boolean showMean) { settings.setShowMean(showMean); }

	@Override public boolean showIntegratedDensityEnabled() { return settings.showIntegratedDensityEnabled(); }
	@Override public void setShowIntegratedDensity(boolean showIntegratedDensity) { settings.setShowIntegratedDensity(showIntegratedDensity); }

	@Override public boolean showCircularityEnabled() { return settings.showCircularityEnabled(); }
	@Override public void setShowCircularity(boolean showCircularity) { settings.setShowCircularity(showCircularity); }    
    
    // =========================================================================
    // OPERATIONS
    // =========================================================================
    
    /** @see ImageSourceManager#replaceCurrentImage(ImagePlus, Component) */
	@Override public void replaceCurrentImage(Component parent) { imageSourceManager.replaceCurrentImage(WindowManager.getCurrentImage(), parent); }

	/** @see ImageSourceManager#resetCurrentImage(ImagePlus) */
	@Override public void resetCurrentImage() { imageSourceManager.resetCurrentImage(WindowManager.getCurrentImage()); }
	
	/** @see PreprocessingManager#applyEnhanceContrast(ImagePlus, double) */
	@Override public void applyEnhanceContrast() { preprocessingManager.applyEnhanceContrast(WindowManager.getCurrentImage(), enhanceContrastEnabled(),getEnhanceSaturatedPercent()); }

	/** @see PreprocessingPreviewMedianWorker */
	@Override public SwingWorker<Void, Void> createPreviewMedianWorker(ImageProcessor ip, boolean isPreviewOn) {
		return preprocessingManager.createPreviewMedianWorker(ip, isPreviewOn, getMedianRadius());
	}

	/** @see PreprocessingApplyMedianWorker */
	@Override public SwingWorker<Void, Void> createApplyMedianWorker(ImageStack stack, boolean processAll, int targetSlice) {  
		return preprocessingManager.createApplyMedianWorker(medianFilterEnabled(), getMedianRadius(), stack, processAll, targetSlice);
	}

	/** @see MeasuresProcessingWorker */
	@Override public SwingWorker<Void, Void> createMeasuresProcessingWorker() {
		return measurementsManager.createMeasuresProcessingWorker(showAreaEnabled(), showEquivalentDiameterEnabled(), showMeanEnabled(), 
				showIntegratedDensityEnabled(), showCircularityEnabled(), analyseExcludeOnEdgesEnabled());
	}

}
