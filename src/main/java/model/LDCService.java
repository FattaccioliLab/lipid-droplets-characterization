package model;

import java.awt.Component;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingWorker;

import org.scijava.service.SciJavaService;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import model.leftpanel.ImageSourceManager;
import model.leftpanel.PreprocessingManager;
import model.workers.measures.MeasuresProcessingWorker;
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
	
	/** Preview manual threshold on the image */
    public void previewManualThreshold(ImagePlus imp);
    
    /** * Preview auto threshold. 
     * @return double[] {min, max} calculated.
     */
    public double[] previewAutoThreshold(ImagePlus imp, String method, boolean darkBackground);
    
    /** Apply final conversion to mask */
    public boolean applyThreshold(ImagePlus imp);
    public boolean resetThreshold(ImagePlus imp);
    
    public void setThresholdMinValue(int value);
    public void setThresholdMaxValue(int value);
    
    public void setThresholdMethod(String method);
	
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
	
	public boolean showMedianEnabled();
	public void setShowMedian(boolean showMedian);

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
     * Reset the content of the current {@link ImagePlus} active image with the originally opened one<br>
     * The LDC implementation delegates the operation to the {@link ImageSourceManager} class.
     */
	public void resetCurrentImage();
	
	/**
	 * Applies contrast enhancement to the given {@link ImageProcessor}.
	 * @param ip The image processor to modify.
	 * @see PreprocessingManager#applyEnhanceContrast(ImageProcessor, double)
	 */
	public void applyEnhanceContrast(ImageProcessor ip);
	
    /**
     * Creates a {@link SwingWorker}, that can apply a median filter preview on a given {@link ImageProcessor} if executed.
     * @param ip The image processor to modify.
     * @see PreprocessingPreviewMedianWorker
     */
	public SwingWorker<Void,Void> createPreviewMedianWorker(ImageProcessor ip);
	
    /**
     * Creates a {@link SwingWorker}, that can apply a median filter on a given {@link ImageStack}, or on an individual {@link ImageProcessor} among the given stack if executed.
	 * @param stack The array of images to process (slices).
	 * @param processAll True to process all slices.
	 * @param targetSlice The specific slice to process if not all.
	 * @see PreprocessingApplyMedianWorker
     */
	public SwingWorker<Void,Void> createApplyMedianWorker(ImageStack stack, boolean processAll, int targetSlice);
	
    /**
     * Creates a {@link SwingWorker}, that take care of processing measurements and showing them, if executed.
	 * @see MeasuresProcessingWorker
     */
	public SwingWorker<Void,Void> createMeasuresProcessingWorker();
	
    /**
     * Export as a CSV file the table passed in parameter. 
     * @param rt the ResultsTable to export as a CSV file.
     * @param path the path of the file as a String. 
     */
	public void exportResultsTable(ResultsTable rt, String path);
	
	 /**
     * Calculate the summary ResultTable generated by ImageJ.
     * @param rt the ResultsTable to generate statistics from.
     * @return the ResultsTable containing the statistics.  
     */
    public ResultsTable calculateSummaryTable(ResultsTable rt);
    
    /**
     * Generate a list containing histograms for each columns of the ResultsTable.
     * @param rt ResultsTable to generated histograms from.
     * @return List that contain ImagePlus Object of histograms for each column of the table.
     */
    public List<ImagePlus> generateHistograms(ResultsTable rt);
}
