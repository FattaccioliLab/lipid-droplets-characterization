package fr.sorbonne_universite.ldc.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;
import fr.sorbonne_universite.ldc.ui.rightpanel.RightPanel;
import ij.ImagePlus;

/**
 * The plugin main GUI.
 */
@SuppressWarnings("serial")
public class MainGUI_LDC extends JFrame {
    
	private static final int WINDOW_WIDTH = 1200;
	private static final int WINDOW_HEIGHT = 900;
	
	@Parameter
    private LDCService selectedSettings;
    
    private final LeftPanel leftContent;
    private final RightPanel rightContent;
    
    
    // The current ImagePlus considered
    private ImagePlus currentImage = null;
    // Reference to the original ImagePlus considered
    private ImagePlus originalImage = null;
    
    public MainGUI_LDC(final Context ctx) {
        ctx.inject(this);
    	
        this.leftContent = new LeftPanel(ctx, this);
        this.rightContent = new RightPanel(ctx, this.leftContent);
        this.leftContent.setPreferredSize(new Dimension(0, 0));
        this.rightContent.setPreferredSize(new Dimension(0, 0));
        
        // Initialization of the MainGUI itself
        
        //Set the initial size
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        
        //Set the MINIMUM size (User cannot resize smaller than this)
        setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
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
    
    // =========================================================================
    // Image management getters / setters
    // =========================================================================
    
    /** @param originalImg The original {@link ImagePlus}, before any process on it */
    public void setOriginalImage(ImagePlus originalImg) { this.originalImage = originalImg; }
    
    /** @return The original image. Can be {@code null} if no image currently opened. */
    public ImagePlus getOriginalImage() { return originalImage; }
    
    /** @param currentImg The currently considered {@link ImagePlus}. */
    public void setCurrentImage(ImagePlus currentImg) { this.currentImage = currentImg; }
    
    /** @return The currently considered {@link ImagePlus}. Can be {@code null} if no image currently considered. */
    public ImagePlus getCurrentImage() { return this.currentImage; }
}