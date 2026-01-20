package mainGUI;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.scijava.Context;

import mainGUI.panels.LeftPanel;
import mainGUI.panels.RightPanel;
import model.AnalysisSettings;

/**
 * The plugin main GUI.
 */
@SuppressWarnings("serial")
public class MainGUI_LDC extends JDialog {
	
	private AnalysisSettings selectedSettings = new AnalysisSettings();
	
	private final JPanel leftContent;
	private final JPanel rightContent;
	
	public MainGUI_LDC(final Context ctx) {
		this.leftContent = new LeftPanel(ctx, selectedSettings);
		this.rightContent = new RightPanel(ctx, selectedSettings);
		
		int gridLines = 1;
		int gridColumns = 2;
		
		// Initialization of the MainGUI itself
		ctx.inject(this);
	    setSize(800, 600);
	    setLocationRelativeTo(null);
		getContentPane().setLayout(new GridLayout(gridLines, gridColumns));
		setTitle("Lipid Droplets Characterization");
		
		// Initialization of the center content panel
		getContentPane().add(leftContent);
		
		// Initialization of the right content panel
		getContentPane().add(rightContent);
	}
}
