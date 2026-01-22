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
import javax.swing.text.NumberFormatter;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import org.scijava.event.EventService;

import io.scif.services.DatasetIOService;
import model.AnalysisSettings;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;

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
	
    public RightPanel(final Context ctx,  AnalysisSettings selectedSettings) {
    	
    	ctx.inject(this);
    	
    	// Layout
    	setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GREEN, 2)
        ));
       
           
        // Measures view Panel at the CENTER
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        
        // PLACEHOLDER data
        String[] columns = {"x", "y", "intensity", "radius", "circularity"};
        Object[][] data = {
        		{1,2,3,4,5},
        		{1,2,5,4,5},
        		{1,2,3,4,5},
        		{1,2,3,4,5},
        		{1,2,3,4,5},
        		{1,2,3,4,5},
        		{1,2,3,4,5},
        		{1,2,3,4,5},
        		{1,2,3,4,5},
        		{1,2,3,4,5},
        };
        
        JTable measuresTable = new JTable(data, columns);
        measuresTable.setAutoCreateRowSorter(true);
        JScrollPane scrollTable = new JScrollPane(measuresTable); 
        
        viewPanel.add(scrollTable, BorderLayout.CENTER);
       
        add(viewPanel , BorderLayout.CENTER);
        
        
        // measures treatment panel at the SOUTH
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayout(1, 3));
        
        // generate histograms button
        JButton histogramsButton = new JButton("histograms");
        histogramsButton.addActionListener(e -> {
        	// TODO
        });
        actionPanel.add(histogramsButton);
        
        // generate statistics button
        JButton statisticsButton = new JButton("statistics");
        statisticsButton.addActionListener(e -> {
        	// TODO
        });
        actionPanel.add(statisticsButton);
        
        // export as csv button
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> {
        	// TODO
        });
        actionPanel.add(exportButton);
                
        add(actionPanel, BorderLayout.SOUTH);
        
        
    }
    
   
    
}