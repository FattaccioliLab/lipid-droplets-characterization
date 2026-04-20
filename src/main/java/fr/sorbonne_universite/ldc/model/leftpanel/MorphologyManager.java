package fr.sorbonne_universite.ldc.model.leftpanel;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.EDM;
import ij.process.ImageProcessor;

/**
 * Provides binary morphological operations (Erode, Dilate, Open, Close, Watershed).
 * Used by the MorphologyPanel.
 */
public class MorphologyManager {

    private Object cleanPixels = null;
    private int cleanSlice = -1;
	
    /**
     * Applies morphological operations to a single slice for preview purposes.
     */
 
    
    /**
     * Applies morphological operations to a single slice for preview purposes.
     * Restores the image from locally stored cleanPixels before applying, so operations don't compound endlessly.
     */
    public void previewMorphology(ImagePlus imp, String morphologicalOperation){
        if (imp == null) return;
        
        ImageProcessor ip = imp.getProcessor();
        
        // 1. Revert to the clean, unmodified binary state using our private backup
        if (cleanPixels != null && imp.getCurrentSlice() == cleanSlice) {
            ip.setSnapshotPixels(cleanPixels);
            ip.reset();
        } else {
            // If they changed slices and we don't have a backup, take one now
            captureSnapshot(imp);
        }

        applyOperationsToProcessor(ip, morphologicalOperation);
        
        imp.updateAndDraw();
    }

    
    /**
     * Resets the preview, returning the image to its original binary state.
     */
    public boolean resetPreview(ImagePlus imp) {
        if (imp == null) return false;
        
        ImageProcessor ip = imp.getProcessor();
        
        // Restore from our private backup
        if (cleanPixels != null && imp.getCurrentSlice() == cleanSlice) {
            ip.setSnapshotPixels(cleanPixels);
            ip.reset();
            imp.updateAndDraw();
        }
        return true;
    }

    
    /**
     * Takes a private snapshot of the current slice to allow non-destructive previews.
     * Stored in a custom variable to prevent ImageJ's global Undo from overwriting it.
     */
    public void captureSnapshot(ImagePlus imp) {
        if (imp != null) {
            ImageProcessor ip = imp.getProcessor();
            // getPixelsCopy() creates a deep copy of the raw image data
            cleanPixels = ip.getPixelsCopy(); 
            cleanSlice = imp.getCurrentSlice();
        }
    }
    

    /**
     * Permanently applies the selected operations to the entire image stack.
     */
    public boolean applyMorphology(ImagePlus imp, String morphologicalOperations) {
        if (imp == null) return false;
        
        try {
            ImageStack stack = imp.getStack();
            int nSlices = stack.getSize();
            
            // Process every slice in the stack
            for (int i = 1; i <= nSlices; i++) {
                ImageProcessor ip = stack.getProcessor(i);
                
                // Ensure we don't have lingering preview artifacts
                ip.reset(); 
                
                applyOperationsToProcessor(ip, morphologicalOperations);
            }
            
            // Update the ImagePlus to reflect the modified stack
            imp.setStack(stack);
            imp.updateAndDraw();
            
            // Capture a new snapshot in case the user wants to preview the NEXT step (Analysis)
            captureSnapshot(imp); 
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to execute the math on a specific ImageProcessor.
     * 
	 * Why we use raw filters (MIN/MAX) instead of ip.erode() and ip.dilate():
	 * * ImageJ's native methods dynamically change their mathematical behavior 
	 * based on the image's Look-Up Table (LUT) state. For example:
	 * public void erode() { return isInvertedLut() ? filter(MIN) : filter(MAX); }
	 *
	 * This causes unpredictable inversions depending on how the binary mask was 
	 * generated or current GUI settings. To guarantee mathematical stability 
	 * (always shrinking/expanding white pixels), we bypass the LUT checks and 
	 * apply the raw deterministic filters directly.
	 
     */
    private void applyOperationsToProcessor(ImageProcessor ip, String morphologicalOperations){
        
    	//A MIN filter replaces pixels with the lowest nearby value (0). White (255) gets eaten by Black (0). This is true Erosion for white objects.
    	//A MAX filter replaces pixels with the highest nearby value (255). White (255) spreads over Black (0). This is true Dilation for white objects.
    	
    	boolean erode = morphologicalOperations.equals("Erode");
    	boolean dilate =  morphologicalOperations.equals("Dilate");
    	boolean open =  morphologicalOperations.equals("Open");
    	boolean close =  morphologicalOperations.equals("Close");
    	
    	//boolean watershed;
    	
    	if (erode) {
    		ip.filter(ImageProcessor.MIN);	// Shrinks 255 (white).
        }else if (dilate) {
        	ip.filter(ImageProcessor.MAX);	// Expands 255 (white).
        }else if (open) { 
        	ip.filter(ImageProcessor.MIN); // Erode first
            ip.filter(ImageProcessor.MAX); // Then Dilate
        }else if (close) { 
        	ip.filter(ImageProcessor.MAX); // Dilate first
            ip.filter(ImageProcessor.MIN); // Then Erode
        }
        
        //if (watershed) {
        //    EDM edm = new EDM();
        //    edm.toWatershed(ip);
        //}
    	
    }
}