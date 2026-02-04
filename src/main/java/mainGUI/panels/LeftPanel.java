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
import mainGUI.panels.subpanels.leftpanel.ThresholdingPanel;

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
    private ThresholdingPanel thresholdingPanel;
    
    private FooterLeftPanel footerLeftPanel;

    // State Flags
    private volatile boolean isProcessing = false; 	//it's false when no img is selected or a task is running to partially disable the interface
    
    // State tracking
    private int currentStepIndex = 0; // 0 = Preprocessing, 1 = Thresholding
    
    


    /**
     * Constructs the LeftPanel, by initializing the main layout and initializing + assembling the sub-panels.
     * * @param ctx              The SciJava context for injection.
     * @param selectedSettings The model object holding analysis parameters.
     */
    public LeftPanel(Context ctx) {
        ctx.inject(this);

        setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

        // ImageSourcePanel (Always visible)
        imageSourcePanel = new ImageSourcePanel(ctx, this);
        mainContainer.add(imageSourcePanel);
        mainContainer.add(Box.createVerticalStrut(10));
        
        // PreprocessingPanel
        preprocessingPanel = new PreprocessingPanel(ctx, this);
        mainContainer.add(preprocessingPanel);
        
        //Thresholding - Initially Hidden
        thresholdingPanel = new ThresholdingPanel(ctx, this);
        thresholdingPanel.setVisible(false); // Hidden by default
        mainContainer.add(thresholdingPanel);
        
        // FooterLeftPanel
        footerLeftPanel = new FooterLeftPanel(ctx, this);
        mainContainer.add(footerLeftPanel);

        add(mainContainer, BorderLayout.NORTH);
        
    }
    
    // =========================================================================
    // Sub-panel getters
    // =========================================================================
    
    /** @return The {@link PreprocessingPanel} JPanel (the top sub-panel) */
    public PreprocessingPanel getPreprocessingPanel() { return preprocessingPanel; }
    
    /** @return The {@link ThresholdingPanel} JPanel (next page's sub-panel) */
    public ThresholdingPanel getThresholdingPanel() {return thresholdingPanel;}
    
    /** @return The {@link ImageSourceControl} JPanel (the center sub-panel) */
    public ImageSourcePanel getImageSourcePanel() { return imageSourcePanel; }
    
    /** @return The {@link FooterLeftPanel} JPanel (the bottom sub-panel) */
    public FooterLeftPanel getFooterLeftPanel() { return footerLeftPanel; }
    
    
    // =========================================================================
    // States getters / setters
    // =========================================================================
    
    /** @return boolean value indicating if the plugin is currently processing a preprocessing operation. */
    public boolean isProcessing() { return isProcessing; }
    
    /** @param isProcessing The new value of the {@code isProcessing} boolean. */
    public void setProcessing(boolean isProcessing) { this.isProcessing = isProcessing; }
    
    // =========================================================================
    // Enabling / disabling footer navigation buttons
    // =========================================================================
    
    /**
     * Enables (or disables) the footer's 'Next' button. Delegates the operation to its sub-panel.
     * @param enabled true : enables, false : disables
     */
    public void setNextButtonEnabled(boolean enabled) { 
    	//goToNextStep();
    	footerLeftPanel.setNextButtonEnabled(enabled); 
    }
    
    /**
     * Enables (or disables) the footer's 'Prev' button. Delegates the operation to its sub-panel.
     * @param enabled true : enables, false : disables
     */
    public void setPrevButtonEnabled(boolean enabled) { 
    	//goToPrevStep();
    	footerLeftPanel.setPrevButtonEnabled(enabled); 
    }
    
    
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
    
    
    // --- Navigation Logic ---
	public void goToNextStep() {
        if (currentStepIndex == 0) {
            // Switch: Preprocessing -> Thresholding
            preprocessingPanel.setVisible(false);
            if(thresholdingPanel != null) {
                thresholdingPanel.setVisible(true);
                thresholdingPanel.updateThresholdLogic(); // Trigger preview for default method
            }else {
            	System.out.println("thresholdingPanel null");
            }

            currentStepIndex = 1;
            
            // Update Footer Buttons
            footerLeftPanel.setPrevButtonEnabled(true);
            footerLeftPanel.setNextButtonEnabled(false); // No step 2 yet
        }
    }
	
	public void goToPrevStep() {
        if (currentStepIndex == 1) {
            // Switch: Thresholding -> Preprocessing
            thresholdingPanel.setVisible(false);
            preprocessingPanel.setVisible(true);
            currentStepIndex = 0;
            
            // Update Footer Buttons
            footerLeftPanel.setPrevButtonEnabled(false);
            footerLeftPanel.setNextButtonEnabled(true);
        }
    }
	
}