package mainGUI.panels;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.scijava.Context;

import ij.ImagePlus;
import ij.WindowManager;
import mainGUI.panels.subpanels.leftpanel.FooterLeftPanel;
import mainGUI.panels.subpanels.leftpanel.ImageSourcePanel;
import mainGUI.panels.subpanels.leftpanel.PreprocessingPanel;
import model.AnalysisSettings;

/**
 * The left side of the plugin main GUI.
 * <p>
 * This panel handles in its sub-panels :
 * <ul>
 * <li>Image loading and replacement.</li>
 * <li>Preprocessing settings (Contrast enhancement, Median filtering).</li>
 * <li>Asynchronous previewing and application of filters.</li>
 * <li>Navigation to the next step of the workflow.</li>
 * </ul>
 */
@SuppressWarnings("serial")
public class LeftPanel extends JPanel {

    // The current image considered
    private ImagePlus img;

    // Layout Containers
    private ImageSourcePanel imageSourcePanel;
    private PreprocessingPanel preprocessingPanel;
    private FooterLeftPanel footerLeftPanel;

    // State Flags
    private boolean isApplicable = false;
    private volatile boolean isProcessing = false; 

    /**
     * Constructs the LeftPanel, by initializing the main layout and initializing + assembling the sub-panels.
     * * @param ctx              The SciJava context for injection.
     * @param selectedSettings The model object holding analysis parameters.
     */
    public LeftPanel(Context ctx, AnalysisSettings selectedSettings) {
        ctx.inject(this);

        setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

        // ImageSourcePanel
        imageSourcePanel = new ImageSourcePanel(ctx, this);
        mainContainer.add(imageSourcePanel);
        mainContainer.add(Box.createVerticalStrut(10));
        
        // PreprocessingPanel
        preprocessingPanel = new PreprocessingPanel(ctx, selectedSettings, this);
        mainContainer.add(preprocessingPanel);
        
        // FooterLeftPanel
        footerLeftPanel = new FooterLeftPanel(ctx, this);
        mainContainer.add(footerLeftPanel);

        add(mainContainer, BorderLayout.NORTH);
        
    }
    
    // =========================================================================
    // Sub-panel getters
    // =========================================================================
    
    /** @return The {@code PreprocessingPanel} JPanel (the top sub-panel) */
    public PreprocessingPanel getPreprocessingPanel() { return preprocessingPanel; }
    
    /** @return The {@code ImageSourceControl} JPanel (the center sub-panel) */
    public ImageSourcePanel getImageSourcePanel() { return imageSourcePanel; }
    
    /** @return The {@code FooterLeftPanel} JPanel (the bottom sub-panel) */
    public FooterLeftPanel getFooterLeftPanel() { return footerLeftPanel; }
    
    
    // =========================================================================
    // States getters / setters
    // =========================================================================
    
    /** @return boolean value indicating if the plugin is currently processing a preprocessing operation. */
    public boolean isProcessing() { return isProcessing; }
    
    /** @param isProcessing The new value of the {@code isProcessing} boolean. */
    public void setProcessing(boolean isProcessing) { this.isProcessing = isProcessing; }
    
    /** @return boolean value indicating if the plugin can apply a preprocessing operation. */
    public boolean isApplicable() { return isApplicable; }
    
    /** @param isApplicable The new value of the {@code isApplicable} boolean. */
    public void setApplicable(boolean isApplicable) { this.isApplicable = isApplicable; }
    
    
    // =========================================================================
    // Enabling / disabling footer navigation buttons
    // =========================================================================
    
    /**
     * Enables (or disables) the footer's 'Next' button. Delegates the operation to its sub-panel.
     * @param enabled true : enables, false : disables
     */
    public void setNextButtonEnabled(boolean enabled) { footerLeftPanel.setNextButtonEnabled(enabled); }
    
    /**
     * Enables (or disables) the footer's 'Prev' button. Delegates the operation to its sub-panel.
     * @param enabled true : enables, false : disables
     */
    public void setPrevButtonEnabled(boolean enabled) { footerLeftPanel.setPrevButtonEnabled(enabled); }
    
    
    // =========================================================================
    // Updating the current image
    // =========================================================================
    
    /**
     * Updates the {@code img} attribute, containing the current image considered, and returns it.
     * @return The current image considered
     */
    public ImagePlus updateAndGetImg() { 
    	img = WindowManager.getCurrentImage();
    	return img; 
    }
}