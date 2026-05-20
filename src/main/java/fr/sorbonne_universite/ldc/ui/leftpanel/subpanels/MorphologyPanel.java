package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.MainGUI_LDC;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;
import fr.sorbonne_universite.ldc.ui.leftpanel.PipelineSubPanel;
import fr.sorbonne_universite.ldc.utils.PanelUtils;
import ij.IJ;
import ij.ImagePlus;



/**
 * Panel for the Binary Mask Refinement step of the pipeline.
 *
 * <p>
 * Allows the user to optionally apply one morphological operation
 * (Erode, Dilate, Open, Close) and/or a Watershed transform to the binary mask
 * generated during the thresholding step.
 * </p>
 *
 * <p>
 * A live preview is available via the Preview checkbox, which applies
 * the selected operation across the entire stack non-destructively.
 * The final Apply button commits the changes permanently.
 * </p>
 *
 * @see fr.sorbonne_universite.ldc.model.leftpanel.MorphologyManager
 */
@SuppressWarnings("serial")
public class MorphologyPanel extends JPanel implements PipelineSubPanel {

    private LeftPanel leftPanel;

    @Parameter
    private LDCService ldc;
    
    private boolean isApplied = false;


    // Components
    private JCheckBox previewCheck;
    private JCheckBox watershedCheck;
    private JButton applyButton;
    
    // Radio Buttons for mutually exclusive selection
    private ButtonGroup optGroup;
    private JRadioButton noneRadio, erosionRadio, dilationRadio, openingRadio, closingRadio;
    
    
    /**
     * Constructs the {@code MorphologyPanel}, initializing all UI components
     * and wiring their listeners to the corresponding logic methods.
     *
     * @param ctx       The SciJava {@link Context}, used to inject plugin dependencies.
     * @param leftPanel The parent {@link LeftPanel}, used to access the binary mask
     *                  and to notify the pipeline of state changes.
     */
    public MorphologyPanel(Context ctx, LeftPanel leftPanel) {
        super();
        this.leftPanel = leftPanel;
        ctx.inject(this);

        PanelUtils.createVerticalPanel(this, "Binary Mask Refinement", 500);

        add(Box.createVerticalStrut(10));

        // Preview Checkbox
        previewCheck = new JCheckBox("Preview");
        previewCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewCheck.addActionListener(e -> triggerPreview());
        add(previewCheck);

        add(Box.createVerticalStrut(10));
        
        // ── Section divider: Morphological Operations────────────────────────────────────────
        add(createSectionDivider("Morphological Operations"));
        add(Box.createVerticalStrut(4));
        
        // Radio Buttons (Single Selection)
        optGroup = new ButtonGroup();
        noneRadio = createRadio("None", true); // Default selected
        erosionRadio = createRadio("Erode (Shrink objects)", false);
        dilationRadio = createRadio("Dilate (Expand objects)", false);
        openingRadio = createRadio("Open (Remove small noise)", false);
        closingRadio = createRadio("Close (Fill small holes)", false);

        add(Box.createVerticalStrut(10));
        
        // ── Section divider: Watershed ────────────────────────────────────────
        add(createSectionDivider("Watershed"));
        add(Box.createVerticalStrut(4));
        
        watershedCheck = new JCheckBox("Apply Watershed (separate touching objects)");
        watershedCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        watershedCheck.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        watershedCheck.addActionListener(e -> {
            updateServiceState();
            if (previewCheck.isSelected()) triggerPreview();
        });
        add(watershedCheck);

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

    
    /**
     * Creates a {@link JRadioButton}, registers it in the {@link ButtonGroup},
     * adds it to this panel, and wires its action listener to {@link #handleSelectionChange()}.
     *
     * @param label      The display label of the radio button.
     * @param isSelected {@code true} if this radio button should be selected by default.
     * @return           The configured {@link JRadioButton}.
     */
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
     * Called whenever a radio button is clicked.
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
        boolean watershed = watershedCheck.isSelected();

        String morphologicalOperation = erode? "Erode" : dilate ? "Dilate" : open? "Open" :  close? "Close" : "None";
        ldc.setMorphologicalOperation(morphologicalOperation);
        ldc.setWatershed(watershed);

    }
    
