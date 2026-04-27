package fr.sorbonne_universite.ldc.ui.leftpanel;

/**
 * Interface implemented by {@link LeftPanel} sub-panels, forcing them to define methods
 * called on new imported parameters.
 */
public interface UIOnParamsImport {

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
