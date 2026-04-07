package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fr.sorbonne_universite.ldc.model.AnalysisSettings;
import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.utils.PanelUtils;
import ij.IJ;

/**
 * Provides particle analysis setup.
 * 
 * <p>
 * Input values for particle size and circularity are currently updated after either :
 * <li> Pressing 'ENTER' after entering a new value into an input.
 * <li> Pressing 'show results' or 'histograms' buttons.
 * </p><br>
 * 
 * <p>
 * This means that if you enter a value in a field without pressing 'ENTER', 
 * the change will not be recognized automatically. The new value will only 
 * be applied after performing one of the actions mentioned above.
 * </p>
 */
@SuppressWarnings("serial")
public class ParticleAnalysisParamsPanel extends JPanel {
	
    @Parameter
    private LDCService selectedSettings;
    
    // Components
    
    // Particle settings
    private JCheckBox noMaxCheckbox;
    private JTextField minSizeField;
    private JTextField maxSizeField;
    private JTextField minCircularityField;
    private JTextField maxCircularityField;
    private JCheckBox excludeOnEdgesCheckbox;
    
    // Measurements
    private JPanel mesurementsInfos;
    private JCheckBox areaCheckbox;
    private JCheckBox medianCheckbox;
    private JCheckBox meanCheckbox;
    private JCheckBox integratedDensityCheckbox;
    private JCheckBox circularityCheckbox;
    
