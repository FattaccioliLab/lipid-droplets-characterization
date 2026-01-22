package mainGUI.panels.subpanels.leftpanel;

import java.awt.Component;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.scijava.Context;

import ij.IJ;
import ij.ImagePlus;
import mainGUI.panels.LeftPanel;
import mainGUI.utils.PanelUtils;

/**
 * Creates the top panel of the LeftPanel, containing image status and the "Replace Image" button.
 * * @return The constructed JPanel.
 */
@SuppressWarnings("serial")
public class ImageSourcePanel extends JPanel {
	
	private JLabel imageStatusLabel;
	private LeftPanel leftPanel; // The panel container
	
	public ImageSourcePanel(Context ctx, LeftPanel leftPanel) {
		
		// Initialization of the panel layout
		super();
		PanelUtils.createVerticalPanel(this, "Image Source", 150);
		
		ctx.inject(this);
		this.leftPanel = leftPanel;
		
	    imageStatusLabel = new JLabel("<html><center>No image opened.<br>Please open one.</center></html>", SwingConstants.CENTER);
	    imageStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

	    JButton fileSelectButton = new JButton("Replace image");
	    fileSelectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	    fileSelectButton.addActionListener(e -> {
	        if (!leftPanel.isProcessing()) replaceImageAction();
	    });

	    add(Box.createVerticalStrut(5));
	    add(imageStatusLabel);
	    add(Box.createVerticalStrut(10));
	    add(fileSelectButton);
	    add(Box.createVerticalStrut(5));
	    
	    
		startImageWatcher();
	}
	
    /**
     * Opens a file chooser to replace the current image stack with a new one from disk.
     */
    private void replaceImageAction() {
    	ImagePlus img = leftPanel.updateAndGetImg();
        if (img == null) {
            IJ.showMessage("Please open an image first (File > Open)");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File imageFile = fileChooser.getSelectedFile();
        ImagePlus newImage = IJ.openImage(imageFile.getAbsolutePath());
        if (newImage == null) return;

        img.setStack(newImage.getTitle(), newImage.getStack());
        img.setCalibration(newImage.getCalibration());
        img.setDimensions(newImage.getNChannels(), newImage.getNSlices(), newImage.getNFrames());
        img.updateAndDraw();
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
