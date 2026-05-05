package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.MainGUI_LDC;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;
import fr.sorbonne_universite.ldc.ui.leftpanel.PipelineSubPanel;
import fr.sorbonne_universite.ldc.utils.PanelUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageStatistics;
import ij.process.StackStatistics;
import net.imagej.display.ImageDisplayService;

/**
 * The panel handling the Segmentation / Thresholding step.
 * Replicates the native ImageJ Threshold Adjuster workflow.
 */
@SuppressWarnings("serial")
public class ThresholdingPanel extends JPanel implements PipelineSubPanel {

    private LeftPanel leftPanel;

    @Parameter
    private LDCService ldc;
    @Parameter
    private ImageDisplayService imageDisplayService;

    // UI Components
    private HistogramPanel histogramPanel;       // Global 3D
    private HistogramPanel localHistogramPanel;  // Local 2D (Current Slice)
    
    private JComboBox<String> methodComboBox;
    private JCheckBox darkBackgroundCheckbox;
    private JSlider minSlider, maxSlider;
    private JSpinner minSpinner, maxSpinner;
    private JButton applyButton, resetButton;
    
    // State Variables
    private int maxPixelIntensity = 255;
    private boolean isApplied = false;

    // Slice Watcher variables
    private Timer sliceWatcher;
    private int lastSlice = -1;

    public ThresholdingPanel(Context ctx, LeftPanel leftPanel) {
        super();
        this.leftPanel = leftPanel;
        ctx.inject(this);
        
        PanelUtils.createVerticalPanel(this, "Segmentation / Thresholding", 550); // Increased height to fit 2nd graph

        // 1. GLOBAL Histogram Visualization
        histogramPanel = new HistogramPanel();
        JPanel globalContainer = new JPanel(new BorderLayout());
        globalContainer.add(new JLabel(" Global Stack Histogram:"), BorderLayout.NORTH);
        globalContainer.add(histogramPanel, BorderLayout.CENTER);
        globalContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        globalContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        add(globalContainer);
        add(Box.createVerticalStrut(5));

        // 1b. LOCAL Histogram Visualization
        localHistogramPanel = new HistogramPanel();
        JPanel localContainer = new JPanel(new BorderLayout());
        localContainer.add(new JLabel(" Current Slice Histogram:"), BorderLayout.NORTH);
        localContainer.add(localHistogramPanel, BorderLayout.CENTER);
        localContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        localContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        add(localContainer);
        add(Box.createVerticalStrut(10));

        // 2. Method Selection
        JPanel methodRow = new JPanel(new BorderLayout());
        methodRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        methodRow.add(new JLabel(" Method: "), BorderLayout.WEST);
        
        String[] methods = ldc.getThresholdMethodsList().toArray(new String[0]);
        methodComboBox = new JComboBox<>(methods);
        methodComboBox.setSelectedItem(ldc.getThresholdMethod());
        methodComboBox.addActionListener(e -> updateThresholdMethod());
        methodRow.add(methodComboBox, BorderLayout.CENTER);
        add(methodRow);
        add(Box.createVerticalStrut(10));

        // 3. Dark Background
        darkBackgroundCheckbox = new JCheckBox("Dark Background");
        darkBackgroundCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        darkBackgroundCheckbox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        darkBackgroundCheckbox.setSelected(ldc.thresholdDarkBackgroundEnabled());
        darkBackgroundCheckbox.addActionListener(e -> updateDarkBackground());
        if ("Manual".equals(ldc.getThresholdMethod())) darkBackgroundCheckbox.setEnabled(false);
        add(darkBackgroundCheckbox);
        add(Box.createVerticalStrut(10));

        // 4. Threshold Sliders
        add(createThresholdControl("Min:", 0, 5000, true));
        add(Box.createVerticalStrut(5));
        add(createThresholdControl("Max:", 0, 5000, false));
        add(Box.createVerticalStrut(15));

        // 5. Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applyThreshold());
        buttonRow.add(applyButton);
        
        resetButton = new JButton("Reset Thresholding");
        resetButton.addActionListener(e -> resetThreshold());
        buttonRow.add(resetButton);
        
        add(buttonRow);
        
        // Initialize the Slice Watcher Timer
        setupSliceWatcher();
    }
    
    // =========================================================================
    // SLICE WATCHER (POLLS FOR IMAGE SCROLLING)
    // =========================================================================
    private void setupSliceWatcher() {
        // Checks the active slice every 150ms. Fast enough to look real-time, slow enough to not lag Java.
        sliceWatcher = new Timer(150, e -> {
            if (!this.isVisible() || !applyButton.isEnabled() || isApplied) return;
            
            ImagePlus img = leftPanel.getCurrentImage();
            if (img != null) {
                int currentSlice = img.getCurrentSlice();
                if (currentSlice != lastSlice) {
                    lastSlice = currentSlice;
                    refreshLocalHistogram(img);
                }
            }
        });
    }

