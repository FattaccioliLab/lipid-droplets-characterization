package mainGUI.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.net.URL;
import java.util.concurrent.CancellationException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.YesNoCancelDialog;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import model.AnalysisSettings;
import net.imagej.display.ImageDisplayService;

/**
 * The left side of the plugin main GUI.
 * <p>
 * This panel handles:
 * <ul>
 * <li>Image loading and replacement.</li>
 * <li>Preprocessing settings (Contrast enhancement, Median filtering).</li>
 * <li>Asynchronous previewing and application of filters.</li>
 * <li>Navigation to the next step of the workflow.</li>
 * </ul>
 */
@SuppressWarnings("serial")
public class LeftPanel extends JPanel {

    // --- Services & Models ---
    @Parameter
    private ImageDisplayService imageDisplayService;
    private final AnalysisSettings selectedSettings;
    private final ContrastEnhancer ce;

    // --- UI Components ---
    private ImagePlus img;
    private JLabel imageStatusLabel;
    
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

    // Layout Containers
    private JPanel filterPanel;
    private JButton nextButton;
    private JButton prevButton;

    // --- State Flags ---
    private boolean isApplicable = false;
    private volatile boolean isProcessing = false; 
    
    // Reference to the currently running worker to allow cancellation
    private SwingWorker<Void, Void> currentWorker;

    /**
     * Constructs the LeftPanel.
     * * @param ctx              The SciJava context for injection.
     * @param selectedSettings The model object holding analysis parameters.
     */
    public LeftPanel(Context ctx, AnalysisSettings selectedSettings) {
        ctx.inject(this);
        this.selectedSettings = selectedSettings;
        this.ce = new ContrastEnhancer();

        initializeGUI();
        startImageWatcher();
    }

    /**
     * Initializes the main layout and assembles the sub-panels.
     */
    private void initializeGUI() {
        setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

        mainContainer.add(createImageSourcePanel());
        mainContainer.add(Box.createVerticalStrut(10));

        filterPanel = createFilterPanel();
        mainContainer.add(filterPanel);
        
        mainContainer.add(createFooterPanel());

        add(mainContainer, BorderLayout.NORTH);
    }

    // =========================================================================
    // UI CONSTRUCTION METHODS
    // =========================================================================

    /**
     * Creates the top panel containing image status and the "Replace Image" button.
     * * @return The constructed JPanel.
     */
    private JPanel createImageSourcePanel() {
        JPanel panel = createVerticalPanel("Image Source", 150);

        imageStatusLabel = new JLabel("<html><center>No image opened.<br>Please open one.</center></html>", SwingConstants.CENTER);
        imageStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton fileSelectButton = new JButton("Replace image");
        fileSelectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        fileSelectButton.addActionListener(e -> {
            if (!isProcessing) replaceImageAction();
        });

        panel.add(Box.createVerticalStrut(5));
        panel.add(imageStatusLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(fileSelectButton);
        panel.add(Box.createVerticalStrut(5));

        return panel;
    }

    /**
     * Creates the middle panel containing preprocessing controls (Contrast, Median Filter).
     * Includes logic for initializing the loading GIF and Cancel button.
     * * @return The constructed JPanel.
     */
    private JPanel createFilterPanel() {
        JPanel panel = createVerticalPanel("Preprocessing", 300);

        // --- Contrast Section ---
        enhanceCheckbox = new JCheckBox("Enhance contrast");
        enhanceCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        enhanceCheckbox.setSelected(selectedSettings.isEnhanceContrast());
        enhanceCheckbox.addActionListener(e -> toggleContrast());

        enhanceSaturatedSpinner = new JSpinner(new SpinnerNumberModel(selectedSettings.getEnhanceSaturatedPercent(), 0.0, 100.0, 0.1));
        enhanceSaturatedSpinner.setEnabled(selectedSettings.isEnhanceContrast());
        enhanceSaturatedSpinner.addChangeListener(e -> {
            selectedSettings.setEnhanceSaturatedPercent(((Number) enhanceSaturatedSpinner.getValue()).doubleValue());
            if (enhanceCheckbox.isSelected() && !isProcessing) enhanceContrast(selectedSettings.getEnhanceSaturatedPercent());
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

        panel.add(enhanceCheckbox);
        panel.add(saturatedRow);
        panel.add(Box.createVerticalStrut(8));

        // --- Median Section ---
        JPanel medianRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        medianRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        medianRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        medianCheckbox = new JCheckBox("Median filter");
        medianCheckbox.setSelected(selectedSettings.isMedianFilter());
        
        medianPreviewCheckbox = new JCheckBox("Preview");
        medianPreviewCheckbox.setEnabled(selectedSettings.isMedianFilter());

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
        medianRadiusField.setEnabled(selectedSettings.isMedianFilter());
        
        ActionListener updateRadiusAction = e -> {
            if (!isProcessing) updateMedianRadiusFromField();
        };
        medianRadiusField.addActionListener(updateRadiusAction);
        medianRadiusField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!isProcessing) updateMedianRadiusFromField();
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
            if (!isProcessing) toggleMedian();
        });
        
        medianPreviewCheckbox.addActionListener(e -> {
            if (isProcessing) return; 
            performMedianPreviewAsync(
                medianPreviewCheckbox.isSelected(), 
                () -> {
                    if (enhanceCheckbox.isSelected()) {
                        enhanceContrast(selectedSettings.getEnhanceSaturatedPercent());
                    }
                }
            );
        });

        panel.add(medianRow);
        panel.add(radiusRow);

        // Apply Button
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {
            if (isProcessing) return;
            applyButton.setEnabled(false);
            runApplyLogic();
            isApplicable = false;
        });

