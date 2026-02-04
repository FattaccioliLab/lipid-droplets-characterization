package model.leftpanel;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Provides segmentation/thresholding operations. 
 * Used by the {@link mainGUI.panels.subpanels.leftpanel.ThresholdingPanel}.
 */
public class ThresholdingManager {

    /**
     * Applies a manual threshold (preview) to the image.
     * Sets the threshold range (red overlay) without converting to binary yet.
     */
    public void setManualThreshold(ImagePlus imp, double min, double max) {
        if (imp == null) return;
        ImageProcessor ip = imp.getProcessor();
        // Set red overlay for visualization
        ip.setThreshold(min, max, ImageProcessor.RED_LUT);
        
        imp.updateAndDraw();
    }

    /**
     * Calculates and applies an automatic threshold (Otsu, Moments, etc.).
     * @param method The name of the method (e.g., "Otsu", "Moments").
     * @param darkBackground True if objects are light on dark background.
     * @return An array {min, max} representing the calculated threshold values.
     */
    public double[] setAutoThreshold(ImagePlus imp, String method, boolean darkBackground) {
        if (imp == null) return new double[]{0, 0};
        
        ImageProcessor ip = imp.getProcessor();
        
        // Auto-thresholding logic
        // "Red" implies we just want to see the overlay, not apply binary conversion yet
        if (darkBackground) {
            ip.setAutoThreshold(method, true, ImageProcessor.RED_LUT);
        } else {
            // If light background, ImageJ usually inverts logic or we handle it via LUT
            ip.setAutoThreshold(method, false, ImageProcessor.RED_LUT);
        }
        
        imp.updateAndDraw();
        
        // Return the computed values so the UI sliders can update
        return new double[]{ip.getMinThreshold(), ip.getMaxThreshold()};
    }

    /**
     * Resets/Removes the threshold overlay.
     */
    public void resetThreshold(ImagePlus imp) {
        if (imp == null) return;
        imp.getProcessor().resetThreshold();
        imp.updateAndDraw();
    }

    /**
     * permanently applies the threshold, converting the image to a Binary Mask (8-bit).
     * if the opertaion finished successfully return true, otherwise false
     */
    public boolean applyThreshold(ImagePlus imp) {
        if (imp == null) return false;
        
        // Lock the image to prevent concurrent modifications
        if (imp.isLocked()) return false;
        
        try {        	
            // "Convert to Mask" is the standard ImageJ command to finalize thresholding
            // It handles the "Dark Background" preference set globally or via AutoThreshold
            IJ.run(imp, "Convert to Mask", "");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}