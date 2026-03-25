package fr.sorbonne_universite.ldc.ui.leftpanel;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.scijava.Context;

import fr.sorbonne_universite.ldc.ui.MainGUI_LDC;
import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.FooterLeftPanel;
import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.ImageSourcePanel;
import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.MorphologyPanel;
import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.ParticleAnalysisParamsPanel;
import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.PreprocessingPanel;
import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.ThresholdingPanel;
import ij.ImagePlus;

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
    
    // Parent container
    private MainGUI_LDC mainGUI;
  
    // Layout Containers
    private ImageSourcePanel imageSourcePanel;
    private PreprocessingPanel preprocessingPanel;
    private ThresholdingPanel thresholdingPanel;
    private MorphologyPanel morphologyPanel;
    private ParticleAnalysisParamsPanel particleAnalysisParamsPanel;
    private FooterLeftPanel footerLeftPanel;

    // State Flags
    private volatile boolean isProcessing = false; 	//it's false when no img is selected or a task is running to partially disable the interface
    private boolean preprocessingDone = false;  
  
    // Sub-panels display indexes
    // 0 = Preprocessing, 1 = Thresholding, 2 = MorphologyPanel, 3 = Particle analysis parameters
    private int navigationIndex = 0; // Which sub-panel is currently showed
    private int workflowIndex = 0; // Which workflow step is currently considered (other sub-panels are locked)

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
        
        // ThresholdingPanel - Initially Hidden
        thresholdingPanel = new ThresholdingPanel(ctx, this);
        thresholdingPanel.setVisible(false);
        mainContainer.add(thresholdingPanel);
        
        //MorphologyPanel
        this.morphologyPanel = new MorphologyPanel(ctx, this);
        morphologyPanel.setVisible(false);
        mainContainer.add(morphologyPanel);
        
        // ParticleAnalysisParamsPanel - Initially Hidden
        particleAnalysisParamsPanel = new ParticleAnalysisParamsPanel(ctx);
        particleAnalysisParamsPanel.setVisible(false);
        mainContainer.add(particleAnalysisParamsPanel);
        
        // FooterLeftPanel (Always visible)
        footerLeftPanel = new FooterLeftPanel(ctx, this);
    	add(footerLeftPanel, BorderLayout.SOUTH);

        //mainContainer.add(footerLeftPanel);
        

        add(mainContainer, BorderLayout.NORTH);
        
        // Disables all sub-panels at the start.
        enablePanels(false);
        
    }
    
    /** @return The {@link MainGUI_LDC} JFrame */
    public MainGUI_LDC getMainGUI() { return mainGUI; }
    
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
    // State getters / setters
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
    
    /** @return index giving the current workflow step considered. 0 = Preprocessing, 1 = Thresholding, 2 = Particle analysis parameters.  */
    public int getWorkflowIndex() { return workflowIndex; }
    
    // =========================================================================
    // Image management getters / setters
    // =========================================================================
    
    /** @param originalImg The original {@link ImagePlus}, before any process on it */
    public void setOriginalImage(ImagePlus originalImg) { mainGUI.setOriginalImage(originalImg); }
    
    /** @return The original image. Can be {@code null} if no image currently opened. */
    public ImagePlus getOriginalImage() { return mainGUI.getOriginalImage(); }
    
    /** @param currentImg The currently considered {@link ImagePlus}. */
    public void setCurrentImage(ImagePlus currentImg) { mainGUI.setCurrentImage(currentImg); }
    
    /** @return The currently considered {@link ImagePlus}. Can be {@code null} if no image currently considered. */
    public ImagePlus getCurrentImage() { return mainGUI.getCurrentImage(); }
    
    // =========================================================================
    // Enabling / Disabling and reseting panels
    // =========================================================================
    
    /**
     * Updates the current workflow step index and updates UI by enabling only the current workflow sub-panel, disabling others.
     * @param workflowIndex The new current workflow step index.
     */
    public void updateWorkflowIndex(int workflowIndex) {
    	this.workflowIndex = workflowIndex;
    	enablePanels(true);
    }
    
    /**
     * Updates the {@link ImageSourcePanel} with the current number of slices considered / number of original slices.
     */
    public void updateUIInfosNbSlices() {
    	imageSourcePanel.updateUIInfosNbSlices();
    }
    
    /**
     * <ul>
     * 	Either :
     * 	<li>Enables UI components of ONLY the current workflow's step sub-panel.</li>
     * 	<li>Disables UI components of ALL sub panels.</li>
     * </ul>
     * @param enable true : enable, false : disable
     */
    public void enablePanels(boolean enable) {
    	
    	// PreprocessingPanel
    	if (enable && workflowIndex == 0) {
    		preprocessingPanel.enableUIComponents(true, false);
    	} else {
    		preprocessingPanel.enableUIComponents(false, false);
    	}
    	
    	// ThresholdingPanel
    	if (enable && workflowIndex == 1) {
    		thresholdingPanel.enableUIComponents(true);
    	} else {
    		thresholdingPanel.enableUIComponents(false);
    	}
    	
    	// MorphologyPanel
    	if (enable && workflowIndex == 2) {
    		morphologyPanel.enableUIComponents(true);
    	} else {
    		morphologyPanel.enableUIComponents(false);
    	}
    	
    	// ParticleAnalysisParamsPanel
    	if (enable && workflowIndex == 3) {
    		particleAnalysisParamsPanel.enableUIComponents(true);
    	} else {
    		particleAnalysisParamsPanel.enableUIComponents(false);
    	}
    }
    
    /**
     * Reset all sub panels, for when the image is reseted.
     * */
    public void resetPanels() {
    	preprocessingPanel.resetUIComponents();
    	thresholdingPanel.resetUIComponents();
    	particleAnalysisParamsPanel.resetUIComponents();
    	
    	workflowIndex = 0; // Workflow back to preprocessing
    	
    	//IDEA : ui goes back to the preprocessing panel
    	/*
    	preprocessingPanel.setVisible(true);
    	thresholdingPanel.setVisible(false);
    	morphologyPanel.setVisible(false);
    	particleAnalysisParamsPanel.setVisible(false);
    	
    	
    	// Update Footer Buttons
        footerLeftPanel.setPrevButtonEnabled(false);
    	footerLeftPanel.setNextButtonEnabled(true);
    	*/
    }
    
    // =========================================================================
    // Navigation Logic
    // =========================================================================
    
    /**
     * Updates LeftPanel UI to show the next sub-panel, and manage allowing/disallowing interaction with Prev/Next footer buttons
     * depending on the new current sub panel.<br>
     * Does not manage enabling/disabling UI components of sub-panels.
     */
    public void goToNextStep() {
        if (navigationIndex == 0) {
            // Switch: Preprocessing -> Thresholding
            preprocessingPanel.setVisible(false);
            thresholdingPanel.setVisible(true);
            navigationIndex = 1;
            
            // Update Footer Buttons
            footerLeftPanel.setPrevButtonEnabled(true);
            //footerLeftPanel.setNextButtonEnabled(true);
            
        }else if (navigationIndex == 1) {
        	// Switch: Thresholding -> Morphology
        	thresholdingPanel.setVisible(false);
        	morphologyPanel.setVisible(true);
            navigationIndex = 2;
            
            // Update Footer Buttons
            //footerLeftPanel.setNextButtonEnabled(t); // Nothing after
        } 
        else if (navigationIndex == 2) {
        	// Switch: Morphology -> Particle analysis parameters
        	morphologyPanel.setVisible(false);
            particleAnalysisParamsPanel.setVisible(true);
            navigationIndex = 3;
            
            // Update Footer Buttons
            footerLeftPanel.setNextButtonEnabled(false); // Nothing after
        }
    }
	
    /**
     * Updates LeftPanel UI to show the previous sub-panel, and manage allowing/disallowing interaction with Prev/Next footer buttons
     * depending on the new current sub panel.<br>
     * Does not manage enabling/disabling UI components of sub-panels.
     */
	public void goToPrevStep() {
        if (navigationIndex == 1) {
            // Switch: Thresholding -> Preprocessing
            thresholdingPanel.setVisible(false);
            preprocessingPanel.setVisible(true);
            navigationIndex = 0;
            
            // Update Footer Buttons
            footerLeftPanel.setPrevButtonEnabled(false);
            
        } else if (navigationIndex == 2) {
        	// Switch: Particle analysis parameters -> Thresholding
        	morphologyPanel.setVisible(false);
        	thresholdingPanel.setVisible(true);
        	navigationIndex = 1;
        	
        	// Update Footer Buttons
            footerLeftPanel.setPrevButtonEnabled(true);
        	footerLeftPanel.setNextButtonEnabled(true);
        } else if (navigationIndex == 3) {
        	// Switch: Particle analysis parameters -> Thresholding
        	particleAnalysisParamsPanel.setVisible(false);
        	morphologyPanel.setVisible(true);
        	navigationIndex = 2;
        	
        	// Update Footer Buttons
            footerLeftPanel.setPrevButtonEnabled(true);
        	footerLeftPanel.setNextButtonEnabled(true);
        }
    }
	
}