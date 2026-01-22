package mainGUI.panels.subpanels.leftpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
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
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import mainGUI.panels.LeftPanel;
import mainGUI.utils.PanelUtils;
import model.AnalysisSettings;
import net.imagej.display.ImageDisplayService;

/**
 * Creates the middle panel of the LeftPanel, containing preprocessing controls (Contrast, Median Filter).
 * Includes logic for initializing the loading GIF and Cancel button.
 */
@SuppressWarnings("serial")
public class PreprocessingPanel extends JPanel{
	
    // --- Services & Models ---
    @Parameter
    private ImageDisplayService imageDisplayService;
	private AnalysisSettings selectedSettings;
	private LeftPanel leftPanel;
	private final ContrastEnhancer ce;
	
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
    private JLabel loadingLabel; 
    private JButton cancelButton; 
    
    // Reference to the currently running worker to allow cancellation
    private SwingWorker<Void, Void> currentWorker;
	
	public PreprocessingPanel(Context ctx, AnalysisSettings selectedSettings, LeftPanel leftPanel) {
		
		// Initialization of the panel layout
		super();
		PanelUtils.createVerticalPanel(this, "Preprocessing", 300);
		
		ctx.inject(this);
		this.selectedSettings = selectedSettings;
		this.leftPanel = leftPanel;
		this.ce = new ContrastEnhancer();

	    // --- Contrast Section ---
	    enhanceCheckbox = new JCheckBox("Enhance contrast");
	    enhanceCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
	    enhanceCheckbox.setSelected(selectedSettings.enhanceContrastEnabled());
	    enhanceCheckbox.addActionListener(e -> toggleContrast());

	    enhanceSaturatedSpinner = new JSpinner(new SpinnerNumberModel(selectedSettings.getEnhanceSaturatedPercent(), 0.0, 100.0, 0.1));
	    enhanceSaturatedSpinner.setEnabled(selectedSettings.enhanceContrastEnabled());
	    enhanceSaturatedSpinner.addChangeListener(e -> {
	        selectedSettings.setEnhanceSaturatedPercent(((Number) enhanceSaturatedSpinner.getValue()).doubleValue());
	        if (enhanceCheckbox.isSelected() && !leftPanel.isProcessing()) enhanceContrast(selectedSettings.getEnhanceSaturatedPercent());
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
	    add(Box.createVerticalStrut(8));

	    // --- Median Section ---
	    JPanel medianRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    medianRow.setAlignmentX(Component.LEFT_ALIGNMENT);
	    medianRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

	    medianCheckbox = new JCheckBox("Median filter");
	    medianCheckbox.setSelected(selectedSettings.medianFilterEnabled());
	    
	    medianPreviewCheckbox = new JCheckBox("Preview");
	    medianPreviewCheckbox.setEnabled(selectedSettings.medianFilterEnabled());

	    // Loading GIF
	    URL gifUrl = getClass().getResource("/loading.gif"); 
	    if (gifUrl != null) {
	        loadingLabel = new JLabel("<html><img src='" + gifUrl + "' width='20' height='20'></html>");
	    } else {
	        loadingLabel = new JLabel("Loading...");
	        loadingLabel.setForeground(Color.RED);
	    }
	    loadingLabel.setVisible(false);
	    
	    // Cancel Button (Small Red 'X' or text)
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
	    
	    ActionListener updateRadiusAction = e -> {
	        if (!leftPanel.isProcessing()) updateMedianRadiusFromField();
	    };
	    medianRadiusField.addActionListener(updateRadiusAction);
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

	    // Logic Connections
	    medianCheckbox.addActionListener(e -> {
	        if (!leftPanel.isProcessing()) toggleMedian();
	    });
	    
	    medianPreviewCheckbox.addActionListener(e -> {
	        if (leftPanel.isProcessing()) return; 
	        performMedianPreviewAsync(
	            medianPreviewCheckbox.isSelected(), 
	            () -> {
	                if (enhanceCheckbox.isSelected()) {
	                    enhanceContrast(selectedSettings.getEnhanceSaturatedPercent());
	                }
	            }
	        );
	    });

	    add(medianRow);
	    add(radiusRow);

	    // Apply Button
	    JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
	    applyButton = new JButton("Apply");
	    applyButton.addActionListener(e -> {
	        if (leftPanel.isProcessing()) return;
	        applyButton.setEnabled(false);
	        runApplyLogic();
	        leftPanel.setApplicable(false);
	    });

	    buttonRow.add(applyButton);
	    
	    add(Box.createVerticalStrut(10));
	    add(buttonRow);
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

        if (selectedSettings.medianFilterEnabled() && img.getStackSize() > 1) {
            YesNoCancelDialog d = new YesNoCancelDialog(IJ.getInstance(), "Process Stack?", 
                "Do you want to process all " + img.getStackSize() + " images?\nThere is no undo.");
            if (d.cancelPressed()) {
                applyButton.setEnabled(true);
                return;
            }
            processAllSlices = d.yesPressed();
        }

        // If applying to stack while preview is active, undo preview first
        if (processAllSlices && medianPreviewCheckbox.isSelected()) {
            medianPreviewCheckbox.setSelected(false);
            
            performMedianPreviewAsync(false, () -> {
                if (enhanceCheckbox.isSelected()) {
                     enhanceContrast(selectedSettings.getEnhanceSaturatedPercent());
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
    // LOGIC & ACTIONS
    // =========================================================================

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
        applyButton.setEnabled(enabled && medianCheckbox.isSelected());
        
        enhanceCheckbox.setEnabled(enabled);
        enhanceSaturatedSpinner.setEnabled(enabled && enhanceCheckbox.isSelected());
        enhanceSaturatedResetButton.setEnabled(enabled);
        
        ImagePlus img = leftPanel.updateAndGetImg();
        leftPanel.setNextButtonEnabled(enabled && img != null && !this.isVisible() == false);
    }
    
    /**
     * Toggles contrast enhancement based on the checkbox state.
     */
    private void toggleContrast() {
        boolean enabled = enhanceCheckbox.isSelected();
        selectedSettings.setEnhanceContrast(enabled);
        enhanceSaturatedSpinner.setEnabled(enabled);
        
        ImagePlus img = leftPanel.updateAndGetImg();
        if (img != null) {
            enhanceContrast(enabled ? selectedSettings.getEnhanceSaturatedPercent() : 0);
        }
    }
    
    /**
     * Resets contrast enhancement to its default value.
     */
    private void resetSaturatedContrast() {
    	enhanceSaturatedSpinner.setValue(AnalysisSettings.DFL_EC_SATURATED);
    	boolean enabled = enhanceCheckbox.isSelected();
    	
    	ImagePlus img = leftPanel.updateAndGetImg();
        if (img != null) {
            enhanceContrast(enabled ? selectedSettings.getEnhanceSaturatedPercent() : 0);
        }
    }

    /**
     * Applies contrast enhancement to the current image using ImageJ's ContrastEnhancer.
     * * @param saturated The percentage of pixels to saturate.
     */
    private void enhanceContrast(double saturated) {
        if (imageDisplayService.getImageDisplays().isEmpty()) return;
        ImagePlus imp = IJ.getImage();
        ce.stretchHistogram(imp, saturated);
        imp.updateAndDraw();
    }

    /**
     * Toggles the median filter controls.
     * If unchecked while preview was active, it resets the image logic.
     */
    private void toggleMedian() {
        boolean enabled = medianCheckbox.isSelected();
        selectedSettings.setMedianFilter(enabled);
        
        medianRadiusField.setEnabled(enabled);
        medianPreviewCheckbox.setEnabled(enabled);
        leftPanel.setApplicable(enabled);

        if (!enabled && medianPreviewCheckbox.isSelected()) {
            medianPreviewCheckbox.setSelected(false);
            performMedianPreviewAsync(false, () -> {
                if (enhanceCheckbox.isSelected()) {
                    enhanceContrast(selectedSettings.getEnhanceSaturatedPercent());
                }
            });
        }
    }
    
    /**
     * Resets median radius to its default value.
     */
    private void resetMedianRadius() {
        boolean enabled = medianCheckbox.isSelected();
        selectedSettings.setMedianRadius(AnalysisSettings.DFL_MEDIAN_RADIUS);
        medianRadiusField.setText(String.valueOf(selectedSettings.getMedianRadius()));

        if (enabled) {
        	updateMedianRadiusFromField();
        }
    }

    /**
     * Reads the radius value from the text field, validates it, and triggers a preview update.
     */
    private void updateMedianRadiusFromField() {
        try {
            String text = medianRadiusField.getText();
            double val = Double.parseDouble(text);
            if (val < 0) val = 0; 
            
            selectedSettings.setMedianRadius(val);
            
            if (medianPreviewCheckbox.isSelected()) {
                performMedianPreviewAsync(true, () -> {
                    if (enhanceCheckbox.isSelected()) {
                        enhanceContrast(selectedSettings.getEnhanceSaturatedPercent());
                    }
                });
            }
        } catch (NumberFormatException ex) {
            medianRadiusField.setText(String.valueOf(selectedSettings.getMedianRadius()));
        }
    }

    /**
     * Asynchronously handles the Median Filter Preview logic.
     * It manages taking snapshots, resetting to clean states, and applying the filter.
     * Handles cancellation to ensure the image is reset to a clean state.
     * * @param isPreviewOn Whether the preview is being turned on (apply filter) or off (reset).
     * @param onComplete  Runnable to execute after the worker finishes (e.g., re-applying contrast).
     */
    private void performMedianPreviewAsync(boolean isPreviewOn, Runnable onComplete) {
    	ImagePlus img = leftPanel.updateAndGetImg();
        if (img == null) return;

        setPreprocessingEnabled(false);

        // Assign to class variable so we can cancel it
        currentWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                ImageProcessor ip = img.getProcessor();

                // Check cancel before starting
                if (isCancelled()) return null;

                if (isPreviewOn) {
                    ip.reset(); 
                    if (isCancelled()) return null;
                    ip.snapshot(); 
                    
                    if (isCancelled()) return null;
                    if(selectedSettings.getMedianRadius()>0) {
                        RankFilters rf = new RankFilters();
                        rf.rank(ip, selectedSettings.getMedianRadius(), RankFilters.MEDIAN);
                    }

                } else {
                    ip.reset();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Check if cancelled
                    if (isCancelled()) {
                        // If cancelled, we MUST revert to the snapshot state
                        // because rank() might have stopped halfway or finished but we don't want the result.
                        img.getProcessor().reset();
                        IJ.showStatus("Preview cancelled.");
                        enhanceContrast(selectedSettings.getEnhanceSaturatedPercent());
                        
                        medianPreviewCheckbox.setSelected(false);
                        
                    } else {
                        get(); // check for exceptions
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
        };

        currentWorker.execute();
    }
    
    /**
     * Launches a dedicated background worker to apply the filter to the image (or stack).
     * * @param doProcessAll      True if the whole stack should be processed.
     * @param currentSliceIndex The index of the current slice (if not processing all).
     */
    private void launchApplyWorker(boolean doProcessAll, int currentSliceIndex) {
        setPreprocessingEnabled(false);
        ImagePlus img = leftPanel.updateAndGetImg();
        
        currentWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                IJ.showStatus("Applying filters...");
                if (selectedSettings.medianFilterEnabled()) {
                    applyMedianFilter(img, selectedSettings.getMedianRadius(), doProcessAll, currentSliceIndex);
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                         IJ.showStatus("Apply cancelled.");
                    } else {
                        get();
                        IJ.showStatus("Preprocessing done.");
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
        };
        currentWorker.execute();
    }

    /**
     * The core logic for applying the median filter using ImageJ's RankFilters.
     * Supports cancellation during the stack loop.
     * * @param imp           The ImagePlus object.
     * @param radius        The filter radius.
     * @param processAll    True to process all slices.
     * @param targetSlice   The specific slice to process if not all.
     */
    private void applyMedianFilter(final ImagePlus imp, final double radius, boolean processAll, int targetSlice) {
        final RankFilters rf = new RankFilters();
        final ImageStack stack = imp.getStack();
        final int n = stack.getSize();

        if (processAll) {
            for (int z = 1; z <= n; z++) {
                // CRITICAL: Check cancellation inside the loop!
                if (currentWorker.isCancelled()) break; 
                
                rf.rank(stack.getProcessor(z), radius, RankFilters.MEDIAN);
                IJ.showProgress(z, n);
            }
        } else if (targetSlice >= 1 && targetSlice <= n) {
            rf.rank(stack.getProcessor(targetSlice), radius, RankFilters.MEDIAN);
        }
        IJ.showProgress(1.0);
    }
    
    /**
     * Enables UI components (inputs) of this preprocessing panel.
     * @param enable true : enables inputs, false : disables inputs
     */
    public void enableUIComponents(boolean enable) {
        enhanceCheckbox.setEnabled(enable);
        enhanceSaturatedResetButton.setEnabled(enable);
        medianCheckbox.setEnabled(enable);
        medianRadiusResetButton.setEnabled(enable);
        enhanceSaturatedSpinner.setEnabled(enable && enhanceCheckbox.isSelected());

        boolean medEnabled = enable && medianCheckbox.isSelected();
        medianRadiusField.setEnabled(medEnabled);
        medianPreviewCheckbox.setEnabled(medEnabled);
        applyButton.setEnabled(medEnabled);
    }

}