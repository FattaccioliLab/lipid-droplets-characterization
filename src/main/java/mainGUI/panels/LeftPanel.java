package mainGUI.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.plugin.filter.RankFilters;
import ij.plugin.ContrastEnhancer;
import model.AnalysisSettings;
import net.imagej.display.ImageDisplayService;

import org.scijava.Context;
import org.scijava.plugin.Parameter;


/**
 * The left side of the plugin main GUI.
 * It contains checkboxes and controls for preprocessing.
 */
@SuppressWarnings("serial")
public class LeftPanel extends JPanel {
	
	private ImagePlus img;
	private final AnalysisSettings selectedSettings;
	
	private JLabel imageStatusLabel;
	private JCheckBox enhanceCheckbox;
	private JSpinner enhanceSaturatedSpinner;
	private JCheckBox medianCheckbox;
	private JSpinner medianRadiusSpinner;
	private JButton resetEC;
	
	// needed for the pretreatments
	private ContrastEnhancer ce;
	
	
	@Parameter
	private ImageDisplayService imageDisplayService;

	
    public LeftPanel(Context ctx, AnalysisSettings selectedSettings) {
    
    	this.selectedSettings = selectedSettings;
    	
        setLayout(new BorderLayout());
        
        // Main container for the column
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        
        // --- 1. IMAGE SOURCE SCOPE ---
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
        imagePanel.setBorder(BorderFactory.createTitledBorder("Image Source"));
        
        // FORCE FULL WIDTH: Set alignment left + Max width to huge value
        imagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        imagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150)); // Height is arbitrary cap, Width is key
        
        // Label
        imageStatusLabel = new JLabel("<html><center>No image opened.<br>Please open one.</center></html>", SwingConstants.CENTER);
        imageStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Button
        JButton fileSelectButton = new JButton("Replace image");
        fileSelectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
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

            img.setStack(newImage.getTitle(), newImage.getStack());
            img.setCalibration(newImage.getCalibration());
            img.setDimensions(
                    newImage.getNChannels(),
                    newImage.getNSlices(),
                    newImage.getNFrames()
            );

            img.updateAndDraw();
        });
        
        imagePanel.add(Box.createVerticalStrut(5));
        imagePanel.add(imageStatusLabel);
        imagePanel.add(Box.createVerticalStrut(10));
        imagePanel.add(fileSelectButton);
        imagePanel.add(Box.createVerticalStrut(5));

        mainContainer.add(imagePanel);
        mainContainer.add(Box.createVerticalStrut(10)); 

        // --- 2. PREPROCESSING SCOPE ---
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Preprocessing"));
        
        // FORCE FULL WIDTH: Match the imagePanel behavior
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300)); // Height is arbitrary cap, Width is key

        // Enhance contrast controls
        enhanceCheckbox = new JCheckBox("Enhance contrast");
        enhanceCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        enhanceCheckbox.setSelected(selectedSettings.isEnhanceContrast());
        enhanceCheckbox.addActionListener(e -> {
            boolean enabled = enhanceCheckbox.isSelected();
            selectedSettings.setEnhanceContrast(enabled);
            enhanceSaturatedSpinner.setEnabled(enabled);
        });
        filterPanel.add(enhanceCheckbox);

        enhanceSaturatedSpinner = new JSpinner(new SpinnerNumberModel(selectedSettings.getEnhanceSaturatedPercent(), 0.01, 5.0, 0.01));
        enhanceSaturatedSpinner.setEnabled(selectedSettings.isEnhanceContrast());
        enhanceSaturatedSpinner.addChangeListener(e -> {
            double val = ((Number) enhanceSaturatedSpinner.getValue()).doubleValue();
            selectedSettings.setEnhanceSaturatedPercent(val);
        });

        JPanel saturatedRow = new JPanel(new BorderLayout());
        saturatedRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        saturatedRow.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));
        saturatedRow.add(new JLabel("  Saturated (%): "), BorderLayout.WEST);
        saturatedRow.add(enhanceSaturatedSpinner, BorderLayout.CENTER);
        filterPanel.add(saturatedRow);

        filterPanel.add(Box.createVerticalStrut(8));

        // Median filter controls
        medianCheckbox = new JCheckBox("Median filter");
        medianCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        medianCheckbox.setSelected(selectedSettings.isMedianFilter());
        medianCheckbox.addActionListener(e -> {
            boolean enabled = medianCheckbox.isSelected();
            selectedSettings.setMedianFilter(enabled);
            medianRadiusSpinner.setEnabled(enabled);
        });
        filterPanel.add(medianCheckbox);

        medianRadiusSpinner = new JSpinner(new SpinnerNumberModel(selectedSettings.getMedianRadius(), 0.5, 50.0, 0.5));
        medianRadiusSpinner.setEnabled(selectedSettings.isMedianFilter());
        medianRadiusSpinner.addChangeListener(e -> {
            double val = ((Number) medianRadiusSpinner.getValue()).doubleValue();
            selectedSettings.setMedianRadius(val);
        });

        JPanel radiusRow = new JPanel(new BorderLayout());
        radiusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        radiusRow.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));
        radiusRow.add(new JLabel("  Radius (px): "), BorderLayout.WEST);
        radiusRow.add(medianRadiusSpinner, BorderLayout.CENTER);
        filterPanel.add(radiusRow);

        // Action buttons
        JPanel buttonRow = new JPanel(new java.awt.GridLayout(1, 2, 6, 6));
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Fix button row width to match others
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JButton previewButton = new JButton("Preview");
        previewButton.addActionListener(e -> runPreprocessingInBackground(true));
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> runPreprocessingInBackground(false));
        buttonRow.add(previewButton);
        buttonRow.add(applyButton);
        
        filterPanel.add(Box.createVerticalStrut(10));
        filterPanel.add(buttonRow);
        filterPanel.add(Box.createVerticalStrut(5));

        mainContainer.add(filterPanel);
        
        add(mainContainer, BorderLayout.NORTH);
           
    	Timer imageWatcher = new Timer(300, e -> {
    		img = WindowManager.getCurrentImage();
    	    if (img != null) {
    	    	imageStatusLabel.setText("<html><center>Image opened:<br>" + img.getTitle() + "</center></html>");
    	    } else {
    	    	imageStatusLabel.setText("<html><center>No image opened.<br>Please open one.</center></html>");
    	    }
    	});
    	imageWatcher.start();
        	      
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLUE, 2)
        ));
    }
    
    
    
    /**
     * Enhances the contrast of the current active image, or does nothing if there is no active image.
     * @param saturated The saturated value for enhancing constrast. Makes the assumption that {@code saturated} is a percentage.
     */
    private void enhanceContrast(double saturated) {
    	// No active image
    	if (imageDisplayService.getImageDisplays().isEmpty()) return;
    	
		ImagePlus imp = IJ.getImage();
		ce.stretchHistogram(imp, saturated);
		imp.updateAndDraw();
    }
    
    
    /**
     * Handles the ON/OFF change of the toggle.
     * @param toggle Enhance contrast toggle
     */
    private void handleToggleEC(JToggleButton toggle) {
        if (toggle.isSelected()) {
            toggle.setText("ON");
            enhanceSaturatedSpinner.setEnabled(true);
            resetEC.setEnabled(true);
            handleUpdateSaturatedValue(enhanceSaturatedSpinner);
            
        } else {
            toggle.setText("OFF");
            enhanceSaturatedSpinner.setEnabled(false);
            resetEC.setEnabled(false);
            enhanceContrast(0);
        }
    }
    
    /**
     * Calls {@code applyContrast}.
     * @param text The formatted input for the 'saturated' enhance contrast value 
     */
    private void handleUpdateSaturatedValue(JSpinner text) {
    	
    	// No active image
    	if (imageDisplayService.getImageDisplays().isEmpty()) return;
    	
    	// Format check first, might be useless
    	if (text.getValue() instanceof Number) {
    		double v = ((Number) text.getValue()).doubleValue();
        	if (v >= 0 && v <= 100) { // Enhancing contrast
        		enhanceContrast(v);
        	} else {
        		System.err.println("Must be in the [0, 100] range");
        	}
    	    
    	} else {
    		System.err.println("Must be a double");
    	}
    }
    
    
    
    /**
     * Handles the ON/OFF change of the toggle.
     * @param toggle Median filter toggle
     */
    private void handleToggleMF(JToggleButton toggle) {
        if (toggle.isSelected()) {
            toggle.setText("ON");
        } else {
            toggle.setText("OFF");
        }
    }
    

    
    
    
    
    
    
    private void runPreprocessingInBackground(final boolean previewOnly) {
        img = WindowManager.getCurrentImage();
        if (img == null) {
            IJ.showMessage("No image", "Please open an image first (File > Open).");
            return;
        }

        SwingWorker<ImagePlus, Void> worker = new SwingWorker<ImagePlus, Void>() {
            @Override
            protected ImagePlus doInBackground() throws Exception {
                ImagePlus work = img.duplicate();
                work.setTitle(img.getTitle() + (previewOnly ? "-preview" : "-preprocessed"));

                IJ.showStatus("Preprocessing...");
                int totalSteps = 1;
                if (selectedSettings.isEnhanceContrast()) totalSteps++;
                if (selectedSettings.isMedianFilter()) totalSteps++;
                int step = 0;

                if (selectedSettings.isEnhanceContrast()) {
                    step++;
                    IJ.showStatus(String.format("Enhancing contrast (saturated=%.2f%%) [%d/%d]", selectedSettings.getEnhanceSaturatedPercent(), step, totalSteps));
                    ContrastEnhancer ce = new ContrastEnhancer();
                    ce.stretchHistogram(work, selectedSettings.getEnhanceSaturatedPercent());
                    IJ.showProgress(step, totalSteps);
                }

                if (selectedSettings.isMedianFilter()) {
                    step++;
                    IJ.showStatus(String.format("Applying median (r=%.2f px) [%d/%d]", selectedSettings.getMedianRadius(), step, totalSteps));
                    applyMedianFilter(work, selectedSettings.getMedianRadius());
                    IJ.showProgress(step, totalSteps);
                }

                IJ.showStatus("Preprocessing done.");
                IJ.showProgress(1.0);
                return work;
            }

            @Override
            protected void done() {
                try {
                    ImagePlus result = get();
                    result.updateAndDraw();
                    result.show();
                } catch (Exception ex) {
                    IJ.showMessage("Error", "Preprocessing failed: " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    IJ.showProgress(0);
                }
            }
        };

        worker.execute();
    }

    private void applyMedianFilter(final ImagePlus imp, final double radius) {
        final RankFilters rf = new RankFilters();
        final ImageStack stack = imp.getStack();
        final int n = stack.getSize();
        for (int z = 1; z <= n; z++) {
            final ImageProcessor ip = stack.getProcessor(z);
            rf.rank(ip, radius, RankFilters.MEDIAN);
            IJ.showProgress(z, n);
        }
    }
}