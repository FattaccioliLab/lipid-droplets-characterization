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
        if(service.enhanceContrastEnabled()) service.applyEnhanceContrast(ip);
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
        if (darkBackground) {
            ip.setAutoThreshold(method, true, ImageProcessor.NO_LUT_UPDATE);
        } else {
            ip.setAutoThreshold(method, false, ImageProcessor.NO_LUT_UPDATE);
        }

        // 2. CAPTURE the calculated values before they get destroyed!
        double computedMin = ip.getMinThreshold();
        double computedMax = ip.getMaxThreshold();

        // 3. Apply your contrast enhancement 
        // (This step internally resets the threshold, making the values -808080.0)
        if (service.enhanceContrastEnabled()) {
            // Use whatever your exact method signature is here
            service.applyEnhanceContrast(ip); 
        }

        // 4. RE-APPLY the captured threshold with the RED overlay
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
     * @param originalImp The source image (will not be modified).
     * @param calculateAllSlices If true, recalculates the auto-threshold for each slice.
     * @return true if successful, false otherwise.
     */
    public ImagePlus applyThreshold(ImagePlus originalImp) {
        if (originalImp == null) return null;
        
        try {
            // Get global threshold from the current active slice as a baseline
            double globalMin = originalImp.getProcessor().getMinThreshold();
            double globalMax = originalImp.getProcessor().getMaxThreshold();
            
            //get the user agreement on calculating threshold for each slice independently or not
            boolean calculateAllSlices = service.getIndependentThreshold();
            
            if (globalMin == ImageProcessor.NO_THRESHOLD && !calculateAllSlices) {
                IJ.log("No threshold set.");
                return null; 
            }

            // Get settings for per-slice calculation
            String method = service.getThresholdMethod();
            boolean darkBackground = service.thresholdDarkBackgroundEnabled();
            boolean isManual = "Manual".equals(method);

            ImageStack originalStack = originalImp.getStack();
            int width = originalStack.getWidth();
            int height = originalStack.getHeight();
            int nSlices = originalStack.getSize();
            
            // Create a NEW empty stack for the 8-bit binary mask
            ImageStack binaryStack = new ImageStack(width, height);
            
            // Process each slice
            for (int i = 1; i <= nSlices; i++) {
                ImageProcessor ip = originalStack.getProcessor(i);
                
                double sliceMin = globalMin;
                double sliceMax = globalMax;

                // RECALCULATE for this specific slice if requested (and not manual)
                if (calculateAllSlices && !isManual) {
                    
                    // Critical: Apply contrast enhancement first if enabled, 
                    // because the auto-threshold math depends on it!
                    if (service.enhanceContrastEnabled()) {
                        service.applyEnhanceContrast(ip);
                    }
                    
                    // Calculate the new limits for this specific slice
                    ip.setAutoThreshold(method, darkBackground, ImageProcessor.NO_LUT_UPDATE);
                    sliceMin = ip.getMinThreshold();
                    sliceMax = ip.getMaxThreshold();
                }
                
                // Temporarily apply threshold to the slice mathematically
                ip.setThreshold(sliceMin, sliceMax, ImageProcessor.NO_LUT_UPDATE);
                
                // Create an 8-bit mask (ByteProcessor) from the thresholded slice
                ImageProcessor maskIp = ip.createMask();
                
                // Add the 8-bit mask to our new stack
                binaryStack.addSlice(originalStack.getSliceLabel(i), maskIp);
                
                // Reset threshold on original slice to avoid messing up the original image
                ip.resetThreshold();
            }
            
            // Create a new ImagePlus with the 8-bit stack
            ImagePlus binaryImp = new ImagePlus(originalImp.getShortTitle() + "_Binary", binaryStack);
            
            // Copy calibration (pixel size, mm/px, etc.)
            binaryImp.setCalibration(originalImp.getCalibration());
            
            // Show the new binary image
            binaryImp.show();
            
            // Restore the red preview overlay on the original image's current slice
            originalImp.getProcessor().setThreshold(globalMin, globalMax, ImageProcessor.NO_LUT_UPDATE);
            originalImp.updateAndDraw();
            
            return binaryImp;
            
        } catch (Exception e) {
            IJ.log("Error generating binary mask: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}