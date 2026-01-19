package mainGUI.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;

import org.scijava.event.EventService;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;

/**
 * Part of the main GUI containing the pretreatments of the plugin
 */
@SuppressWarnings("serial")
public class RightPanel extends JPanel {

	@Parameter
	private DatasetIOService datasetIOService;
	
	@Parameter(type = ItemIO.OUTPUT)
	private Dataset image;
	
	@Parameter
	private UIService uiService;
	
	@Parameter
	private ImageDisplayService imageDisplayService;
	
	@Parameter
	private DisplayService displayService;
	
    @Parameter
    private EventService eventService;
	
    // UI pretreatment elements
	private JToggleButton toggleEC;
	private JToggleButton toggleMF;
	private JFormattedTextField saturatedField;
	
	// indicates if UI pretreatment elements can be modified
	private boolean canModifyPretreatments;
	
	// reset buttons
	private JButton resetEC;
	
	// needed for the pretreatments
	private ContrastEnhancer ce;
	
	// default values
	public final static double DFL_SATURATED = 0.35;
	
    public RightPanel(final Context ctx) {
    	
    	ctx.inject(this);
    	canModifyPretreatments = false;
    	ce = new ContrastEnhancer();
    	
    	// Layout
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GREEN, 2)
        ));
        
        JLabel labelPreatreatments = new JLabel("Preatreatments", SwingConstants.CENTER);
        labelPreatreatments.setAlignmentX(CENTER_ALIGNMENT);
        add(labelPreatreatments);
        
        // temporary
        JButton btnImportImage = new JButton("[TMP] Import test image");
        btnImportImage.setAlignmentX(CENTER_ALIGNMENT);
        btnImportImage.addActionListener(e -> handleImportImage(btnImportImage));
        add(btnImportImage);
        
        //Enhance Contrast
        
        JPanel eCPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JLabel labelEC = new JLabel("Enhance contrast", SwingConstants.CENTER);
        eCPanel.add(labelEC);

        toggleEC = new JToggleButton("OFF", false);
        toggleEC.setEnabled(true);
        toggleEC.addActionListener(e -> handleToggleEC(toggleEC));
        eCPanel.add(toggleEC);

        JLabel labelSaturated = new JLabel("Saturated (%)", SwingConstants.CENTER);
        eCPanel.add(labelSaturated);
        
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        NumberFormatter formatter = new NumberFormatter(format);
        saturatedField = new JFormattedTextField(formatter);
        saturatedField.setEnabled(canModifyPretreatments);
        saturatedField.setColumns(5);
        saturatedField.setValue(DFL_SATURATED);
        saturatedField.addActionListener(e -> handleUpdateSaturatedValue(saturatedField));
        eCPanel.add(saturatedField);
        
        resetEC = new JButton("Reset");
        resetEC.setEnabled(canModifyPretreatments);
        resetEC.addActionListener(e -> {
        	saturatedField.setValue(DFL_SATURATED);
        	handleUpdateSaturatedValue(saturatedField);
        });
        eCPanel.add(resetEC);

        add(eCPanel);
        
        // Median filter
        
        JPanel mFPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JLabel labelMF = new JLabel("Median filter", SwingConstants.CENTER);
        labelMF.setAlignmentX(CENTER_ALIGNMENT);
        mFPanel.add(labelMF);
        
        toggleMF = new JToggleButton("OFF", false);
        toggleMF.setEnabled(true);
        toggleMF.addActionListener(e -> handleToggleMF(toggleMF));
        mFPanel.add(toggleMF);
        
        add(mFPanel);
        
        updateCanModifyPretreatments();
    }
    
    // PUBLIC METHODS
    
    /**
     * @return true if Enhance contrast is ON, otherwise false.
     */
    public boolean enhanceContrastEnabled() {
    	return toggleEC.isEnabled();
    }
    
    /**
     * @return The last valid value of the 'saturated' field, for the enhance contrast option.
     */
    public Object enhanceContrastSaturated() {
    	return saturatedField.getValue();
    }
    
    /**
     * @return true if Median filter is ON, otherwise false.
     */
    public boolean medianFilterEnabled() {
    	return toggleMF.isEnabled();
    }
    
    /**
     * If an image is currently open, unlocks pretreatment toggles, otherwise it locks them. <br>
     * Must be called each time an image is closed or opened.
     */
    public void updateCanModifyPretreatments() {
    	canModifyPretreatments = !imageDisplayService.getImageDisplays().isEmpty();
    	toggleEC.setEnabled(canModifyPretreatments);
    	toggleMF.setEnabled(canModifyPretreatments);
    }
    
    // PRIVATE METHODS
    
    // temporary
    private void handleImportImage(JButton button) {
        try {
        	image = datasetIOService.open(RightPanel.class.getResource("/TestSample.tif").getPath());
        	uiService.show(image);
        	updateCanModifyPretreatments();
        } catch (Exception error) {
        	error.printStackTrace();
        }
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
            saturatedField.setEnabled(true);
            resetEC.setEnabled(true);
            handleUpdateSaturatedValue(saturatedField);
            
        } else {
            toggle.setText("OFF");
            saturatedField.setEnabled(false);
            resetEC.setEnabled(false);
            enhanceContrast(0);
        }
    }
    
    /**
     * Calls {@code applyContrast}.
     * @param text The formatted input for the 'saturated' enhance contrast value 
     */
    private void handleUpdateSaturatedValue(JFormattedTextField text) {
    	
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

    
}