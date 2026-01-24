package model.leftpanel;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import mainGUI.panels.subpanels.leftpanel.PreprocessingPanel;

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
	
}
