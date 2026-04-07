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
    // EROSION
    // =========================================================================
	
	@Test
	public void test1() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_binary_operations/test1.tif"); // works with test2.tif (dilatation)
		ImagePlus mask = importImage("/TestMask.tif");
		
		ldcPlugin.setErosion(true);
		ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
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
		
		ldcPlugin.setDilation(true);
		ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
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
		
		ldcPlugin.setOpening(true);
		ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
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
		
		ldcPlugin.setClosing(true);
		ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
    // =========================================================================
    // MIXED OPERATIONS
    // =========================================================================
	
	@Test
	public void test5() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_binary_operations/test5.tif");
		ImagePlus mask = importImage("/TestMask.tif");
		
		ldcPlugin.setErosion(true);
		ldcPlugin.setOpening(true);
		ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
	
	@Test
	public void test6() {
		LDCService ldcPlugin = new LDCServiceImpl();
		ldcPlugin.initialize();
		
		ImagePlus expectedMask = importImage("/expected/test_binary_operations/test6.tif");
		ImagePlus mask = importImage("/TestMask.tif");
		
		ldcPlugin.setDilation(true);
		ldcPlugin.setOpening(true);
		ldcPlugin.setClosing(true);
		ldcPlugin.applyMorphology(mask);
        
        Utils.checkSameDimensions(expectedMask, mask);
        Utils.checkSameDisplayRange(expectedMask, mask);
        Utils.checkSamePixels(expectedMask, mask);
	}
}
