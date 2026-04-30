package fr.sorbonne_universite.ldc.ui.leftpanel;

/**
 * Define the contract followed by each {@link LeftPanel} sub-panel being part of the workflow pipeline.
 */
public interface PipelineSubPanel extends LeftPanelSubPanel {

    // =========================================================================
    // ON IMAGE RESET
    // =========================================================================
	
    /**
     * Reset the sub-panel UI components, for when the image is reseted.
     */
	public void resetUIComponents();
	
    // =========================================================================
    // ON NEW PARAMETERS IMPORT
    // =========================================================================
	
	/**
	 * Synchronizes the current UI class with the current analysis parameters.
	 * 
	 * <p>This method suppose that the current UI class has been previously reseted.</p>
	 */
	public void syncUIWithParams();
	
	/**
	 * Applies treatments of the current UI class with the current analysis parameters.
	 */
	public void applyUIWithParams();
}
