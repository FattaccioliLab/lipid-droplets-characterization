package mainGUI.panels;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.scijava.Context;

import ij.ImagePlus;
import ij.WindowManager;
import mainGUI.MainGUI_LDC;
import mainGUI.panels.subpanels.leftpanel.ParticleAnalysisParamsPanel;
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
    
    // Parent container
    private MainGUI_LDC mainGUI;

  
    // Layout Containers
    private ImageSourcePanel imageSourcePanel;
    private PreprocessingPanel preprocessingPanel;
    private ThresholdingPanel thresholdingPanel;
    private ParticleAnalysisParamsPanel particleAnalysisParamsPanel;
    private FooterLeftPanel footerLeftPanel;

    // State Flags
    private volatile boolean isProcessing = false; 	//it's false when no img is selected or a task is running to partially disable the interface
    private boolean preprocessingDone = false;  
  
    // State tracking
    private int currentStepIndex = 0; // 0 = Preprocessing, 1 = Thresholding 

    /**
     * Constructs the LeftPanel, by initializing the main layout and initializing + assembling the sub-panels.
     * @param ctx              The SciJava context for injection.
     * @param mainGUI          The parent component.
     * @param selectedSettings The model object holding analysis parameters.
     */
    public LeftPanel(Context ctx, MainGUI_LDC mainGUI) {
        ctx.inject(this);
        
        this.mainGUI = mainGUI;

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
        
        //Particle analysis params - Initially Hidden
        particleAnalysisParamsPanel = new ParticleAnalysisParamsPanel(ctx);
        particleAnalysisParamsPanel.setVisible(false); // Hidden by default
        mainContainer.add(particleAnalysisParamsPanel);
        
        // FooterLeftPanel
        footerLeftPanel = new FooterLeftPanel(ctx, this);
        mainContainer.add(footerLeftPanel);

        add(mainContainer, BorderLayout.NORTH);
        
    }
    
    // =========================================================================
    // Sub-panel getters
    // =========================================================================
    
    /** @return The {@link ImageSourceControl} JPanel (the top sub-panel) */
    public ImageSourcePanel getImageSourcePanel() { return imageSourcePanel; }
    
    /** @return The {@link PreprocessingPanel} JPanel (the 1st center sub-panel) */
    public PreprocessingPanel getPreprocessingPanel() { return preprocessingPanel; }
    
    /** @return The {@link ThresholdingPanel} JPanel (the 2nd center sub-panel) */
    public ThresholdingPanel getThresholdingPanel() {return thresholdingPanel;}
    
    /** @return The {@link ParticleAnalysisParamsPanel} JPanel (the 3rd center sub-panel) */
    public ParticleAnalysisParamsPanel getParticleAnalysisParamsPanel() { return particleAnalysisParamsPanel; }
    
    /** @return The {@link FooterLeftPanel} JPanel (the bottom sub-panel) */
    public FooterLeftPanel getFooterLeftPanel() { return footerLeftPanel; }
    
    // =========================================================================
    // States getters / setters
    // =========================================================================
    
    /** @return boolean value indicating if the plugin is currently processing a preprocessing operation. */
    public boolean isProcessing() { return isProcessing; }
    
    /** @param isProcessing The new value of the {@code isProcessing} boolean. */
    public void setProcessing(boolean isProcessing) { this.isProcessing = isProcessing; }
    
    /** @return boolean value indicating if a preprocessing workflow has been done. */
    public boolean isPreprocessingDone() { return preprocessingDone; }
    
    /** 
     * If true, locks PreprocessingPanel's UI components. Called when a preprocessing workflow has been done.
     * @param preprocessingDone The new value of the {@code preprocessingDone} boolean. 
     */
    public void setPreprocessingDone(boolean preprocessingDone) { this.preprocessingDone = preprocessingDone; }
    
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
    
    /**
     * Updates the {@link ImageSourcePanel} with the current number of slices considered / number of original slices.
     */
    public void updateUIInfosNbSlices() {
    	imageSourcePanel.updateUIInfosNbSlices();
    }
    
    // =========================================================================
    // Navigation Logic
    // =========================================================================
    
	  public void goToNextStep() {
        if (currentStepIndex == 0) {
            // Switch: Preprocessing -> Thresholding
            preprocessingPanel.setVisible(false);
            thresholdingPanel.setVisible(true);
            thresholdingPanel.updateThresholdLogic(); // Trigger preview for default method

            currentStepIndex = 1;
            
            // Update Footer Buttons
            footerLeftPanel.setPrevButtonEnabled(true);
            footerLeftPanel.setNextButtonEnabled(true);
            
        } else if (currentStepIndex == 1) {
        	// Switch: Thresholding -> Particle analysis parameters
        	thresholdingPanel.setVisible(false);
            particleAnalysisParamsPanel.setVisible(true);
            
            currentStepIndex = 2;
            
            // Update Footer Buttons
            footerLeftPanel.setNextButtonEnabled(false); // Nothing after
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
            
        } else if (currentStepIndex == 2) {
        	// Switch: Particle analysis parameters -> Thresholding
        	particleAnalysisParamsPanel.setVisible(false);
        	thresholdingPanel.setVisible(true);
            currentStepIndex = 1;
        	
        	// Update Footer Buttons
            footerLeftPanel.setPrevButtonEnabled(true);
        	footerLeftPanel.setNextButtonEnabled(true);
        }
    }
	
    // =========================================================================
    // Setting the original ImagePlus
    // =========================================================================
    
    /**
     * Set the original {@link ImagePlus}, before any process on it.
     * @param originalImg The original {@link ImagePlus}.
     */
    public void setOriginalImage(ImagePlus originalImg) {
    	mainGUI.setOriginalImage(originalImg);
    }
    
    /**
     * Get the original {@link ImagePlus} attribute. Can be {@code null} if no image currently opened.
     * @return The original image.
     */
    public ImagePlus getOriginalImage() {
    	return mainGUI.getOriginalImage();
    }
    
    // =========================================================================
    // Enabling/Disabling and reseting panels
    // =========================================================================
    
    /**
     * Enable or disable UI components of sub panels.
     * @param enable true : enable, false : disable
     */
    public void enablePanels(boolean enable) {
        preprocessingPanel.enableUIComponents(enable, false);
        thresholdingPanel.enableUIComponents(enable);
        particleAnalysisParamsPanel.enableUIComponents(enable);
        // add here the enableUIComponents method call for the incoming sub panels
    }
    
    /**
     * Reset sub panels, for when the image is reseted.
     * */
    public void resetPanels() {
    	preprocessingPanel.resetUIComponents();
    	thresholdingPanel.resetUIComponents();
    	particleAnalysisParamsPanel.resetUIComponents();
    	// add here the resetUIComponents method call for the incoming sub panels
    }
    
    
}