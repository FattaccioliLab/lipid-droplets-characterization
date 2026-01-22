package model;

import java.util.List;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * The implementation of the LDCService. It delegates its getters / setters treatments to an attribute.
 */
@Plugin(type = Service.class)
public class LDCServiceImpl extends AbstractService implements LDCService{

	// Object holding all LDC parameters (pure state)
	private AnalysisSettings settings;
	
	@Override
	public void initialize() { settings = new AnalysisSettings(); }

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

}
