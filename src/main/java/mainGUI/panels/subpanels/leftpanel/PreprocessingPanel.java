package mainGUI.panels.subpanels.leftpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URL;
import java.util.concurrent.CancellationException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.YesNoCancelDialog;
import mainGUI.panels.LeftPanel;
import mainGUI.utils.InputUtils;
import mainGUI.utils.PanelUtils;
import model.AnalysisSettings;
import model.LDCService;
import net.imagej.display.ImageDisplayService;

/**
 * Creates the middle panel of the {@link LeftPanel}, containing preprocessing controls (Contrast, Median Filter).<br>
 * Includes logic for initializing the loading GIF and Cancel button.
 */
@SuppressWarnings("serial")
public class PreprocessingPanel extends JPanel{
	
	// The parent panel
	private LeftPanel leftPanel;
	
    // --- Services ---
    @Parameter
    private ImageDisplayService imageDisplayService;
    @Parameter
	private LDCService selectedSettings;
	
    // Contrast
    private JCheckBox enhanceCheckbox;
    private JSpinner enhanceSaturatedSpinner;
    private JButton enhanceSaturatedResetButton;

    // Median
    private JCheckBox medianCheckbox;
    private JCheckBox medianPreviewCheckbox;
    private JButton medianRadiusResetButton;
    private JTextField medianRadiusField; 
    private JButton applyButton;
    private JTextField medianApplyRangeField;
    private JLabel loadingLabel; 
    private JButton cancelButton; 
    
    // Reference to the currently running worker to allow cancellation
    private SwingWorker<Void, Void> currentWorker;
	
