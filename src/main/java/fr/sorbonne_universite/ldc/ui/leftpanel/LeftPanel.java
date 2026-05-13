package fr.sorbonne_universite.ldc.ui.leftpanel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fr.sorbonne_universite.ldc.model.LDCService;
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
 * 
 * <p>
 * This panel handles in its sub-panels :
 * </p>
 * 	<ul>
 * 		<li>Image loading and replacement, with its {@link ImageSourcePanel}.</li>
 * 		<li>Pipeline sub-panels (being {@link PipelineSubPanel}s).</li>
 * 		<li>Navigation to the next step of the workflow, with its {@link FooterLeftPanel}.</li>
 * 	</ul>
 */
@SuppressWarnings("serial")
public class LeftPanel extends JPanel {
	
    @Parameter
    private LDCService ldc;
    
    // Parent main frame
    private MainGUI_LDC mainGUI;
  
    // Sub-panels
    private ImageSourcePanel imageSourcePanel;
    private PreprocessingPanel preprocessingPanel;
    private ThresholdingPanel thresholdingPanel;
    private MorphologyPanel morphologyPanel;
    private ParticleAnalysisParamsPanel particleAnalysisParamsPanel;
    private FooterLeftPanel footerLeftPanel;
    
    /** Sub-panels part of the workflow pipeline. */
	private PipelineSubPanel[] pipelineSubPanels;

	/** 
	 * State flag, it's false when no img is selected or a task is running to partially disable the interface.<br>
	 * Used only by {@link PreprocessingPanel}.
	 */
    private volatile boolean isProcessing = false; 	//
  
    // Sub-panels display indexes
    // 0 = Preprocessing, 1 = Thresholding, 2 = MorphologyPanel, 3 = Particle analysis parameters
    /** Which pipeline sub-panel is currently showed. */
    private int navigationIndex = 0;
    /** Which workflow step is currently considered (other sub-panels are locked). */
    private int workflowIndex = 0;

