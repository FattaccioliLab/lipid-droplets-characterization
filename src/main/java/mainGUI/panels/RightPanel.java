package mainGUI.panels;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import model.AnalysisSettings;

/**
 * The right side of the plugin main GUI.
 * It contains a preview of generated measures, according to the selected setting.
 */
@SuppressWarnings("serial")
public class RightPanel extends JPanel {
	
	private AnalysisSettings selectedSettings;

    public RightPanel(AnalysisSettings selectedSettings) {
		
		this.selectedSettings = selectedSettings;
		
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GREEN, 2)
        ));
        
    }
    
}