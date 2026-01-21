package mainGUI.panels;

import javax.swing.JPanel;
import org.scijava.Context;
import model.AnalysisSettings;


/**
 * Part of the main GUI containing the pretreatments of the plugin
 */
@SuppressWarnings("serial")
public class RightPanel extends JPanel {
	
    public RightPanel(Context ctx, AnalysisSettings selectedSettings) {
    	ctx.inject(this);
    }
    
}