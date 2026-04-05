package fr.sorbonne_universite.ldc.tests.unit;

import static org.junit.jupiter.api.Assertions.*;

import javax.swing.SwingWorker;

import org.junit.jupiter.api.Test;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.model.LDCServiceImpl;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class TestPreprocessing {

	/**
	 * Imports an image from the src/test/resources folder.
	 * @param imageName 					The path of the image within the folder.
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
	 * Checks if 2 given images have the same dimensions.
	 * @param expected 		The reference image.
	 * @param actual 		The image that needs to have the same dimensions as {@code expected}.
	 */
	private void checkSameDimensions(ImagePlus expected, ImagePlus actual) {
        assertEquals(expected.getWidth(), actual.getWidth(), "Different width");
        assertEquals(expected.getHeight(), actual.getHeight(), "Different height");
        assertEquals(expected.getNSlices(), actual.getNSlices(), "Different number of slices");
	}
	
	/**
	 * Checks if 2 given images have the same pixels across all slices.
	 * @param expected 		The reference image.
	 * @param actual 		The image that needs to have the same pixels as {@code expected}.
	 */
	private void checkSamePixels(ImagePlus expected, ImagePlus actual) {
	    // Iterate through all slices
	    for (int slice = 1; slice <= expected.getStackSize(); slice++) {
	        expected.setSlice(slice);
	        actual.setSlice(slice);
	        
	        ImageProcessor ip1 = expected.getProcessor();
	        ImageProcessor ip2 = actual.getProcessor();

	        for (int y = 0; y < expected.getHeight(); y++) {
	            for (int x = 0; x < actual.getWidth(); x++) {
	                assertEquals(ip1.getPixelValue(x, y), ip2.getPixelValue(x, y), 
	                		"Different pixel at slice " + slice + " in (" + x + ", " + y + ")");
	            }
	        }
	    }
	}
	
	/**
	 * Checks if 2 given images have the same display range (min/max values).
	 * This verifies the Brightness & Contrast settings (LUT parameters) without comparing actual pixel values.
	 * @param expected 		The reference image.
	 * @param actual 		The image that needs to have the same display range as {@code expected}.
	 */
	private void checkSameDisplayRange(ImagePlus expected, ImagePlus actual) {
	    ImageProcessor ip1 = expected.getProcessor();
	    ImageProcessor ip2 = actual.getProcessor();
	    
	    assertEquals(ip1.getMin(), ip2.getMin(), "Different display min");
	    assertEquals(ip1.getMax(), ip2.getMax(), "Different display max");
	}
	
    // =========================================================================
    // CONTRAST ENHANCEMENT TESTS (does not modify pixel values)
    // =========================================================================
	
	@Test
	public void test1() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test1.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(0.35);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
        
        checkSameDimensions(expectedImage, image);
        checkSameDisplayRange(expectedImage, image);
	}
	
	@Test
	public void test2() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test2.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(10);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
        
        checkSameDimensions(expectedImage, image);
        checkSameDisplayRange(expectedImage, image);
	}
	
	@Test
	public void test3() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test3.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(40);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
        
        checkSameDimensions(expectedImage, image);
        checkSameDisplayRange(expectedImage, image);
	}
	
    // =========================================================================
    // MEDIAN FILTER TESTS
    // =========================================================================
	
	@Test
	public void test4() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test4.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(0);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack(), true, 0);
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        checkSameDimensions(expectedImage, image);
        checkSamePixels(expectedImage, image);
	}
	
	@Test
	public void test5() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test5.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(2);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack(), true, 0);
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        checkSameDimensions(expectedImage, image);
        checkSamePixels(expectedImage, image);
	}
	
	@Test
	public void test6() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test6.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(20);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack(), true, 0);
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        checkSameDimensions(expectedImage, image);
        checkSamePixels(expectedImage, image);
	}
	
	@Test
	public void test7() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test7.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(2);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack(), false, 1);
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        checkSameDimensions(expectedImage, image);
        checkSamePixels(expectedImage, image);
	}
	
	@Test
	public void test8() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test8.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(4);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack(), false, 5);
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        checkSameDimensions(expectedImage, image);
        checkSamePixels(expectedImage, image);
	}
	
    // =========================================================================
    // BOTH CONTRAST ENHANCEMENT AND MEDIAN FILTER TESTS
    // =========================================================================
	
	@Test
	public void test9() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test9.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(0.35);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(10);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack(), false, 3);
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        checkSameDimensions(expectedImage, image);
        checkSameDisplayRange(expectedImage, image);
        checkSamePixels(expectedImage, image);
	}
	
	@Test
	public void test10() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test10.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(10);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(5);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack(), true, 0);
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        checkSameDimensions(expectedImage, image);
        checkSameDisplayRange(expectedImage, image);
        checkSamePixels(expectedImage, image);
	}
}
