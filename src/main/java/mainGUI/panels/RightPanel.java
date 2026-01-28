package mainGUI.panels;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import org.scijava.event.EventService;

import io.scif.services.DatasetIOService;
import model.LDCService;
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
    
    @Parameter
    private LDCService selectedSettings;
	
    public RightPanel(Context ctx) {
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
        	SwingWorker<Void,Void> measuresWorker = selectedSettings.createMeasuresProcessingWorker();
        	measuresWorker.execute();
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