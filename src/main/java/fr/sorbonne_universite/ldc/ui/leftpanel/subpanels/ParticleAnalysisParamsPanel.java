package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fr.sorbonne_universite.ldc.model.AnalysisSettings;
import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.utils.PanelUtils;
import ij.IJ;
import ij.measure.Calibration;

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
    
    // Calibration settings
    private JCheckBox isCalibratedCheckbox;
    private JTextField unitField;
    private JSpinner manualCalibrationSpinner;
    private JButton resetCalibrationButton;
    private double defaultPixelSize = 1.0;
    private String defaultUnit;
    
    // Particle settings
    private JCheckBox noMaxCheckbox;
    private JLabel sizeLabel;
    private JTextField minSizeField;
    private JTextField maxSizeField;
    private JTextField minCircularityField;
    private JTextField maxCircularityField;
    private JCheckBox excludeOnEdgesCheckbox;
    private JSpinner circularityThresholdSpinner;
    
    // Measurements
    private JPanel measurementsInfos;
    private JCheckBox areaCheckbox;
    private JCheckBox diameterCheckbox;
    private JCheckBox medianCheckbox;
    private JCheckBox meanCheckbox;
    private JCheckBox integratedDensityCheckbox;
    private JCheckBox circularityCheckbox;
    
    public ParticleAnalysisParamsPanel(Context ctx) {
        super();
        ctx.inject(this);
        
        PanelUtils.createVerticalPanel(this, "Particle analysis parameters", 800);
        
        // CALIBRATION SETTINGS
                
        JPanel calibrationPanel = new JPanel(new GridBagLayout());
        calibrationPanel.setBorder(
        		BorderFactory.createCompoundBorder(
        				BorderFactory.createEmptyBorder(0, 8, 0, 8),
        				BorderFactory.createTitledBorder("Calibration settings")
        				)
        		);
        calibrationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        calibrationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        // initialize the isCalibrated check box
        isCalibratedCheckbox = new JCheckBox("Calibrate the image");
        isCalibratedCheckbox.setSelected(selectedSettings.isCalibrated());
        isCalibratedCheckbox.setFocusPainted(false);
        isCalibratedCheckbox.addActionListener(e -> toggleIsCalibrated());
        
        // update the defaults values
        updateDefaultCalibration();
        
        // initialize the unit field
        unitField = new JTextField(defaultUnit, 4);
        unitField.setHorizontalAlignment(JTextField.CENTER);
        unitField.setEnabled(isCalibratedCheckbox.isSelected());
        
        unitField.addActionListener(e -> enterManualCalibrationSpinner());
        
        // initialize the manual calibration spinner
        manualCalibrationSpinner = new JSpinner(new SpinnerNumberModel(defaultPixelSize, 0.0001, 10000.0, 0.0000001));
        manualCalibrationSpinner.setEditor(new JSpinner.NumberEditor(manualCalibrationSpinner, "0.0000000"));
        manualCalibrationSpinner.setEnabled(isCalibratedCheckbox.isSelected());
        manualCalibrationSpinner.addChangeListener(e -> enterManualCalibrationSpinner());
        
        // keep the user from entering non numeric values
        JFormattedTextField calTxt = ((JSpinner.DefaultEditor) manualCalibrationSpinner.getEditor()).getTextField();
        calTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!(Character.isDigit(c) || c == '.' || c == ',' || c == java.awt.event.KeyEvent.VK_BACK_SPACE)) {
                    e.consume();
                }
            }
        });
        
        // reset calibration button
        resetCalibrationButton = new JButton("Reset");
        resetCalibrationButton.setMargin(new Insets(2, 5, 2, 5));
        resetCalibrationButton.setEnabled(isCalibratedCheckbox.isSelected());
        resetCalibrationButton.addActionListener(e -> resetManualCalibrationSpinner());
        
        // GridBagLayout setup
        GridBagConstraints cCalib = new GridBagConstraints();
        cCalib.insets = new Insets(2, 5, 2, 5);
        cCalib.fill = GridBagConstraints.HORIZONTAL;
        cCalib.anchor = GridBagConstraints.WEST;
        
        // first line : isCalibratedCheckbox
        cCalib.gridy = 0; 
        cCalib.gridx = 0;
        cCalib.gridwidth = 3;
        cCalib.weightx = 1.0; 
        calibrationPanel.add(isCalibratedCheckbox, cCalib);
        
        // second line : label + unit field + per pixel label
        cCalib.gridy = 1;
        cCalib.gridwidth = 1;
        
        cCalib.gridx = 0;
        cCalib.weightx = 0.0;
        calibrationPanel.add(new JLabel("Unit:"), cCalib);
        
        cCalib.gridx = 1;
        cCalib.weightx = 1.0;
        calibrationPanel.add(unitField, cCalib);
        
        cCalib.gridx = 2;
        cCalib.weightx = 0.0;
        calibrationPanel.add(new JLabel("/px"), cCalib);
        
        // third line : label + Spinner + reset button
        cCalib.gridy = 2; 
        cCalib.gridwidth = 1;
        
        cCalib.gridx = 0;
        cCalib.weightx = 0.0;
        calibrationPanel.add(new JLabel("Pixel size (unit/px):"), cCalib);
        
        cCalib.gridx = 1;
        cCalib.weightx = 1.0;
        calibrationPanel.add(manualCalibrationSpinner, cCalib);
                
        cCalib.gridx = 2;
        cCalib.weightx = 0.0;
        calibrationPanel.add(resetCalibrationButton, cCalib);

        // PARTICLE SETTINGS
        
        JPanel particlesSettings = PanelUtils.createVerticalPanel("Particle settings", 400);
        
        // --- Size section ---
        
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
        String unit = selectedSettings.isCalibrated() ? "μm" : "px";
        sizeLabel = new JLabel("Particle size (" + unit + "²)", JLabel.CENTER);
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
        
        
        // --- Circularity threshold section ---
        
        JPanel circularityThresholdPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        
        // label
        JLabel circularityThresholdLabel = new JLabel("Circularity threshold for isolation");
        circularityThresholdPanel.add(circularityThresholdLabel);
        
        // spinner
        circularityThresholdSpinner = new JSpinner(
        		new SpinnerNumberModel(AnalysisSettings.DFL_ANALYSE_CIRC_THRESHOLD, 0.0, 1.0, 0.01));
        // showing two decimals
        circularityThresholdSpinner.setEditor(new JSpinner.NumberEditor(circularityThresholdSpinner, "0.00"));
        
        // update the threshold
        circularityThresholdSpinner.addChangeListener(e -> enterCircularityThresholdSpinner());
        
        // keep the user from entering non numeric values
        JFormattedTextField txt = ((JSpinner.DefaultEditor) circularityThresholdSpinner.getEditor()).getTextField();
        txt.addKeyListener(new java.awt.event.KeyAdapter() {
        	public void keyTyped(java.awt.event.KeyEvent e) {
        		char c = e.getKeyChar();
        		if (!(Character.isDigit(c) || c == '.' || c == ',' || c == java.awt.event.KeyEvent.VK_BACK_SPACE)) {
        			e.consume();
        		}
        	}
        });
        
        circularityThresholdPanel.add(circularityThresholdSpinner);
        circularityThresholdPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // MEASUREMENTS
        
        measurementsInfos = PanelUtils.createVerticalPanel("Measurements", 200);
        
	    // --- Show area section ---
        areaCheckbox = new JCheckBox("Show area measures");
        areaCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        areaCheckbox.setSelected(selectedSettings.showAreaEnabled());
        areaCheckbox.setFocusPainted(false);
        areaCheckbox.addActionListener(e -> toggleArea());
        
	    // --- Show diameter section ---
        diameterCheckbox = new JCheckBox("Show diameter measures");
        diameterCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        diameterCheckbox.setSelected(selectedSettings.showAreaEnabled());
        diameterCheckbox.setFocusPainted(false);
        diameterCheckbox.addActionListener(e -> toggleDiameter());

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
	    
	    add(Box.createVerticalStrut(10));
	    
	    add(calibrationPanel);
	    
	    particlesSettings.add(Box.createVerticalStrut(10));
	    particlesSettings.add(sizePanel);
	    particlesSettings.add(Box.createVerticalStrut(10));
	    particlesSettings.add(circularityPanel);
	    particlesSettings.add(Box.createVerticalStrut(10));
	    particlesSettings.add(excludePanel);
	    particlesSettings.add(Box.createVerticalStrut(10));
	    particlesSettings.add(circularityThresholdPanel);
	    add(particlesSettings);
	    
	    
	    // --- CREATE A HORIZONTAL CONTAINER FOR THE COLUMNS ---
	    add(Box.createVerticalStrut(10));
        JPanel checkboxColumnsContainer = new JPanel();
        checkboxColumnsContainer.setLayout(new BoxLayout(checkboxColumnsContainer, BoxLayout.X_AXIS));
        checkboxColumnsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- LEFT COLUMN (First 3 items) ---
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setAlignmentY(Component.TOP_ALIGNMENT); // Aligns column to the top
        
        leftColumn.add(areaCheckbox);
        leftColumn.add(Box.createVerticalStrut(5));
        leftColumn.add(diameterCheckbox);
        leftColumn.add(Box.createVerticalStrut(5));
        leftColumn.add(medianCheckbox);

        // --- RIGHT COLUMN (Rest of the items) ---
        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setAlignmentY(Component.TOP_ALIGNMENT); // Aligns column to the top
        
        rightColumn.add(meanCheckbox);
        rightColumn.add(Box.createVerticalStrut(5));
        rightColumn.add(integratedDensityCheckbox);
        rightColumn.add(Box.createVerticalStrut(5));
        rightColumn.add(circularityCheckbox);

        // --- ADD COLUMNS TO CONTAINER WITH A GAP BETWEEN THEM ---
        checkboxColumnsContainer.add(leftColumn);
        checkboxColumnsContainer.add(Box.createHorizontalStrut(30)); // 30px gap between left and right columns
        checkboxColumnsContainer.add(rightColumn);

        // --- ADD TO THE MAIN MEASUREMENTS PANEL ---
        measurementsInfos.add(checkboxColumnsContainer);
        add(measurementsInfos);
	    
	    add(Box.createVerticalStrut(10));
    }
    
    // =========================================================================
    // UI ACTIONS
    // =========================================================================
    
    // CALIBRATION SETTINGS
    
    /**
     * Toggles the manual calibration field and updates the LDCService state.
     */
    private void toggleIsCalibrated() {
    	boolean isSelected = isCalibratedCheckbox.isSelected();
    	unitField.setEnabled(isSelected);
    	manualCalibrationSpinner.setEnabled(isSelected);
    	resetCalibrationButton.setEnabled(isSelected);
    	selectedSettings.setIsCalibrated(isSelected);
        
    	String unit = "px";
    	if (isSelected) {
    		manualCalibrationSpinner.setEnabled(true);
    		enterManualCalibrationSpinner();
    		
    		unit = unitField.getText().trim();
            if(unit.isEmpty() || unit.equals("pixel")) unit = "µm";
    	}
    	
        sizeLabel.setText("Particle size (" + unit + "²)");
    }

    /**
     * Updates the Calibration in the AnalysisSettings when the user changes the pixel size.
     */
    private void enterManualCalibrationSpinner() {
        if (!isCalibratedCheckbox.isSelected()) return;

        double pixelSize = (double) manualCalibrationSpinner.getValue();
        String currentUnit = unitField.getText().trim();
        
        Calibration cal = selectedSettings.getCalibration();
        if (cal == null) {
            cal = new Calibration();
        }
        cal.pixelWidth = pixelSize;
        cal.pixelHeight = pixelSize;
        
        if (currentUnit.isEmpty()) {
            currentUnit = "pixel"; 
        }
        cal.setUnit(currentUnit);
        
        selectedSettings.setCalibration(cal);
        
        // update the unit in sizeLabel
        String displayUnit = currentUnit.equals("pixel") ? "px" : currentUnit;
        sizeLabel.setText("Particle size (" + displayUnit + "²)");
    }
    
    /**
     * Update the default pixel size and unit for the calibration, try to take them from the image if it is already
     * calibrated, otherwise take 1.0 and µm.
     */
    private void updateDefaultCalibration() {
    	// get the default pixel size if the image is already calibrated
        Calibration cal = selectedSettings.getCalibration();
        if (cal != null && cal.scaled()) {
        	defaultUnit = cal.getUnit();
            defaultPixelSize = cal.pixelWidth;
        } else {
        	defaultUnit = "µm";
        	defaultPixelSize = 1.0;
        }
    }
    
    /**
     * Reset the value of the calibration spinner.
     */
    private void resetManualCalibrationSpinner() {
    	unitField.setText(defaultUnit);
        manualCalibrationSpinner.setValue(defaultPixelSize);
        enterManualCalibrationSpinner();
    }
    
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
    
    /**
     * Update the circularity threshold with the value of the circularity threshold spinner.
     */
    private void enterCircularityThresholdSpinner() {
    	selectedSettings.setAnalyseCircularityThreshold((double)circularityThresholdSpinner.getValue());
    }
    
    // MEASUREMENTS
    
    /**
     * Toggles area measurements based on the checkbox state.
     */
    private void toggleArea() {
    	selectedSettings.setShowArea(areaCheckbox.isSelected());
    }
    
    /**
     * Toggles diameter measurements based on the checkbox state.
     */
    private void toggleDiameter() {
    	selectedSettings.setShowDiameter(diameterCheckbox.isSelected());
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
    	// CALIBRATION SETTINGS
    	isCalibratedCheckbox.setEnabled(enable);
    	unitField.setEnabled(enable && isCalibratedCheckbox.isSelected());
    	manualCalibrationSpinner.setEnabled(enable && isCalibratedCheckbox.isSelected());
    	resetCalibrationButton.setEnabled(enable && isCalibratedCheckbox.isSelected());
    	
    	// PARTICLE SETTINGS
        noMaxCheckbox.setEnabled(enable);
        minSizeField.setEnabled(enable);
        maxSizeField.setEnabled(!noMaxCheckbox.isSelected());
        minCircularityField.setEnabled(enable);
        maxCircularityField.setEnabled(enable);
        excludeOnEdgesCheckbox.setEnabled(enable);
        circularityThresholdSpinner.setEnabled(enable);
    	
    	// MEASUREMENTS
        areaCheckbox.setEnabled(enable);
        diameterCheckbox.setEnabled(enable);
        medianCheckbox.setEnabled(enable);
        meanCheckbox.setEnabled(enable);
        integratedDensityCheckbox.setEnabled(enable);
        circularityCheckbox.setEnabled(enable);
    }
    
    /**
     * Reset the particle analysis parameters panel UI components, for when the image is reseted.
     */
    public void resetUIComponents() {
    	// CALIBRATION SETTINGS
    	isCalibratedCheckbox.setSelected(selectedSettings.isCalibrated());
    	updateDefaultCalibration();
    	unitField.setText(defaultUnit);
		manualCalibrationSpinner.setValue(defaultPixelSize);
    	manualCalibrationSpinner.setEnabled(isCalibratedCheckbox.isSelected());
    	
    	// PARTICLE SETTINGS
    	selectedSettings.setAnalyseMinSize(AnalysisSettings.DFL_ANALYSE_MIN_SIZE);
    	selectedSettings.setAnalyseMaxSize(AnalysisSettings.DFL_ANALYSE_MAX_SIZE);
    	selectedSettings.setAnalyseMinCircularity(AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY);
    	selectedSettings.setAnalyseMaxCircularity(AnalysisSettings.DFL_ANALYSE_MAX_CIRCULARITY);
    	selectedSettings.setAnalyseExcludeOnEdges(AnalysisSettings.DFL_ANALYSE_EXCL_EDGES);
    	selectedSettings.setAnalyseCircularityThreshold(AnalysisSettings.DFL_ANALYSE_CIRC_THRESHOLD);
    	
        noMaxCheckbox.setSelected(true);
        minSizeField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MIN_SIZE));
        maxSizeField.setText("∞");
        minCircularityField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MIN_CIRCULARITY));
        maxCircularityField.setText(Double.toString(AnalysisSettings.DFL_ANALYSE_MAX_CIRCULARITY));
        excludeOnEdgesCheckbox.setSelected(AnalysisSettings.DFL_ANALYSE_EXCL_EDGES);
        circularityThresholdSpinner.setValue(AnalysisSettings.DFL_ANALYSE_CIRC_THRESHOLD);
    	
    	// MEASUREMENTS
    	selectedSettings.setShowArea(AnalysisSettings.DFL_SHOWING_OPT);
    	selectedSettings.setShowDiameter(AnalysisSettings.DFL_SHOWING_OPT);
    	selectedSettings.setShowMedian(AnalysisSettings.DFL_SHOWING_OPT);
    	selectedSettings.setShowMean(AnalysisSettings.DFL_SHOWING_OPT);
    	selectedSettings.setShowIntegratedDensity(AnalysisSettings.DFL_SHOWING_OPT);
    	selectedSettings.setShowCircularity(AnalysisSettings.DFL_SHOWING_OPT);
    	
        areaCheckbox.setSelected(AnalysisSettings.DFL_SHOWING_OPT);
        diameterCheckbox.setSelected(AnalysisSettings.DFL_SHOWING_OPT);
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
    	enterManualCalibrationSpinner();
    	enterMinSizeField();
    	enterMaxSizeField();
    	enterMinCircularityField();
    	enterMaxCircularityField();
    	enterCircularityThresholdSpinner();
    }
}