    /**
     * Creates a flat section divider: a label with a horizontal line to its right.
     * Replaces nested titled border boxes.
     */
    private JPanel createSectionDivider(String title) {
        JPanel divider = new JPanel();
        divider.setLayout(new BoxLayout(divider, BoxLayout.X_AXIS));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel label = new JLabel(title);
        label.setForeground(Color.GRAY);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));

        divider.add(label);
        divider.add(Box.createHorizontalStrut(6));
        divider.add(sep);

        return divider;
    }

    /**
     * Called when this panel becomes visible or hidden in the pipeline navigation.
     *
     * <p>When made visible, captures a snapshot of the current binary mask as the
     * clean baseline for non-destructive preview operations.</p>
     *
     * <p>When hidden, automatically deselects the preview checkbox and resets
     * the mask to its clean state to avoid leaving preview artifacts.</p>
     *
     * @param aFlag {@code true} to make the panel visible, {@code false} to hide it.
     */
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            ImagePlus img = leftPanel.getCurrentImage();
            if (img != null) {
                // FIX: We must take the snapshot of the BINARY image, not the original image!
            	ImagePlus binaryImp = leftPanel.getMask();
                
                if (binaryImp != null) {
                	ldc.captureMorphologySnapshot(binaryImp);
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
    

    /**
     * Triggers or resets the morphological preview on the binary mask,
     * depending on the current state of the preview checkbox.
     *
     * <p>If the preview checkbox is selected, applies the currently selected
     * operation (and watershed if checked) across the entire stack via
     * {@link LDCService#previewMorphology(ImagePlus)}.</p>
     *
     * <p>If deselected, restores the mask to its clean baseline via
     * {@link LDCService#resetMorphologyPreview(ImagePlus)}.</p>
     */
    private void triggerPreview() {
        ImagePlus img = leftPanel.getCurrentImage();
        if(img == null) return;
        
        ImagePlus binaryImp = leftPanel.getMask();
        
        if (binaryImp == null) return;

        if (previewCheck.isSelected()) {
        	ldc.previewMorphology(binaryImp);
        } else {
        	ldc.resetMorphologyPreview(binaryImp);
        }
    }

    
    /**
     * Permanently applies the selected morphological operation and optional watershed
     * to the entire binary mask stack, then advances the workflow to the
     * particle analysis parameters step.
     *
     * <p>Resets the preview before applying to ensure the operation is performed
     * on the clean baseline, not on a previously previewed state.</p>
     */
    private void applyMorphology() {
        ImagePlus img = leftPanel.getCurrentImage();
        if(img == null) return;

        ImagePlus binaryImp = leftPanel.getMask();
        
        if (binaryImp == null) return;

        previewCheck.setSelected(false);
        
        //we apply changes on the fresh binary mask,
        ldc.resetMorphologyPreview(binaryImp);
        isApplied = ldc.applyMorphology(binaryImp);
        
        if (isApplied) {
            IJ.showStatus("Morphology applied.");
            leftPanel.updateWorkflowIndex(MainGUI_LDC.ANALYSIS_PARAMETERS_STEP);
        }
    }
    
    // =========================================================================
    // ENABLING / DISABLING UI COMPONENTS
    // =========================================================================
    
    /**
     * Enables or disables all interactive UI components of this panel.
     *
     * @param enabled {@code true} to enable components, {@code false} to disable them.
     */
    @Override
    public void enableUIComponents(boolean enabled) {
        noneRadio.setEnabled(enabled);
        erosionRadio.setEnabled(enabled);
        dilationRadio.setEnabled(enabled);
        openingRadio.setEnabled(enabled);
        closingRadio.setEnabled(enabled);
        previewCheck.setEnabled(enabled);
        watershedCheck.setEnabled(enabled);
        applyButton.setEnabled(enabled);
    }
    
    // =========================================================================
    // ON IMAGE RESET
    // =========================================================================
    
    /**
     * Resets all UI components to their default values: operation set to None,
     * watershed unchecked, and preview unchecked.
     *
     * <p>Called when the image is reset or replaced in the pipeline.</p>
     */
    @Override
    public void resetUIComponents() {
        noneRadio.setSelected(true);            // Reset radio group to 'None'
        previewCheck.setSelected(false);
        updateServiceState();                   // Ensure service knows everything is false
    }
    
    // =========================================================================
    // ON NEW PARAMETERS IMPORT
    // =========================================================================
    
    /**
     * Synchronizes all UI components with the current plugin parameters,
     * restoring the selected morphological operation and watershed checkbox state.
     *
     * <p>Called after a JSON parameter import, before {@link #applyUIWithParams()}.</p>
     */
	@Override
	public void syncUIWithParams() {
		switch (ldc.getMorphologicalOperation()) {
			case "None":
				optGroup.setSelected(noneRadio.getModel(), true);
				break;
			case "Erode":
				optGroup.setSelected(erosionRadio.getModel(), true);
				break;
			case "Dilate":
				optGroup.setSelected(dilationRadio.getModel(), true);
				break;
			case "Open":
				optGroup.setSelected(openingRadio.getModel(), true);
				break;
			case "Close":
				optGroup.setSelected(closingRadio.getModel(), true);
				break;
			default:
				System.err.println("Unknown binary mask operation "+ldc.getMorphologicalOperation());
		}
        watershedCheck.setSelected(ldc.watershedEnabled());
	}

	/**
	 * Applies the current morphological parameters to the binary mask
	 * by triggering the apply logic.
	 *
	 * <p>Used during parameter import to replay the morphological step.</p>
	 */
	@Override
	public void applyUIWithParams() {
		applyMorphology();
	}
}