	public PreprocessingPanel(Context ctx, LeftPanel leftPanel) {
		
		// Initialization of the panel layout
		super();
		PanelUtils.createVerticalPanel(this, "Preprocessing", 300);
		
		ctx.inject(this);
		this.leftPanel = leftPanel;
		
		add(Box.createVerticalStrut(15));

	    // --- Contrast Section ---
	    enhanceCheckbox = new JCheckBox("Enhance contrast");
	    enhanceCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
	    enhanceCheckbox.setSelected(selectedSettings.enhanceContrastEnabled());
	    enhanceCheckbox.addActionListener(e -> toggleContrast());

	    enhanceSaturatedSpinner = new JSpinner(new SpinnerNumberModel(selectedSettings.getEnhanceSaturatedPercent(), 0.0, 100.0, 0.1));
	    enhanceSaturatedSpinner.setEnabled(selectedSettings.enhanceContrastEnabled());
	    enhanceSaturatedSpinner.addChangeListener(e -> {
	        selectedSettings.setEnhanceSaturatedPercent(((Number) enhanceSaturatedSpinner.getValue()).doubleValue());
	        if (enhanceCheckbox.isSelected() && !leftPanel.isProcessing()) enhanceContrast();
	    });
	    
	    enhanceSaturatedResetButton = new JButton("Reset");
	    enhanceSaturatedResetButton.setMargin(new Insets(0, 5, 0, 5)); // Compact
	    enhanceSaturatedResetButton.addActionListener(e -> resetSaturatedContrast());

	    JPanel saturatedRow = new JPanel(new BorderLayout());
	    saturatedRow.setAlignmentX(Component.LEFT_ALIGNMENT);
	    saturatedRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
	    saturatedRow.add(new JLabel("  Saturated (%): "), BorderLayout.WEST);
	    saturatedRow.add(enhanceSaturatedSpinner, BorderLayout.CENTER);
	    saturatedRow.add(enhanceSaturatedResetButton, BorderLayout.EAST);

	    add(enhanceCheckbox);
	    add(saturatedRow);

	    // --- Median Section ---
	    add(Box.createVerticalStrut(15));
	    
	    JPanel medianRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    medianRow.setAlignmentX(Component.LEFT_ALIGNMENT);
	    medianRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

	    medianCheckbox = new JCheckBox("Median filter");
	    medianCheckbox.setSelected(selectedSettings.medianFilterEnabled());
	    medianCheckbox.addActionListener(e -> {
	        if (!leftPanel.isProcessing()) toggleMedian();
	    });
	    
	    medianPreviewCheckbox = new JCheckBox("Preview");
	    medianPreviewCheckbox.setEnabled(selectedSettings.medianFilterEnabled());
	    medianPreviewCheckbox.addActionListener(e -> {
	        if (leftPanel.isProcessing()) return; 
	        performMedianPreviewAsync(
	            medianPreviewCheckbox.isSelected(), 
	            () -> {
	                if (enhanceCheckbox.isSelected()) {
	                    enhanceContrast();
	                }
	            }
	        );
	    });

	    // Loading GIF
	    URL gifUrl = getClass().getResource("/loading.gif"); 
	    if (gifUrl != null) {
	        loadingLabel = new JLabel("<html><img src='" + gifUrl + "' width='20' height='20'></html>");
	    } else {
	        loadingLabel = new JLabel("Loading...");
	        loadingLabel.setForeground(Color.RED);
	    }
	    loadingLabel.setVisible(false);
	    
	    // Cancel Button
	    cancelButton = new JButton("Cancel");
	    cancelButton.setMargin(new Insets(0, 5, 0, 5)); // Compact
	    cancelButton.setForeground(Color.RED);
	    cancelButton.setVisible(false);
	    cancelButton.addActionListener(e -> cancelCurrentTask());
	    
	    medianRow.add(medianCheckbox);
	    medianRow.add(Box.createHorizontalStrut(10));
	    medianRow.add(medianPreviewCheckbox);
	    medianRow.add(Box.createHorizontalStrut(10)); 
	    medianRow.add(loadingLabel);
	    medianRow.add(Box.createHorizontalStrut(5));
	    medianRow.add(cancelButton); 

	    // Radius Field
	    medianRadiusField = new JTextField(String.valueOf(selectedSettings.getMedianRadius()));
	    medianRadiusField.setHorizontalAlignment(SwingConstants.RIGHT); 
	    medianRadiusField.setEnabled(selectedSettings.medianFilterEnabled());
	    medianRadiusField.addActionListener(e-> {
	    	if (!leftPanel.isProcessing()) updateMedianRadiusFromField();
	    });
	    medianRadiusField.addFocusListener(new FocusAdapter() {
	        @Override
	        public void focusLost(FocusEvent e) {
	            if (!leftPanel.isProcessing()) updateMedianRadiusFromField();
	        }
	    });
	    
	    medianRadiusResetButton= new JButton("Reset");
	    medianRadiusResetButton.setMargin(new Insets(0, 5, 0, 5)); // Compact
	    medianRadiusResetButton.addActionListener(e -> resetMedianRadius());

	    JPanel radiusRow = new JPanel(new BorderLayout());
	    radiusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
	    radiusRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
	    radiusRow.add(new JLabel("  Radius (px): "), BorderLayout.WEST);
	    radiusRow.add(medianRadiusField, BorderLayout.CENTER);
	    radiusRow.add(medianRadiusResetButton, BorderLayout.EAST);

	    add(medianRow);
	    add(radiusRow);

	    // Median filter range row
	    JPanel rangeRow = new JPanel(new BorderLayout());
	    rangeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

	    rangeRow.add(new JLabel("  Range (stack slices):  "), BorderLayout.WEST);
	    medianApplyRangeField = new JTextField();
	    medianApplyRangeField.setToolTipText("Example: 1-3,5,8-10");
	    rangeRow.add(medianApplyRangeField, BorderLayout.CENTER);

	    add(Box.createVerticalStrut(10));
	    add(rangeRow);
	    
	    // Apply median filter row
	    JPanel applyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    applyRow.setAlignmentX(Component.LEFT_ALIGNMENT);
	    
	    applyButton = new JButton("Apply median filter");
	    applyButton.addActionListener(e -> {
	        if (leftPanel.isProcessing()) return;
	        applyButton.setEnabled(false);
	        runApplyLogic();
	    });
	    applyRow.add(applyButton, BorderLayout.WEST);
	    
	    add(applyRow);
	    add(Box.createVerticalStrut(10));
	    add(applyRow);
	    add(Box.createVerticalStrut(5));
	}
	
