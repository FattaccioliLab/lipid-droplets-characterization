package fr.sorbonne_universite.ldc.tests.unit;

import static org.junit.jupiter.api.Assertions.*;

import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Utility class for unit tests.
 */
public class Utils {

	/**
	 * Checks if 2 given images have the same dimensions.
	 * @param expected 		The reference image.
	 * @param actual 		The image that needs to have the same dimensions as {@code expected}.
	 */
	public static void checkSameDimensions(ImagePlus expected, ImagePlus actual) {
        assertEquals(expected.getWidth(), actual.getWidth(), "Different width");
        assertEquals(expected.getHeight(), actual.getHeight(), "Different height");
        assertEquals(expected.getStackSize(), actual.getStackSize(), "Different number of slices");
	}
	
	/**
	 * Checks if 2 given images have the same pixels across all slices.
	 * @param expected 		The reference image.
	 * @param actual 		The image that needs to have the same pixels as {@code expected}.
	 */
	public static void checkSamePixels(ImagePlus expected, ImagePlus actual) {
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
	public static void checkSameDisplayRange(ImagePlus expected, ImagePlus actual) {
	    ImageProcessor ip1 = expected.getProcessor();
	    ImageProcessor ip2 = actual.getProcessor();
	    
	    assertEquals(ip1.getMin(), ip2.getMin(), "Different display min");
	    assertEquals(ip1.getMax(), ip2.getMax(), "Different display max");
	}
	
}
