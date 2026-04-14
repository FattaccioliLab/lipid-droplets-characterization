package fr.sorbonne_universite.ldc.tests.unit;

import static org.junit.jupiter.api.Assertions.*;

import fr.sorbonne_universite.ldc.model.LDCService;
import ij.ImagePlus;
import ij.measure.ResultsTable;
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
	    for (int i = 1; i <= expected.getStackSize(); i++) {
	        expected.setSlice(i);
	        actual.setSlice(i);

	        ImageProcessor ip1 = expected.getProcessor();
	        ImageProcessor ip2 = actual.getProcessor();

	        assertEquals(ip1.getMin(), ip2.getMin(), "Different display min at slice " + i);
	        assertEquals(ip1.getMax(), ip2.getMax(), "Different display max at slice " + i);
	    }
	}
	
	/**
	 * Checks that {@code actual} contains at least {@code expected} numerical datas.
	 * @param expected     The reference table.
	 * @param actual       The table to check.
	 * @param tolerance    Tolerance for double values comparison.
	 */
	public static void checkSameResultsTable(ResultsTable expected, ResultsTable actual, double tolerance) {
	    assertEquals(expected.size(), actual.size(), "Different number of rows");
	    
	    String[] expectedColumns = expected.getHeadings();
	    String[] actualColumns = actual.getHeadings();
	    
	    String areaColumn = null; // area column name in our plugin, with the scale used in parenthesis
	    for (String expectedCol : expectedColumns) {
	    	// dont't count AR, Round, Solidity columns for now
	    	if (!expectedCol.equals("AR") && !expectedCol.equals("Round") && !expectedCol.equals("Solidity")) {
		    	boolean expectedIsHere = false;
		    	for (String actualCol : actualColumns) {
		    		if (actualCol.contains(expectedCol)) {
			    		if (expectedCol.equals("Area")) {
			    			areaColumn = actualCol;
			    		}
		    			expectedIsHere = true;
		    			break;
		    		}
		    	}
		    	assertTrue(expectedIsHere, "Missing column: " + expectedCol);
	    	}
	    }
	    
	    for (int row = 0; row < expected.size(); row++) {
	        for (String column : expectedColumns) {
	        	// dont't count AR, Round, Solidity columns for now
		    	if (!column.equals("AR") && !column.equals("Round") && !column.equals("Solidity")) {
		            double expectedValue = expected.getValue(column, row);
		            double actualValue;
		            if (column.equals("Area")) {
		            	actualValue = actual.getValue(areaColumn, row);
		            } else {
		            	actualValue = actual.getValue(column, row);
		            }
		            assertEquals(expectedValue, actualValue, tolerance, 
		            		String.format("Row %d, column '%s': expected %.3f but was %.3f", row, column, expectedValue, actualValue));
		    	}
	            
	        }
	    }
	}
	
	/**
	 * Cleans resources after a test.
	 * @param images 	List of images to close.
	 * @param ldc 		The Lipid Droplets Characterization service.
	 */
	public static void cleanup(ImagePlus[] images, LDCService ldc) {
		for (ImagePlus image : images) {
			if (image != null) image.close();
		}
		if (ldc != null) {
			ldc.dispose();
			ldc = null;
		}
	}
	
}
