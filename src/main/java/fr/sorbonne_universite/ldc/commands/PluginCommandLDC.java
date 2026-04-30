package fr.sorbonne_universite.ldc.commands;
import javax.swing.SwingUtilities;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import fr.sorbonne_universite.ldc.ui.MainGUI_LDC;

/**
 * Command associated with the menu entry Plugins>Lipid Droplets Characterization.
 */
@Plugin(type = Command.class, menuPath = "Plugins>Lipid Droplets Characterization", priority = Priority.FIRST)
public class PluginCommandLDC implements Command {

	@Parameter
	private Context ctx;
	
	private static MainGUI_LDC dialog = null;

	/**
	 * Opens the plugin main GUI.
	 */
	@Override
	public void run() {
		SwingUtilities.invokeLater(() -> {
			if (dialog == null) {
				dialog = new MainGUI_LDC(ctx);
			}
			dialog.setVisible(true);
		});
	}

}
