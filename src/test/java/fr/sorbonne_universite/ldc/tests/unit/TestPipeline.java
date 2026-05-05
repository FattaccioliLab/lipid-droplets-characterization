package fr.sorbonne_universite.ldc.tests.unit;

import java.io.IOException;

import javax.swing.SwingWorker;

import org.junit.jupiter.api.Test;

import fr.sorbonne_universite.ldc.model.AnalysisSettings;
import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.model.LDCServiceImpl;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;

/**
 * Integration tests, for the entire pipeline workflow with different scenarios, including .csv results check.
 */
public class TestPipeline {

	/**
	 * Imports an image from the src/test/resources folder.
	 * @param imagePath 					The path of the image within the folder.
	 * @return								The corresponding image.
	 * @throws IllegalArgumentException		If the image has not been found.
	 */
	private ImagePlus importImage(String imagePath) {
		String path = getClass().getResource(imagePath).getPath();
        ImagePlus image = IJ.openImage(path);
        if (image == null) {
        	throw new IllegalArgumentException(imagePath+" not found");
        }
        return image;
	}
	
	/**
	 * Imports a ResultsTable from the src/test/resources folder.
	 * @param tablePath						The path of the .csv table within the folder.
	 * @return								The corresponding ResultsTable.
	 * @throws IllegalArgumentException		If the table has not been found.
	 */
	private ResultsTable importTable(String tablePath) {
		String path = getClass().getResource(tablePath).getPath();
		try {
			ResultsTable results = ResultsTable.open(path);
	        if (results == null) throw new IllegalArgumentException(tablePath+" not found");
	        return results;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Test
	public void test1() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_pipeline/test1_res.tif");
		ImagePlus expectedMask = importImage("/expected/test_pipeline/test1_mask.tif");
		ResultsTable expectedResults = importTable("/expected/test_pipeline/test1_table.csv");
		ImagePlus image = importImage("/TestSample.tif");
		
		// Preprocessing
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(2);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        // Segmentation / Thresholding
		ldcPlugin.setThresholdMethod("Moments");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
		
		// Particle Analysis
		ldcPlugin.setShowArea(true);
		ldcPlugin.setShowCircularity(true);
		ldcPlugin.setShowIntegratedDensity(true);
		ldcPlugin.setShowMean(true);
		ldcPlugin.setShowMedian(true);
		ldcPlugin.setAnalyseMinSize(0); // Not mandatory
		ldcPlugin.setAnalyseMaxSize(AnalysisSettings.DFL_ANALYSE_MAX_SIZE); // Not mandatory
		ldcPlugin.setAnalyseMinCircularity(0); // Not mandatory
		ldcPlugin.setAnalyseMaxCircularity(1); // Not mandatory
		ldcPlugin.setAnalyseExcludeOnEdges(true);
		ldcPlugin.setAnalyseIncludeHoles(false); // Not mandatory
		ldcPlugin.setCalibration(image.getCalibration());
		ldcPlugin.setIsCalibrated(true);
        SwingWorker<ResultsTable, Void> worker2 = ldcPlugin.createMeasuresProcessingWorker(image, mask);
        worker2.execute();
        ResultsTable results = null;
        try {
        	results = worker2.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.checkSameResultsTable(expectedResults, results, 0.0001);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, expectedMask, image, mask}, ldcPlugin);
	}
	
	@Test
	public void test2() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_pipeline/test2_res.tif");
		ImagePlus expectedMask = importImage("/expected/test_pipeline/test2_mask.tif");
		ResultsTable expectedResults = importTable("/expected/test_pipeline/test2_table.csv");
		ImagePlus image = importImage("/TestSample.tif");
		
		// Preprocessing
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(0.35);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(4);

        // Segmentation / Thresholding
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ldcPlugin.setThresholdMethod("Triangle");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
		
