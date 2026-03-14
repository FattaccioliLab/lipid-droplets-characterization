package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CancellationException;

import javax.swing.Box;
import javax.swing.BoxLayout;
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

import fr.sorbonne_universite.ldc.model.AnalysisSettings;
import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;
import fr.sorbonne_universite.ldc.utils.InputUtils;
import fr.sorbonne_universite.ldc.utils.PanelUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.YesNoCancelDialog;
import ij.process.ImageProcessor;
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
    private JLabel loadingLabel; 
    private JButton cancelButton;
    
    // Apply
    private JTextField applyRangeField;
    private JButton applyButton;
    
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
	    add(Box.createVerticalStrut(20));
	    
	    JPanel finalizationPanel = new JPanel();
	    finalizationPanel.setLayout(new BoxLayout(finalizationPanel, BoxLayout.Y_AXIS));
	    finalizationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    finalizationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

	    // Apply preprocessing range row
	    JPanel rangeRow = new JPanel(new BorderLayout());
	    rangeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

	    rangeRow.add(new JLabel("  Range (stack slices):  "), BorderLayout.WEST);
	    applyRangeField = new JTextField();
	    applyRangeField.setToolTipText("Example: 1-3,5,8-10");
	    rangeRow.add(applyRangeField, BorderLayout.CENTER);
	    
	    // Apply preprocessing row
	    JPanel applyRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
	    applyRow.setAlignmentX(Component.LEFT_ALIGNMENT);
	    
	    applyButton = new JButton("Apply preprocessing parameters");
	    applyButton.addActionListener(e -> {
	        if (leftPanel.isProcessing()) return;
	        applyButton.setEnabled(false);
	        runApplyLogic();
	    });
	    applyRow.add(applyButton, BorderLayout.WEST);
	    
	    finalizationPanel.add(rangeRow);
	    finalizationPanel.add(Box.createVerticalStrut(10));
	    finalizationPanel.add(applyRow);
	    //finalizationPanel.add(Box.createVerticalStrut(10));
	    add(finalizationPanel);
	    
	    add(Box.createVerticalStrut(10));
	    
	    // UI components disabled at start
	    enableUIComponents(false, false);
	}
	
    /**
     * The main entry point for the "Apply preprocessing parameters" button logic.
     * 
     * <p>
     * Checks if sub stack processing is needed, and if we process all slices or only the current. Then :
     * <li>Either applies the median filter by handling preview state conflicts, and launching the median filter worker.
     * <li>Otherwise eventually enhances contrast.
     * </p>
     * 
     * <p>
     * In each case, we work on a copy of the original image, before replacing the current image by this copy.
     * </p>
     */
    private void runApplyLogic() {
    	ImagePlus img = leftPanel.getCurrentImage();
        if (img == null) {
            IJ.showMessage("No image", "Please open an image first.");
            applyButton.setEnabled(true);
            return;
        }
        
        boolean processAllSlices = false;
        Set<Integer> slices = null;
        int nbSlices = img.getStackSize();
        
        // We consider the new slices, according to the given ranges if given
        if (!applyRangeField.getText().isEmpty()) {
        	slices = InputUtils.parseSliceRangeToSet(applyRangeField.getText(), nbSlices);
        	nbSlices = slices.size();
        }
        // if slices == null : we don't use any sub stack, we use a copy of the original entire stack
        
        if (nbSlices == 0) { // median filter processing cancelled if no images
        	applyButton.setEnabled(true);
        	IJ.showMessage("No slices available.");
        	return;
        }
        
        // We ask if we must process all slices
        YesNoCancelDialog d = new YesNoCancelDialog(IJ.getInstance(), "Process Stack?", 
        		"Do you want to process all " + nbSlices + " images ?\nIf 'No' only the current slice will be processed.\nThere is no undo.");
        if (d.cancelPressed()) {
        	applyButton.setEnabled(true);
        	return;
        }
        // We (might) process all given slices
        processAllSlices = d.yesPressed();
        
        // If median filter is enabled : 
        // Start from the original image's copy : applies eventually enhance contrast + applies median filter + locks preprocessing UI at the end
        // (all of that is done in the 'launchApplyWorker' method)
        if (selectedSettings.medianFilterEnabled()) {
	
	        // If applying to stack while preview is active, undo preview first
	        if (processAllSlices && medianPreviewCheckbox.isSelected()) {
	        	medianPreviewCheckbox.setSelected(false);
	        	performMedianPreviewAsync(); // reset the preview, synchronously in that case
	        }
	
	        // The new stack range will be considered inside the called method
	        launchApplyWorker(processAllSlices, img.getCurrentSlice(), slices);
        
	    // Else :
	    // Start from the original image's copy : applies eventually enhance contrast + locks preprocessing UI here
        } else {
        	
        	ImagePlus copy = leftPanel.getOriginalImage().duplicate();
        	ImageProcessor ipCopy = copy.getProcessor();
        	
        	if (selectedSettings.enhanceContrastEnabled()) selectedSettings.applyEnhanceContrast(ipCopy);
        	img.setProcessor(ipCopy);
        	
        	// The new stack range considered (if we must)
        	if (slices != null) img.setStack(InputUtils.buildStackFromSlices(slices, copy.getImageStack()));
        	
        	leftPanel.updateUIInfosNbSlices();
        	leftPanel.setPreprocessingDone(true);
        	leftPanel.updateWorkflowIndex(1);
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
        	ImagePlus img = leftPanel.getCurrentImage();
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
        applyRangeField.setEnabled(enabled);
        applyButton.setEnabled(enabled && medianCheckbox.isSelected());
        
        enhanceCheckbox.setEnabled(enabled);
        enhanceSaturatedSpinner.setEnabled(enabled && enhanceCheckbox.isSelected());
        enhanceSaturatedResetButton.setEnabled(enabled && enhanceCheckbox.isSelected());
        
        // Disables "Next" button during preprocessing (enabled = false)
        // Enables "Next" button once preprocessing is done (enabled = true)
        ImagePlus img = leftPanel.getCurrentImage();
        leftPanel.getFooterLeftPanel().setNextButtonEnabled(enabled && img != null && !this.isVisible() == false);
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
    		applyRangeField.setEnabled(enable);
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
        		applyRangeField.setText("");
    		}
    		
    	// If components are enabled
    	} else {
    		enhanceCheckbox.setEnabled(enable);
    		medianCheckbox.setEnabled(enable);
    		applyRangeField.setEnabled(enable);
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
        ImagePlus img = leftPanel.getCurrentImage();
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
    	ImagePlus img = leftPanel.getCurrentImage();
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
     * Launches a dedicated background worker to apply the filter to the image (= stack).<br>
     * If a set of {@code slices} is given, it considers them for the current image.
     * @param doProcessAll      True if the whole stack should be processed.
     * @param currentSliceIndex The index of the current slice (if not processing all).
     * @param slices    		Slices associated to a sub-stack of the current image's stack to consider. If {@code null}, the original stack is considered.
     */
    private void launchApplyWorker(boolean doProcessAll, int currentSliceIndex, Set<Integer> slices) {
        setPreprocessingEnabled(false);
        ImagePlus img = leftPanel.getCurrentImage();
        
        // save
    	lastImageStackBeforeMedian = img.getStack().duplicate();
        
        // We try to apply on an independent copy. 
        // If it works out, we replace the current's image Stack with this images's modified ImageStack.
    	ImagePlus toProcess = leftPanel.getOriginalImage().duplicate();
    	if (slices != null) { 
    		// 'toProcess' ImageStack corresponds to a sub-stack of the original ImageStack.
    		toProcess.setStack(InputUtils.buildStackFromSlices(slices, toProcess.getImageStack()));
    	}
        
        // There is no enhance contrast on the original ImageProcessor, so we apply it if needed.
        if (selectedSettings.enhanceContrastEnabled()) selectedSettings.applyEnhanceContrast(toProcess.getProcessor());
        
        SwingWorker<Void,Void> applyWorker = selectedSettings.createApplyMedianWorker(toProcess.getImageStack(), doProcessAll, currentSliceIndex);
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
	                	img.setStack(toProcess.getImageStack()); // Applies changes to the current image
	                	img.updateAndDraw();
	                	leftPanel.updateUIInfosNbSlices();
                        IJ.showStatus("Median filter preprocessing done.");
                        setPreprocessingEnabled(true);
                        leftPanel.setPreprocessingDone(true);
                        leftPanel.updateWorkflowIndex(1);
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