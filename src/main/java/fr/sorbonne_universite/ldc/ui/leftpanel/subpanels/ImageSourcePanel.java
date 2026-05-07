package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.MainGUI_LDC;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanelSubPanel;
import fr.sorbonne_universite.ldc.ui.leftpanel.PipelineSubPanel;
import fr.sorbonne_universite.ldc.utils.PanelUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileInfo;
import ij.measure.Calibration;

/**
 * Creates the top panel of the {@link LeftPanel}, containing image status, current image management buttons
 * and current parameters management buttons.
 */
@SuppressWarnings("serial")
public class ImageSourcePanel extends JPanel implements LeftPanelSubPanel {
	
	// The parent panel
	private LeftPanel leftPanel;
	
    @Parameter
	private LDCService ldc;
	
	private JLabel imageStatusLabel;
	
	private JLabel infosNbSlicesLabel;
	
	JButton openImageButton;
	
	// Parameters management buttons
	private JButton importParametersButton;
	private JButton exportParametersButton;
	
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

	    // Open / Replace image button
	    JButton openOrReplaceButton = new JButton("Open / Replace image");
	    openOrReplaceButton.addActionListener(e -> {
	    	if (!leftPanel.isProcessing()) {
	    		openOrReplaceImageAction();
	    	}
	    });
	    
	    // Reset image button
	    JButton resetImageButton = new JButton("Reset image");
	    resetImageButton.addActionListener(e -> {
	    	if (!leftPanel.isProcessing()) {
	    		resetButtonAction(true);
	    	}
	    });
	    
	    // Horizontal panel for Reset and Replace
	    JPanel imageButtonsPanel = new JPanel();
	    imageButtonsPanel.setLayout(new BoxLayout(imageButtonsPanel, BoxLayout.X_AXIS));
	    imageButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
	    imageButtonsPanel.add(openOrReplaceButton);
	    imageButtonsPanel.add(Box.createHorizontalStrut(10));
	    imageButtonsPanel.add(resetImageButton);
 
	    // Import parameters button
	    importParametersButton = new JButton("Import parameters");
	    importParametersButton.addActionListener(e -> {
	    	importParametersAction();
	    });
	    
	    // Export parameters button
	    exportParametersButton = new JButton("Export parameters");
	    exportParametersButton.addActionListener(e -> {
	    	exportParametersAction();
	    });
 
	    // Horizontal panel for Importing and Exporting parameters
	    JPanel parametersButtonsPanel = new JPanel();
	    parametersButtonsPanel.setLayout(new BoxLayout(parametersButtonsPanel, BoxLayout.X_AXIS));
	    parametersButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
	    parametersButtonsPanel.add(importParametersButton);
	    parametersButtonsPanel.add(Box.createHorizontalStrut(10));
	    parametersButtonsPanel.add(exportParametersButton);
 
	    add(Box.createVerticalStrut(5));
	    add(imageStatusLabel);
	    add(Box.createVerticalStrut(5));
	    add(infosNbSlicesLabel);
	    add(Box.createVerticalStrut(10));
	    add(imageButtonsPanel);
	    add(Box.createVerticalStrut(10));
	    add(parametersButtonsPanel);
	    add(Box.createVerticalStrut(10));
 
	    int fixedHeight = 200;
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
    // IMAGE MANAGEMENT
    // =========================================================================
    
    /**
     * Open or replace the current image by a new one.
     */
    private void openOrReplaceImageAction() {
        JFileChooser fileChooser = new JFileChooser();

        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File imageFile = fileChooser.getSelectedFile();
        ImagePlus newImage = IJ.openImage(imageFile.getAbsolutePath());
        if (newImage == null) return;

        ImagePlus currentImg = leftPanel.getCurrentImage();

        closeBinaryWindow();
        
		int oldWorkflowIndex = leftPanel.getWorkflowIndex(); 
        
        if (currentImg == null) { // importing case

        	newImage.show();
            leftPanel.setOriginalImage(newImage.duplicate());
            leftPanel.setCurrentImage(newImage);

            updateCalibrationState(newImage);
            
            updateUIInfosNbSlices(); 
            imageStatusLabel.setText("<html><center>Image opened:<br>" + newImage.getTitle() + "</center></html>");
            leftPanel.resetPanels();

        } else { // replacement case
    		
    		// Updates file informations
            FileInfo newFi = newImage.getOriginalFileInfo();
            currentImg.setFileInfo(newFi);

            currentImg.setStack(newImage.getTitle(), newImage.getStack());
            currentImg.setCalibration(newImage.getCalibration());
            currentImg.setDimensions(newImage.getNChannels(), newImage.getNSlices(), newImage.getNFrames());
            currentImg.updateAndDraw();
            currentImg.repaintWindow();
            updateCalibrationState(currentImg);
    		
    		leftPanel.setOriginalImage(currentImg.duplicate()); 
    		
    		updateUIInfosNbSlices(); 
    		imageStatusLabel.setText("<html><center>Image opened:<br>" + currentImg.getTitle() + "</center></html>"); 
    		leftPanel.resetPanels(); 
    		if (oldWorkflowIndex > 0) IJ.showMessage("Workflow reseted to preprocessing."); 
        	
        }
    }
    
    
    /**
     * Resets the current image, by calling the {@code resetCurrentImage} method.
     * @param displayMessage		Indicates if an informational message must be displayed.
     */
    private void resetButtonAction(boolean displayMessage) {
		try {
			int oldWorkflowIndex = leftPanel.getWorkflowIndex();
			resetCurrentImage(leftPanel.getCurrentImage(), leftPanel.getOriginalImage());
    		updateUIInfosNbSlices();
    		leftPanel.resetPanels();
    		if (oldWorkflowIndex > MainGUI_LDC.PREPROCESSING_STEP && displayMessage) IJ.showMessage("Workflow reseted to preprocessing.");
		}catch (IllegalArgumentException error) {
			IJ.showMessage("Please open an image first (File > Open)");
		}
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
        
        closeBinaryWindow();
        closePreviewWindow();
    }

    
    /**
     * Closes the generated binary mask window associated with the given original image.
     * This ensures the workspace is cleaned up when the user resets the workflow.
     */
    public void closeBinaryWindow() {
        ImagePlus binaryImp = leftPanel.getMask();
        
        // If the mask exists, safely close it
        if (binaryImp != null) {
            // Tell ImageJ no changes were made to bypass the "Save changes?" dialog
            binaryImp.changes = false; 
            binaryImp.close();
            leftPanel.setMask(null);
        }
    }
    
