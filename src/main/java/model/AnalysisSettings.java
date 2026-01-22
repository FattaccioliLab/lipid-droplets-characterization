package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class containing the user's current image processing settings for the plugin. 
 */
public class AnalysisSettings {
	
    // =========================================================================
    // SETTINGS
    // =========================================================================
	
    // =============
    // Preprocessing
    // =============
	
	// Enhance contrast
	private boolean enhanceContrast = false;
	
	public final static double DFL_EC_SATURATED = 0.35; // default saturated value (%)
	private double enhanceSaturatedPercent = DFL_EC_SATURATED;
	
	// Median filter
	private boolean medianFilter = false;
	
	public final static double DFL_MEDIAN_RADIUS = 2.0; // default radius (px)
	private double medianRadius = DFL_MEDIAN_RADIUS;
	
	// Gaussian filter (gaussian blur) (not yet used)
	private boolean gausianFilter = false;
	
	public final static double DFL_GAUSSIAN_RADIUS = 2.0; // default radius (px)
	private double gaussianRadius = DFL_GAUSSIAN_RADIUS;
	
    // ===========================
    // Segmentation / thresholding
    // ===========================
	
	// Possible threshold methods
	private List<String> thresholdMethodsList = new ArrayList<>(Arrays.asList("Manual", "Otsu", "Moments", "Triangle", "Yen", "Li"));
	
	// The current threshold method
	public final static String DFL_THRESHOLD_METHOD = "Manual";
	private String thresholdMethod = DFL_THRESHOLD_METHOD;
	
	// Threshold range
	public final static int DFL_THRESHOLD_MIN_VALUE = 0;
	public final static int DFL_THRESHOLD_MAX_VALUE = 1585;
	private int thresholdMinValue = DFL_THRESHOLD_MIN_VALUE;
	private int thresholdMaxValue = DFL_THRESHOLD_MAX_VALUE;
	
	// Dark background option
	public final static boolean DFL_THRESHOLD_DARK_BACKGROUND = false;
	private boolean thresholdDarkBackground = DFL_THRESHOLD_DARK_BACKGROUND;
	
	// Process one slice or all slices (global)
	public final static boolean DFL_THRESHOLD_GLOBAL = false;
	private boolean thresholdGlobal = DFL_THRESHOLD_GLOBAL;

    // ====================================
    // Binary mask morphological operations
    // ====================================
	
	public final static boolean DFL_BINARY_MASK_OP = false;
	private boolean erosion = DFL_BINARY_MASK_OP;
	private boolean dilation = DFL_BINARY_MASK_OP;
	private boolean opening = DFL_BINARY_MASK_OP;
	private boolean closing = DFL_BINARY_MASK_OP;
	private boolean watershed = DFL_BINARY_MASK_OP; // bonus
	
    // =================
    // Analyse particles
    // =================

	// Particles sizes (px²)
	public final static double DFL_ANALYSE_MIN_SIZE = 0;
	public final static double DFL_ANALYSE_MAX_SIZE = -1; // -1 represents infinity
	private double analyseMinSize = DFL_ANALYSE_MIN_SIZE;
	private double analyseMaxSize = DFL_ANALYSE_MAX_SIZE;
	
	// Circularity range
	public final static double DFL_ANALYSE_MIN_CIRCULARITY = 0;
	public final static double DFL_ANALYSE_MAX_CIRCULARITY = 1;
	private double analyseMinCircularity = DFL_ANALYSE_MIN_CIRCULARITY;
	private double analyseMaxCircularity = DFL_ANALYSE_MAX_CIRCULARITY;
	
	// Exclude on edges
	public final static boolean DFL_ANALYSE_EXCL_EDGES = false;
	private boolean analyseExcludeOnEdges = DFL_ANALYSE_EXCL_EDGES;
	
	
    // =========================================================================
    // METHODS (getters / setters)
    // =========================================================================
	
    // =============
    // Preprocessing
    // =============
	
	// Enhance contrast
	public boolean enhanceContrastEnabled() { return enhanceContrast; }
	public void setEnhanceContrast(boolean enhanceContrast) { this.enhanceContrast = enhanceContrast; }
	
	public double getEnhanceSaturatedPercent() { return enhanceSaturatedPercent; }
	public void setEnhanceSaturatedPercent(double enhanceSaturatedPercent) { 
		if (enhanceSaturatedPercent < 0 || enhanceSaturatedPercent > 100) throw new IllegalArgumentException(Double.toString(enhanceSaturatedPercent)+" must be a percentage (range [0 - 100])");
		this.enhanceSaturatedPercent = enhanceSaturatedPercent; 
	}
	
	// Median filter
	public boolean medianFilterEnabled() { return medianFilter; }
	public void setMedianFilter(boolean medianFilter) { this.medianFilter = medianFilter; }

	public double getMedianRadius() { return medianRadius; }
	public void setMedianRadius(double medianRadius) { 
		if (medianRadius < 0) throw new IllegalArgumentException(Double.toString(medianRadius)+" must be positive");
		this.medianRadius = medianRadius; 
	}

	// Gaussian filter (gaussian blur) (not yet used)
	public boolean gausianFilterEnabled() { return gausianFilter; }
	public void setGausianFilter(boolean gausianFilter) { this.gausianFilter = gausianFilter; }
	
