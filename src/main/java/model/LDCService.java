package model;

import java.util.List;

import org.scijava.service.SciJavaService;

/**
 * Service defining all parameters and state used for the Lipid Droplet Characterization Fiji plugin.
 */
public interface LDCService extends SciJavaService {

    // =========================================================================
    // METHODS (getters / setters)
    // =========================================================================
	
    // =============
    // Preprocessing
    // =============
	
	// Enhance contrast
	public boolean enhanceContrastEnabled();
	public void setEnhanceContrast(boolean enhanceContrast);
	
	public double getEnhanceSaturatedPercent();
	public void setEnhanceSaturatedPercent(double enhanceSaturatedPercent);
	
	// Median filter
	public boolean medianFilterEnabled();
	public void setMedianFilter(boolean medianFilter);

	public double getMedianRadius();
	public void setMedianRadius(double medianRadius);

	// Gaussian filter (gaussian blur) (not yet used)
	public boolean gausianFilterEnabled();
	public void setGausianFilter(boolean gausianFilter);
	
	public double getGaussianRadius();
	public void setGaussianRadius(double gaussianRadius);
	
    // ===========================
    // Segmentation / thresholding
    // ===========================
	
	// Threshold methods
	public List<String> getThresholdMethodsList();
	
	// Threshold current method
	public String getThresholdMethod();
	
	// Threshold range
	public int getThresholdMinValue();
	public int getThresholdMaxValue();
	
	// Dark background option
	public boolean thresholdDarkBackgroundEnabled();
	public void setThresholdDarkBackground(boolean thresholdDarkBackground);
	
	// Process one slice or all slices
	public boolean thresholdGlobalEnabled();
	public void setThresholdGlobal(boolean thresholdGlobal);
	
    // ====================================
    // Binary mask morphological operations
    // ====================================
	
	public boolean erosionEnabled();
	public void setErosion(boolean erosion);
	
	public boolean dilationEnabled();
	public void setDilation(boolean dilation);
	
	public boolean openingEnabled();
	public void setOpening(boolean opening);
	
	public boolean closingEnabled();
	public void setClosing(boolean closing);
	
	public boolean watershedEnabled();
	public void setWathershed(boolean watershed);
	
    // =================
    // Analyse particles
    // =================

	// Particles sizes
	public double getAnalyseMinSize();
	public void setAnalyseMinSize(double analyseMinSize);
	public double getAnalyseMaxSize();
	public void setAnalyseMaxSize(double analyseMaxSize);
	
	// Circularity range
	public double getAnalyseMinCircularity();
	public void setAnalyseMinCircularity(double analyseMinCircularity);
	public double getAnalyseMaxCircularity();
	public void setAnalyseMaxCircularity(double analyseMaxCircularity);
	
	// Exclude on edges
	public boolean analyseExcludeOnEdgesEnabled();
	public void setAnalyseExcludeOnEdges(boolean analyseExcludeOnEdges);
	
	
}