    /**
     * Closes the generated preview window associated with the given original image.
     * This ensures the workspace is cleaned up when the user resets the workflow.
     */
    public void closePreviewWindow() {
        ImagePlus previewWindow = leftPanel.getPreviewWindow();
        
        // If the window exists, safely close it
        if (previewWindow != null) {
            // Tell ImageJ no changes were made to bypass the "Save changes?" dialog
        	previewWindow.changes = false; 
        	previewWindow.close();
            leftPanel.setPreviewWindow(null);
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
    		ldc.setCalibration(cal);
    		ldc.setIsCalibrated(cal != null && cal.scaled());
    	}else {
    		ldc.setIsCalibrated(false);
    		ldc.setCalibration(null);
    	}
    	
    }
    
    // =========================================================================
    // CURRENT PARAMETERS MANAGEMENT
    // =========================================================================
    
    /**
     * Open a JFileChooser for selecting an analysis parameters JSON to import.
     * Then applies the pipeline until the selected pipeline step, and imports parameters into the UI for every step.
     * Note that the calibration is kept with the current opened image.
     */
    private void importParametersAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Parameters");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            String[] steps = {
                "Preprocessing",
                "Thresholding",
                "Morphological operations",
                "Particle analysis"
            };

            JComboBox<String> stepCombo = new JComboBox<>(steps);
            stepCombo.setSelectedIndex(3); // Particle analysis by default

            JPanel panel = new JPanel();
            panel.add(new JLabel("Go to step:"));
            panel.add(stepCombo);

            int choice = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Import parameters",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (choice != JOptionPane.OK_OPTION) return;

            int selectedStepIndex = stepCombo.getSelectedIndex();

            try {
                // Reset image & workflow
                resetButtonAction(false);

                // Load JSON
                ldc.loadAnalysis(selectedFile.getAbsolutePath());

                // Keep calibration of current image
                updateCalibrationState(leftPanel.getCurrentImage());

                new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() {

                        int currentStep = MainGUI_LDC.PREPROCESSING_STEP;
                        int i = 0;

                        for (PipelineSubPanel ui : leftPanel.getPipelineSubPanels()) {

                            // Always import new parameters with the UI, for each sub-panel
                            ui.syncUIWithParams();

                            // But apply them only until the selected step
                            if (i <= selectedStepIndex) {
                                ui.applyUIWithParams();

                                if (currentStep < MainGUI_LDC.ANALYSIS_PARAMETERS_STEP && i < selectedStepIndex) {
                                    leftPanel.goToNextStep();
                                }
                            }

                            currentStep++;
                            i++;
                        }

                        return null;
                    }

                    @Override
                    protected void done() {
                        JOptionPane.showMessageDialog(
                            ImageSourcePanel.this,
                            "Parameters imported up to: " + steps[selectedStepIndex],
                            "Import Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }

                }.execute();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error importing parameters:\n" + e.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Open a JFileChooser for selecting the save path
     * and exporting current analysis parameters to JSON.
     */
    private void exportParametersAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Parameters");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Add .json extension if not already present
            String filePath = selectedFile.getAbsolutePath();
            if (!filePath.endsWith(".json")) {
                filePath += ".json";
            }
            
            try {
            	ldc.saveAnalysis(filePath);
                
                JOptionPane.showMessageDialog(this, 
                    "Parameters exported successfully!", 
                    "Export Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting parameters:\n" + e.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // =========================================================================
    // ENABLING / DISABLING UI COMPONENTS
    // =========================================================================
    
    @Override
    public void enableUIComponents(boolean enable) {
    	importParametersButton.setEnabled(enable);
    	exportParametersButton.setEnabled(enable);
    }
}