    /**
     * The main entry point for the "Apply" button logic.
     * Checks if stack processing is needed, handles preview state conflicts, 
     * and launches the appropriate worker.
     */
    private void runApplyLogic() {
    	ImagePlus img = leftPanel.updateAndGetImg();
        if (img == null) {
            IJ.showMessage("No image", "Please open an image first.");
            applyButton.setEnabled(true);
            return;
        }

        boolean processAllSlices = false;
        
        
        // We consider the new stack, according to the given ranges
        int oldStackSize = img.getStackSize();
        ImageStack oldStack = img.getImageStack();
        ImageStack newRangedStack = InputUtils.parseSliceRanges(medianApplyRangeField.getText(), oldStackSize, oldStack);
        if (newRangedStack.getSize() == 0) { // median filter processing cancelled if no images
        	applyButton.setEnabled(true);
        	IJ.showMessage("No images available.");
        	return;
        }
        
        if (selectedSettings.medianFilterEnabled() && newRangedStack.size() > 1) {
            YesNoCancelDialog d = new YesNoCancelDialog(IJ.getInstance(), "Process Stack?", 
                "Do you want to process all " + newRangedStack.size() + " images?\nThere is no undo.");
            if (d.cancelPressed()) {
                applyButton.setEnabled(true);
                return;
            }
            
            // We process all given slices
            processAllSlices = d.yesPressed();
            
            // If it is a new stack and we decided to process all given images : we update the current stack
            if (newRangedStack != img.getImageStack() && processAllSlices) {
            	img.setStack(newRangedStack);
            	img.setDimensions(img.getNChannels(), newRangedStack.getSize(), img.getNFrames());
            	img.updateAndDraw();
            }
        }

        // If applying to stack while preview is active, undo preview first
        if (processAllSlices && medianPreviewCheckbox.isSelected()) {
            medianPreviewCheckbox.setSelected(false);
            
            performMedianPreviewAsync(false, () -> {
                if (enhanceCheckbox.isSelected()) {
                     enhanceContrast();
                }
                launchApplyWorker(true, img.getCurrentSlice());
            });
            return; 
        }
        
        // If applying to current slice (same as preview), just commit changes
        if (!processAllSlices && medianPreviewCheckbox.isSelected()) {
            medianPreviewCheckbox.setSelected(false);
            img.getProcessor().snapshot(); 
            applyButton.setEnabled(true); 
            return; 
        }

        launchApplyWorker(processAllSlices, img.getCurrentSlice());
    }
    
    // =========================================================================
    // UI ACTIONS
    // =========================================================================
    
    // --- Contrast Section ---
    
    /**
     * Toggles contrast enhancement based on the checkbox state.
     */
    private void toggleContrast() {
        boolean enabled = enhanceCheckbox.isSelected();
        selectedSettings.setEnhanceContrast(enabled);
        enhanceSaturatedSpinner.setEnabled(enabled);
        enhanceSaturatedResetButton.setEnabled(enabled);
        
        ImagePlus img = leftPanel.updateAndGetImg();
        if (img != null) {
            enhanceContrast();
        }
    }
    
    /**
     * Resets contrast enhancement to its default value.
     */
    private void resetSaturatedContrast() {
    	selectedSettings.setEnhanceSaturatedPercent(AnalysisSettings.DFL_EC_SATURATED);
    	enhanceSaturatedSpinner.setValue(AnalysisSettings.DFL_EC_SATURATED);
    	
    	ImagePlus img = leftPanel.updateAndGetImg();
        if (img != null) {
            enhanceContrast();
        }
    }
    
    // --- Median Filter Section ---

    /**
     * Toggles the median filter controls.
     * If unchecked while preview was active, it resets the image logic.
     */
    private void toggleMedian() {
        boolean enabled = medianCheckbox.isSelected();
        selectedSettings.setMedianFilter(enabled);
        
        medianRadiusField.setEnabled(enabled);
        medianPreviewCheckbox.setEnabled(enabled);
        medianRadiusResetButton.setEnabled(enabled);
        medianApplyRangeField.setEnabled(enabled);

        if (!enabled && medianPreviewCheckbox.isSelected()) {
            medianPreviewCheckbox.setSelected(false);
            performMedianPreviewAsync(false, () -> {
                if (enhanceCheckbox.isSelected()) {
                    enhanceContrast();
                }
            });
        }
    }
    
