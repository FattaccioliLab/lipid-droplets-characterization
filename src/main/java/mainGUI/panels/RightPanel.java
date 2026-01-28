package mainGUI.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.text.NumberFormatter;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import core.MeasuresProcessing;

import org.scijava.event.EventService;

import io.scif.services.DatasetIOService;
import model.AnalysisSettings;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;

import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ParticleAnalyzer;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.Measurements;
import ij.measure.ResultsTable;

@SuppressWarnings("serial")
public class RightPanel extends JPanel {

	@Parameter
	private DatasetIOService datasetIOService;
	
	@Parameter(type = ItemIO.OUTPUT)
	private Dataset image;
	
	@Parameter
	private UIService uiService;
	
	@Parameter
	private ImageDisplayService displayService;
	
    @Parameter
    private EventService eventService;
	
	private boolean canModifyPretreatments;
	
	private JToggleButton toggleEC;
	private JToggleButton toggleMF;
	private JFormattedTextField saturatedField;
	
	private AnalysisSettings selectedSettings;
	private MeasuresProcessing mp;
	
    public RightPanel(Context ctx) {
    public RightPanel(final Context ctx,  AnalysisSettings selectedSettings) {
    	
    	this.selectedSettings = selectedSettings;
    	this.mp = new MeasuresProcessing(selectedSettings);
    	
    	ctx.inject(this);
    	
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GREEN, 2)
        ));
 
        // measures treatment panel
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayout(3, 1));
        
        // show measures button
        JButton resultsButton = new JButton("show results");
        resultsButton.addActionListener(e -> {
        	new Thread(() -> {
                mp.generateMeasures();
            }).start();
        });
        actionPanel.add(resultsButton);
        
        // generate histograms button
        JButton histogramsButton = new JButton("histograms");
        histogramsButton.addActionListener(e -> {
        	// TODO
        });
        actionPanel.add(histogramsButton);
        
        add(actionPanel);
        
        
    }
    
}