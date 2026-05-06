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

	    private Object[] cleanStackPixels = null; // one pixel array per slice

	    /**
	     * Previews a morphological operation across the ENTIRE stack.
	     * Always restores from the clean backup first to prevent compounding.
	     */
	    public void previewMorphology(ImagePlus imp, String morphologicalOperation, boolean applyWatershed) {
	        if (imp == null) return;

	        // First time: capture the whole stack as our clean baseline
	        if (cleanStackPixels == null) {
	            captureSnapshot(imp);
	        }

	        ImageStack stack = imp.getStack();
	        int nSlices = stack.getSize();

	        for (int i = 1; i <= nSlices; i++) {
	            ImageProcessor ip = stack.getProcessor(i);

	            // Restore this slice to its clean state before applying
	            ip.setSnapshotPixels(cleanStackPixels[i - 1]);
	            ip.reset();

	            // Apply the operation (skip if None)
	            if (morphologicalOperation != null && !morphologicalOperation.equals("None")) {
	                applyOperationsToProcessor(ip, morphologicalOperation);
	            }
	            
	            if (applyWatershed) {
	                applyWatershedToProcessor(ip);
	            }
	        }

	        imp.updateAndDraw();
	    }

	    /**
	     * Resets ALL slices back to the clean binary state.
	     */
	    public boolean resetPreview(ImagePlus imp) {
	        if (imp == null || cleanStackPixels == null) return false;

	        ImageStack stack = imp.getStack();
	        int nSlices = stack.getSize();

	        for (int i = 1; i <= nSlices; i++) {
	            ImageProcessor ip = stack.getProcessor(i);
	            ip.setSnapshotPixels(cleanStackPixels[i - 1]);
	            ip.reset();
	        }

	        imp.updateAndDraw();
	        return true;
	    }

	    /**
	     * Captures a deep copy of every slice in the stack as the clean baseline.
	     */
	    public void captureSnapshot(ImagePlus imp) {
	        if (imp == null) return;

	        ImageStack stack = imp.getStack();
	        int nSlices = stack.getSize();
	        cleanStackPixels = new Object[nSlices];

	        for (int i = 1; i <= nSlices; i++) {
	            cleanStackPixels[i - 1] = stack.getProcessor(i).getPixelsCopy();
	        }
	    }

	    
	    /**
	     * Permanently applies the selected operations to the entire image stack.
	     */
	    public boolean applyMorphology(ImagePlus imp, String morphologicalOperations, boolean applyWatershed) {
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
	                
	                if (applyWatershed) {
	                    applyWatershedToProcessor(ip);
	                }
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
	    }
	    
	    private void applyWatershedToProcessor(ImageProcessor ip) {
	        EDM edm = new EDM();
	        edm.toWatershed(ip);
	    }
	    
	}