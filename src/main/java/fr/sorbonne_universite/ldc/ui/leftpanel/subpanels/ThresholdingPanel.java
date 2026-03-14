package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

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
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;
import fr.sorbonne_universite.ldc.utils.PanelUtils;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.display.ImageDisplayService;

/**
 * The panel handling the Segmentation / Thresholding step.
 * Replicates the native ImageJ Threshold Adjuster workflow.
 */
@SuppressWarnings("serial")
public class ThresholdingPanel extends JPanel {

    private LeftPanel leftPanel;

    @Parameter
    private LDCService service;
    @Parameter
    private ImageDisplayService imageDisplayService;

    // Components
    private HistogramPanel histogramPanel; 
    private Integer effectiveMaxBin = 255;	//Store the calculated max bin from the histogram
    private JComboBox<String> methodComboBox;
    private JCheckBox darkBackgroundCheckbox;
    
    // Sliders & Spinners
    private JSlider minSlider, maxSlider;
    private JSpinner minSpinner, maxSpinner;
    
    private JButton applyButton;
    private JButton resetButton;
    private boolean isApplied = false;
    private boolean isReset = false;

    public ThresholdingPanel(Context ctx, LeftPanel leftPanel) {
        super();
        this.leftPanel = leftPanel;
        ctx.inject(this);

        PanelUtils.createVerticalPanel(this, "Segmentation / Thresholding", 450); // Increased height

        // 1. Histogram Visualization (Top)
        histogramPanel = new HistogramPanel();
        // Wrap in a panel to manage margins/alignment
        JPanel histContainer = new JPanel(new BorderLayout());
        histContainer.add(histogramPanel, BorderLayout.CENTER);
        histContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // Fixed height for graph
        add(histContainer);
        
        add(Box.createVerticalStrut(10));

        // 2. Method Selection
        JPanel methodRow = new JPanel(new BorderLayout());
        methodRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        methodRow.add(new JLabel(" Method: "), BorderLayout.WEST);
        
        String[] methods = service.getThresholdMethodsList().toArray(new String[0]);
        methodComboBox = new JComboBox<>(methods);
        methodComboBox.setSelectedItem(service.getThresholdMethod());
        methodComboBox.addActionListener(e -> updateThresholdLogic());
        methodRow.add(methodComboBox, BorderLayout.CENTER);
        add(methodRow);
        
        add(Box.createVerticalStrut(10));


        // 3. Dark Background Option
        darkBackgroundCheckbox = new JCheckBox("Dark Background");
        darkBackgroundCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        darkBackgroundCheckbox.setSelected(service.thresholdDarkBackgroundEnabled());
        darkBackgroundCheckbox.addActionListener(e -> {
            service.setThresholdDarkBackground(darkBackgroundCheckbox.isSelected());
            updateThresholdLogic();
        });
        add(darkBackgroundCheckbox);
        
        add(Box.createVerticalStrut(10));

        // 4. Threshold Sliders
        add(createThresholdControl("Min:", 0, 5000, true));	//5000 is just to initialize, this value will be replced by effectiveMaxBin, so that slider max value matchs with the histogram's X-axis value
        add(Box.createVerticalStrut(5));
        add(createThresholdControl("Max:", 0, 5000, false));	//same as Min

        add(Box.createVerticalStrut(15));

        // 5. Apply Button
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applyThreshold());
        buttonRow.add(applyButton);
        add(buttonRow);
        
        //it would only reset the method and manual value before applying, doesnot work if we press apply then reset. 
        //in that case we need a hard reset, like Yahya's Reset button inside the ImageSource
        //having reset button only for method reset is not interesting to me. user can do so just by sliding the bar to 0, 0.
        
        // 6. Reset Button
        resetButton = new JButton("Reset Thresholding");
        resetButton.addActionListener(e -> resetThreshold());
        buttonRow.add(resetButton);
        
