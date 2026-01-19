package mainGUI;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.scijava.Context;

import mainGUI.panels.LeftPanel;
import mainGUI.panels.RightPanel;

/**
 * The plugin main GUI.
 */
@SuppressWarnings("serial")
public class MainGUI_LDC extends JFrame {

	private final JPanel leftContent;
	private final JPanel rightContent;
	
	public MainGUI_LDC(final Context ctx) {
		
		int gridLines = 1;
		int gridColumns = 2;
		
		// Initialization of the MainGUI itself
		ctx.inject(this);
	    setSize(800, 600);
	    setLocationRelativeTo(null);
		getContentPane().setLayout(new GridLayout(gridLines, gridColumns));
		setTitle("Lipid Droplets Characterization");
		
		// Initialization of the left content panel
		leftContent = new LeftPanel(ctx);
		getContentPane().add(leftContent);
		
		// Initialization of the right content panel
		rightContent = new RightPanel(ctx);
		getContentPane().add(rightContent);
	}
}
