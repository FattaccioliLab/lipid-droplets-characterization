package mainGUI.panels;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import org.scijava.event.EventService;

import io.scif.services.DatasetIOService;
import model.LDCService;
import model.workers.measures.MeasuresProcessing;
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
	
	private MeasuresProcessing mp;
	
    public RightPanel(Context ctx) {
    	
    	ctx.inject(this);
    	
    	this.mp = new MeasuresProcessing();
    	
    	
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
                mp.generateMeasures(selectedSettings.showAreaEnabled(), selectedSettings.showEquivalentDiameterEnabled(),
                		selectedSettings.showMeanEnabled(), selectedSettings.showIntegratedDensityEnabled(),
                		selectedSettings.showCircularityEnabled(), selectedSettings.analyseExcludeOnEdgesEnabled());
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