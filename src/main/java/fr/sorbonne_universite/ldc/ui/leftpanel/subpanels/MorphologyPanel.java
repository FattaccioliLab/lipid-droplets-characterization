package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;
import fr.sorbonne_universite.ldc.utils.PanelUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

@SuppressWarnings("serial")
public class MorphologyPanel extends JPanel {

    private LeftPanel leftPanel;

    @Parameter
    private LDCService service;
    
    private boolean isApplied = false;
    private Object cleanPixelBackup = null; // Stores a secure deep copy of the raw pixels


    // Components
    private JCheckBox previewCheck;
    private JButton applyButton;
    
    // Radio Buttons for mutually exclusive selection
    private ButtonGroup optGroup;
    private JRadioButton noneRadio, erosionRadio, dilationRadio, openingRadio, closingRadio;
    
    public MorphologyPanel(Context ctx, LeftPanel leftPanel) {
        super();
        this.leftPanel = leftPanel;
        ctx.inject(this);

        PanelUtils.createVerticalPanel(this, "Morphological Operations", 500);

        add(Box.createVerticalStrut(10));
        


        // Preview Checkbox
        previewCheck = new JCheckBox("Preview on binary slice");
        previewCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewCheck.addActionListener(e -> triggerPreview());
        add(previewCheck);

        add(Box.createVerticalStrut(10));

        // Radio Buttons (Single Selection)
        optGroup = new ButtonGroup();
        noneRadio = createRadio("None", true); // Default selected
        erosionRadio = createRadio("Erosion (Shrink objects)", false);
        dilationRadio = createRadio("Dilation (Expand objects)", false);
        openingRadio = createRadio("Opening (Remove small noise)", false);
        closingRadio = createRadio("Closing (Fill small holes)", false);

        add(Box.createVerticalStrut(15));

        // Apply Button
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyButton = new JButton("Apply to Stack");
        applyButton.addActionListener(e -> {
        	applyMorphology();
        	leftPanel.updateWorkflowIndex(3);
        });
        buttonRow.add(applyButton);
        add(buttonRow);
    }

    private JRadioButton createRadio(String label, boolean isSelected) {
        JRadioButton rb = new JRadioButton(label, isSelected);
        rb.setAlignmentX(Component.LEFT_ALIGNMENT);
        rb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        optGroup.add(rb);
        
        rb.addActionListener(e -> handleSelectionChange());
        add(rb);
        return rb;
    }

    /**
     * Called whenever a radio button or the background checkbox is clicked.
     * Updates the logic and refreshes the preview.
     */
    private void handleSelectionChange() {
        updateServiceState();
        if (previewCheck.isSelected()) {
            triggerPreview();
        }
    }

    /**
     * Translates the UI selection into the boolean flags the service expects,
     * handling the Black/White background inversion automatically.
     */
    private void updateServiceState() {
        boolean erode = erosionRadio.isSelected();
        boolean dilate = dilationRadio.isSelected();
        boolean open = openingRadio.isSelected();
        boolean close = closingRadio.isSelected();

 
        //we need to inverse this as by default these operations think the objects are black and background is white
        //however, in our plugin we we get an binary mask where objects are white and background is black
        //that's why we invert the operatations
        service.setErosion(dilate);
        service.setDilation(erode);
        service.setOpening(close);
        service.setClosing(open);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            ImagePlus img = leftPanel.getCurrentImage();
            if (img != null) {
                // FIX: We must take the snapshot of the BINARY image, not the original image!
                String binaryTitle = img.getShortTitle() + "_Binary";
                ImagePlus binaryImp = WindowManager.getImage(binaryTitle);
                
                if (binaryImp != null) {
                    service.captureMorphologySnapshot(binaryImp);
                }
            }
        } else {
            // Turn off preview when leaving the panel
            if (previewCheck.isSelected()) {
                previewCheck.setSelected(false);
                triggerPreview();
            }
        }
    }

    private void triggerPreview() {
        ImagePlus img = leftPanel.getCurrentImage();
        if(img == null) return;
        
        String binaryTitle = img.getShortTitle() + "_Binary";
        ImagePlus binaryImp = WindowManager.getImage(binaryTitle);
        
        
        if (binaryImp == null) return;

        if (previewCheck.isSelected()) {
            service.previewMorphology(binaryImp);
        } else {
            service.resetMorphologyPreview(binaryImp);
        }
    }

    private void applyMorphology() {
        ImagePlus img = leftPanel.getCurrentImage();
        if(img == null) return;

        String binaryTitle = img.getShortTitle() + "_Binary";
        ImagePlus binaryImp = WindowManager.getImage(binaryTitle);
        
        if (binaryImp == null) return;

        previewCheck.setSelected(false);
        
        //we apply changes on the fresh binary mask,
        service.resetMorphologyPreview(binaryImp);
        isApplied = service.applyMorphology(binaryImp);
        
        if (isApplied) {
            IJ.showStatus("Morphology applied.");
            enableUIComponents(true);
            leftPanel.updateWorkflowIndex(3); 
        }
    }

    public void enableUIComponents(boolean enabled) {
        //blackBackgroundCheck.setEnabled(enabled);
        noneRadio.setEnabled(enabled);
        erosionRadio.setEnabled(enabled);
        dilationRadio.setEnabled(enabled);
        openingRadio.setEnabled(enabled);
        closingRadio.setEnabled(enabled);
        previewCheck.setEnabled(enabled);
        applyButton.setEnabled(enabled);
    }
    
    public void resetUIComponents() {
        //blackBackgroundCheck.setSelected(false); // Reset to default
        noneRadio.setSelected(true);            // Reset radio group to 'None'
        previewCheck.setSelected(false);
        updateServiceState();                   // Ensure service knows everything is false
    }
}