    /**
     * Reads the radius value from the text field, validates it, and triggers a preview update.
     */
    private void updateMedianRadiusFromField() {
        try {
            double val = Double.parseDouble(medianRadiusField.getText());
            if (val < 0) val = 0; 
            
            selectedSettings.setMedianRadius(val);
            
            if (medianPreviewCheckbox.isSelected()) {
                performMedianPreviewAsync(true, () -> {
                    if (enhanceCheckbox.isSelected()) {
                        enhanceContrast();
                    }
                });
            }
        } catch (NumberFormatException ex) {
            medianRadiusField.setText(String.valueOf(selectedSettings.getMedianRadius()));
        }
    }
    
    /**
     * Resets median radius to its default value.
     */
    private void resetMedianRadius() {
        selectedSettings.setMedianRadius(AnalysisSettings.DFL_MEDIAN_RADIUS);
        medianRadiusField.setText(String.valueOf(AnalysisSettings.DFL_MEDIAN_RADIUS));

        if (medianCheckbox.isSelected()) {
        	updateMedianRadiusFromField();
        }
    }
    
    /**
     * Cancels the currently running SwingWorker (preview or apply), if it exists.
     * This triggers the isCancelled() check inside the worker's logic.
     */
    private void cancelCurrentTask() {
        if (currentWorker != null && !currentWorker.isDone()) {
            IJ.showStatus("Cancelling...");
            // true = interrupt the thread
            currentWorker.cancel(true);
        }
    }
    
    // =========================================================================
    // ENABLING/DISABLING UI COMPONENTS
    // =========================================================================

    /**
     * Locks or unlocks the UI elements during processing to prevent race conditions.
     * Shows/hides the loading GIF and Cancel button appropriately.
     * * @param enabled If false, inputs are disabled and loading/cancel controls are shown.
     */
    private void setPreprocessingEnabled(boolean enabled) {
        leftPanel.setProcessing(!enabled);

        // Toggle Loading/Cancel visibility
        loadingLabel.setVisible(!enabled);
        cancelButton.setVisible(!enabled); 

        // Lock inputs
        medianCheckbox.setEnabled(enabled);
        medianPreviewCheckbox.setEnabled(enabled);
        medianRadiusField.setEnabled(enabled);
        medianRadiusResetButton.setEnabled(enabled);
        medianApplyRangeField.setEnabled(enabled);
        applyButton.setEnabled(enabled && medianCheckbox.isSelected());
        
        enhanceCheckbox.setEnabled(enabled);
        enhanceSaturatedSpinner.setEnabled(enabled && enhanceCheckbox.isSelected());
        enhanceSaturatedResetButton.setEnabled(enabled);
        
        ImagePlus img = leftPanel.updateAndGetImg();
        leftPanel.setNextButtonEnabled(enabled && img != null && !this.isVisible() == false);
    }
    
    /**
     * Enables, or disables, UI components (inputs) of this preprocessing panel.<br>
     * If it disables, inputs are reseted to their default value.
     * @param enable true : enables inputs, false : disables inputs
     */
    public void enableUIComponents(boolean enable) {
    	// If components are disabled
    	if (enable == false) {
    		
    		// Components become impossible to interact with
    		
    		enhanceCheckbox.setEnabled(enable);
    		enhanceSaturatedSpinner.setEnabled(enable);
    		enhanceSaturatedResetButton.setEnabled(enable);
    		
    		medianCheckbox.setEnabled(enable);
    		medianRadiusResetButton.setEnabled(enable);
    		medianRadiusField.setEnabled(enable);
    		medianApplyRangeField.setEnabled(enable);
    		applyButton.setEnabled(enable);
    		
    		// Inputs take their original values
    		
    		selectedSettings.setEnhanceContrast(enable);
    		selectedSettings.setEnhanceSaturatedPercent(AnalysisSettings.DFL_EC_SATURATED);
    		
    		enhanceCheckbox.setSelected(enable);
    		enhanceSaturatedSpinner.setValue(AnalysisSettings.DFL_EC_SATURATED);
    		
    		selectedSettings.setMedianFilter(enable);
    		selectedSettings.setMedianRadius(AnalysisSettings.DFL_MEDIAN_RADIUS);
    		
    		medianCheckbox.setSelected(enable);
    		medianRadiusField.setText(Double.toString(AnalysisSettings.DFL_MEDIAN_RADIUS));
    		medianApplyRangeField.setText("");
    		
    	// If components are enabled
    	} else {
    		enhanceCheckbox.setEnabled(enable);
    		medianCheckbox.setEnabled(enable);
    		if (!leftPanel.isProcessing()) applyButton.setEnabled(enable);
    	}
    }
    
