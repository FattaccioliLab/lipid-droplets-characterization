package mainGUI.utils;

import java.util.HashSet;
import java.util.Set;

import ij.ImageStack;

/**
 * Utility class for managing inputs.
 */
public class InputUtils {
	
    /**
     * Parses a slice range string (e.g. "1-3,5,8-10") and returns a set of valid 1-based slice indices.<br>
     * Out-of-bounds values are ignored and duplicates are removed.
     *
     * @param input Slice range string.
     * @param maxSlices Maximum number of slices available.
     * @return Set of valid slice indices, or an empty set if input is null/empty.
     */
    public static Set<Integer> parseSliceRangeToSet(String input, int maxSlices) {

        Set<Integer> slices = new HashSet<>();

        if (input == null || input.trim().isEmpty()) {
            return slices;
        }

        String[] parts = input.split(",");

        for (String part : parts) {
            part = part.trim();

            // Single index
            if (part.matches("\\d+")) {
                int slice = Integer.parseInt(part);
                if (slice >= 1 && slice <= maxSlices) {
                    slices.add(slice);
                }

            // Range
            } else if (part.matches("\\d+-\\d+")) {
                String[] bounds = part.split("-");
                int start = Integer.parseInt(bounds[0]);
                int end = Integer.parseInt(bounds[1]);

                if (start > end) {
                    int tmp = start;
                    start = end;
                    end = tmp;
                }

                for (int slice = start; slice <= end; slice++) {
                    if (slice >= 1 && slice <= maxSlices) {
                        slices.add(slice);
                    }
                }
            }
        }

        return slices;
    }

    /**
     * Builds a new {@link ImageStack} containing the specified 1-based slice indices.<br>
     * Invalid indices are ignored.
     *
     * @param slices Set of slice indices.
     * @param stack Source {@link ImageStack}.
     * @return New {@link ImageStack} containing selected slices.
     */
    public static ImageStack buildStackFromSlices(Set<Integer> slices, ImageStack stack) {

        ImageStack newStack = new ImageStack();

        if (slices == null || slices.isEmpty()) {
            return newStack;
        }

        int maxSlices = stack.getSize();

        for (Integer slice : slices) {
            if (slice != null && slice >= 1 && slice <= maxSlices) {
                newStack.addSlice(stack.getProcessor(slice));
            }
        }

        return newStack;
    }
}