    public ParticleAnalysisParamsPanel(Context ctx) {
        super();
        ctx.inject(this);
        
        PanelUtils.createVerticalPanel(this, "Particle analysis parameters", 700);
        
        // PARTICLE SETTINGS
        
        JPanel particlesSettings = PanelUtils.createVerticalPanel("Particle settings", 400);
        
        // --- Size (px²) section ---
        
        JPanel sizePanel = new JPanel(new GridBagLayout());
        sizePanel.setBorder(
        	    BorderFactory.createCompoundBorder(
        	        BorderFactory.createEmptyBorder(0, 8, 0, 8),
        	        BorderFactory.createTitledBorder("")
        	    )
        	);
        sizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sizePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));

        // Grid
        GridBagConstraints cSize = new GridBagConstraints();
        cSize.insets = new Insets(2, 5, 2, 5);
        cSize.fill = GridBagConstraints.HORIZONTAL;
        cSize.weightx = 1.0; 
        
        // 1st line, 'Particles size' label
        cSize.gridy = 0; cSize.gridx = 0;
        cSize.gridwidth = 3;
        JLabel sizeLabel = new JLabel("Particle size (px²)", JLabel.CENTER);
        sizePanel.add(sizeLabel, cSize);
        cSize.gridwidth = 1;
        
        // 2nd line
        cSize.gridy = 1;
        
        // 1st column, 'Min' label
        JLabel minSizeLabel = new JLabel("Min", JLabel.CENTER);
        sizePanel.add(minSizeLabel, cSize);
        
        // 2nd column, 'No max' checkbox
        cSize.gridx = 1;
        cSize.anchor = GridBagConstraints.CENTER; cSize.fill = GridBagConstraints.NONE;
        noMaxCheckbox = new JCheckBox("No max");
        noMaxCheckbox.setSelected(true);
        noMaxCheckbox.setFocusPainted(false);
        noMaxCheckbox.addActionListener(e -> toggleNoMax());
        sizePanel.add(noMaxCheckbox, cSize);
        cSize.anchor = GridBagConstraints.WEST; cSize.fill = GridBagConstraints.HORIZONTAL;
        
        // 3rd column, 'Max' label
        cSize.gridx = 2;
        JLabel maxSizeLabel = new JLabel("Max", JLabel.CENTER);
        sizePanel.add(maxSizeLabel, cSize);
        
        // 3rd line
        cSize.gridy = 2;
        
        // 1st column, 'Min' field
        cSize.gridx = 0;
        minSizeField = new JTextField(Double.toString(AnalysisSettings.DFL_ANALYSE_MIN_SIZE));
        minSizeField.setHorizontalAlignment(JTextField.CENTER);
        minSizeField.addActionListener(e -> enterMinSizeField());
        sizePanel.add(minSizeField, cSize);
        
        // 2nd column, nothing
        cSize.gridx = 1;
        sizePanel.add(Box.createHorizontalStrut(1), cSize);
        
        // 3rd column, 'Max' field
        cSize.gridx = 2;
        maxSizeField = new JTextField("∞");
        maxSizeField.setHorizontalAlignment(JTextField.CENTER);
        maxSizeField.setEnabled(false);
        maxSizeField.addActionListener(e -> enterMaxSizeField());
        sizePanel.add(maxSizeField, cSize);
        
        // --- Circularity range section ---
        
        JPanel circularityPanel = new JPanel(new GridBagLayout());
        circularityPanel.setBorder(
        	    BorderFactory.createCompoundBorder(
        	        BorderFactory.createEmptyBorder(0, 8, 0, 8),
        	        BorderFactory.createTitledBorder("")
        	    )
        	);
        circularityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        circularityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));

        // Grid
        GridBagConstraints cCircularity = new GridBagConstraints();
        cCircularity.insets = new Insets(2, 5, 2, 5);
        cCircularity.fill = GridBagConstraints.HORIZONTAL;
        cCircularity.weightx = 1.0; 
        
        // 1st line, 'Circularity range' label
        cCircularity.gridy = 0; cCircularity.gridx = 0;
        cCircularity.gridwidth = 2;
        JLabel circularityLabel = new JLabel("Circularity range", JLabel.CENTER);
        circularityPanel.add(circularityLabel, cCircularity);
        cCircularity.gridwidth = 1;
        
        // 2nd line
        cCircularity.gridy = 1;
        
        // 1st column, 'Min' label
        JLabel minCircularityLabel = new JLabel("Min", JLabel.CENTER);
        circularityPanel.add(minCircularityLabel, cCircularity);
        
        // 2nd column, 'Max' label
        cCircularity.gridx = 1;
        JLabel maxCircularityLabel = new JLabel("Max", JLabel.CENTER);
        circularityPanel.add(maxCircularityLabel, cCircularity);
        
        // 3rd line
        cCircularity.gridy = 2;
        
        // 1st column, 'Min' field
        cCircularity.gridx = 0;
        minCircularityField = new JTextField(Double.toString(AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY));
        minCircularityField.setHorizontalAlignment(JTextField.CENTER);
        minCircularityField.addActionListener(e -> enterMinCircularityField());
        circularityPanel.add(minCircularityField, cCircularity);
        
        // 2nd column, 'Max' field
        cCircularity.gridx = 1;
        maxCircularityField = new JTextField(Double.toString(AnalysisSettings.DFL_ANALYSE_MAX_CIRCULARITY));
        maxCircularityField.setHorizontalAlignment(JTextField.CENTER);
        maxCircularityField.addActionListener(e -> enterMaxCircularityField());
        circularityPanel.add(maxCircularityField, cCircularity);
        
	    // --- Exclude on edges section ---
        
        JPanel excludePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        
        excludeOnEdgesCheckbox = new JCheckBox("Exclude on edges");
        excludeOnEdgesCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        excludeOnEdgesCheckbox.setSelected(selectedSettings.showAreaEnabled());
        excludeOnEdgesCheckbox.setFocusPainted(false);
        excludeOnEdgesCheckbox.addActionListener(e -> toggleExcludeOnEdges());
        
        excludePanel.add(excludeOnEdgesCheckbox);
        excludePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // MEASUREMENTS
        
        mesurementsInfos = PanelUtils.createVerticalPanel("Measurements", 200);
        
	    // --- Show area section ---
        areaCheckbox = new JCheckBox("Show area measures");
        areaCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        areaCheckbox.setSelected(selectedSettings.showAreaEnabled());
        areaCheckbox.setFocusPainted(false);
        areaCheckbox.addActionListener(e -> toggleArea());

	    // --- Show equivalent diameter section ---
        medianCheckbox = new JCheckBox("Show median measures");
        medianCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        medianCheckbox.setSelected(selectedSettings.showMedianEnabled());
        medianCheckbox.setFocusPainted(false);
        medianCheckbox.addActionListener(e -> toggleMedian());

	    // --- Show mean section ---
        meanCheckbox = new JCheckBox("Show mean measures");
        meanCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        meanCheckbox.setSelected(selectedSettings.showMeanEnabled());
        meanCheckbox.setFocusPainted(false);
        meanCheckbox.addActionListener(e -> toggleMean());

	    // --- Show integrated density section ---
	    integratedDensityCheckbox = new JCheckBox("Show integrated density measures");
	    integratedDensityCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
	    integratedDensityCheckbox.setSelected(selectedSettings.showIntegratedDensityEnabled());
	    integratedDensityCheckbox.setFocusPainted(false);
	    integratedDensityCheckbox.addActionListener(e -> toggleIntegratedDensity());

	    // --- Show circularity section ---
	    circularityCheckbox = new JCheckBox("Show circularity measures");
	    circularityCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
	    circularityCheckbox.setSelected(selectedSettings.showCircularityEnabled());
	    circularityCheckbox.setFocusPainted(false);
	    circularityCheckbox.addActionListener(e -> toggleCircularity());

	    // LAYOUT
	    
	    add(Box.createVerticalStrut(5));
	    
	    particlesSettings.add(sizePanel);
	    particlesSettings.add(Box.createVerticalStrut(10));
	    particlesSettings.add(circularityPanel);
	    particlesSettings.add(Box.createVerticalStrut(10));
	    particlesSettings.add(excludePanel);
	    add(particlesSettings);
	    
	    mesurementsInfos.add(areaCheckbox);
	    mesurementsInfos.add(Box.createVerticalStrut(5));
	    mesurementsInfos.add(medianCheckbox);
	    mesurementsInfos.add(Box.createVerticalStrut(5));
	    mesurementsInfos.add(meanCheckbox);
	    mesurementsInfos.add(Box.createVerticalStrut(5));
	    mesurementsInfos.add(integratedDensityCheckbox);
	    mesurementsInfos.add(Box.createVerticalStrut(5));
	    mesurementsInfos.add(circularityCheckbox);
	    add(mesurementsInfos);
	    
	    add(Box.createVerticalStrut(10));
    }
    
    // =========================================================================
    // UI ACTIONS
    // =========================================================================
    
    // PARTICLE SETTINGS
    
    /**
     * Enables or disables the 'Max' input for particle size.
     * 
     * <p>
     * If the 'No Max' checkbox is selected, it disables and resets the input, 
     * otherwise it enables the input with a default value of '10' px².<br>
     * If the minimum size is greater than 10, then the new maximum size is equal to that minimal value.
     * </p>
     */
    private void toggleNoMax() {
        boolean noMax = noMaxCheckbox.isSelected();
        maxSizeField.setEnabled(!noMax);
        if (noMax) {
            maxSizeField.setText("∞");
            selectedSettings.setAnalyseMaxSize(AnalysisSettings.DFL_ANALYSE_MAX_SIZE);
        } else {
        	double minValue = Double.valueOf(minSizeField.getText());
        	double maxValue = 10;
        	if (minValue > maxValue) {
        		maxValue = minValue;
        	}
            maxSizeField.setText(Double.toString(maxValue));
            selectedSettings.setAnalyseMaxSize(maxValue);
        }
    }
    
    /**
     * Validates the input of the minimum size text field and updates the corresponding setting, when ENTER is pressed on the input.<br>
     * If the maximum field is invalid, resets it to its last valid value.
     * 
     * <p>
     * Behavior:
     * <li> If the input is a valid number within the allowed range, updates the field and the setting.
     * <li> If the input is less than the default minimum size, resets the field and the setting to the default minimum.
     * <li> If the input is greater than the current maximum size, resets the field and the setting to the maximum size.
     * <li> If the input is not a valid number, resets the field and the setting to the default minimum or maximum, 
     *   depending on which is applicable.
     * </p>
     */
    private void enterMinSizeField() {
    	double maxValue;
    	if (maxSizeField.getText().equals("∞")) {
    		maxValue = Double.MAX_VALUE;
    	} else {
        	try {
            	maxValue = Double.valueOf(maxSizeField.getText());
        	} catch (NumberFormatException ex) {
        		IJ.showMessage("Invalid maximum size input format.");
        		maxValue = selectedSettings.getAnalyseMaxSize();
        		maxSizeField.setText(Double.toString(maxValue));
        	}
    	}

    	try {
    		double val = Double.parseDouble(minSizeField.getText());
            if (val < AnalysisSettings.DFL_ANALYSE_MIN_SIZE) {
            	minSizeField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MIN_SIZE));
            	selectedSettings.setAnalyseMinSize(AnalysisSettings.DFL_ANALYSE_MIN_SIZE);
            } else if (val > maxValue) {
                minSizeField.setText(Double.toString(maxValue));
                selectedSettings.setAnalyseMinSize(maxValue);
            } else {
                minSizeField.setText(Double.toString(val));
                selectedSettings.setAnalyseMinSize(val);
            }
    	} catch (NumberFormatException ex) {
    		IJ.showMessage("Invalid minimum size input format.");
            if (AnalysisSettings.DFL_ANALYSE_MIN_SIZE <= maxValue) {
                minSizeField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MIN_SIZE));
    			selectedSettings.setAnalyseMinSize(AnalysisSettings.DFL_ANALYSE_MIN_SIZE);
            } else {
                minSizeField.setText(Double.toString(maxValue));
                selectedSettings.setAnalyseMinSize(maxValue);
            }
    	}
    }
    
    /**
     * Validates the input of the maximum size text field and updates the corresponding setting, when ENTER is pressed on the input.<br>
     * If the minimum field is invalid, resets it to its last valid value.
     * 
     * <p>
     * Behavior:
     * <li> If the input is a valid number greater than or equal to the current minimum size, updates the field and the setting.
     * <li> If the input is less than the current minimum size, resets the field and the setting to the minimum size.
     * <li> If the input is not a valid number : resets to the default maximum size (displayed as "∞") 
     * if it is greater than or equal to the current minimum size. Otherwise, resets to the minimum size.
     * </p>
     */
    private void enterMaxSizeField() {
    	double minValue;
    	try {
    		minValue = Double.valueOf(minSizeField.getText());
    	} catch (NumberFormatException ex) {
    		IJ.showMessage("Invalid minimum size input format.");
    		minValue = selectedSettings.getAnalyseMinSize();
    		minSizeField.setText(Double.toString(minValue));
    	}
    	
    	try {
    		double val = Double.parseDouble(maxSizeField.getText());
            if (val < minValue) {
            	maxSizeField.setText(Double.toString(minValue));
            	selectedSettings.setAnalyseMaxSize(minValue);
            } else {
            	maxSizeField.setText(Double.toString(val));
                selectedSettings.setAnalyseMaxSize(val);
            }
    	} catch (NumberFormatException ex) {
    		if (!maxSizeField.getText().equals("∞")) {
        		IJ.showMessage("Invalid maximum size input format.");
                if (AnalysisSettings.DFL_ANALYSE_MAX_SIZE >= minValue) {
                	maxSizeField.setText("∞");
        			selectedSettings.setAnalyseMaxSize(AnalysisSettings.DFL_ANALYSE_MAX_SIZE);
                } else {
                	maxSizeField.setText(Double.toString(minValue));
                    selectedSettings.setAnalyseMaxSize(minValue);
                }
    		}
    	}
    }
    
    /**
     * Validates the input of the minimum circularity text field and updates the corresponding setting, when ENTER is pressed on the input.<br>
     * If the maximum field is invalid, resets it to its last valid value.
     * 
     * <p>
     * Behavior:
     * <li> If the input is a valid number within the allowed range, updates the field and the setting.
     * <li> If the input is less than the default minimum circularity, resets the field and the setting to the default minimum.
     * <li> If the input is greater than the current maximum circularity, resets the field and the setting to the maximum circularity.
     * <li> If the input is not a valid number, resets the field and the setting to the default minimum or maximum, 
     *   depending on which is applicable.
     * </p>
     */
    private void enterMinCircularityField() {
    	double maxValue;
    	try {
    		maxValue = Double.valueOf(maxCircularityField.getText());
    	} catch (NumberFormatException ex) {
    		IJ.showMessage("Invalid maximum circularity input format.");
    		maxValue = selectedSettings.getAnalyseMaxCircularity();
    		maxCircularityField.setText(Double.toString(maxValue));
    	}
    	
    	try {
    		double val = Double.parseDouble(minCircularityField.getText());
            if (val < AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY) {
            	minCircularityField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY));
            	selectedSettings.setAnalyseMinCircularity(AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY);
            } else if (val > maxValue) {
            	minCircularityField.setText(Double.toString(maxValue));
                selectedSettings.setAnalyseMinCircularity(maxValue);
            } else {
            	minCircularityField.setText(Double.toString(val));
                selectedSettings.setAnalyseMinCircularity(val);
            }
    	} catch (NumberFormatException ex) {
    		IJ.showMessage("Invalid minimum circularity input format.");
            if (AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY <= maxValue) {
            	minCircularityField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY));
    			selectedSettings.setAnalyseMinCircularity(AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY);
            } else {
            	minCircularityField.setText(Double.toString(maxValue));
                selectedSettings.setAnalyseMinCircularity(maxValue);
            }
    	}
    }
    
    /**
     * Validates the input of the maximum circularity text field and updates the corresponding setting, when ENTER is pressed on the input.<br>
     * If the minimum field is invalid, resets it to its last valid value.
     * 
     * <p>
     * Behavior:
     * <li> If the input is a valid number greater than or equal to the current minimum circularity, updates the field and the setting.
     * <li> If the input is less than the current minimum circularity, resets the field and the setting to the minimum circularity.
     * <li> If the input is not a valid number : resets to the default maximum circularity
     * if it is greater than or equal to the current minimum circularity. Otherwise, resets to the minimum circularity.
     * </p>
     */
    private void enterMaxCircularityField() {
    	double minValue;
    	try {
    		minValue = Double.valueOf(minCircularityField.getText());
    	} catch (NumberFormatException ex) {
    		IJ.showMessage("Invalid minimum circularity input format.");
    		minValue = selectedSettings.getAnalyseMinCircularity();
    		minCircularityField.setText(Double.toString(minValue));
    	}
    	
    	try {
    		double val = Double.parseDouble(maxCircularityField.getText());
            if (val < minValue) {
            	maxCircularityField.setText(Double.toString(minValue));
            	selectedSettings.setAnalyseMaxCircularity(minValue);
            } else {
            	maxCircularityField.setText(Double.toString(val));
                selectedSettings.setAnalyseMaxCircularity(val);
            }
    	} catch (NumberFormatException ex) {
    		IJ.showMessage("Invalid maximum circularity input format.");
            if (AnalysisSettings.DFL_ANALYSE_MAX_CIRCULARITY >= minValue) {
            	maxCircularityField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MAX_CIRCULARITY));
    			selectedSettings.setAnalyseMaxCircularity(AnalysisSettings.DFL_ANALYSE_MAX_CIRCULARITY);
            } else {
            	maxCircularityField.setText(Double.toString(minValue));
                selectedSettings.setAnalyseMaxCircularity(minValue);
            }
    	}
    }
    
    /**
     * Toggles whether droplets touching the edges of the current image
     * are excluded from analysis, based on the checkbox state.
     */
    private void toggleExcludeOnEdges() {
    	selectedSettings.setAnalyseExcludeOnEdges(excludeOnEdgesCheckbox.isSelected());
    }
    
    // MEASUREMENTS
    
    /**
     * Toggles area measurements based on the checkbox state.
     */
    private void toggleArea() {
    	selectedSettings.setShowArea(areaCheckbox.isSelected());
    }
    
    /**
     * Toggles median measurements based on the checkbox state.
     */
    private void toggleMedian() {
    	selectedSettings.setShowMedian(medianCheckbox.isSelected());
    }
    
    /**
     * Toggles mean measurements based on the checkbox state.
     */
    private void toggleMean() {
    	selectedSettings.setShowMean(meanCheckbox.isSelected());
    }
    
    /**
     * Toggles integrated density measurements based on the checkbox state.
     */
    private void toggleIntegratedDensity() {
    	selectedSettings.setShowIntegratedDensity(integratedDensityCheckbox.isSelected());
    }
    
    /**
     * Toggles circularity measurements based on the checkbox state.
     */
    private void toggleCircularity() {
    	selectedSettings.setShowCircularity(circularityCheckbox.isSelected());
    }
    
    // =========================================================================
    // ENABLING/DISABLING UI COMPONENTS
    // =========================================================================
    
    /**
     * Enables, or disables, UI components (inputs) of this particle analysis parameters panel.
     * @param enable true : enables inputs, false : disables inputs.
     */
    public void enableUIComponents(boolean enable) {
    	// PARTICLE SETTINGS
        noMaxCheckbox.setEnabled(enable);
        minSizeField.setEnabled(enable);
        maxSizeField.setEnabled(!noMaxCheckbox.isSelected());
        minCircularityField.setEnabled(enable);
        maxCircularityField.setEnabled(enable);
        excludeOnEdgesCheckbox.setEnabled(enable);
    	
    	// MEASUREMENTS
        areaCheckbox.setEnabled(enable);
        medianCheckbox.setEnabled(enable);
        meanCheckbox.setEnabled(enable);
        integratedDensityCheckbox.setEnabled(enable);
        circularityCheckbox.setEnabled(enable);
    }
    
    /**
     * Reset the particle analysis parameters panel UI components, for when the image is reseted.
     */
    public void resetUIComponents() {
    	// PARTICLE SETTINGS
    	selectedSettings.setAnalyseMinSize(AnalysisSettings.DFL_ANALYSE_MIN_SIZE);
    	selectedSettings.setAnalyseMaxSize(AnalysisSettings.DFL_ANALYSE_MAX_SIZE);
    	selectedSettings.setAnalyseMinCircularity(AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY);
    	selectedSettings.setAnalyseMaxCircularity(AnalysisSettings.DFL_ANALYSE_MAX_CIRCULARITY);
    	selectedSettings.setAnalyseExcludeOnEdges(AnalysisSettings.DFL_ANALYSE_EXCL_EDGES);
    	
        noMaxCheckbox.setSelected(true);
        minSizeField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MIN_SIZE));
        maxSizeField.setText("∞");
        minCircularityField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY));
        maxCircularityField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MAX_CIRCULARITY));
        excludeOnEdgesCheckbox.setSelected(AnalysisSettings.DFL_ANALYSE_EXCL_EDGES);
    	
    	// MEASUREMENTS
    	selectedSettings.setShowArea(AnalysisSettings.DFL_SHOWING_OPT);
    	selectedSettings.setShowMedian(AnalysisSettings.DFL_SHOWING_OPT);
    	selectedSettings.setShowMean(AnalysisSettings.DFL_SHOWING_OPT);
    	selectedSettings.setShowIntegratedDensity(AnalysisSettings.DFL_SHOWING_OPT);
    	selectedSettings.setShowCircularity(AnalysisSettings.DFL_SHOWING_OPT);
    	
        areaCheckbox.setSelected(AnalysisSettings.DFL_SHOWING_OPT);
        medianCheckbox.setSelected(AnalysisSettings.DFL_SHOWING_OPT);
        meanCheckbox.setSelected(AnalysisSettings.DFL_SHOWING_OPT);
        integratedDensityCheckbox.setSelected(AnalysisSettings.DFL_SHOWING_OPT);
        circularityCheckbox.setSelected(AnalysisSettings.DFL_SHOWING_OPT);
    }
    
    // =========================================================================
    // UPDATING CURRENT INPUT VALUES
    // =========================================================================
    
    /**
     * Synchronize the {@link LDCService} with current particle analysis input values.<br>
     * It considers only min/max size and circularity inputs, as measures checkboxes are always kept synchronized with the service.
     */
    public void updateInputValues() {
    	enterMinSizeField();
    	enterMaxSizeField();
    	enterMinCircularityField();
    	enterMaxCircularityField();
    }
}
