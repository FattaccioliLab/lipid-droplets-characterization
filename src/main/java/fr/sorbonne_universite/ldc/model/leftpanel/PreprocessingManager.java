package fr.sorbonne_universite.ldc.model.leftpanel;

import javax.swing.SwingWorker;

import fr.sorbonne_universite.ldc.model.workers.PreprocessingApplyMedianWorker;
import fr.sorbonne_universite.ldc.model.workers.PreprocessingPreviewMedianWorker;
import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.PreprocessingPanel;
import ij.ImageStack;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageProcessor;

/**
 * Provides preprocessing management operations. Indirectly used by the LDC {@link PreprocessingPanel} UI class.
 */
public class PreprocessingManager {

	ContrastEnhancer ce = new ContrastEnhancer();

	/**
	 * Applies contrast enhancement on the given {@link ImageProcessor} using ImageJ's ContrastEnhancer.
	 * @param ip The image processor to modify.
	 * @param enhanceContrastEnabled Indicates if the 'Enhance contrast' option is enabled.
	 * @param saturated The saturation percentage for contrast enhancement.
	 * @throws IllegalArgumentException if {@code ip} == {@code null}.
	 */
	public void applyEnhanceContrast(ImageProcessor ip, boolean enhanceContrastEnabled, double saturated) {
    	if (ip == null) throw new IllegalArgumentException("No image processor given.");
    	
    	if (enhanceContrastEnabled) ce.stretchHistogram(ip, saturated);
	}
	
    /**
     * Creates a {@link SwingWorker}, that can apply a median filter preview on a given {@link ImageProcessor} if executed.
     * @param ip The image processor to modify.
     * @param radius The radius of the median filter.
     * @throws IllegalArgumentException if {@code ip} == {@code null}.
     * @see PreprocessingPreviewMedianWorker
     */
	public SwingWorker<Void,Void> createPreviewMedianWorker(ImageProcessor ip, double radius){
		if (ip == null) throw new IllegalArgumentException("No image processor given.");
		
		return new PreprocessingPreviewMedianWorker(ip, radius); 
	}
	
    /**
     * Creates a {@link SwingWorker}, that applies a median filter on a given {@link ImageStack} if executed.
	 * @param medianFilterEnabled Indicates if the median filter is enabled.
	 * @param radius The median filter radius (px).
	 * @param stack The array of images to process (slices).
	 * @throws IllegalArgumentException if {@code stack} == {@code null}.
	 * @see PreprocessingApplyMedianWorker
     */
	public SwingWorker<Void,Void> createApplyMedianWorker(boolean medianFilterEnabled, double radius, ImageStack stack){
		if (stack == null) throw new IllegalArgumentException("No stack given.");
		
		return new PreprocessingApplyMedianWorker(medianFilterEnabled, radius, stack); 
	}
}
