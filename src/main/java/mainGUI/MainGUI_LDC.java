package mainGUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import mainGUI.panels.LeftPanel;
import mainGUI.panels.RightPanel;
import model.LDCService;

/**
 * The plugin main GUI.
 */
@SuppressWarnings("serial")
public class MainGUI_LDC extends JFrame {
    
	@Parameter
    private LDCService selectedSettings;
    
    private final JPanel leftContent;
    private final JPanel rightContent;
    
    public MainGUI_LDC(final Context ctx) {
        ctx.inject(this);
    	
        this.leftContent = new LeftPanel(ctx);
        this.rightContent = new RightPanel(ctx);
        this.leftContent.setPreferredSize(new Dimension(0, 0));
        this.rightContent.setPreferredSize(new Dimension(0, 0));
        
        // Initialization of the MainGUI itself
        
        //Set the initial size
        setSize(800, 600);
        
        //Set the MINIMUM size (User cannot resize smaller than this)
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        
        setTitle("Lipid Droplets Characterization");

        // --- Layout Configuration (GridBagLayout) ---
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // General settings for all components
        gbc.fill = GridBagConstraints.BOTH; // Fill available space vertically and horizontally
        gbc.weighty = 1.0;                  // Give full vertical height
        
        //Adding Left Panel
        gbc.gridx = 0;
        gbc.weightx = 0.5; // Allocate 50% of width for leftPanel
        getContentPane().add(leftContent, gbc);
        
        //Adding Vertical Separator
        gbc.gridx = 1;
        gbc.weightx = 0.0; //no extra width to keep it thin
        gbc.insets = new Insets(0, 5, 0, 5);  // small padding around the line if desired

        
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setForeground(Color.GRAY); // Optional: Make it more visible
        getContentPane().add(sep, gbc);
        
        //Adding Right Panel
        gbc.gridx = 2;
        gbc.weightx = 0.5; // Allocate remaining 50% of width for rightPanel
        getContentPane().add(rightContent, gbc);
    }
}