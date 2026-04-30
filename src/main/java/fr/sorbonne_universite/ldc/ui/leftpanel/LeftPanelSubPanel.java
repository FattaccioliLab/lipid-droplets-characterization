package fr.sorbonne_universite.ldc.ui.leftpanel;

/**
 * Define the contract followed by each {@link LeftPanel} sub-panel.
 */
public interface LeftPanelSubPanel {
	
    /**
     * Enables, or disables, UI components of the sub-panel.
     * @param enable true : enables components, false : disables components.
     */
    public void enableUIComponents(boolean enable);
}
