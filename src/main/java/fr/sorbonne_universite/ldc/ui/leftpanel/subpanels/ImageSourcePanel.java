package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;
import fr.sorbonne_universite.ldc.utils.PanelUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileInfo;
import ij.measure.Calibration;

/**
 * Creates the top panel of the {@link LeftPanel}, containing image status and the "Replace Image" button.
 */
@SuppressWarnings("serial")
public class ImageSourcePanel extends JPanel {
	
	// The parent panel
	private LeftPanel leftPanel;
	
	// Lipid Droplet Characterization service
    @Parameter
	private LDCService selectedSettings;
	
	private JLabel imageStatusLabel;
	
	private JLabel infosNbSlicesLabel;
	
	public ImageSourcePanel(Context ctx, LeftPanel leftPanel) {
		
		// Initialization of the panel layout
		super();
		PanelUtils.createVerticalPanel(this, "Image Source", 200);
		
		ctx.inject(this);
		this.leftPanel = leftPanel;
		
	    imageStatusLabel = new JLabel("<html><center>No image opened.<br>Please open one.</center></html>", SwingConstants.CENTER);
	    imageStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	    
	    // Current number of slices considered / original number of slices
	    
	    infosNbSlicesLabel = new JLabel("", SwingConstants.CENTER);
	    infosNbSlicesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

	    // Replace image button
	    
	    JButton replaceImageButton = new JButton("Replace image");
	    replaceImageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    replaceImageButton.addActionListener(e -> {
	    	if (!leftPanel.isProcessing()) {
	    		replaceButtonAction();
	    	}
	    });
	    
	    // Reset image button

	    JButton resetImageButton = new JButton("Reset image");
	    resetImageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    resetImageButton.addActionListener(e -> {
	    	if (!leftPanel.isProcessing()) {
	    		resetButtonAction();
	    	}
	    });

	    add(Box.createVerticalStrut(5));
	    add(imageStatusLabel);
	    add(Box.createVerticalStrut(5));
	    add(infosNbSlicesLabel);
	    add(Box.createVerticalStrut(10));
	    add(replaceImageButton);
	    add(Box.createVerticalStrut(10));
	    add(resetImageButton);
	    add(Box.createVerticalStrut(10));
	    
	    int fixedHeight = 150; 
	    Dimension fixedSize = new Dimension(Integer.MAX_VALUE, fixedHeight);
	    
	    this.setMinimumSize(fixedSize);
	    this.setPreferredSize(fixedSize);
	    this.setMaximumSize(fixedSize);
	    
		startImageWatcher();
	}
    
    /**
     * Starts a timer that regularly checks the status of the current image.
     * Updates UI enable/disable states based on image presence and processing status.
     */
    private void startImageWatcher() {
    	
        Timer imageWatcher = new Timer(300, e -> {
            if (leftPanel.isProcessing()) return;
            
         	// Check if the other panels are initialized yet
            if (leftPanel.getPreprocessingPanel() == null 
            		|| leftPanel.getThresholdingPanel() == null 
            		|| leftPanel.getPreprocessingPanel() == null) 
            	return;
            
            ImagePlus currentImgLDC = leftPanel.getCurrentImage();
            ImagePlus originalImgLDC = leftPanel.getOriginalImage();
            ImagePlus currentImg = WindowManager.getCurrentImage();
            
            // Checks if the current LDC image window has been closed, updates attributes if it is the case
            if (currentImgLDC != null && currentImgLDC.getWindow() == null) {
            	leftPanel.setCurrentImage(null);
            	currentImgLDC = null;
            }
            
            // If there is no current LDC image opened, we might do something, otherwise we don't change anything
            if (currentImgLDC == null) {
            	
                // If another current image is opened, then this another image becomes the new current LDC image to consider
                if (currentImg != null) {
                	
                	leftPanel.setOriginalImage(currentImg.duplicate()); // original image saved
                	leftPanel.setCurrentImage(currentImg);
                	updateUIInfosNbSlices();
                	
                	// update the calibration
                	updateCalibrationState(currentImg);
                	
                	imageStatusLabel.setText("<html><center>Current image: " + currentImg.getTitle() + "</center></html>");
                	leftPanel.resetPanels();
                	leftPanel.enablePanels(true);
                	
                // If there is an original LDC image previously saved, and there is no other current image opened, 
                // then we reset and lock LeftPanel sub-panels
                // This case guarantees that after closing a current LDC image, it will reset and lock UI only once until a new image is opened.
                } else if (originalImgLDC != null && currentImg == null) {
                	
                	leftPanel.setOriginalImage(null);
                	updateUIInfosNbSlices();
                	imageStatusLabel.setText("<html><center>No image opened.<br>Please open one.</center></html>");
                	leftPanel.resetPanels();
                	leftPanel.enablePanels(false);
                }
            	
            }
        });
        imageWatcher.start();
    }
    
    // =========================================================================
    // UI ACTIONS
    // =========================================================================
    
    /**
     * Replaces the current image, by calling the {@code replaceCurrentImage} method.
     */
    private void replaceButtonAction() {
        try {
        	int oldWorkflowIndex = leftPanel.getWorkflowIndex();
        	ImagePlus currentImgLDC = leftPanel.getCurrentImage();
        	replaceCurrentImage(currentImgLDC);
        	leftPanel.setOriginalImage(currentImgLDC.duplicate()); // New original ImagePlus when replacing the current image.
        	updateUIInfosNbSlices();
        	imageStatusLabel.setText("<html><center>Image opened:<br>" + currentImgLDC.getTitle() + "</center></html>");
        	leftPanel.resetPanels();
            leftPanel.enablePanels(true);
        	if (oldWorkflowIndex > 0) IJ.showMessage("Workflow reseted to preprocessing.");;
        } catch (IllegalArgumentException error) {
        	IJ.showMessage("Please open an image first (File > Open)");
        }
    }
    
