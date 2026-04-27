package fr.sorbonne_universite.ldc.tests.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import ij.measure.Calibration;
import fr.sorbonne_universite.ldc.model.AnalysisSettings;
import fr.sorbonne_universite.ldc.model.rightpanel.JSONManager;

/**
 * Unit tests for importing / exporting LDC plug-in parameters as JSON.
 */
public class TestJSON {
	
	private JSONManager jsonManager;
	private AnalysisSettings settings;
	private String testFilePath;
	
	@BeforeEach
	public void setUp() {
		jsonManager = new JSONManager();
		settings = new AnalysisSettings();
		testFilePath = "test_settings.json";
	}
	
	@AfterEach
	public void tearDown() {
		// Clean up the test file after each test
		File testFile = new File(testFilePath);
		if (testFile.exists()) {
			testFile.delete();
		}
	}
	
	// =========================================================================
	// Save tests
	// =========================================================================
	
	@Test
	public void testSaveAnalysis_DefaultSettings() throws IOException {
		// Save default settings
		jsonManager.saveAnalysis(testFilePath, settings);
		
		// Check file creation
		File testFile = new File(testFilePath);
		assertTrue(testFile.exists(), "JSON file should exist");
		assertTrue(testFile.length() > 0, "JSON file should not be empty");
	}
	
	@Test
	public void testSaveAnalysis_CustomSettings() throws IOException {
		settings.setEnhanceContrast(true);
		settings.setEnhanceSaturatedPercent(0.5);
		settings.setMedianFilter(true);
		settings.setMedianRadius(3.0);
		settings.setThresholdMethod("Otsu");
		settings.setThresholdMinValue(50);
		settings.setThresholdMaxValue(200);
		settings.setThresholdDarkBackground(true);
		
		jsonManager.saveAnalysis(testFilePath, settings);
		
		// Check file existence
		File testFile = new File(testFilePath);
		assertTrue(testFile.exists(), "JSON file should exist");
	}
	
	@Test
	public void testSaveAnalysis_InvalidExtension() {
		assertThrows(IllegalArgumentException.class, () -> {
			jsonManager.saveAnalysis("test_settings.txt", settings);
		});
	}
	
	@Test
	public void testSaveAnalysis_NoExtension() {
		assertThrows(IllegalArgumentException.class, () -> {
			jsonManager.saveAnalysis("test_settings", settings);
		});
	}
	
	// =========================================================================
	// Load tests
	// =========================================================================
	
	@Test
	public void testLoadAnalysis_DefaultSettings() throws IOException {
		// Save then load
		jsonManager.saveAnalysis(testFilePath, settings);
		AnalysisSettings loadedSettings = jsonManager.loadAnalysis(testFilePath);
		
		// Check object
		assertNotNull(loadedSettings, "Loaded settings should not be null");
		
		// Check default values
		assertFalse(loadedSettings.enhanceContrastEnabled(), "enhanceContrast should be false by default");
		assertEquals(AnalysisSettings.DFL_EC_SATURATED, loadedSettings.getEnhanceSaturatedPercent(), 0.001,
				"enhanceSaturatedPercent should match default value");
		assertFalse(loadedSettings.medianFilterEnabled(), "medianFilter should be false by default");
		assertEquals(AnalysisSettings.DFL_MEDIAN_RADIUS, loadedSettings.getMedianRadius(), 0.001,
				"medianRadius should match default value");
	}
	
