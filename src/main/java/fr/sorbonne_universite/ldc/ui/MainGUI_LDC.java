package fr.sorbonne_universite.ldc.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

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
    
	public static final int WINDOW_WIDTH = 1000;
	public static final int WINDOW_HEIGHT = 800;
    
    public static final int PREPROCESSING_STEP = 0;
    public static final int THRESHOLDING_STEP = 1;
    public static final int MORPHOLOGICAL_STEP = 2;
    public static final int ANALYSIS_PARAMETERS_STEP = 3;
    
    @Parameter
    private LDCService selectedSettings;
    
    private final LeftPanel leftContent;
    private final RightPanel rightContent;
    
    /** Reference to the original ImagePlus considered. */
    private ImagePlus originalImage = null;
    /** The current ImagePlus considered. */
    private ImagePlus currentImage = null;
    /** The mask attached to the currently considered image. */
    private ImagePlus mask = null;
    /** The preview window of the right panel */
    private ImagePlus previewWindow = null;
    
    public MainGUI_LDC(final Context ctx) {
        ctx.inject(this);
        
        this.leftContent = new LeftPanel(ctx, this);
        this.rightContent = new RightPanel(ctx, this.leftContent);
        
        // --- Enforce Minimum Widths ---
        // JSplitPane will absolutely refuse to drag past these minimums, 
        // protecting your Left Panel from getting squished.
        this.leftContent.setMinimumSize(new Dimension(470, 0)); 
        this.rightContent.setMinimumSize(new Dimension(400, 0)); 
        
        // Initialization of the MainGUI itself
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setTitle("Lipid Droplets Characterization");

        // --- Layout Configuration (JSplitPane) ---
        // We switch to BorderLayout as it's the best container for a SplitPane
        setLayout(new BorderLayout());
        
        // Create the Split Pane, putting Left on the left, Right on the right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftContent, rightContent);
        
        // --- UX Upgrades ---
        splitPane.setContinuousLayout(true); // Redraws the panels smoothly AS you drag, not just when you let go
        splitPane.setOneTouchExpandable(true); // Adds tiny little arrows to the divider to instantly collapse/expand a panel
        splitPane.setDividerSize(8); //the draggable bar thickness
        
        // Initial configuration
        splitPane.setDividerLocation(470); // Start with 470 pixels given to the Left Panel
        splitPane.setResizeWeight(0.4); // When the whole window resizes, give slightly more of the new space to the Right Panel (data)
        
        // Add it to the window
        getContentPane().add(splitPane, BorderLayout.CENTER);
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
    
    /** @return The currently considered {@link ImagePlus}. May be {@code null} if no image currently considered. */
    public ImagePlus getCurrentImage() { return this.currentImage; }
    
    /** @param mask The mask attached to the currently considered image. */
    public void setMask(ImagePlus mask) { this.mask = mask; }
    
    /** @return The mask attached to the currently considered image. May be {@code null} if no mask has been generated yet. */
    public ImagePlus getMask() { return this.mask; }

    /** @param previewWindow The window containing the preview of the measures. */
    public void setPreviewWindow(ImagePlus previewWindow) { this.previewWindow = previewWindow; }
    
    /** @return The window containing the preview of the measures. */
    public ImagePlus getPreviewWindow() { return this.previewWindow; }
}