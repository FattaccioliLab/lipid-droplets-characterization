package fr.sorbonne_universite.ldc.model.leftpanel;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.EDM;
import ij.process.ImageProcessor;

/**
 * Provides binary morphological operations (Erosion, Dilation, Opening, Closing, Watershed).
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
    public void previewMorphology(ImagePlus imp, boolean erode, boolean dilate, boolean open, boolean close, boolean watershed) {
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
        
        //we need to inverse this because by default these operations think the objects are black and background is white
        //however, in our plugin we we get an binary mask where objects are white and background is black
        //that's why we invert the operatations : erode -> dilate; dilate -> erode; open -> close; close -> open
        applyOperationsToProcessor(ip, dilate, erode, close, open, watershed);
        
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
    public boolean applyMorphology(ImagePlus imp, boolean erode, boolean dilate, boolean open, boolean close, boolean watershed) {
        if (imp == null) return false;
        
        try {
            ImageStack stack = imp.getStack();
            int nSlices = stack.getSize();
            
            // Process every slice in the stack
            for (int i = 1; i <= nSlices; i++) {
                ImageProcessor ip = stack.getProcessor(i);
                
                // Ensure we don't have lingering preview artifacts
                ip.reset(); 
                
                //we need to inverse this because by default these operations think the objects are black and background is white
                //however, in our plugin we we get an binary mask where objects are white and background is black
                //that's why we invert the operatations
                // erode -> dilate; dilate -> erode; open -> close; close -> open
                applyOperationsToProcessor(ip, dilate, erode, close, open, watershed);
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
     */
    private void applyOperationsToProcessor(ImageProcessor ip, boolean erode, boolean dilate, boolean open, boolean close, boolean watershed) {
        if (erode) ip.erode();
        
        if (dilate) ip.dilate();
        
        if (open) { 
            ip.erode(); 
            ip.dilate(); 
        }
        
        if (close) { 
            ip.dilate(); 
            ip.erode(); 
        }
        
        if (watershed) {
            EDM edm = new EDM();
            edm.toWatershed(ip);
        }
    }
}