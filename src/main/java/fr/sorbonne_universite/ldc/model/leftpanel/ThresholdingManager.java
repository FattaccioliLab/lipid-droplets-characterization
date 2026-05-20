package fr.sorbonne_universite.ldc.model.leftpanel;

import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.ThresholdingPanel;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * Provides segmentation and thresholding operations on {@link ImagePlus} objects.
 *
 * <p>
 * Supports both manual threshold preview and automatic threshold computation
 * using ImageJ's native {@code setAutoThreshold} method with stack histogram support.
 * The final {@link #applyThreshold(ImagePlus)} method generates a new binary mask
 * without modifying the original image.
 * </p>
 *
 * @see fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.ThresholdingPanel
 */
public class ThresholdingManager {

	public ThresholdingManager() {}
	
	/**
	 * Applies a manual threshold preview to the current image by setting the
	 * threshold range with a red LUT overlay.
	 *
	 * <p>This is a visual-only operation and does not generate a binary mask.</p>
	 *
	 * @param imp The image on which the preview must be applied. Does nothing if {@code null}.
	 * @param min The minimum pixel value of the threshold range.
	 * @param max The maximum pixel value of the threshold range.
	 */
    public void setManualThreshold(ImagePlus imp, double min, double max) {
        if (imp == null) return;
        ImageProcessor ip = imp.getProcessor();
        ip.setThreshold(min, max, ImageProcessor.RED_LUT);
        imp.updateAndDraw();
    }
    
    
    
    /**
     * Calculates and applies an automatic threshold (Otsu, Moments, etc.) to the image
     * using the stack histogram (all slices combined) to compute the threshold.
     *
     * <p>
     * Delegates to ImageJ's native {@code ImagePlus.setAutoThreshold()} with the
     * {@code stack} option, ensuring the threshold is computed globally across
     * the entire volume rather than on a single slice.
     * The display range (contrast) is not reset.
     * </p>
     *
     * @param imp             The image on which the threshold must be computed and previewed.
     *                        Returns {@code {0, 0}} if {@code null}.
     * @param method          The name of the auto-threshold algorithm (e.g. {@code "Otsu"},
     *                        {@code "Moments"}, {@code "Triangle"}).
     * @param darkBackground  {@code true} if the image has a dark background (bright objects),
     *                        {@code false} otherwise.
     * @return                An array {@code {min, max}} containing the computed threshold bounds.
     */
    public double[] setAutoThreshold(ImagePlus imp, String method, boolean darkBackground) {
        if (imp == null) return new double[]{0, 0};
        
        ImageProcessor ip = imp.getProcessor();

		boolean stack = true;
		String methodAndOptions = method+(darkBackground?" dark":"")+(stack?" stack":"");
		imp.setAutoThreshold(methodAndOptions);
		
		double computedMin = ip.getMinThreshold();
		double computedMax = ip.getMaxThreshold();
		
        if (computedMin != ImageProcessor.NO_THRESHOLD) {
            ip.setThreshold(computedMin, computedMax, ImageProcessor.RED_LUT);
        }
        
		imp.updateAndDraw();

        return new double[]{computedMin, computedMax};
    }

    
    /**
     * Resets the threshold on the given image, removing any threshold overlay.
     *
     * @param imp The image whose threshold must be reset. Does nothing and returns
     *            {@code false} if {@code null}.
     * @return    {@code true} if the threshold was successfully reset, {@code false} otherwise.
     */
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
                ImageProcessor ip = originalStack.getProcessor(i).duplicate();;
                
                // Temporarily apply threshold to the slice mathematically
                ip.setThreshold(min, max, ImageProcessor.NO_LUT_UPDATE);
                
                // Create an 8-bit mask (ByteProcessor) from the thresholded slice
                ImageProcessor maskIp = ip.createMask();
                
                // Add the 8-bit mask to our new stack
                binaryStack.addSlice(originalStack.getSliceLabel(i), maskIp);
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
            
            // Remove the RED preview overlay on the original image
            originalImp.getProcessor().setThreshold(min, max, ImageProcessor.NO_LUT_UPDATE);
            originalImp.updateAndDraw();
            
            return binaryImp;
            
        } catch (Exception e) {
            IJ.log("Error generating binary mask: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}