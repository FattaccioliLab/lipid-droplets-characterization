package model;

import java.awt.Component;
import java.util.List;

import javax.swing.SwingWorker;

import org.scijava.service.SciJavaService;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import model.leftpanel.ImageSourceManager;
import model.leftpanel.PreprocessingManager;
import model.workers.preprocessing.PreprocessingApplyMedianWorker;
import model.workers.preprocessing.PreprocessingPreviewMedianWorker;

/**
 * Service exposing the API for manipulating parameters, operations, and the general state used for the Lipid Droplet Characterization Fiji plugin.
 */
public interface LDCService extends SciJavaService {

    // =========================================================================
    // GETTERS / SETTERS for the LDC state management.
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
	
    // ============================
    // Measurements showing options
    // ============================
	
	public boolean showAreaEnabled();
	public void setShowArea(boolean showArea);
	
	public boolean showEquivalentDiameterEnabled();
	public void setShowEquivalentDiameter(boolean showEquivalentDiameter);

	public boolean showMeanEnabled();
	public void setShowMean(boolean showMean);
	
	public boolean showIntegratedDensityEnabled();
	public void setShowIntegratedDensity(boolean showIntegratedDensity);
	
	public boolean showCircularityEnabled();
	public void setShowCircularity(boolean showCircularity);
	
	
    // =========================================================================
    // OPERATIONS
    // =========================================================================
	
    /**
     * Replaces the content of the current {@link ImagePlus} active image with an image loaded from disk.<br>
     * The LDC implementation delegates the operation to the {@link ImageSourceManager} class.
	 * @param parent The parent {@link Component} for the file chooser dialog, or {@code null} if none.
     * @see ImageSourceManager#replaceCurrentImage(ImagePlus, Component)
     */
	public void replaceCurrentImage(Component parent);
	
	/**
	 * Applies contrast enhancement to the current active image.
	 * @see PreprocessingManager#applyEnhanceContrast(ImagePlus, double)
	 */
	public void applyEnhanceContrast();
	
    /**
     * Creates a {@link SwingWorker}, that can apply a median filter preview on a given {@link ImageProcessor} if executed.
     * @param ip The image processor to modify.
     * @param isPreviewOn Whether preview mode is enabled. If it is false it will reset the given image processor.
     * @see PreprocessingPreviewMedianWorker
     */
	public SwingWorker<Void,Void> createPreviewMedianWorker(ImageProcessor ip, boolean isPreviewOn);
	
    /**
     * Creates a {@link SwingWorker}, that can apply a median filter on a given {@link ImageStack}, or on an individual {@link ImageProcessor} among the given stack if executed.
	 * @param stack The array of images to process (slices).
	 * @param processAll True to process all slices.
	 * @param targetSlice The specific slice to process if not all.
	 * @see PreprocessingApplyMedianWorker
     */
	public SwingWorker<Void,Void> createApplyMedianWorker(ImageStack stack, boolean processAll, int targetSlice);
}
