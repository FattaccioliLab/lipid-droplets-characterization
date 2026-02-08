package mainGUI.panels.subpanels.leftpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import mainGUI.panels.LeftPanel;
import mainGUI.utils.PanelUtils;
import model.AnalysisSettings;
import model.LDCService;

@SuppressWarnings("serial")
public class ParticleAnalysisParamsPanel extends JPanel {

    private LeftPanel leftPanel;

    @Parameter
    private LDCService selectedSettings;
    
    // Components
    private JPanel particlesSettings;
    private JCheckBox excludeOnEdgesCheckbox;
    
    private JPanel mesurementsInfos;
    private JCheckBox areaCheckbox;
    private JCheckBox medianCheckbox;
    private JCheckBox meanCheckbox;
    private JCheckBox integratedDensityCheckbox;
    private JCheckBox circularityCheckbox;
    
    public ParticleAnalysisParamsPanel(Context ctx, LeftPanel leftPanel) {
        super();
        this.leftPanel = leftPanel;
        ctx.inject(this);
        
        PanelUtils.createVerticalPanel(this, "Particle analysis parameters", 500);
        
        // PARTICLES SETTINGS
        
        particlesSettings = PanelUtils.createVerticalPanel("Particles settings", 250);
        
	    // --- Exclude on edges section ---
        excludeOnEdgesCheckbox = new JCheckBox("Exclude on edges");
        excludeOnEdgesCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        excludeOnEdgesCheckbox.setSelected(selectedSettings.showAreaEnabled());
        excludeOnEdgesCheckbox.addActionListener(e -> toggleExcludeOnEdges());
        
        // MEASUREMENTS
        
        mesurementsInfos = PanelUtils.createVerticalPanel("Measurements", 250);
        
	    // --- Show area section ---
        areaCheckbox = new JCheckBox("Show area measures");
        areaCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        areaCheckbox.setSelected(selectedSettings.showAreaEnabled());
        areaCheckbox.addActionListener(e -> toggleArea());

	    // --- Show equivalent diameter section ---
        medianCheckbox = new JCheckBox("Show median measures");
        medianCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        medianCheckbox.setSelected(selectedSettings.showMedianEnabled());
        medianCheckbox.addActionListener(e -> toggleMedian());

	    // --- Show mean section ---
        meanCheckbox = new JCheckBox("Show mean measures");
        meanCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        meanCheckbox.setSelected(selectedSettings.showMeanEnabled());
        meanCheckbox.addActionListener(e -> toggleMean());

	    // --- Show integrated density section ---
	    integratedDensityCheckbox = new JCheckBox("Show integrated density measures");
	    integratedDensityCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
	    integratedDensityCheckbox.setSelected(selectedSettings.showIntegratedDensityEnabled());
	    integratedDensityCheckbox.addActionListener(e -> toggleIntegratedDensity());

	    // --- Show circularity section ---
	    circularityCheckbox = new JCheckBox("Show circularity measures");
	    circularityCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
	    circularityCheckbox.setSelected(selectedSettings.showCircularityEnabled());
	    circularityCheckbox.addActionListener(e -> toggleCircularity());

	    // LAYOUT
	    
	    add(Box.createVerticalStrut(5));
	    
	    particlesSettings.add(excludeOnEdgesCheckbox);
	    add(particlesSettings);
	    
	    
	    add(Box.createVerticalStrut(10));
	    
	    mesurementsInfos.add(areaCheckbox);
	    mesurementsInfos.add(Box.createVerticalStrut(10));
	    mesurementsInfos.add(medianCheckbox);
	    mesurementsInfos.add(Box.createVerticalStrut(10));
	    mesurementsInfos.add(meanCheckbox);
	    mesurementsInfos.add(Box.createVerticalStrut(10));
	    mesurementsInfos.add(integratedDensityCheckbox);
	    mesurementsInfos.add(Box.createVerticalStrut(10));
	    mesurementsInfos.add(circularityCheckbox);
	    add(mesurementsInfos);
	    
	    add(Box.createVerticalStrut(5));
    }
    
    // =========================================================================
    // UI ACTIONS
    // =========================================================================
    
    private void toggleExcludeOnEdges() {
    	selectedSettings.setAnalyseExcludeOnEdges(excludeOnEdgesCheckbox.isSelected());
    }
    
    private void toggleArea() {
    	selectedSettings.setShowArea(areaCheckbox.isSelected());
    }
    
    private void toggleMedian() {
    	selectedSettings.setShowMedian(medianCheckbox.isSelected());
    }
    
    private void toggleMean() {
    	selectedSettings.setShowMean(meanCheckbox.isSelected());
    }
    
    private void toggleIntegratedDensity() {
    	selectedSettings.setShowIntegratedDensity(integratedDensityCheckbox.isSelected());
    }
    
    private void toggleCircularity() {
    	selectedSettings.setShowCircularity(circularityCheckbox.isSelected());
    }
    
    public void resetUIComponents() {
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
    
    public void enableUIComponents(boolean enable) {
        areaCheckbox.setEnabled(enable);
        medianCheckbox.setEnabled(enable);
        meanCheckbox.setEnabled(enable);
        integratedDensityCheckbox.setEnabled(enable);
        circularityCheckbox.setEnabled(enable);
    }
}
