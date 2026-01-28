package model.leftpanel;

import javax.swing.SwingWorker;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageProcessor;
import mainGUI.panels.subpanels.leftpanel.PreprocessingPanel;
import model.workers.preprocessing.PreprocessingPreviewMedianWorker;
import model.workers.preprocessing.PreprocessingApplyMedianWorker;

/**
 * Provides preprocessing management operations. Indirectly used by the LDC {@link PreprocessingPanel} UI class.
 */
public class PreprocessingManager {

	ContrastEnhancer ce = new ContrastEnhancer();
	
	/**
	 * Applies contrast enhancement to the current active image using ImageJ's ContrastEnhancer.
	 * @param currentImg The currently active image whose contrast will be enhanced.
	 * @param enhanceContrastEnabled Indicates if the 'Enhance contrast' option is enabled.
	 * @param saturated The saturation percentage for contrast enhancement.
	 * @throws IllegalArgumentException if {@code currentImg} is {@code null}.
	 */
	public void applyEnhanceContrast(ImagePlus currentImg, boolean enhanceContrastEnabled, double saturated) {
    	if (currentImg == null) {
            throw new IllegalArgumentException("No current image.");
        }
    	
    	if (enhanceContrastEnabled) {
	    	ce.stretchHistogram(currentImg, saturated);
    	} else {
    		ce.stretchHistogram(currentImg, 0);
    	}
    	currentImg.updateAndDraw();
	}
	
    /**
     * Creates a {@link SwingWorker}, that can apply a median filter preview on a given {@link ImageProcessor} if executed.
     * @param ip The image processor to modify.
     * @param isPreviewOn Whether preview mode is enabled. If it is false it will reset the given image processor.
     * @param radius The radius of the median filter.
     * @see PreprocessingPreviewMedianWorker
     */
	public SwingWorker<Void,Void> createPreviewMedianWorker(ImageProcessor ip, boolean isPreviewOn, double radius){
		return new PreprocessingPreviewMedianWorker(ip, isPreviewOn, radius); 
	}
	
    /**
     * Creates a {@link SwingWorker}, that can apply a median filter on a given {@link ImageStack}, or on an individual {@link ImageProcessor} among the given stack if executed.
	 * @param medianFilterEnabled Indicates if the median filter is enabled.
	 * @param radius The median filter radius (px).
	 * @param stack The array of images to process (slices).
	 * @param processAll True to process all slices.
	 * @param targetSlice The specific slice to process if not all.
	 * @see PreprocessingApplyMedianWorker
     */
	public SwingWorker<Void,Void> createApplyMedianWorker(boolean medianFilterEnabled, double radius, ImageStack stack, boolean processAll, int targetSlice){
		return new PreprocessingApplyMedianWorker(medianFilterEnabled, radius, stack, processAll, targetSlice); 
	}
}