    // =========================================================================
    // UI BUILDER HELPERS
    // =========================================================================

    private JPanel createThresholdControl(String label, int min, int max, boolean isMinControl) {
        JPanel row = new JPanel(new BorderLayout());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(" " + label + " ");
        lbl.setPreferredSize(new Dimension(40, 30));
        row.add(lbl, BorderLayout.WEST);

        int initialVal = isMinControl ? ldc.getThresholdMinValue() : ldc.getThresholdMaxValue();
        
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialVal, min, max, 1));
        JSlider slider = new JSlider(min, max, initialVal);
        
        if (isMinControl) { minSlider = slider; minSpinner = spinner; }
        else              { maxSlider = slider; maxSpinner = spinner; }

        slider.addChangeListener(e -> {
            int val = slider.getValue();
            spinner.setValue(val);
            
            if (isMinControl && val > maxSlider.getValue()) {
                maxSlider.setValue(val);
                maxSpinner.setValue(val);
                ldc.setThresholdMaxValue(val);
            } else if (!isMinControl && val < minSlider.getValue()) {
                minSlider.setValue(val);
                minSpinner.setValue(val);
                ldc.setThresholdMinValue(val);
            }
            updateManualValues(isMinControl, val);
        });

        spinner.addChangeListener(e -> {
            slider.setValue((Integer) spinner.getValue());
        });

        row.add(slider, BorderLayout.CENTER);
        row.add(spinner, BorderLayout.EAST);
        return row;
    }

    // =========================================================================
    // CORE LOGIC & EVENT HANDLERS
    // =========================================================================

    private void updateManualValues(boolean isMin, int value) {
        if(isApplied) return;
        
        if(isMin) ldc.setThresholdMinValue(value);
        else      ldc.setThresholdMaxValue(value);
        
        histogramPanel.setThresholdRange(ldc.getThresholdMinValue(), ldc.getThresholdMaxValue());
        localHistogramPanel.setThresholdRange(ldc.getThresholdMinValue(), ldc.getThresholdMaxValue());

        if ("Manual".equals(methodComboBox.getSelectedItem())) {
            ImagePlus img = leftPanel.getCurrentImage();
            if (img != null) {
                ldc.previewManualThreshold(img);
            }
        }
    }

    public void updateThresholdMethod() {
        String selectedMethod = (String) methodComboBox.getSelectedItem();
        boolean isManual = "Manual".equals(selectedMethod);
        
        darkBackgroundCheckbox.setEnabled(!isManual);
        ldc.setThresholdMethod(selectedMethod);
        updateThresholdLogic();
    }

    public void updateDarkBackground() {
        ldc.setThresholdDarkBackground(darkBackgroundCheckbox.isSelected());
        updateThresholdLogic();
    }

    public void updateThresholdLogic() {
        if (isApplied) return;

        ImagePlus img = leftPanel.getCurrentImage();
        if (img == null) return;

        boolean isManual = "Manual".equals(methodComboBox.getSelectedItem());

        if (isManual) {
            enableSliders(methodComboBox.isEnabled());
            ldc.previewManualThreshold(img);
            
            histogramPanel.setThresholdRange(ldc.getThresholdMinValue(), ldc.getThresholdMaxValue());
            localHistogramPanel.setThresholdRange(ldc.getThresholdMinValue(), ldc.getThresholdMaxValue());
            
        } else {
            enableSliders(false);
            double[] computed = ldc.previewAutoThreshold(img);
            
            int cMin = (int) computed[0];
            int cMax = (int) computed[1];
            
            ldc.setThresholdMinValue(cMin);
            ldc.setThresholdMaxValue(cMax);
            
            minSlider.setValue(cMin);
            maxSlider.setValue(cMax);
            minSpinner.setValue(cMin);
            maxSpinner.setValue(cMax);
            
            histogramPanel.setThresholdRange(cMin, cMax);
            localHistogramPanel.setThresholdRange(cMin, cMax);
        }
    }

    private void applyThreshold() {
        ImagePlus img = leftPanel.getCurrentImage();
        if(img == null) return;
        
        ImagePlus mask = ldc.applyThreshold(img);
        
        if(mask != null) {
            mask.show();
            leftPanel.setMask(mask);
            IJ.showStatus("Threshold applied.");
            leftPanel.updateWorkflowIndex(MainGUI_LDC.MORPHOLOGICAL_STEP);
        }
        isApplied = true;
        sliceWatcher.stop(); // Save CPU when done
    }
    
    private void resetThreshold() {
        ImagePlus img = leftPanel.getCurrentImage();
        if(img == null) return;
        
        ldc.resetThreshold(img);
        IJ.showStatus("Threshold reset.");
        setVisible(true);
        resetUIComponents();
        methodComboBox.setSelectedIndex(0);  
        isApplied = false;
        sliceWatcher.start();
    }

    // =========================================================================
    // DATA & RANGE MANAGEMENT
    // =========================================================================
    
    private void refreshHistogramData() {
        ImagePlus imp = leftPanel.getCurrentImage();
        if (imp != null) {
            // 1. GLOBAL STATS
            StackStatistics stats = new StackStatistics(imp);
            maxPixelIntensity = (int) Math.ceil(stats.max);
            if (maxPixelIntensity < 255) maxPixelIntensity = 255;
            histogramPanel.setHistogram(stats.histogram, stats.histMin, stats.histMax); 
            
            // 2. LOCAL STATS
            refreshLocalHistogram(imp);
            
        } else {
            histogramPanel.setHistogram(null, 0, 0);
            localHistogramPanel.setHistogram(null, 0, 0);
        }
    }
    
    /**
     * Calculates statistics purely for the currently visible 2D slice.
     */
    private void refreshLocalHistogram(ImagePlus imp) {
        if (imp == null) return;
        // ImageStatistics calculates based purely on the active ImageProcessor (the current slice)
        ImageStatistics localStats = imp.getProcessor().getStatistics();
        localHistogramPanel.setHistogram(localStats.histogram, localStats.histMin, localStats.histMax);
        localHistogramPanel.setThresholdRange(ldc.getThresholdMinValue(), ldc.getThresholdMaxValue());
    }

    private void configureRanges() { 
        updateControlModel(minSlider, minSpinner, 0, maxPixelIntensity, ldc.getThresholdMinValue());
        updateControlModel(maxSlider, maxSpinner, 0, maxPixelIntensity, ldc.getThresholdMaxValue());
    }

    private void updateControlModel(JSlider slider, JSpinner spinner, int min, int max, int current) {
        if (current > max) current = max;
        
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        model.setMaximum(max);
        model.setMinimum(min);
        model.setValue(current);
        
        slider.setMaximum(max);
        slider.setMinimum(min);
        slider.setValue(current);
    }

    // =========================================================================
    // PIPELINE IMPLEMENTATIONS (Enable, Disable, Sync, Reset)
    // =========================================================================

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {            
            refreshHistogramData();
            configureRanges();
            updateThresholdLogic();
            if (!isApplied && applyButton.isEnabled()) {
                sliceWatcher.start();
            }
        } else {
            sliceWatcher.stop();
        }
    }

    private void enableSliders(boolean enabled) {
        if(leftPanel.getWorkflowIndex() != MainGUI_LDC.MORPHOLOGICAL_STEP && !enabled) {
            // Safety measure, but allow disabling
        }
        minSlider.setEnabled(enabled);
        maxSlider.setEnabled(enabled);
        minSpinner.setEnabled(enabled);
        maxSpinner.setEnabled(enabled);
    }
    
    @Override
    public void enableUIComponents(boolean enabled) {
        boolean isManual = "Manual".equals(methodComboBox.getSelectedItem());

        methodComboBox.setEnabled(enabled);
        darkBackgroundCheckbox.setEnabled(enabled && !isManual);
        applyButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        
        enableSliders(enabled ? isManual : false);

        if(enabled) {
            refreshHistogramData();
            configureRanges();
            if (!isApplied) sliceWatcher.start();
        } else {
            sliceWatcher.stop();
            if(leftPanel.getCurrentImage() == null) {
                histogramPanel.setHistogram(null, 0.0, 0.0);
                localHistogramPanel.setHistogram(null, 0.0, 0.0);
            }
        }
    }
    
    @Override
    public void resetUIComponents() {
    	isApplied = false;
    	
        ldc.setThresholdMethod("Manual");
        methodComboBox.setSelectedItem(ldc.getThresholdMethod());
        
        darkBackgroundCheckbox.setSelected(false);
        ldc.setThresholdDarkBackground(false);
        
        minSlider.setValue(0);
        maxSlider.setValue(0);
        minSpinner.setValue(0);
        maxSpinner.setValue(0);
        
        ldc.setThresholdMinValue(0);
        ldc.setThresholdMaxValue(0);
        
        
        histogramPanel.setThresholdRange(-1, -1);
        localHistogramPanel.setThresholdRange(-1, -1);
        
        ImagePlus img = leftPanel.getCurrentImage();
        if(img != null) ldc.resetThreshold(img);
        
        if (this.isVisible()) {
            sliceWatcher.start();
        }
    }
    
    @Override
    public void syncUIWithParams() {
        methodComboBox.setSelectedItem(ldc.getThresholdMethod());
        darkBackgroundCheckbox.setSelected(ldc.thresholdDarkBackgroundEnabled());

        minSlider.setValue(ldc.getThresholdMinValue());
        minSpinner.setValue(ldc.getThresholdMinValue());
        maxSlider.setValue(ldc.getThresholdMaxValue());
        maxSpinner.setValue(ldc.getThresholdMaxValue());
        
        refreshHistogramData();
        configureRanges();
        updateThresholdLogic();
    }

    @Override
    public void applyUIWithParams() {
        applyThreshold();
    }
}