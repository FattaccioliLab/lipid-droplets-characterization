package fr.sorbonne_universite.ldc.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingWorker;

import org.scijava.service.SciJavaService;

import fr.sorbonne_universite.ldc.model.leftpanel.PreprocessingManager;
import fr.sorbonne_universite.ldc.model.workers.BatchWorker;
import fr.sorbonne_universite.ldc.model.workers.MeasuresProcessingWorker;
import fr.sorbonne_universite.ldc.model.workers.PreprocessingApplyMedianWorker;
import fr.sorbonne_universite.ldc.model.workers.PreprocessingPreviewMedianWorker;
import fr.sorbonne_universite.ldc.ui.rightpanel.BatchWindow;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

/**
 * Service exposing the API for manipulating parameters, operations, and the general state used for the Lipid Droplet Characterization Fiji plugin.
 */
public interface LDCService extends SciJavaService {

    // =========================================================================
    // GETTERS / SETTERS for the LDC state management.
    // =========================================================================
	
    // =============================================
    // Preprocessing
    // =============================================
	
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
	
    // =============================================
    // Segmentation / thresholding
    // =============================================
	
	// Threshold methods
	public List<String> getThresholdMethodsList();
	public void setThresholdMethod(String method);
	
	// Threshold current method
	public String getThresholdMethod();
	
	// Threshold range
	public int getThresholdMinValue();
	public int getThresholdMaxValue();
    public void setThresholdMinValue(int value);
    public void setThresholdMaxValue(int value);
	
	// Dark background option
	public boolean thresholdDarkBackgroundEnabled();
	public void setThresholdDarkBackground(boolean thresholdDarkBackground);
	
	
    // =============================================
    // Binary mask morphological operations
    // =============================================
	
    public List<String> getMorphologicalOperationsList();
    public String getMorphologicalOperation();
    public void setMorphologicalOperation(String method);
	
	public boolean watershedEnabled();
	public void setWathershed(boolean watershed);
    
    // =============================================
    // Analyse particles
    // =============================================

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
	
	// Circularity threshold
	public double getAnalyseCircularityThreshold();
	public void setAnalyseCircularityThreshold(double analyseCircularityThreshold);
	
	// isCalibrated
	public boolean isCalibrated();
	public void setIsCalibrated(boolean isCalibrated);
	
	// Calibration
	public Calibration getCalibration();
	public void setCalibration(Calibration calibration);
	
    // =============================================
    // Measurements showing options
    // =============================================
	
	public boolean showAreaEnabled();
	public void setShowArea(boolean showArea);
	
	public boolean showDiameterEnabled();
	public void setShowDiameter(boolean showDiameter);
	
	public boolean showMedianEnabled();
	public void setShowMedian(boolean showMedian);

	public boolean showMeanEnabled();
	public void setShowMean(boolean showMean);
	
	public boolean showIntegratedDensityEnabled();
	public void setShowIntegratedDensity(boolean showIntegratedDensity);
	
	public boolean showCircularityEnabled();
	public void setShowCircularity(boolean showCircularity);
	
	public boolean showDefaultCalibrationEnabled();
	public void setShowDefaultCalibration(boolean showDefaultCalibration);
	
    // =========================================================================
    // OPERATIONS
    // =========================================================================
	
    // =============================================
    // Preprocessing
    // =============================================
	
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
     * Creates a {@link SwingWorker} that applies a median filter on a given {@link ImageStack} if executed.
	 * @param stack The array of images to process (slices).
	 * @see PreprocessingApplyMedianWorker
     */
	public SwingWorker<Void,Void> createApplyMedianWorker(ImageStack stack);
	
    // =============================================
    // Segmentation / thresholding
    // =============================================
	
	/**
	 * Preview manual threshold on the image.
	 * @param imp The image on which the preview must be done.
	 */
    public void previewManualThreshold(ImagePlus imp);
    
    /**
     * Preview auto threshold on the image.
     * @param imp The image on which the preview must be done.
     * @return {min, max} computed threshold bounds.
     */
    public double[] previewAutoThreshold(ImagePlus imp);
    
    /**
     * Apply final conversion to mask.
     * @param imp The image on which the preview has been previously done.
     * @return The resulting mask.
     */
    public ImagePlus applyThreshold(ImagePlus imp);
    
    /**
     * Reset threshold.
     * @param imp
     * @return
     */
    public boolean resetThreshold(ImagePlus imp);
    
    // =============================================
    // Morphology
    // =============================================
    
    public void captureMorphologySnapshot(ImagePlus imp);
    
    public void previewMorphology(ImagePlus imp);
    
    public boolean resetMorphologyPreview(ImagePlus imp);
    
    public boolean applyMorphology(ImagePlus imp);
	
    // =============================================
    // Measurements
    // =============================================
    
    /**
     * Create a {@link SwingWorker}, that take care of processing the preview of the selected measured parameters,
     * it shows the preview as a new window showing outlines of detected particles.
     * @param img	The current image to consider.
     * @see	MeasuresPreviewWorker
     */
    public SwingWorker<Void,Void> createMeasuresPreviewWorker(ImagePlus img);
    
    /**
     * Create a {@link SwingWorker}, that take care of processing measurements and returning them, if executed.
     * @param img The current image to consider.
	 * @see MeasuresProcessingWorker
     */
	public SwingWorker<ResultsTable,Void> createMeasuresProcessingWorker(ImagePlus img, ImagePlus binaryImg);
	
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
    
    // =============================================
    // Batch mode
    // =============================================
    
    /**
     * Create a {@link SwingWorker}, that has the goal of applying particle analysis on a whole given {@code directory}.
	 * @param inputDirectory	The input directory where it has to find '.tif' and '.tiff' files.
	 * @param outputFile		The output '.csv' where it writes the results.
	 * @param bw				A reference to the {@link BatchWindow} creating this worker, needed to update the progress bar.
     * @see BatchWorker
     */
    public SwingWorker<Void,Void> createBatchWorker(File inputDirectory, File outputFile, BatchWindow bw);
    
    // =============================================
    // Saving / loading analysis parameters, as JSON
    // =============================================
    
    /**
     * Save current plug-in general state into a file (.json) specified by the {@code outputPath}.
     * @param outputPath 	The path where the current plug-in state needs to be saved.
     * @throws IOException	If an error occurs while attempting to save the file.
     */
    public void saveAnalysis(String outputPath) throws IOException;
    
    /**
     * Load a plug-in general state from a file (.json) specified by the {@code inputPath}, and the current state by it.
     * @param inputPath 	The path from where the new current plug-in state needs to be loaded.
     * @throws IOException	If an error occurs while attempting to load the file.
     */
    public void loadAnalysis(String inputPath) throws IOException;
}