        buttonRow.add(applyButton);
        
        panel.add(Box.createVerticalStrut(10));
        panel.add(buttonRow);
        panel.add(Box.createVerticalStrut(5));

        return panel;
    }

    /**
     * Creates the bottom panel containing navigation buttons (Prev/Next).
     * * @return The constructed JPanel.
     */
    private JPanel createFooterPanel() {
        JPanel footerRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        footerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        prevButton = new JButton("Prev");
        prevButton.setEnabled(false);
        nextButton = new JButton("Next");

        nextButton.addActionListener(e -> {
            if(isProcessing) return;
            filterPanel.setVisible(false);
            prevButton.setEnabled(true);
            nextButton.setEnabled(false);
            revalidate();
            repaint();
        });

        prevButton.addActionListener(e -> {
            if(isProcessing) return;
            filterPanel.setVisible(true);
            prevButton.setEnabled(false);
            nextButton.setEnabled(true);
            revalidate();
            repaint();
        });

        footerRow.add(prevButton);
        footerRow.add(nextButton);
        return footerRow;
    }

    /**
     * Helper to create standardized vertical panels with a titled border.
     * * @param title     The title of the border.
     * @param maxHeight The maximum height constraint for the panel.
     * @return The configured JPanel.
     */
    private JPanel createVerticalPanel(String title, int maxHeight) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
        return p;
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
        isProcessing = !enabled;

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
        nextButton.setEnabled(enabled && img != null && !filterPanel.isVisible() == false);
    }

    /**
     * Opens a file chooser to replace the current image stack with a new one from disk.
     */
    private void replaceImageAction() {
        img = WindowManager.getCurrentImage();
        if (img == null) {
            IJ.showMessage("Please open an image first (File > Open)");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File imageFile = fileChooser.getSelectedFile();
        ImagePlus newImage = IJ.openImage(imageFile.getAbsolutePath());
        if (newImage == null) return;

        img.setStack(newImage.getTitle(), newImage.getStack());
        img.setCalibration(newImage.getCalibration());
        img.setDimensions(newImage.getNChannels(), newImage.getNSlices(), newImage.getNFrames());
        img.updateAndDraw();
    }

    /**
     * Toggles contrast enhancement based on the checkbox state.
     */
    private void toggleContrast() {
        boolean enabled = enhanceCheckbox.isSelected();
        selectedSettings.setEnhanceContrast(enabled);
        enhanceSaturatedSpinner.setEnabled(enabled);
        
        img = WindowManager.getCurrentImage();
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
    	
    	img = WindowManager.getCurrentImage();
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
        isApplicable = enabled;

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
        img = WindowManager.getCurrentImage();
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
     * The main entry point for the "Apply" button logic.
     * Checks if stack processing is needed, handles preview state conflicts, 
     * and launches the appropriate worker.
     */
    private void runApplyLogic() {
        img = WindowManager.getCurrentImage();
        if (img == null) {
            IJ.showMessage("No image", "Please open an image first.");
            applyButton.setEnabled(true);
            return;
        }

        boolean processAllSlices = false;

        if (selectedSettings.isMedianFilter() && img.getStackSize() > 1) {
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
    
    /**
     * Launches a dedicated background worker to apply the filter to the image (or stack).
     * * @param doProcessAll      True if the whole stack should be processed.
     * @param currentSliceIndex The index of the current slice (if not processing all).
     */
    private void launchApplyWorker(boolean doProcessAll, int currentSliceIndex) {
        setPreprocessingEnabled(false);
        
        currentWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                IJ.showStatus("Applying filters...");
                if (selectedSettings.isMedianFilter()) {
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
     * Starts a timer that regularly checks the status of the current image.
     * Updates UI enable/disable states based on image presence and processing status.
     */
    private void startImageWatcher() {
        Timer imageWatcher = new Timer(300, e -> {
            if (isProcessing) return;

            img = WindowManager.getCurrentImage();
            boolean hasImage = (img != null);

            imageStatusLabel.setText(hasImage 
                ? "<html><center>Image opened:<br>" + img.getTitle() + "</center></html>"
                : "<html><center>No image opened.<br>Please open one.</center></html>");

            enhanceCheckbox.setEnabled(hasImage);
            enhanceSaturatedResetButton.setEnabled(hasImage);
            medianCheckbox.setEnabled(hasImage);
            medianRadiusResetButton.setEnabled(hasImage);
            enhanceSaturatedSpinner.setEnabled(hasImage && enhanceCheckbox.isSelected());

            boolean medEnabled = hasImage && medianCheckbox.isSelected();
            medianRadiusField.setEnabled(medEnabled);
            medianPreviewCheckbox.setEnabled(medEnabled);
            applyButton.setEnabled(medEnabled);
            nextButton.setEnabled(hasImage && !filterPanel.isVisible() == false);
        });
        imageWatcher.start();
    }
}