	@Test
	public void testLoadAnalysis_CustomSettings() throws IOException {
		settings.setEnhanceContrast(true);
		settings.setEnhanceSaturatedPercent(0.75);
		settings.setMedianFilter(true);
		settings.setMedianRadius(5.0);
		settings.setThresholdMethod("Triangle");
		settings.setThresholdMinValue(30);
		settings.setThresholdMaxValue(220);
		settings.setThresholdDarkBackground(true);
		
		// Save then load
		jsonManager.saveAnalysis(testFilePath, settings);
		AnalysisSettings loadedSettings = jsonManager.loadAnalysis(testFilePath);
		
		// Check values
		assertTrue(loadedSettings.enhanceContrastEnabled(), "enhanceContrast should be true");
		assertEquals(0.75, loadedSettings.getEnhanceSaturatedPercent(), 0.001, "enhanceSaturatedPercent incorrect");
		assertTrue(loadedSettings.medianFilterEnabled(), "medianFilter should be true");
		assertEquals(5.0, loadedSettings.getMedianRadius(), 0.001, "medianRadius incorrect");
		assertEquals("Triangle", loadedSettings.getThresholdMethod(), "thresholdMethod incorrect");
		assertEquals(30, loadedSettings.getThresholdMinValue(), "thresholdMinValue incorrect");
		assertEquals(220, loadedSettings.getThresholdMaxValue(), "thresholdMaxValue incorrect");
		assertTrue(loadedSettings.thresholdDarkBackgroundEnabled(), "thresholdDarkBackground should be true");
	}
	
	@Test
	public void testLoadAnalysis_MorphologicalOperations() throws IOException {
		// Configure morphological operations
		settings.setMorphologicalOperation("Dilate");
		settings.setErosion(true);
		settings.setDilation(true);
		settings.setOpening(false);
		settings.setClosing(true);
		settings.setWathershed(true);
		
		// Save then load
		jsonManager.saveAnalysis(testFilePath, settings);
		AnalysisSettings loadedSettings = jsonManager.loadAnalysis(testFilePath);
		
		// Check
		assertEquals("Dilate", loadedSettings.getMorphologicalOperation(), "morphologicalOperation incorrect");
		assertTrue(loadedSettings.erosionEnabled(), "erosion should be true");
		assertTrue(loadedSettings.dilationEnabled(), "dilation should be true");
		assertFalse(loadedSettings.openingEnabled(), "opening should be false");
		assertTrue(loadedSettings.closingEnabled(), "closing should be true");
		assertTrue(loadedSettings.watershedEnabled(), "watershed should be true");
	}
	
	@Test
	public void testLoadAnalysis_ParticleAnalysis() throws IOException {
		// Configure particle analysis
		settings.setAnalyseMinSize(10.0);
		settings.setAnalyseMaxSize(1000.0);
		settings.setAnalyseMinCircularity(0.2);
		settings.setAnalyseMaxCircularity(0.9);
		settings.setAnalyseExcludeOnEdges(true);
		settings.setAnalyseCircularityThreshold(0.7);
		
		// Save then load
		jsonManager.saveAnalysis(testFilePath, settings);
		AnalysisSettings loadedSettings = jsonManager.loadAnalysis(testFilePath);
		
		// Check
		assertEquals(10.0, loadedSettings.getAnalyseMinSize(), 0.001, "analyseMinSize incorrect");
		assertEquals(1000.0, loadedSettings.getAnalyseMaxSize(), 0.001, "analyseMaxSize incorrect");
		assertEquals(0.2, loadedSettings.getAnalyseMinCircularity(), 0.001, "analyseMinCircularity incorrect");
		assertEquals(0.9, loadedSettings.getAnalyseMaxCircularity(), 0.001, "analyseMaxCircularity incorrect");
		assertTrue(loadedSettings.analyseExcludeOnEdgesEnabled(), "analyseExcludeOnEdges should be true");
		assertEquals(0.7, loadedSettings.getAnalyseCircularityThreshold(), 0.001, "analyseCircularityThreshold incorrect");
	}
	
	@Test
	public void testLoadAnalysis_CalibrationSettings() throws IOException {
		// Configure calibration
		settings.setIsCalibrated(true);
		Calibration cal = new Calibration();
		cal.pixelWidth = 0.5;
		cal.pixelHeight = 0.5;
		cal.setUnit("µm");
		settings.setCalibration(cal);
		
		// Save then load
		jsonManager.saveAnalysis(testFilePath, settings);
		AnalysisSettings loadedSettings = jsonManager.loadAnalysis(testFilePath);
		
		// Check
		assertTrue(loadedSettings.isCalibrated(), "isCalibrated should be true");
		assertNotNull(loadedSettings.getCalibration(), "calibration should not be null");
		assertEquals(0.5, loadedSettings.getCalibration().pixelWidth, 0.001, "pixelWidth incorrect");
		assertEquals(0.5, loadedSettings.getCalibration().pixelHeight, 0.001, "pixelHeight incorrect");
		assertEquals("µm", loadedSettings.getCalibration().getUnit(), "unit incorrect");
	}
	
