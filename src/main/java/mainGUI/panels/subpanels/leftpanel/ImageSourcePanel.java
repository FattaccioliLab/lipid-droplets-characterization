package mainGUI.panels.subpanels.leftpanel;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import ij.IJ;
import ij.ImagePlus;
import mainGUI.panels.LeftPanel;
import mainGUI.utils.PanelUtils;
import model.LDCService;

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
		PanelUtils.createVerticalPanel(this, "Image Source", 170);
		
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
	        	try {
	        		selectedSettings.replaceCurrentImage(this);
	        		ImagePlus img = leftPanel.updateAndGetImg();
	        		leftPanel.setOriginalImage(img.duplicate()); // New original ImageProcessor when replacing the current image.
	        		leftPanel.resetPanels();
	        		updateUIInfosNbSlices();
	        	} catch (IllegalArgumentException error) {
	        		IJ.showMessage("Please open an image first (File > Open)");
	        	}
	        }
	    });
	    
	    // Reset image button

	    JButton resetImageButton = new JButton("Reset image");
	    resetImageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    resetImageButton.addActionListener(e -> {
	    	if (!leftPanel.isProcessing()) {
	    		try {
	    			selectedSettings.resetCurrentImage();
	    			ImagePlus img = leftPanel.updateAndGetImg();
	    			leftPanel.setOriginalImage(img.duplicate()); // New original ImageProcessor when replacing the current image.
	    			leftPanel.resetPanels();
	    			updateUIInfosNbSlices();
	    		}catch (IllegalArgumentException error) {
	    			IJ.showMessage("Please open an image first (File > Open)");
	    		}catch (IllegalStateException error) {
	    			IJ.showMessage(error.getMessage());
	    		}
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
            if ((leftPanel.getPreprocessingPanel() == null) ||
            		(leftPanel.getThresholdingPanel() == null)) return;

            ImagePlus img = leftPanel.updateAndGetImg();
            boolean hasImage = (img != null);
            
            // If an image is opened, and there is no original image, its ImageProcessor copy becomes the original ImageProcessor.
            if (hasImage && leftPanel.getOriginalImage() == null) {
            	leftPanel.setOriginalImage(img.duplicate());
            	updateUIInfosNbSlices();
            }
            
            // If there is no current image, the original ImageProcessor within the MainGui is set null.
            if (!hasImage) {
            	leftPanel.setOriginalImage(null);
            	leftPanel.resetPanels();
            	updateUIInfosNbSlices();
            }


            imageStatusLabel.setText(hasImage 
                ? "<html><center>Image opened:<br>" + img.getTitle() + "</center></html>"
                : "<html><center>No image opened.<br>Please open one.</center></html>");

            PreprocessingPanel ppp = leftPanel.getPreprocessingPanel();
            ThresholdingPanel tp = leftPanel.getThresholdingPanel();

            ppp.enableUIComponents(hasImage, false);
            tp.enableUIComponents(hasImage);

            leftPanel.setNextButtonEnabled(hasImage && !ppp.isVisible() == false);
            leftPanel.setPrevButtonEnabled(hasImage && !tp.isVisible()==false);
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
    	
    	ImagePlus currentImg = leftPanel.updateAndGetImg();
    	if (currentImg != null) nbCurrentSlices = currentImg.getStackSize();
    	
    	if (nbOriginalSlices == 0) { // If there is no originl image (= no image currently considered)
    		infosNbSlicesLabel.setText("");
    	} else {
    		infosNbSlicesLabel.setText("Number of slices considered : "+ nbCurrentSlices +"/" + nbOriginalSlices);
    	}
    }

}
