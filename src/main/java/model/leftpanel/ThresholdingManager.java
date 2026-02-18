package model.leftpanel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * Provides segmentation/thresholding operations. 
 * Used by the {@link mainGUI.panels.subpanels.leftpanel.ThresholdingPanel}.
 */
public class ThresholdingManager {

    /**
     * Applies a manual threshold (preview) to the image.
     */
    public void setManualThreshold(ImagePlus imp, double min, double max) {
        if (imp == null) return;
        ImageProcessor ip = imp.getProcessor();
        ip.setThreshold(min, max, ImageProcessor.RED_LUT);
        imp.updateAndDraw();
    }

    /**
     * Calculates and applies an automatic threshold (Otsu, Moments, etc.).
     */
    public double[] setAutoThreshold(ImagePlus imp, String method, boolean darkBackground) {
        if (imp == null) return new double[]{0, 0};
        
        ImageProcessor ip = imp.getProcessor();
        
        if (darkBackground) {
            ip.setAutoThreshold(method, true, ImageProcessor.RED_LUT);
        } else {
            ip.setAutoThreshold(method, false, ImageProcessor.RED_LUT);
        }
        
        imp.updateAndDraw();
        return new double[]{ip.getMinThreshold(), ip.getMaxThreshold()};
    }

    public boolean resetThreshold(ImagePlus imp) {
        if (imp == null) return false;
        imp.getProcessor().resetThreshold();
        imp.updateAndDraw();
        return true;
    }

/**
     * Creates a NEW binary mask image based on the current threshold settings.
     * The original image is left unchanged.
     * @param originalImp The source image (will not be modified).
     * @return true if successful, false otherwise.
     */
    public boolean applyThreshold(ImagePlus originalImp) {
        if (originalImp == null) return false;
        
        try {
            // 1. Get threshold from the original image
            double min = originalImp.getProcessor().getMinThreshold();
            double max = originalImp.getProcessor().getMaxThreshold();
            
            if (min == ImageProcessor.NO_THRESHOLD) {
                IJ.log("No threshold set.");
                return false; 
            }

            ImageStack originalStack = originalImp.getStack();
            int width = originalStack.getWidth();
            int height = originalStack.getHeight();
            int nSlices = originalStack.getSize();
            
            // 2. Create a NEW empty stack for the 8-bit binary mask
            ImageStack binaryStack = new ImageStack(width, height);
            
            // 3. Process each slice
            for (int i = 1; i <= nSlices; i++) {
                ImageProcessor ip = originalStack.getProcessor(i);
                
                // Temporarily apply threshold to the slice mathematically
                ip.setThreshold(min, max, ImageProcessor.NO_LUT_UPDATE);
                
                // Create an 8-bit mask (ByteProcessor) from the thresholded slice
                ImageProcessor maskIp = ip.createMask();
                
                // Add the 8-bit mask to our new stack
                binaryStack.addSlice(originalStack.getSliceLabel(i), maskIp);
                
                // Reset threshold on original slice to avoid messing up the original image
                ip.resetThreshold();
            }
            
            // 4. Create a new ImagePlus with the 8-bit stack
            ImagePlus binaryImp = new ImagePlus(originalImp.getShortTitle() + "_Binary", binaryStack);
            
            // Copy calibration (pixel size, mm/px, etc.)
            binaryImp.setCalibration(originalImp.getCalibration());
            
            // 5. Show the new binary image
            binaryImp.show();
            
            // Restore the red preview overlay on the original image's current slice
            originalImp.getProcessor().setThreshold(min, max, ImageProcessor.RED_LUT);
            originalImp.updateAndDraw();
            
            return true;
            
        } catch (Exception e) {
            IJ.log("Error generating binary mask: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}