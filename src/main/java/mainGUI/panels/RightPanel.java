package mainGUI.panels;

import java.awt.Color;
import java.awt.FlowLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GREEN, 2)
        ));
        
        
    }
    
   
    
}