package mainGUI;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.scijava.Context;

import mainGUI.panels.CenterPanel;
import mainGUI.panels.LeftPanel;
import mainGUI.panels.RightPanel;

/**
 * The plugin main GUI.
 */
@SuppressWarnings("serial")
public class MainGUI_LDC extends JDialog {

	private final JPanel leftContent = new LeftPanel();
	private final JPanel centerContent = new CenterPanel();
	private final JPanel rightContent = new RightPanel();
	
	public MainGUI_LDC(final Context ctx) {
		
		int gridLines = 1;
		int gridColumns = 3;
		
		// Initialization of the MainGUI itself
		ctx.inject(this);
	    setSize(1200, 700);
	    setLocationRelativeTo(null);
		getContentPane().setLayout(new GridLayout(gridLines, gridColumns));
		setTitle("Lipid Droplets Characterization");
		
		// Initialization of the left content panel
		getContentPane().add(leftContent);
		
		// Initialization of the center content panel
		getContentPane().add(centerContent);
		
		// Initialization of the right content panel
		getContentPane().add(rightContent);
	}
}