    public LeftPanel(Context ctx, MainGUI_LDC mainGUI) {
        ctx.inject(this);
        
        this.mainGUI = mainGUI;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 0)); // (top, left, bottom, right) : 15 pixels of left padding
    	// --- Add a visual separator line and padding ---
        // A CompoundBorder applies two borders together: an outer line, and inner padding.
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY), // Outer: 1px gray line on the right
            BorderFactory.createEmptyBorder(8, 5, 8, 8)                  // Inner: 5px left padding, 15px right padding
        ));
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        
        // PreprocessingPanel
        preprocessingPanel = new PreprocessingPanel(ctx, this);
        
        // ThresholdingPanel - Initially Hidden
        thresholdingPanel = new ThresholdingPanel(ctx, this);
        thresholdingPanel.setVisible(false);
        
        //MorphologyPanel - Initially Hidden
        morphologyPanel = new MorphologyPanel(ctx, this);
        morphologyPanel.setVisible(false);
        
        // ParticleAnalysisParamsPanel - Initially Hidden
        particleAnalysisParamsPanel = new ParticleAnalysisParamsPanel(ctx);
        particleAnalysisParamsPanel.setVisible(false);
        
        // ImageSourcePanel (Always visible)
        imageSourcePanel = new ImageSourcePanel(ctx, this);
        
        // FooterLeftPanel (Always visible)
        footerLeftPanel = new FooterLeftPanel(ctx, this);
        
        mainContainer.add(imageSourcePanel);
        mainContainer.add(Box.createVerticalStrut(10));
        mainContainer.add(preprocessingPanel);
        mainContainer.add(thresholdingPanel);
        mainContainer.add(morphologyPanel);
        mainContainer.add(particleAnalysisParamsPanel);
        
        add(footerLeftPanel, BorderLayout.SOUTH);

        add(mainContainer, BorderLayout.NORTH);
        
        // Disables all sub-panels at the start.
        enablePanels(false);
        
        pipelineSubPanels = new PipelineSubPanel[] {preprocessingPanel, thresholdingPanel, 
        		morphologyPanel, particleAnalysisParamsPanel};
    }
    
    /** @return The {@link MainGUI_LDC} JFrame */
    public MainGUI_LDC getMainGUI() { return mainGUI; }
    
    // =========================================================================
    // State getters / setters
    // =========================================================================
    
    /** @return boolean value indicating if the plugin is currently processing a preprocessing operation. */
    public boolean isProcessing() { return isProcessing; }
    
    /** 
     * Modifies the state of the whole left panel, by disabling UI components of the sub-panels
     * if one of them is currently processing. Otherwise it allows enabling sub-panels.
     * @param isProcessing The new value of the {@code isProcessing} boolean. 
     */
    public void setProcessing(boolean isProcessing) {
    	this.isProcessing = isProcessing; 
    	// even if true is given as parameter, the method itself will consider the new 'isProcessing' flag
    	// and potentially disable sub-panels
    	enablePanels(true);
    }
    
    /** @return index giving the current pipeline sub-panel showed. */
    public int getNavigationIndex() { return navigationIndex; }
    
    /** @return index giving the current workflow step considered. */
    public int getWorkflowIndex() { return workflowIndex; }
    
    /** @return Sub-panels part of the workflow pipeline. */
    public PipelineSubPanel[] getPipelineSubPanels() { return pipelineSubPanels; }
    
    // =========================================================================
    // Image management getters / setters
    // =========================================================================
    
    /** @param originalImg The original {@link ImagePlus}, before any process on it */
    public void setOriginalImage(ImagePlus originalImg) { mainGUI.setOriginalImage(originalImg); }
    
    /** @return The original image. Can be {@code null} if no image currently opened. */
    public ImagePlus getOriginalImage() { return mainGUI.getOriginalImage(); }
    
    /** @param currentImg The currently considered {@link ImagePlus}. */
    public void setCurrentImage(ImagePlus currentImg) { mainGUI.setCurrentImage(currentImg); }
    
    /** @return The currently considered {@link ImagePlus}. May be {@code null} if no image currently considered. */
    public ImagePlus getCurrentImage() { return mainGUI.getCurrentImage(); }
    
    /** @param mask The mask attached to the currently considered image. */
    public void setMask(ImagePlus mask) { mainGUI.setMask(mask); }
    
    /** @return The mask attached to the currently considered image. May be {@code null} if no mask has been generated yet. */
    public ImagePlus getMask() { return mainGUI.getMask(); }
    
    /** @param previewWindow The window containing the preview of the measures. */
    public void setPreviewWindow(ImagePlus previewWindow) { mainGUI.setPreviewWindow(previewWindow); }
    
    /** @return The window containing the preview of the measures. */
    public ImagePlus getPreviewWindow() { return mainGUI.getPreviewWindow(); }
    
    // =========================================================================
    // Enabling / Disabling UI components of sub-panels
    // =========================================================================

    /**
     * <ul>
     * 	Either :
     * 	<li>Enables UI components of ONLY the current workflow's step sub-panel, if not processing.</li>
     * 	<li>Disables UI components of ALL sub panels.</li>
     * </ul>
     * @param enable true : enable, false : disable
     */
    public void enablePanels(boolean enable) {
    	
    	// ImageSourcePanel
    	imageSourcePanel.enableUIComponents(enable);
    	
    	// PreprocessingPanel
    	if (enable && workflowIndex == MainGUI_LDC.PREPROCESSING_STEP && !isProcessing) {
    		preprocessingPanel.enableUIComponents(true);
    	} else {
    		preprocessingPanel.enableUIComponents(false);
    	}
    	
    	// ThresholdingPanel
    	if (enable && workflowIndex == MainGUI_LDC.THRESHOLDING_STEP && !isProcessing) {
    		thresholdingPanel.enableUIComponents(true);
    	} else {
    		thresholdingPanel.enableUIComponents(false);
    	}
    	
    	// MorphologyPanel
    	if (enable && workflowIndex == MainGUI_LDC.MORPHOLOGICAL_STEP && !isProcessing) {
    		morphologyPanel.enableUIComponents(true);
    	} else {
    		morphologyPanel.enableUIComponents(false);
    	}
    	
    	// ParticleAnalysisParamsPanel
    	if (enable && workflowIndex == MainGUI_LDC.ANALYSIS_PARAMETERS_STEP && !isProcessing) {
    		particleAnalysisParamsPanel.enableUIComponents(true);
    	} else {
    		particleAnalysisParamsPanel.enableUIComponents(false);
    	}
    	
    	// Disable navigation while processing
    	if (isProcessing) {
    		footerLeftPanel.enableUIComponents(false);
    	} else {
    		footerLeftPanel.enableUIComponents(true);
    	}
    }
    
    // =========================================================================
    // Reseting UI components of sub-panels
    // =========================================================================
    
    /**
     * Reset all pipeline sub panels, for when the image is reseted.<br>
     * ALSO set the preprocessing panel as the current sub-panel shown.
     */
    public void resetPanels() {
    	isProcessing = false;
    	
    	for (PipelineSubPanel subPanel : pipelineSubPanels) {
    		subPanel.resetUIComponents();
    	}
    	
    	// Workflow and navigation back to preprocessing
    	workflowIndex = MainGUI_LDC.PREPROCESSING_STEP;
    	navigationIndex = MainGUI_LDC.PREPROCESSING_STEP;
    	goToStep(MainGUI_LDC.PREPROCESSING_STEP);
    	enablePanels(true);
    }
    
    // =========================================================================
    // Navigation Logic
    // =========================================================================
    
    
    /**
     * Updates LeftPanel UI to show the given sub-panel, and manage allowing/disallowing interaction with Prev/Next footer buttons
     * depending on the new current sub panel.
     * <p>Does not manage enabling/disabling UI components of sub-panels.</p>
     * @param 			The new navigation index.
     */
    private void goToStep(int newNavigationIndex) {
    	
    	switch (newNavigationIndex) {
    		case MainGUI_LDC.PREPROCESSING_STEP:
                preprocessingPanel.setVisible(true);
                thresholdingPanel.setVisible(false);
                morphologyPanel.setVisible(false);
                particleAnalysisParamsPanel.setVisible(false);
    			navigationIndex = MainGUI_LDC.PREPROCESSING_STEP;
    			break;
    		case MainGUI_LDC.THRESHOLDING_STEP:
                preprocessingPanel.setVisible(false);
                thresholdingPanel.setVisible(true);
                morphologyPanel.setVisible(false);
                particleAnalysisParamsPanel.setVisible(false);
    			navigationIndex = MainGUI_LDC.THRESHOLDING_STEP;
    			break;
    		case MainGUI_LDC.MORPHOLOGICAL_STEP:
                preprocessingPanel.setVisible(false);
                thresholdingPanel.setVisible(false);
                morphologyPanel.setVisible(true);
                particleAnalysisParamsPanel.setVisible(false);
    			navigationIndex = MainGUI_LDC.MORPHOLOGICAL_STEP;
    			break;
    		case MainGUI_LDC.ANALYSIS_PARAMETERS_STEP:
                preprocessingPanel.setVisible(false);
                thresholdingPanel.setVisible(false);
                morphologyPanel.setVisible(false);
                particleAnalysisParamsPanel.setVisible(true);
    			navigationIndex = MainGUI_LDC.ANALYSIS_PARAMETERS_STEP;
    			break;
    		default:
    			System.err.println("Unkown navigation index");
    	}
    	
    	// Update Footer Buttons
        footerLeftPanel.enableUIComponents(true);
    }
    
    /**
     * Updates LeftPanel UI to show the next sub-panel, and manage allowing/disallowing interaction with Prev/Next footer buttons
     * depending on the new current sub panel.
     * <p>Does not manage enabling/disabling UI components of sub-panels.</p>
     */
    public void goToNextStep() {
        if (navigationIndex == MainGUI_LDC.PREPROCESSING_STEP) {
            // Switch: Preprocessing -> Thresholding
            preprocessingPanel.setVisible(false);
            thresholdingPanel.setVisible(true);
            navigationIndex = MainGUI_LDC.THRESHOLDING_STEP;
            
        } else if (navigationIndex == MainGUI_LDC.THRESHOLDING_STEP) {
        	// Switch: Thresholding -> Morphology
        	thresholdingPanel.setVisible(false);
        	morphologyPanel.setVisible(true);
            navigationIndex = MainGUI_LDC.MORPHOLOGICAL_STEP;

        } else if (navigationIndex == MainGUI_LDC.MORPHOLOGICAL_STEP) {
        	// Switch: Morphology -> Particle analysis parameters
        	morphologyPanel.setVisible(false);
            particleAnalysisParamsPanel.setVisible(true);
            navigationIndex = MainGUI_LDC.ANALYSIS_PARAMETERS_STEP;

        } else {
        	System.err.println("Unkown next step reached");
        }
        
        // Update Footer Buttons
        footerLeftPanel.enableUIComponents(true);
    }
	
    /**
     * Updates LeftPanel UI to show the previous sub-panel, and manage allowing/disallowing interaction with Prev/Next footer buttons
     * depending on the new current sub panel.
     * <p>Does not manage enabling/disabling UI components of sub-panels.</p>
     */
	public void goToPrevStep() {
        if (navigationIndex == MainGUI_LDC.THRESHOLDING_STEP) {
            // Switch: Thresholding -> Preprocessing
            thresholdingPanel.setVisible(false);
            preprocessingPanel.setVisible(true);
            navigationIndex = MainGUI_LDC.PREPROCESSING_STEP;
            
        } else if (navigationIndex == MainGUI_LDC.MORPHOLOGICAL_STEP) {
        	// Switch: Morphological operations -> Thresholding
        	morphologyPanel.setVisible(false);
        	thresholdingPanel.setVisible(true);
        	navigationIndex = MainGUI_LDC.THRESHOLDING_STEP;

        } else if (navigationIndex == MainGUI_LDC.ANALYSIS_PARAMETERS_STEP) {
        	// Switch: Particle analysis parameters -> Morphological operations
        	particleAnalysisParamsPanel.setVisible(false);
        	morphologyPanel.setVisible(true);
        	navigationIndex = MainGUI_LDC.MORPHOLOGICAL_STEP;

        } else {
        	System.err.println("Unkown previous step reached");
        }
        
        // Update Footer Buttons
        footerLeftPanel.enableUIComponents(true);
    }
	
    // =========================================================================
    // Update / Synchronization methods
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
     * Synchronize the {@link LDCService} with current particle analysis input values.
     * Delegates this synchronization to the {@link ParticleAnalysisParamsPanel}.
     */
    public void syncAnalysisParametersInputValues() {
    	particleAnalysisParamsPanel.syncInputValues();
    }
}