    // =========================================================================
    // ENHANCE CONTRAST OPERATION
    // =========================================================================

    /**
     * Applies contrast enhancement to the current image using ImageJ's ContrastEnhancer.<br>
     * If contrast enhancement is not enabled, it applies a contrast enhancement of 0%.
     */
    private void enhanceContrast() {
        if (imageDisplayService.getImageDisplays().isEmpty()) return;
        leftPanel.updateAndGetImg();
        selectedSettings.applyEnhanceContrast();
    }
    
    // =========================================================================
    // MEDIAN FILTER OPERATIONS (ASYNCHRONOUS)
    // =========================================================================

    /**
     * Asynchronously handles the Median Filter Preview logic.
     * It manages taking snapshots, resetting to clean states, and applying the filter.
     * Handles cancellation to ensure the image is reset to a clean state.
     * @param isPreviewOn Whether the preview is being turned on (apply filter) or off (reset).
     * @param onComplete  Runnable to execute after the worker finishes (e.g., re-applying contrast).
     */
    private void performMedianPreviewAsync(boolean isPreviewOn, Runnable onComplete) {
    	ImagePlus img = leftPanel.updateAndGetImg();
        if (img == null) return;

        setPreprocessingEnabled(false);
        SwingWorker<Void,Void> previewWorker = selectedSettings.createPreviewMedianWorker(img.getProcessor(), isPreviewOn);
        currentWorker = previewWorker;
        previewWorker.addPropertyChangeListener(evt -> {
        	
        	// When the worker is done
        	// i.e. when the worker "state" property becomes DONE
            if ("state".equals(evt.getPropertyName())
                    && SwingWorker.StateValue.DONE == evt.getNewValue()) {
	            try {
	                // Check if cancelled
	                if (previewWorker.isCancelled()) {
	                    // If cancelled, we MUST revert to the snapshot state
	                    // because rank() might have stopped halfway or finished but we don't want the result.
	                    img.getProcessor().reset();
	                    IJ.showStatus("Preview cancelled.");
	                    enhanceContrast(); // Re-apply contrast enhancement
	                    
	                    medianPreviewCheckbox.setSelected(false);
	                    
	                } else {
	                	previewWorker.get(); // check for exceptions
	                	if (onComplete != null) onComplete.run();
	                    IJ.showStatus("Preview updated.");
	                }
	                img.updateAndDraw();
	            } catch (CancellationException ce) {
	                img.getProcessor().reset();
	                img.updateAndDraw();
	                IJ.showStatus("Preview cancelled.");
	            } catch (Exception ex) {
	                IJ.log("Preview Error: " + ex.getMessage());
	                ex.printStackTrace();
	            } finally {
	                setPreprocessingEnabled(true);
	                currentWorker = null;
	            }
            }
        });
        previewWorker.execute();
    }
    
    /**
     * Launches a dedicated background worker to apply the filter to the image (or stack).
     * * @param doProcessAll      True if the whole stack should be processed.
     * @param currentSliceIndex The index of the current slice (if not processing all).
     */
    private void launchApplyWorker(boolean doProcessAll, int currentSliceIndex) {
        setPreprocessingEnabled(false);
        ImagePlus img = leftPanel.updateAndGetImg();
        
        SwingWorker<Void,Void> applyWorker = selectedSettings.createApplyMedianWorker(img.getStack(), doProcessAll, currentSliceIndex);
        currentWorker = applyWorker;
        applyWorker.addPropertyChangeListener(evt -> {
        	
        	// When the worker is done
        	// i.e. when the worker "state" property becomes DONE
            if ("state".equals(evt.getPropertyName())
                && SwingWorker.StateValue.DONE == evt.getNewValue()) {

                try {
                    if (applyWorker.isCancelled()) {
                    	IJ.showStatus("Apply cancelled.");
                    } else {
                    	applyWorker.get(); // exceptions
                        IJ.showStatus("Median filter preprocessing done.");
                    }
                    img.updateAndDraw();
                } catch (CancellationException ce) {
                    IJ.showStatus("Apply cancelled.");
                    img.updateAndDraw();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setPreprocessingEnabled(true);
                    currentWorker = null;
                }
            }
        });
        applyWorker.execute();
    }
}