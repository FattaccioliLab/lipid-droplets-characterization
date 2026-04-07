package fr.sorbonne_universite.ldc.tests.unit;

import javax.swing.SwingWorker;

import org.junit.jupiter.api.Test;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.model.LDCServiceImpl;
import ij.IJ;
import ij.ImagePlus;

/**
 * Pipeline tests.
 */
public class TestPipeline {

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
	
	@Test
	public void test1() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_pipeline/test1_res.tif");
		ImagePlus expectedMask = importImage("/expected/test_pipeline/test1_mask.tif");
		
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
        
		ldcPlugin.setThresholdMethod("Moments");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
		
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
	@Test
	public void test2() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_pipeline/test2_res.tif");
		ImagePlus expectedMask = importImage("/expected/test_pipeline/test2_mask.tif");
		
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(0.35);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(4);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack(), false, 2);
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        // Need to investigate here
		ldcPlugin.setThresholdMethod("Triangle");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
		
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
	@Test
	public void test3() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedImage = importImage("/expected/test_pipeline/test3_res.tif");
		ImagePlus expectedMask = importImage("/expected/test_pipeline/test3_mask.tif");
		
		ImagePlus image = importImage("/TestSample.tif");
		
        ldcPlugin.setEnhanceContrast(true);
        ldcPlugin.setEnhanceSaturatedPercent(4);
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
		
        ldcPlugin.setMedianFilter(true);
        ldcPlugin.setMedianRadius(2);
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack(), true, 0);
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		ldcPlugin.setThresholdMethod("Otsu");
		ldcPlugin.setThresholdDarkBackground(true);
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
		
		ldcPlugin.setErosion(true);
		ldcPlugin.applyMorphology(mask);
		
        Utils.checkSameDimensions(expectedImage, image);
        Utils.checkSameDisplayRange(expectedImage, image);
        Utils.checkSamePixels(expectedImage, image);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
}