    /**
     * Resets the current image, by calling the {@code resetCurrentImage} method.
     */
    private void resetButtonAction() {
		try {
			int oldWorkflowIndex = leftPanel.getWorkflowIndex();
			resetCurrentImage(leftPanel.getCurrentImage(), leftPanel.getOriginalImage());
    		updateUIInfosNbSlices();
    		leftPanel.resetPanels();
        	leftPanel.enablePanels(true);
    		if (oldWorkflowIndex > 0) IJ.showMessage("Workflow reseted to preprocessing.");;
		}catch (IllegalArgumentException error) {
			IJ.showMessage("Please open an image first (File > Open)");
		}
    }
    
    
    /**
     * Updates the {@link JLabel} containing the number of current considered slices / original slices.
     */
    public void updateUIInfosNbSlices() {
    	int nbOriginalSlices = 0;
    	int nbCurrentSlices = 0;
    	
    	ImagePlus originalImg = leftPanel.getOriginalImage();
    	if (originalImg != null) nbOriginalSlices = originalImg.getStackSize();
    	
    	ImagePlus currentImg = leftPanel.getCurrentImage();
    	if (currentImg != null) nbCurrentSlices = currentImg.getStackSize();
    	
    	if (nbOriginalSlices == 0) { // If there is no originl image (= no image currently considered)
    		infosNbSlicesLabel.setText("");
    	} else {
    		infosNbSlicesLabel.setText("Number of slices considered : "+ nbCurrentSlices +"/" + nbOriginalSlices);
    	}
    }
    
    // =========================================================================
    // CURRENT IMAGE REPLACEMENT / RESET
    // =========================================================================
    
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
	 * @param currentImg					The currently active image whose content will be replaced.
	 * @throws IllegalArgumentException		if {@code currentImg} is {@code null}.
	 */
    private void replaceCurrentImage(ImagePlus currentImg) {
    	if (currentImg == null) throw new IllegalArgumentException("No current image.");
    	
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File imageFile = fileChooser.getSelectedFile();
        ImagePlus newImage = IJ.openImage(imageFile.getAbsolutePath());
        if (newImage == null) return;
        
        // Updates file informations
        FileInfo newFi = newImage.getOriginalFileInfo();
        currentImg.setFileInfo(newFi);

        currentImg.setStack(newImage.getTitle(), newImage.getStack());
        currentImg.setCalibration(newImage.getCalibration());
        currentImg.setDimensions(newImage.getNChannels(), newImage.getNSlices(), newImage.getNFrames());
        currentImg.updateAndDraw();
        currentImg.repaintWindow();
        
        closeBinaryWindow(currentImg);
        // Update the calibration with the one of the new image
        updateCalibrationState(currentImg);
    }
    
    /**
     * Resets the image currently opened, by replacing it with the one without any modifications.
     * 
     * @param currentImg					The currently active image whose content will be replaced.
     * @param originalImg					The original {@code currentImg} before modifications.
	 * @throws IllegalArgumentException		if {@code currentImg} is {@code null} or {@code originalImg} is {@code null}.
     */
    private void resetCurrentImage(ImagePlus currentImg, ImagePlus originalImg) {
		if (currentImg == null) throw new IllegalArgumentException("No current image.");
		if (originalImg == null) throw new IllegalArgumentException("No original image.");
		
		// Saves the current title (without the "DUP_" prefix added by the duplication)
	    String currentTitle = currentImg.getTitle();
		
    	currentImg.setStack(originalImg.getTitle(), originalImg.getStack());
        currentImg.setCalibration(originalImg.getCalibration());
        currentImg.setDimensions(originalImg.getNChannels(), originalImg.getNSlices(), originalImg.getNFrames());
        currentImg.setTitle(currentTitle);
        currentImg.updateAndDraw();
        currentImg.repaintWindow();
        
        // update the calibration with the default one
        updateCalibrationState(currentImg);
        
        closeBinaryWindow(currentImg);
    }

    
    /**
     * Finds and closes the generated binary mask window associated with the given original image.
     * This ensures the workspace is cleaned up when the user resets the workflow.
     * * @param originalImp The original source image. The method uses its short title 
     * to locate the corresponding "[Title]_Binary" window.
     */
    public void closeBinaryWindow(ImagePlus originalImp) {
        if (originalImp == null) return;

        // Reconstruct the exact title given to the generated binary image
        String binaryTitle = originalImp.getShortTitle() + "_Binary";
        
        // Ask ImageJ if an image with this title is currently open
        ImagePlus binaryImp = WindowManager.getImage(binaryTitle);
        
        // If it exists, safely close it
        if (binaryImp != null) {
            // Tell ImageJ no changes were made to bypass the "Save changes?" dialog
            binaryImp.changes = false; 
            binaryImp.close();
        }
    }
    
    
    /**
     * Call the LDCService API to update the settings about the calibration of the image,
     * used when image is initialized.
     * @param img		The current image to update it's calibration.
     */
    public void updateCalibrationState(ImagePlus img) {
    	
    	if (img != null) {
    		Calibration cal = img.getCalibration();
    		selectedSettings.setCalibration(cal);
    		selectedSettings.setIsCalibrated(cal != null && cal.scaled());
    	}else {
    		selectedSettings.setIsCalibrated(false);
    		selectedSettings.setCalibration(null);
    	}
    	
    }
}