	@Test
	public void testLoadAnalysis_ShowingOptions() throws IOException {
		// Configure display options
		settings.setShowArea(true);
		settings.setShowDiameter(true);
		settings.setShowMedian(false);
		settings.setShowMean(true);
		settings.setShowIntegratedDensity(true);
		settings.setShowCircularity(false);
		settings.setShowDefaultCalibration(true);
		
		// Save then load
		jsonManager.saveAnalysis(testFilePath, settings);
		AnalysisSettings loadedSettings = jsonManager.loadAnalysis(testFilePath);
		
		// Check
		assertTrue(loadedSettings.showAreaEnabled(), "showArea should be true");
		assertTrue(loadedSettings.showDiameterEnabled(), "showDiameter should be true");
		assertFalse(loadedSettings.showMedianEnabled(), "showMedian should be false");
		assertTrue(loadedSettings.showMeanEnabled(), "showMean should be true");
		assertTrue(loadedSettings.showIntegratedDensityEnabled(), "showIntegratedDensity should be true");
		assertFalse(loadedSettings.showCircularityEnabled(), "showCircularity should be false");
		assertTrue(loadedSettings.showDefaultCalibrationEnabled(), "showDefaultCalibration should be true");
	}
	
	@Test
	public void testLoadAnalysis_InvalidExtension() {
		assertThrows(IllegalArgumentException.class, () -> {
			jsonManager.loadAnalysis("test_settings.txt");
		});
	}
	
	@Test
	public void testLoadAnalysis_FileNotFound() {
		assertThrows(IOException.class, () -> {
			jsonManager.loadAnalysis("non_existent_file.json");
		});
	}
	
	// =========================================================================
	// Integration tests
	// =========================================================================
	
