package fr.sorbonne_universite.ldc.model.leftpanel;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.ImageSourcePanel;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;

/**
 * Provides image management operations. Indirectly used by the LDC {@link ImageSourcePanel} UI class.
 */
public class ImageSourceManager {
	
	/**
	 * Opens a file chooser and replaces the content of the given {@link ImagePlus} with an image loaded from disk.
	 * <p>
	 * The replacement is done in place: the image stack, calibration and dimensions
	 * of {@code currentImg} are updated to match the newly opened image.
	 * </p>
	 * <p>
	 * If the file chooser is cancelled or the selected file cannot be opened, the method returns without modifying the current image.
	 * </p>
	 *
	 * @param currentImg The currently active image whose content will be replaced.
	 * @param parent The parent {@link Component} for the file chooser dialog, or {@code null} if none.
	 * @throws IllegalArgumentException if {@code currentImg} is {@code null}.
	 */
    public void replaceCurrentImage(ImagePlus currentImg, Component parent) {
    	if (currentImg == null) {
            throw new IllegalArgumentException("No current image.");
        }
    	
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File imageFile = fileChooser.getSelectedFile();
        ImagePlus newImage = IJ.openImage(imageFile.getAbsolutePath());
        if (newImage == null) return;
        
        // update the file informations
        FileInfo newFi = newImage.getOriginalFileInfo();
        currentImg.setFileInfo(newFi);

        currentImg.setStack(newImage.getTitle(), newImage.getStack());
        currentImg.setCalibration(newImage.getCalibration());
        currentImg.setDimensions(newImage.getNChannels(), newImage.getNSlices(), newImage.getNFrames());
        currentImg.updateAndDraw();
    }
    
    /**
     * Reset the image currently opened, replace the current image with the one without any modification.
     * 
     * @param currentImg The currently active image whose content will be replaced.
	 * @throws IllegalArgumentException if {@code currentImg} is {@code null}.
	 * @throws IllegalStateException if the image has no source file or cannot be reloaded.
     */
    public void resetCurrentImage(ImagePlus currentImg) {
		if (currentImg == null) {
			throw new IllegalArgumentException("No current image.");
		}
		
		FileInfo fi = currentImg.getOriginalFileInfo();

		if (fi == null || fi.directory == null || fi.fileName == null) {
			throw new IllegalStateException("The current image has no source file on disk and cannot be reset."); 
		}
		
		String fullPath = fi.directory + fi.fileName;
		
		ImagePlus initialImg = IJ.openImage(fullPath);
		
		if (initialImg == null) {
	        throw new IllegalStateException("Could not reload file from: " + fullPath);
	    }
		
    	currentImg.setStack(initialImg.getTitle(), initialImg.getStack());
        currentImg.setCalibration(initialImg.getCalibration());
        currentImg.setDimensions(initialImg.getNChannels(), initialImg.getNSlices(), initialImg.getNFrames());
        currentImg.updateAndDraw();
    }
    
}
