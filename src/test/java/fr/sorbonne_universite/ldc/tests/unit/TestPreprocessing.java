package fr.sorbonne_universite.ldc.tests.unit;

import javax.swing.SwingWorker;

import org.junit.jupiter.api.Test;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.model.LDCServiceImpl;
import ij.IJ;
import ij.ImagePlus;

/**
 * Unit tests for LDC plugin preprocessing treatments, including :
 * <ul>
 * 	<li>Contrast enhancement (preview).</li>
 * 	<li>Median filter application.</li>
 * </ul> 
 */
public class TestPreprocessing {

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
        
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, image}, ldcPlugin);
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
        
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, image}, ldcPlugin);
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
        
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, image}, ldcPlugin);
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
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, image}, ldcPlugin);
	}
	
	@Test
	public void test5() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test5.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(2);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, image}, ldcPlugin);
	}
	
	@Test
	public void test6() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test6.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(20);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, image}, ldcPlugin);
	}
	
    // =========================================================================
    // BOTH CONTRAST ENHANCEMENT AND MEDIAN FILTER TESTS
    // =========================================================================
	
	@Test
	public void test7() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test7.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(10);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(5);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, image}, ldcPlugin);
	}
	
	@Test
	public void test8() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_preprocessing/test8.tif");
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(10);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(2.45);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.cleanup(new ImagePlus[]{expectedImage, image}, ldcPlugin);
	}
	
}
