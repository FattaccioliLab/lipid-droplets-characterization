package fr.sorbonne_universite.ldc.model.leftpanel;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.ThresholdingPanel;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * Provides segmentation/thresholding operations. 
 * Used by the {@link ThresholdingPanel}.
 */
public class ThresholdingManager {

	private LDCService service;
	public ThresholdingManager(LDCService ldcService) {
		this.service = ldcService;
	}
	
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

        // 1. Let ImageJ calculate the Auto-Threshold values
        // We use NO_LUT_UPDATE because we don't care about the visuals yet
        ip.setAutoThreshold(method, darkBackground, ImageProcessor.NO_LUT_UPDATE);

        // 2. CAPTURE the calculated values before they get destroyed!
        double computedMin = ip.getMinThreshold();
        double computedMax = ip.getMaxThreshold();

        // 3. RE-APPLY the captured threshold with the RED overlay
        if (computedMin != ImageProcessor.NO_THRESHOLD) {
            ip.setThreshold(computedMin, computedMax, ImageProcessor.RED_LUT);
        }

        imp.updateAndDraw();
        
        // Return the captured values, NOT ip.getMinThreshold() (just to be safe)
        return new double[]{computedMin, computedMax};
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
     * @return the NEW binary mask image based on the current threshold settings, or {@code null} if an error occurred.
     */
    public ImagePlus applyThreshold(ImagePlus originalImp) {
        if (originalImp == null) return null;
        
        try {
            // 1. Get threshold from the original image
            double min = originalImp.getProcessor().getMinThreshold();
            double max = originalImp.getProcessor().getMaxThreshold();
            
            if (min == ImageProcessor.NO_THRESHOLD) {
                IJ.log("No threshold set.");
                return null; 
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
            binaryImp.setDimensions(
                    originalImp.getNChannels(),
                    originalImp.getNSlices(),
                    originalImp.getNFrames()
                );
            
            // Copy calibration (pixel size, mm/px, etc.)
            binaryImp.setCalibration(originalImp.getCalibration());
            
            // Restore the red preview overlay on the original image's current slice
            originalImp.getProcessor().setThreshold(min, max, ImageProcessor.RED_LUT);
            originalImp.updateAndDraw();
            
            return binaryImp;
            
        } catch (Exception e) {
            IJ.log("Error generating binary mask: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}