	@Test
	public void testSaveAndLoad_AllSettings() throws IOException {
		// Configure all settings
		settings.setEnhanceContrast(true);
		settings.setEnhanceSaturatedPercent(0.45);
		settings.setMedianFilter(true);
		settings.setMedianRadius(2.5);
		settings.setThresholdMethod("Yen");
		settings.setThresholdMinValue(40);
		settings.setThresholdMaxValue(210);
		settings.setThresholdDarkBackground(true);
		settings.setMorphologicalOperation("Open");
		settings.setErosion(true);
		settings.setDilation(false);
		settings.setOpening(true);
		settings.setClosing(false);
		settings.setWathershed(true);
		settings.setAnalyseMinSize(15.5);
		settings.setAnalyseMaxSize(800.0);
		settings.setAnalyseMinCircularity(0.3);
		settings.setAnalyseMaxCircularity(0.85);
		settings.setAnalyseExcludeOnEdges(true);
		settings.setAnalyseCircularityThreshold(0.6);
		settings.setIsCalibrated(true);
		Calibration cal = new Calibration();
		cal.pixelWidth = 0.25;
		cal.pixelHeight = 0.25;
		cal.setUnit("mm");
		settings.setCalibration(cal);
		settings.setShowArea(true);
		settings.setShowDiameter(false);
		settings.setShowMedian(true);
		settings.setShowMean(true);
		settings.setShowIntegratedDensity(false);
		settings.setShowCircularity(true);
		settings.setShowDefaultCalibration(false);
		
		// Save
		jsonManager.saveAnalysis(testFilePath, settings);
		
		// Load
		AnalysisSettings loadedSettings = jsonManager.loadAnalysis(testFilePath);
		
		// Check all values
		assertEquals(settings.enhanceContrastEnabled(), loadedSettings.enhanceContrastEnabled());
		assertEquals(settings.getEnhanceSaturatedPercent(), loadedSettings.getEnhanceSaturatedPercent(), 0.001);
		assertEquals(settings.medianFilterEnabled(), loadedSettings.medianFilterEnabled());
		assertEquals(settings.getMedianRadius(), loadedSettings.getMedianRadius(), 0.001);
		assertEquals(settings.getThresholdMethod(), loadedSettings.getThresholdMethod());
		assertEquals(settings.getThresholdMinValue(), loadedSettings.getThresholdMinValue());
		assertEquals(settings.getThresholdMaxValue(), loadedSettings.getThresholdMaxValue());
		assertEquals(settings.thresholdDarkBackgroundEnabled(), loadedSettings.thresholdDarkBackgroundEnabled());
		assertEquals(settings.getMorphologicalOperation(), loadedSettings.getMorphologicalOperation());
		assertEquals(settings.erosionEnabled(), loadedSettings.erosionEnabled());
		assertEquals(settings.dilationEnabled(), loadedSettings.dilationEnabled());
		assertEquals(settings.openingEnabled(), loadedSettings.openingEnabled());
		assertEquals(settings.closingEnabled(), loadedSettings.closingEnabled());
		assertEquals(settings.watershedEnabled(), loadedSettings.watershedEnabled());
		assertEquals(settings.getAnalyseMinSize(), loadedSettings.getAnalyseMinSize(), 0.001);
		assertEquals(settings.getAnalyseMaxSize(), loadedSettings.getAnalyseMaxSize(), 0.001);
		assertEquals(settings.getAnalyseMinCircularity(), loadedSettings.getAnalyseMinCircularity(), 0.001);
		assertEquals(settings.getAnalyseMaxCircularity(), loadedSettings.getAnalyseMaxCircularity(), 0.001);
		assertEquals(settings.analyseExcludeOnEdgesEnabled(), loadedSettings.analyseExcludeOnEdgesEnabled());
		assertEquals(settings.getAnalyseCircularityThreshold(), loadedSettings.getAnalyseCircularityThreshold(), 0.001);
		assertEquals(settings.isCalibrated(), loadedSettings.isCalibrated());
		assertEquals(settings.getCalibration().pixelWidth, loadedSettings.getCalibration().pixelWidth, 0.001);
		assertEquals(settings.getCalibration().pixelHeight, loadedSettings.getCalibration().pixelHeight, 0.001);
		assertEquals(settings.getCalibration().getUnit(), loadedSettings.getCalibration().getUnit());
		assertEquals(settings.showAreaEnabled(), loadedSettings.showAreaEnabled());
		assertEquals(settings.showDiameterEnabled(), loadedSettings.showDiameterEnabled());
		assertEquals(settings.showMedianEnabled(), loadedSettings.showMedianEnabled());
		assertEquals(settings.showMeanEnabled(), loadedSettings.showMeanEnabled());
		assertEquals(settings.showIntegratedDensityEnabled(), loadedSettings.showIntegratedDensityEnabled());
		assertEquals(settings.showCircularityEnabled(), loadedSettings.showCircularityEnabled());
		assertEquals(settings.showDefaultCalibrationEnabled(), loadedSettings.showDefaultCalibrationEnabled());
	}
	
	@Test
	public void testSaveAndLoad_IndependentInstances() throws IOException {
		// Modify original settings
		settings.setEnhanceContrast(true);
		settings.setThresholdMethod("Li");
		
		// Save and load
		jsonManager.saveAnalysis(testFilePath, settings);
		AnalysisSettings loadedSettings = jsonManager.loadAnalysis(testFilePath);
		
		// Modify loaded settings
		loadedSettings.setEnhanceContrast(false);
		loadedSettings.setThresholdMethod("Moments");
		
		// Check independence
		assertTrue(settings.enhanceContrastEnabled(), "Original instance should not be modified");
		assertEquals("Li", settings.getThresholdMethod(), "Original instance should not be modified");
		assertFalse(loadedSettings.enhanceContrastEnabled(), "Loaded instance should be modified");
		assertEquals("Moments", loadedSettings.getThresholdMethod(), "Loaded instance should be modified");
	}
}