	public double getGaussianRadius() { return gaussianRadius; }
	public void setGaussianRadius(double gaussianRadius) {
		if (gaussianRadius < 0) throw new IllegalArgumentException(Double.toString(gaussianRadius)+" must be positive");
		this.gaussianRadius = gaussianRadius; 
	}
	
    // ===========================
    // Segmentation / thresholding
    // ===========================
	
	// Threshold methods
	public List<String> getThresholdMethodsList() { return new ArrayList<>(thresholdMethodsList); } // new independant copy 
	
	// Threshold current method
	public String getThresholdMethod() { return thresholdMethod; }
	public void setThresholdMethod(String newThresholdMethod) {
			if (!thresholdMethodsList.contains(newThresholdMethod)) throw new IllegalArgumentException(newThresholdMethod+" is not an existing method");
			thresholdMethod = newThresholdMethod;
	}
	
	// Threshold range
	public int getThresholdMinValue() { return thresholdMinValue; }
	public void setThresholdMinValue(int thresholdMinValue) { 
		if (thresholdMinValue < 0) throw new IllegalArgumentException(Integer.toString(thresholdMinValue)+"must be positive");
		if (thresholdMinValue > this.thresholdMaxValue) throw new IllegalArgumentException(Integer.toString(thresholdMinValue)+"must be less (or equal) than max threshold value");
		this.thresholdMinValue = thresholdMinValue;
	}
	public int getThresholdMaxValue() { return thresholdMaxValue; }
	public void setThresholdMaxValue(int thresholdMaxValue) { 
		if (thresholdMaxValue < this.thresholdMinValue) throw new IllegalArgumentException(Integer.toString(thresholdMaxValue)+"must be greater (or equal) than min threshold value");
		this.thresholdMaxValue = thresholdMaxValue;
	}
	
	// Dark background option
	public boolean thresholdDarkBackgroundEnabled() { return thresholdDarkBackground; }
	public void setThresholdDarkBackground(boolean thresholdDarkBackground) { this.thresholdDarkBackground = thresholdDarkBackground; }
	
	// Process one slice or all slices
	public boolean thresholdGlobalEnabled() { return thresholdGlobal; }
	public void setThresholdGlobal(boolean thresholdGlobal) { this.thresholdGlobal = thresholdGlobal; }
	
    // ====================================
    // Binary mask morphological operations
    // ====================================
	
	public boolean erosionEnabled() { return erosion; }
	public void setErosion(boolean erosion) { this.erosion = erosion; }
	
	public boolean dilationEnabled() { return dilation; }
	public void setDilation(boolean dilation) { this.dilation = dilation; }
	
	public boolean openingEnabled() { return opening; }
	public void setOpening(boolean opening) { this.opening = opening; }
	
	public boolean closingEnabled() { return closing; }
	public void setClosing(boolean closing) { this.closing = closing; }
	
	public boolean watershedEnabled() { return watershed; }
	public void setWathershed(boolean watershed) { this.watershed = watershed; }
	
    // =================
    // Analyse particles
    // =================

	// Particles sizes
	public double getAnalyseMinSize() { return analyseMinSize; }
	public void setAnalyseMinSize(double analyseMinSize) {
		if (analyseMinSize < 0) throw new IllegalArgumentException(Double.toString(analyseMinSize)+"must be positive");
		if (analyseMinSize > this.analyseMaxSize) throw new IllegalArgumentException(Double.toString(analyseMinSize)+"must be less (or equal) than max analyse size");
		this.analyseMinSize = analyseMinSize;
	}
	public double getAnalyseMaxSize() { return analyseMaxSize; }
	public void setAnalyseMaxSize(double analyseMaxSize) {
		if (analyseMaxSize == -1) { // Special case, analyseMaxSize = infinity
			this.analyseMaxSize = analyseMaxSize;
			return;
		}
		if (analyseMaxSize < this.analyseMinSize) throw new IllegalArgumentException(Double.toString(analyseMaxSize)+"must be greater (or equal) than min analyse size");
		this.analyseMaxSize = analyseMaxSize;
	}
	
	// Circularity range
	public double getAnalyseMinCircularity() { return analyseMinCircularity; }
	public void setAnalyseMinCircularity(double analyseMinCircularity) {
		if (analyseMinCircularity < 0) throw new IllegalArgumentException(Double.toString(analyseMinCircularity)+"must be positive");
		if (analyseMinCircularity > this.analyseMaxCircularity) throw new IllegalArgumentException(Double.toString(analyseMinCircularity)+"must be less (or equal) than max analyse circularity");
		this.analyseMinCircularity = analyseMinCircularity;
	}
	public double getAnalyseMaxCircularity() { return analyseMaxCircularity; }
	public void setAnalyseMaxCircularity(double analyseMaxCircularity) {
		if (analyseMaxCircularity < this.analyseMinCircularity) throw new IllegalArgumentException(Double.toString(analyseMaxCircularity)+"must be greater (or equal) than min analyse circularity");
		this.analyseMaxCircularity = analyseMaxCircularity;
	}
	
	// Exclude on edges
	public boolean analyseExcludeOnEdgesEnabled() { return analyseExcludeOnEdges; }
	public void setAnalyseExcludeOnEdges(boolean analyseExcludeOnEdges) { this.analyseExcludeOnEdges = analyseExcludeOnEdges; }
	
}
