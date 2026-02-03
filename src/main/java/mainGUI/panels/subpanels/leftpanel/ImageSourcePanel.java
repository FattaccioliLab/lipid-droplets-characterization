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
	
	public ImageSourcePanel(Context ctx, LeftPanel leftPanel) {
		
		// Initialization of the panel layout
		super();
		PanelUtils.createVerticalPanel(this, "Image Source", 170);
		
		ctx.inject(this);
		this.leftPanel = leftPanel;
		
	    imageStatusLabel = new JLabel("<html><center>No image opened.<br>Please open one.</center></html>", SwingConstants.CENTER);
	    imageStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

	    // File select button
	    
	    JButton fileSelectButton = new JButton("Replace image");
	    fileSelectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    fileSelectButton.addActionListener(e -> {
	        if (!leftPanel.isProcessing()) {
	        	leftPanel.updateAndGetImg(); // this method call seem useless
	        	try {
	        		selectedSettings.replaceCurrentImage(this);
	        		ImagePlus img = leftPanel.updateAndGetImg();
	        		leftPanel.setOriginalImage(img.duplicate()); // New original ImageProcessor when replacing the current image.
	        		leftPanel.setPreprocessingDone(false); // reset the preprocessingDone flag of the LeftPanel
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
	    			if (leftPanel.isProcessing()) {
	    				return;
	    			}
	    			selectedSettings.resetCurrentImage();
	    			leftPanel.resetPanels();
	    		}catch (IllegalArgumentException error) {
	    			IJ.showMessage("Please open an image first (File > Open)");
	    		}catch (IllegalStateException error) {
	    			IJ.showMessage(error.getMessage());
	    		}
	    	}
	    });

	    add(Box.createVerticalStrut(5));
	    add(imageStatusLabel);
	    add(Box.createVerticalStrut(10));
	    add(fileSelectButton);
	    add(resetImageButton);
	    add(Box.createVerticalStrut(5));
	    
	    
		startImageWatcher();
	}
    
    /**
     * Starts a timer that regularly checks the status of the current image.
     * Updates UI enable/disable states based on image presence and processing status.
     */
    private void startImageWatcher() {
        Timer imageWatcher = new Timer(300, e -> {
            if (leftPanel.isProcessing()) return;

            ImagePlus img = leftPanel.updateAndGetImg();
            boolean hasImage = (img != null);
            
            // If an image is opened, and there is no original image, its ImageProcessor copy becomes the original ImageProcessor.
            if (hasImage && leftPanel.getOriginalImage() == null) 
            	leftPanel.setOriginalImage(img.duplicate());
            
            // If there is no current image, the original ImageProcessor within the MainGui is set null.
            if (!hasImage)
            	leftPanel.setOriginalImage(null);

            imageStatusLabel.setText(hasImage 
                ? "<html><center>Image opened:<br>" + img.getTitle() + "</center></html>"
                : "<html><center>No image opened.<br>Please open one.</center></html>");

            PreprocessingPanel ppp = leftPanel.getPreprocessingPanel();
            ppp.enableUIComponents(hasImage);
            leftPanel.setNextButtonEnabled(hasImage && !ppp.isVisible() == false);
        });
        imageWatcher.start();
    }

}
