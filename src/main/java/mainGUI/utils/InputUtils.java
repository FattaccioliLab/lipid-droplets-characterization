package mainGUI.utils;

import java.util.HashSet;
import java.util.Set;

import ij.ImageStack;

/**
 * Utility class for managing inputs.
 */
public class InputUtils {
	
	/**
	 * Builds a new {@link ImageStack} containing slices specified by a range string (e.g. "1-3,5,8-10").<br> 
	 * Indices are 1-based; out-of-bounds values are ignored.<br> 
	 * If a slice is present more than once in the input, it will be added only once to the output {@link ImageStack}.
	 *
	 * @param input Slice range string.
	 * @param maxSlices Maximum number of slices in the source stack.
	 * @param stack Source {@link ImageStack}.
	 * @return New {@link ImageStack} with selected slices, or the original stack if input is empty/null.
	 */
	public static ImageStack parseSliceRanges(String input, int maxSlices, ImageStack stack) {
		
		ImageStack newStack = new ImageStack();
		Set<Integer> slices = new HashSet<>();

	    if (input == null || input.trim().isEmpty()) {
	        return stack;
	    }

	    // Each part is separated by a ','
	    String[] parts = input.split(",");

	    for (String part : parts) {
	        part = part.trim();

	        // Match case : just an integer
	        if (part.matches("\\d+")) {
	            int slice = Integer.parseInt(part);
	            if (slice >= 1 && slice <= maxSlices && !slices.contains(slice)) {
	            	newStack.addSlice(stack.getProcessor(slice));
	            	slices.add(slice);
	            }
	            
	        // Match case : a range
	        } else if (part.matches("\\d+-\\d+")) {
	            String[] bounds = part.split("-");
	            int start = Integer.parseInt(bounds[0]);
	            int end = Integer.parseInt(bounds[1]);

	            // Swap between start and end
	            if (start > end) {
	                int tmp = start;
	                start = end;
	                end = tmp;
	            }

	            for (int slice = start; slice <= end; slice++) {
		            if (slice >= 1 && slice <= maxSlices && !slices.contains(slice)) {
		            	newStack.addSlice(stack.getProcessor(slice));
		            	slices.add(slice);
		            }
	            }
	        }
	    }
	    return newStack;
	}
}
