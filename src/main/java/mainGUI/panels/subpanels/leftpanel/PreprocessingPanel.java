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
import ij.process.ImageProcessor;
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
    
    // Reference to the last ImageStack before a median preview / apply.
    ImageStack lastImageStackBeforeMedian = null;
	
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
	        performMedianPreviewAsync();
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
	    
	    applyButton = new JButton("Apply preprocessing parameters");
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
     * The main entry point for the "Apply preprocessing parameters" button logic.
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
        
        // If median filter is enabled : applies eventually enhance contrast + applies median filter + locks preprocessing UI at the end
        if (selectedSettings.medianFilterEnabled()) {
	
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
	            
	            // If the newRangedStack is equal to the original (old) stack, then there is no newRangedStack.
	            if (newRangedStack == img.getImageStack()) {
	    	        newRangedStack = null; 
	            }
	        }
	
	        // If applying to stack while preview is active, undo preview first
	        if (processAllSlices && medianPreviewCheckbox.isSelected()) {
	        	medianPreviewCheckbox.setSelected(false);
	        	performMedianPreviewAsync(); // reset the preview, synchronously in that case
	        }
	
	        launchApplyWorker(processAllSlices, img.getCurrentSlice(), newRangedStack);
        
	    // Else : don't change actual image (which can be contrast enhanced or unmodified) + locks preprocessing UI here
        } else {
        	leftPanel.setPreprocessingDone(true);
        }
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
        
        // If the option is selected, we just enhance the constrast
        if (enabled) {
        	enhanceContrast();
        } else {
        	
        	// otherwise, we replace the current ImageStack with the original stack
        	ImagePlus img = leftPanel.updateAndGetImg();
        	img.setStack(leftPanel.getOriginalImage().getStack().duplicate());
        	img.updateAndDraw();
        	
        	// And we apply the median filter preview, if selected
        	if (selectedSettings.medianFilterEnabled() && medianPreviewCheckbox.isSelected()) {
        		performMedianPreviewAsync();
        	}
        }
        
    }
    
    /**
     * Resets contrast enhancement to its default value.
     */
    private void resetSaturatedContrast() {
    	selectedSettings.setEnhanceSaturatedPercent(AnalysisSettings.DFL_EC_SATURATED);
    	enhanceSaturatedSpinner.setValue(AnalysisSettings.DFL_EC_SATURATED);
    	toggleContrast();
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
            performMedianPreviewAsync(); // reset median filter preview
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
            
            // If median filter preview is enabled
            if (selectedSettings.medianFilterEnabled() && medianPreviewCheckbox.isSelected()) {
            	// reset median preview first
            	lastImageStackBeforeMedian = null;
            	medianPreviewCheckbox.setSelected(false);
            	performMedianPreviewAsync();
            	
            	// then re-apply apply median filter
            	medianPreviewCheckbox.setSelected(true);
            	performMedianPreviewAsync();
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

        if (selectedSettings.medianFilterEnabled()) {
        	updateMedianRadiusFromField();
        }
    }
    
    /**
     * Cancels the currently running SwingWorker (preview or apply), if it exists.
     * This triggers the isCancelled() check inside the worker's logic.
     */
    private void cancelCurrentTask() {
        if (currentWorker != null && !currentWorker.isDone()) {
            IJ.showStatus("Cancelling current work...");
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
     * @param enabled If false, inputs are disabled and loading/cancel controls are shown.
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
        enhanceSaturatedResetButton.setEnabled(enabled && enhanceCheckbox.isSelected());
        
        ImagePlus img = leftPanel.updateAndGetImg();
        leftPanel.setNextButtonEnabled(enabled && img != null && !this.isVisible() == false);
    }
    
    /**
     * Enables, or disables, UI components (inputs) of this preprocessing panel.<br>
     * If it disables, inputs can be reseted to their default value.
     * @param enable true : enables inputs, false : disables inputs
     * @param resetParameters true : reset parameters. Taken into account ONLY if {@code enable} == {@code false}.
     */
    public void enableUIComponents(boolean enable, boolean resetParameters) {
    	
    	if (leftPanel.isPreprocessingDone()) enable = false;
    	
    	// If components are disabled
    	if (enable == false) {
    		
    		// Components become impossible to interact with
    		
    		enhanceCheckbox.setEnabled(enable);
    		enhanceSaturatedSpinner.setEnabled(enable);
    		enhanceSaturatedResetButton.setEnabled(enable);
    		
    		medianCheckbox.setEnabled(enable);
    		medianPreviewCheckbox.setEnabled(enable);
    		medianRadiusResetButton.setEnabled(enable);
    		medianRadiusField.setEnabled(enable);
    		medianApplyRangeField.setEnabled(enable);
    		applyButton.setEnabled(enable);
    		
    		// Inputs take their original values
    		
    		if (resetParameters) {
    			selectedSettings.setEnhanceContrast(enable);
        		selectedSettings.setEnhanceSaturatedPercent(AnalysisSettings.DFL_EC_SATURATED);
        		
        		enhanceCheckbox.setSelected(enable);
        		enhanceSaturatedSpinner.setValue(AnalysisSettings.DFL_EC_SATURATED);
        		
        		selectedSettings.setMedianFilter(enable);
        		selectedSettings.setMedianRadius(AnalysisSettings.DFL_MEDIAN_RADIUS);
        		
        		medianCheckbox.setSelected(enable);
        		medianPreviewCheckbox.setSelected(enable);
        		medianRadiusField.setText(Double.toString(AnalysisSettings.DFL_MEDIAN_RADIUS));
        		medianApplyRangeField.setText("");
    		}
    		
    	// If components are enabled
    	} else {
    		enhanceCheckbox.setEnabled(enable);
    		medianCheckbox.setEnabled(enable);
    		if (!leftPanel.isProcessing()) applyButton.setEnabled(enable);
    	}
    }
    
    /**
     * Reset the pre-processing panel UI components, for when the image is reseted.
     * */
    public void resetUIComponents() {
    	leftPanel.setPreprocessingDone(false);
    	enableUIComponents(false, true); // disables UI components
    	enableUIComponents(true, true); // enable ONLY enhance contrast and median filter checkboxes
    }
    
    // =========================================================================
    // ENHANCE CONTRAST OPERATION
    // =========================================================================

    /**
     * Try to apply contrast enhancement to the current image using ImageJ's ContrastEnhancer<br>
     * LDC service applies the enhancement only if the option is set, within it.
     */
    private void enhanceContrast() {
        ImagePlus img = leftPanel.updateAndGetImg();
        if (img == null) return;
        selectedSettings.applyEnhanceContrast(img.getProcessor()); // If the option is not enabled, does nothing.
        img.updateAndDraw();
    }
    
    // =========================================================================
    // MEDIAN FILTER OPERATIONS (ASYNCHRONOUS)
    // =========================================================================
    
    /**
     * Handles the Median Filter preview logic, called after each toggle of the median filter preview checkbox.
     *
     * <p>
     * If called after unselecting the median preview toggle, it restores the previous {@link ImageStack} state from before the preview (synchronously).
     * </p>
     *
     * <p>
     * If called after selecting the median preview toggle, it saves the current {@link ImageStack} before launching 
     * the preview asynchronously.<br>
     * Cancellation is handled to ensure the image is restored to a clean state.
     * </p>
     */
    private void performMedianPreviewAsync() {
    	ImagePlus img = leftPanel.updateAndGetImg();
        if (img == null) return;
    	
        // Case when restoring from before the preview, synchronously
    	if (!medianPreviewCheckbox.isSelected()) {
    		// If there is an ImageStack saved before a preview, restore it and end
    		if (lastImageStackBeforeMedian != null) {
    			img.setStack(lastImageStackBeforeMedian);
    			enhanceContrast();
    			return;
    		// If there was no ImageStack saved before preview, perform median preview since original stack
    		} else {
    			img.setStack(leftPanel.getOriginalImage().getStack().duplicate());
    		}
    	}
    	
    	// save
    	lastImageStackBeforeMedian = img.getStack().duplicate();
        
        // We try to do a preview on an independent copy. 
        // If it works out, we replace the current's image Processor with this modified ImageProcessor.
        // 'toProcess' is the ImageProcessor corresponding in the original ImageStack.
        ImageProcessor toProcess = leftPanel.getOriginalImage().getStack().getProcessor(img.getCurrentSlice()).duplicate();
        // There is no enhance contrast on the original ImageProcessor, so we apply it if needed.
        if (selectedSettings.enhanceContrastEnabled()) selectedSettings.applyEnhanceContrast(toProcess);

        setPreprocessingEnabled(false);
        
        SwingWorker<Void,Void> previewWorker = selectedSettings.createPreviewMedianWorker(toProcess);
        currentWorker = previewWorker;
        previewWorker.addPropertyChangeListener(evt -> {
        	
        	// When the worker is done
        	// i.e. when the worker "state" property becomes DONE
            if ("state".equals(evt.getPropertyName())
                    && SwingWorker.StateValue.DONE == evt.getNewValue()) {
            	
	            try {
	                // Check if CANCELLED
	                if (previewWorker.isCancelled()) {
	                	IJ.showStatus("Median preview cancelled.");
	                    medianPreviewCheckbox.setSelected(false);
	                // if it went OK
	                } else {
	                	previewWorker.get(); // check for exceptions
	                	img.setProcessor(toProcess); // Applies changes to the current image
	                	img.updateAndDraw();
	                    IJ.showStatus("Preview updated.");
	                }
	                
	            } catch (CancellationException ce) {
	            	IJ.showStatus("Median preview cancelled.");
                    medianPreviewCheckbox.setSelected(false);
	            } catch (Exception e) {
	            	IJ.showStatus("Median preview cancelled.");
	                IJ.log("Preview Error: " + e.getMessage());
	                e.printStackTrace();
                    medianPreviewCheckbox.setSelected(false);
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
     * @param doProcessAll      True if the whole stack should be processed.
     * @param currentSliceIndex The index of the current slice (if not processing all).
     */
    private void launchApplyWorker(boolean doProcessAll, int currentSliceIndex) {
    	launchApplyWorker(doProcessAll, currentSliceIndex, null);
    }
    
    /**
     * Launches a dedicated background worker to apply the filter to the image (or stack).<br>
     * If a {@code newRangedStack} is given, it considers it.
     * @param doProcessAll      True if the whole stack should be processed.
     * @param currentSliceIndex The index of the current slice (if not processing all).
     * @param newRangedStack    A sub-stack of the current image's stack to consider. If {@code null}, the original stack is considered.
     */
    private void launchApplyWorker(boolean doProcessAll, int currentSliceIndex, ImageStack newRangedStack) {
        setPreprocessingEnabled(false);
        ImagePlus img = leftPanel.updateAndGetImg();
        
        // save
    	lastImageStackBeforeMedian = img.getStack().duplicate();
        
        // We try to apply on an independent copy. 
        // If it works out, we replace the current's image Stack with this modified ImageStack.
    	ImageStack toProcess;
    	if (newRangedStack == null) {
    		// 'toProcess' is the ImageStack corresponding in the original ImageStack.
            toProcess = leftPanel.getOriginalImage().getStack().duplicate();
    	} else {
    		// 'toProcess' is a given ImageStack, corresponding to a sub-stack of the original ImageStack.
    		toProcess = newRangedStack.duplicate();
    	}
        
        
        // There is no enhance contrast on the original ImageProcessor, so we apply it if needed.
        if (selectedSettings.enhanceContrastEnabled()) selectedSettings.applyEnhanceContrast(toProcess.getProcessor(currentSliceIndex));
    	
        SwingWorker<Void,Void> applyWorker = selectedSettings.createApplyMedianWorker(toProcess, doProcessAll, currentSliceIndex);
        currentWorker = applyWorker;
        applyWorker.addPropertyChangeListener(evt -> {
        	
        	// When the worker is done
        	// i.e. when the worker "state" property becomes DONE
            if ("state".equals(evt.getPropertyName())
                && SwingWorker.StateValue.DONE == evt.getNewValue()) {

                try {
                	// Check if CANCELLED
                    if (applyWorker.isCancelled()) {
                    	IJ.showStatus("Median filter cancelled.");
                    	setPreprocessingEnabled(true);
                    // if it went OK
                    } else {
                    	applyWorker.get(); // exceptions
	                	img.setStack(toProcess); // Applies changes to the current image
	                	img.updateAndDraw();
	                	leftPanel.updateUIInfosNbSlices();
                        IJ.showStatus("Median filter preprocessing done.");
                        setPreprocessingEnabled(true);
                        leftPanel.setPreprocessingDone(true);
                    }

                } catch (CancellationException ce) {
                    IJ.showStatus("Median filter cancelled.");
                } catch (Exception e) {
                	IJ.showStatus("Median filter cancelled.");
	                IJ.log("Preview Error: " + e.getMessage());
	                e.printStackTrace();
                } finally {
                    currentWorker = null;
                }
            }
        });
        applyWorker.execute();
    }

}