		// Particle Analysis
		ldcPlugin.setShowArea(true);
		ldcPlugin.setShowCircularity(true);
		ldcPlugin.setShowIntegratedDensity(true);
		ldcPlugin.setShowMean(true);
		ldcPlugin.setShowMedian(true);
		ldcPlugin.setAnalyseMinSize(0); // Not mandatory
		ldcPlugin.setAnalyseMaxSize(AnalysisSettings.DFL_ANALYSE_MAX_SIZE); // Not mandatory
		ldcPlugin.setAnalyseMinCircularity(0); // Not mandatory
		ldcPlugin.setAnalyseMaxCircularity(0.8);
		ldcPlugin.setAnalyseExcludeOnEdges(true);
		ldcPlugin.setAnalyseIncludeHoles(false); // Not mandatory
		ldcPlugin.setCalibration(image.getCalibration());
		ldcPlugin.setIsCalibrated(true);
        SwingWorker<ResultsTable, Void> worker2 = ldcPlugin.createMeasuresProcessingWorker(image, mask);
        worker2.execute();
        ResultsTable results = null;
        try {
        	results = worker2.get();
		} catch (Exception e) {
			e.printStackTrace();
		}

        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.checkSameResultsTable(expectedResults, results, 0.0001);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, expectedMask, image, mask}, ldcPlugin);
	}
	
	@Test
	public void test3() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_pipeline/test3_res.tif");
		ImagePlus expectedMask = importImage("/expected/test_pipeline/test3_mask.tif");
		ResultsTable expectedResults = importTable("/expected/test_pipeline/test3_table.csv");
		
		ImagePlus image = importImage("/TestSample.tif");
		
		// Preprocessing
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(4);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(2);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        // Segmentation / Thresholding
		ldcPlugin.setThresholdMethod("Otsu");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
		
		// Particle Analysis
		ldcPlugin.setShowArea(true);
		ldcPlugin.setShowCircularity(true);
		ldcPlugin.setShowIntegratedDensity(true);
		ldcPlugin.setShowMean(true);
		ldcPlugin.setShowMedian(true);
		ldcPlugin.setAnalyseMinSize(1);
		ldcPlugin.setAnalyseMaxSize(AnalysisSettings.DFL_ANALYSE_MAX_SIZE); // Not mandatory
		ldcPlugin.setAnalyseMinCircularity(0); // Not mandatory
		ldcPlugin.setAnalyseMaxCircularity(1); // Not mandatory
		ldcPlugin.setAnalyseExcludeOnEdges(false); // Not mandatory
		ldcPlugin.setAnalyseIncludeHoles(false); // Not mandatory
		ldcPlugin.setCalibration(image.getCalibration());
		ldcPlugin.setIsCalibrated(true);
        SwingWorker<ResultsTable, Void> worker2 = ldcPlugin.createMeasuresProcessingWorker(image, mask);
        worker2.execute();
        ResultsTable results = null;
        try {
        	results = worker2.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.checkSameResultsTable(expectedResults, results, 0.0001);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, expectedMask, image, mask}, ldcPlugin);
	}
	
	@Test
	public void test4() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_pipeline/test4_res.tif");
		ImagePlus expectedMask = importImage("/expected/test_pipeline/test4_mask.tif");
		ResultsTable expectedResults = importTable("/expected/test_pipeline/test4_table.csv");
		
		ImagePlus image = importImage("/TestSample.tif");
		
		// Preprocessing
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(4);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(2);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        // Segmentation / Thresholding
		ldcPlugin.setThresholdMethod("Otsu");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
		
		// Binary morphological operation
		ldcPlugin.setMorphologicalOperation("Erode");
		ldcPlugin.applyMorphology(mask);
		
		// Particle Analysis
		ldcPlugin.setShowArea(true);
		ldcPlugin.setShowCircularity(true);
		ldcPlugin.setShowIntegratedDensity(true);
		ldcPlugin.setShowMean(true);
		ldcPlugin.setShowMedian(true);
		ldcPlugin.setAnalyseMinSize(1);
		ldcPlugin.setAnalyseMaxSize(AnalysisSettings.DFL_ANALYSE_MAX_SIZE); // Not mandatory
		ldcPlugin.setAnalyseMinCircularity(0); // Not mandatory
		ldcPlugin.setAnalyseMaxCircularity(1); // Not mandatory
		ldcPlugin.setAnalyseExcludeOnEdges(false); // Not mandatory
		ldcPlugin.setAnalyseIncludeHoles(false); // Not mandatory
		ldcPlugin.setCalibration(image.getCalibration());
		ldcPlugin.setIsCalibrated(true);
        SwingWorker<ResultsTable, Void> worker2 = ldcPlugin.createMeasuresProcessingWorker(image, mask);
        worker2.execute();
        ResultsTable results = null;
        try {
        	results = worker2.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.checkSameResultsTable(expectedResults, results, 0.0001);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, expectedMask, image, mask}, ldcPlugin);
	}

	@Test
	public void test5() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_pipeline/test5_res.tif");
		ImagePlus expectedMask = importImage("/expected/test_pipeline/test5_mask.tif");
		ResultsTable expectedResults = importTable("/expected/test_pipeline/test5_table.csv");
		
		ImagePlus image = importImage("/TestSample.tif");
		
		// Preprocessing
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(2);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        // Segmentation / Thresholding
		ldcPlugin.setThresholdMethod("Moments");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
		
		// Particle Analysis
		ldcPlugin.setShowArea(true);
		ldcPlugin.setShowCircularity(true);
		ldcPlugin.setShowIntegratedDensity(true);
		ldcPlugin.setShowMean(true);
		ldcPlugin.setShowMedian(true);
		ldcPlugin.setAnalyseMinSize(0);
		ldcPlugin.setAnalyseMaxSize(AnalysisSettings.DFL_ANALYSE_MAX_SIZE); // Not mandatory
		ldcPlugin.setAnalyseMinCircularity(0); // Not mandatory
		ldcPlugin.setAnalyseMaxCircularity(1); // Not mandatory
		ldcPlugin.setAnalyseExcludeOnEdges(true);
		ldcPlugin.setAnalyseIncludeHoles(true);
		ldcPlugin.setCalibration(image.getCalibration());
		ldcPlugin.setIsCalibrated(true);
        SwingWorker<ResultsTable, Void> worker2 = ldcPlugin.createMeasuresProcessingWorker(image, mask);
        worker2.execute();
        ResultsTable results = null;
        try {
        	results = worker2.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.checkSameResultsTable(expectedResults, results, 0.0001);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, expectedMask, image, mask}, ldcPlugin);
	}
}
