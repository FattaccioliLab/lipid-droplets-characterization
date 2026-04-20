package fr.sorbonne_universite.ldc.tests.unit;

import org.junit.jupiter.api.Test;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.model.LDCServiceImpl;
import ij.IJ;
import ij.ImagePlus;

/**
 * Unit tests for LDC plugin binary mask operations, including :
 * <ul>
 * 	<li>Erosion.</li>
 * 	<li>Dilatation.</li>
 * 	<li>Opening.</li>
 * 	<li>Closing.</li>
 * </ul> 
 */
public class TestBinaryMaskOperations {

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
    // EROSION
    // =========================================================================
	
	@Test
	public void test1() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_binary_operations/test1.tif"); // works with test2.tif (dilatation)
		ImagePlus mask = importImage("/TestMask.tif");
		
		ldcPlugin.setMorphologicalOperation("Erode");
		ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.cleanup(new ImagePlus[]{expectedMask, mask}, ldcPlugin);
	}
	
    // =========================================================================
    // DILATATION
    // =========================================================================
	
	@Test
	public void test2() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_binary_operations/test2.tif");
		ImagePlus mask = importImage("/TestMask.tif");
		
		ldcPlugin.setMorphologicalOperation("Dilate");
		ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.cleanup(new ImagePlus[]{expectedMask, mask}, ldcPlugin);
	}
	
    // =========================================================================
    // OPENING
    // =========================================================================
	
	@Test
	public void test3() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_binary_operations/test3.tif");
		ImagePlus mask = importImage("/TestMask.tif");
		
		ldcPlugin.setMorphologicalOperation("Open");
		ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.cleanup(new ImagePlus[]{expectedMask, mask}, ldcPlugin);
	}
	
    // =========================================================================
    // CLOSING
    // =========================================================================
	
	@Test
	public void test4() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_binary_operations/test4.tif");
		ImagePlus mask = importImage("/TestMask.tif");
		
		ldcPlugin.setMorphologicalOperation("Close");
		ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.cleanup(new ImagePlus[]{expectedMask, mask}, ldcPlugin);
	}
	
    // =========================================================================
    // MIXED OPERATIONS
    // =========================================================================
	
	//Opening -> Erosion
	@Test
	public void test5() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_binary_operations/test5.tif");
		ImagePlus mask = importImage("/TestMask.tif");
		
		// Apply Opening FIRST
		ldcPlugin.setMorphologicalOperation("Open");
        ldcPlugin.applyMorphology(mask);
        
        //Apply Erosion SECOND
		ldcPlugin.setMorphologicalOperation("Erode");
        ldcPlugin.applyMorphology(mask);

        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.cleanup(new ImagePlus[]{expectedMask, mask}, ldcPlugin);
	}
	
	//Dilation -> Opening -> Closing
	@Test
	public void test6() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_binary_operations/test6.tif");
		ImagePlus mask = importImage("/TestMask.tif");
		
		// Apply Dilation
		ldcPlugin.setMorphologicalOperation("Dilate");
        ldcPlugin.applyMorphology(mask);
        
        // Apply Opening
		ldcPlugin.setMorphologicalOperation("Open");
        ldcPlugin.applyMorphology(mask);
        
        // Apply Closing
		ldcPlugin.setMorphologicalOperation("Close");
        ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
        
        Utils.cleanup(new ImagePlus[]{expectedMask, mask}, ldcPlugin);
	}
}
