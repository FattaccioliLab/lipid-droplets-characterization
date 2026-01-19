package mainGUI.panels;

import java.awt.Color;
import java.awt.FlowLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import org.scijava.event.EventService;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;

@SuppressWarnings("serial")
public class RightPanel extends JPanel {

	@Parameter
	private DatasetIOService datasetIOService;
	
	@Parameter(type = ItemIO.OUTPUT)
	private Dataset image;
	
	@Parameter
	private UIService uiService;
	
	@Parameter
	private ImageDisplayService displayService;
	
    @Parameter
    private EventService eventService;
	
	private boolean canModifyPretreatments;
	
	private JToggleButton toggleEC;
	private JToggleButton toggleMF;
	private JFormattedTextField saturatedField;
	
    public RightPanel(final Context ctx) {
    	
    	ctx.inject(this);
    	
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

        toggleEC = new JToggleButton("OFF");
        toggleEC.setSelected(false);
        toggleEC.setEnabled(canModifyPretreatments);
        toggleEC.addActionListener(e -> handleToggleEC(toggleEC));
        eCPanel.add(toggleEC);

        JLabel labelSaturated = new JLabel("Saturated", SwingConstants.CENTER);
        eCPanel.add(labelSaturated);
        
        // might change later for a slider instead
        NumberFormat format = NumberFormat.getNumberInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        saturatedField = new JFormattedTextField(formatter);
        saturatedField.setColumns(5);
        saturatedField.setValue(0.35);
        eCPanel.add(saturatedField);

        add(eCPanel);
        
        // Median filter
        
        JPanel mFPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JLabel labelMF = new JLabel("Median filter", SwingConstants.CENTER);
        labelMF.setAlignmentX(CENTER_ALIGNMENT);
        mFPanel.add(labelMF);
        
        toggleMF = new JToggleButton("OFF");
        toggleMF.setSelected(false);
        toggleMF.setEnabled(canModifyPretreatments);
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
     * If an image is currently open, unlocks preatreatment toggles, otherwise it locks them. <br>
     * Must be called each time an image is closed or opened.
     */
    public void updateCanModifyPretreatments() {
    	canModifyPretreatments = !displayService.getImageDisplays().isEmpty();
    	toggleEC.setEnabled(canModifyPretreatments);
    	toggleMF.setEnabled(canModifyPretreatments);
    	saturatedField.setEnabled(canModifyPretreatments);
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
     * 
     * @param toggle Enhance contrast toggle
     */
    private void handleToggleEC(JToggleButton toggle) {
        if (toggle.isSelected()) {
            toggle.setText("ON");
        } else {
            toggle.setText("OFF");
        }
    }
    
    /**
     * 
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