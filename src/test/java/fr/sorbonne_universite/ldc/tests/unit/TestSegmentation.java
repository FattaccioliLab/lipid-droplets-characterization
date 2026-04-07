package fr.sorbonne_universite.ldc.tests.unit;
import org.junit.jupiter.api.Test;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.model.LDCServiceImpl;
import ij.IJ;
import ij.ImagePlus;

/**
 * Unit tests for LDC plugin segmentation/threshold treatments, including :
 * <ul>
 * 	<li>Manual method.</li>
 * 	<li>Otsu method.</li>
 * 	<li>Moments method.</li>
 * 	<li>Triangle method.</li>
 * 	<li>Yen method.</li>
 * 	<li>Li method.</li>
 * 	<li>Dark background option.</li>
 * </ul> 
 */
public class TestSegmentation {

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
	
    // =========================================================================
    // MANUAL METHOD
    // =========================================================================
	
	@Test
	public void test1() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test1.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Manual");
		ldcPlugin.setThresholdMinValue(1048);
		ldcPlugin.setThresholdMaxValue(2576);
		ldcPlugin.previewManualThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
	@Test
	public void test2() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test2.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Manual");
		ldcPlugin.setThresholdMinValue(430);
		ldcPlugin.setThresholdMaxValue(3731);
		ldcPlugin.previewManualThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
    // =========================================================================
    // OTSU METHOD
    // =========================================================================
	
	@Test
	public void test3() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		// pas dark_black
		// pas dark
		// pas default_black
		
		// light black ok
		// light ok
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test3.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Otsu");
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
	@Test
	public void test4() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test4.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Otsu");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
    // =========================================================================
    // MOMENTS METHOD
    // =========================================================================
	
	@Test
	public void test5() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test5.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Moments");
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
	@Test
	public void test6() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test6.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Moments");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
    // =========================================================================
    // TRIANGLE METHOD
    // =========================================================================
	
	@Test
	public void test7() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test7.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Triangle");
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
	@Test
	public void test8() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test8.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Triangle");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
    // =========================================================================
    // YEN METHOD
    // =========================================================================
	
	@Test
	public void test9() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test9.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Yen");
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
	@Test
	public void test10() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test10.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Yen");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
    // =========================================================================
    // LI METHOD
    // =========================================================================
	
	@Test
	public void test11() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test11.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Li");
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
	@Test
	public void test12() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_segmentation/test12.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
		ldcPlugin.setThresholdMethod("Li");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
}