        add(buttonRow);
    }
    
    /**
     * Called when this panel becomes visible (navigated to from Preprocessing).
     * Needs to refresh the histogram based on the current image.
     */
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            refreshHistogramData();
            configureRanges();
            updateThresholdLogic();
        }
    }

    private void refreshHistogramData() {
        ImagePlus img = leftPanel.getCurrentImage();
        if (img != null) {
            // Get raw 8-bit histogram (256 bins)
            int[] stats = img.getProcessor().getHistogram();
             this.effectiveMaxBin = histogramPanel.setHistogram(stats);
        } else {
            histogramPanel.setHistogram(null);
        }
    }

    private JPanel createThresholdControl(String label, int min, int max, boolean isMinControl) {
        JPanel row = new JPanel(new BorderLayout());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(" " + label + " ");
        lbl.setPreferredSize(new Dimension(40, 30));
        row.add(lbl, BorderLayout.WEST);

        int initialVal = isMinControl ? service.getThresholdMinValue() : service.getThresholdMaxValue();
        
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialVal, min, max, 1));
        JSlider slider = new JSlider(min, max, initialVal);
        
        if(isMinControl) { minSlider = slider; minSpinner = spinner; }
        else             { maxSlider = slider; maxSpinner = spinner; }

        slider.addChangeListener(e -> {
            spinner.setValue(slider.getValue());
      
            updateManualValues(isMinControl, slider.getValue());
            
            //with these two special cases, we force the minSlider value alws to be <= maxSlider value and vice-versa
            
            //Special case i : min slider is crossing the max slider, 
            //we will make them move simultanously, so that min slider never cross max slider 
            if(isMinControl && slider.getValue() > service.getThresholdMaxValue()) {
                updateManualValues(!isMinControl, slider.getValue());
                maxSpinner.setValue(slider.getValue());
            }
            
            //Special case ii : max slider value is getting lesser than Min slider value, we will decrease also the min slider value
            else if(!isMinControl && slider.getValue() < service.getThresholdMinValue()) {	 
                updateManualValues(isMinControl, slider.getValue());
                minSpinner.setValue(slider.getValue());
            }
        });

        spinner.addChangeListener(e -> {
            slider.setValue((Integer) spinner.getValue());
            updateManualValues(isMinControl, (Integer) spinner.getValue());
        });

        row.add(slider, BorderLayout.CENTER);
        row.add(spinner, BorderLayout.EAST);
        return row;
    }

    private void updateManualValues(boolean isMin, int value) {
        if(isApplied) return;
        
        if(isMin) service.setThresholdMinValue(value);
        else      service.setThresholdMaxValue(value);
        
        // Update the Red Graph Overlay immediately
        histogramPanel.setThresholdRange(service.getThresholdMinValue(), service.getThresholdMaxValue());

        if ("Manual".equals(methodComboBox.getSelectedItem())) {
            updateThresholdLogic();
        }
    }

    public void updateThresholdLogic() {
        if (isApplied) return;

        ImagePlus img = leftPanel.getCurrentImage();
        if (img == null) return;

        String method = (String) methodComboBox.getSelectedItem();
        boolean isManual = "Manual".equals(method);
        boolean isDark = darkBackgroundCheckbox.isSelected();

        
        service.setThresholdMethod(method);
        
        darkBackgroundCheckbox.setEnabled(!isManual);	//if manual mode, disables dark BG check box, otherwise enables it

        if (isManual) {
            enableSliders(methodComboBox.isEnabled()); // Sliders enabled if the method combo box with "Manual" is also enabled
            service.previewManualThreshold(img);
            darkBackgroundCheckbox.setSelected(false);
        } else {
            enableSliders(false);
            double[] computed = service.previewAutoThreshold(img, method, isDark);
            
            // Update sliders to match what the algo found
            int cMin = (int)computed[0];
            int cMax = (int)computed[1];
            
            // Suspend listeners if possible, or just set values (sliders will trigger updateManualValues -> loop safe)
            minSlider.setValue(cMin);
            maxSlider.setValue(cMax);
            minSpinner.setValue(cMin);
            maxSpinner.setValue(cMax);
        }
        
        // Final sync of the graph visualization
        histogramPanel.setThresholdRange(service.getThresholdMinValue(), service.getThresholdMaxValue());
    }

    private void applyThreshold() {
        ImagePlus img = leftPanel.getCurrentImage();
        if(img == null) return;
        isApplied = service.applyThreshold(img);
        if(isApplied) {
            IJ.showStatus("Threshold applied.");
            //refreshHistogramData();
            leftPanel.updateWorkflowIndex(2);
        }
    }
    
    
    private void resetThreshold() {
        ImagePlus img = leftPanel.getCurrentImage();
        if(img == null) return;
        isReset = service.resetThreshold(img);
        if(isReset) {
            isApplied = false;
            enableUIComponents(true);
            IJ.showStatus("Threshold reset.");
            //refreshHistogramData();
            setVisible(true);
            resetUIComponents();
            methodComboBox.setSelectedIndex(0);  //putting back to the manual method
        }
    }
    

    private void enableSliders(boolean enabled) {
        minSlider.setEnabled(enabled);
        maxSlider.setEnabled(enabled);
        minSpinner.setEnabled(enabled);
        maxSpinner.setEnabled(enabled);
    }
    
    /**
     * Detects the image effective max value and updates the slider/spinner ranges.
     * This ensures 16-bit sliders aren't 90% empty space.
     */
    private void configureRanges() { 
    	if(this.effectiveMaxBin != null) {
            // Update sliders to this new "Zoomed" range
            updateControlModel(minSlider, minSpinner, 0, effectiveMaxBin, service.getThresholdMinValue());
            updateControlModel(maxSlider, maxSpinner, 0, effectiveMaxBin, service.getThresholdMaxValue());
    	}
    }

    private void updateControlModel(JSlider slider, JSpinner spinner, int min, int max, int current) {
        // Ensure current value is valid in the new range
        if (current > max) current = max;
        
        // Update Spinner Model
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        model.setMaximum(max);
        model.setMinimum(min);
        model.setValue(current);
        
        // Update Slider Model
        slider.setMaximum(max);
        slider.setMinimum(min);
        slider.setValue(current);
    }
    
    
    public void enableUIComponents(boolean enabled) {
    	resetButton.setEnabled(enabled);
    	
        if (isApplied && enabled) return; 
        
        boolean isManual = "Manual".equals(methodComboBox.getSelectedItem());

        methodComboBox.setEnabled(enabled);
        darkBackgroundCheckbox.setEnabled(enabled && !isManual);	//dark bg check box visibility
        applyButton.setEnabled(enabled);
        
        if(enabled) {
            enableSliders(isManual);
            isApplied = false; 
            refreshHistogramData(); // Refresh graph on re-enable
            configureRanges();	
        } else {
        	if(isManual) {
            	minSlider.setValue(0);
            	maxSlider.setValue(0);
            	minSpinner.setValue(0);
            	maxSpinner.setValue(0);
        	}
            enableSliders(false);
            histogramPanel.setHistogram(null);
        }
    }
    
    
    
    /**
     * Reset the Thresholding panel UI components, when the image is reset.
     * */
    public void resetUIComponents() {
    	isApplied = false;
    	
    	service.setThresholdMethod("Manual");
    	methodComboBox.setSelectedItem(service.getThresholdMethod());
    	
    	darkBackgroundCheckbox.setSelected(false);
    	service.setThresholdDarkBackground(darkBackgroundCheckbox.isSelected());
    	
    	minSlider.setValue(0);
    	maxSlider.setValue(0);
    	minSpinner.setValue(0);
    	maxSpinner.setValue(0);
    	service.setThresholdMinValue(0);
    	service.setThresholdMaxValue(0);
    }
}