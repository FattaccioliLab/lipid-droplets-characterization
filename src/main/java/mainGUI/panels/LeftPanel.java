package mainGUI.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import model.AnalysisSettings;

/**
 * The left side of the plugin main GUI.
 * It contains checkboxes and sliders that let users set theirs preferred setting.
 */
@SuppressWarnings("serial")
public class LeftPanel extends JPanel {
	
	private ImagePlus img;
	private AnalysisSettings selectedSettings;
	
    public LeftPanel(AnalysisSettings selectedSettings) {
    
    	this.selectedSettings = selectedSettings;
    	
        setLayout(new BorderLayout());
        
        // top panel contains image imported label and button
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new java.awt.GridLayout(2, 1)); 
        
        
        JLabel label = new JLabel();
        topPanel.add(label);
        
        // button that let user open a tiff file
        JButton fileSelectButton =  new JButton("replace image");
        fileSelectButton.addActionListener(e -> {

            img = WindowManager.getCurrentImage();

            if (img == null) {
                IJ.showMessage("Please open an image first (File > Open)");
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            int response = fileChooser.showOpenDialog(this);

            if (response != JFileChooser.APPROVE_OPTION) return;

            File imageFile = fileChooser.getSelectedFile();

            ImagePlus newImage = IJ.openImage(imageFile.getAbsolutePath());
            if (newImage == null) {
            	return;
            }

            // replace the current image
            img.setStack(newImage.getTitle(), newImage.getStack());
            img.setCalibration(newImage.getCalibration());
            img.setDimensions(
                    newImage.getNChannels(),
                    newImage.getNSlices(),
                    newImage.getNFrames()
            );

            img.updateAndDraw();
        });
        topPanel.add(fileSelectButton);
        
        add(topPanel, BorderLayout.NORTH);
           
        // Timer that check regularly if an image is opened and get the image
    	Timer imageWatcher = new Timer(300, e -> {
    		img = WindowManager.getCurrentImage();
    	    if (img != null) {
    	    	label.setText("Image opened : " + img.getTitle());
    	    }else {
    	    	label.setText("There is no opened image, please open one.");
    	    }
    	    
    	});
    	imageWatcher.start();
        	      
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLUE, 2)
        ));
        
    }
    
}