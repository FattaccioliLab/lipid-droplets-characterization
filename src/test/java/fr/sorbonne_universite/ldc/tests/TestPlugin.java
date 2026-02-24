package fr.sorbonne_universite.ldc.tests;
import javax.swing.WindowConstants;

import fr.sorbonne_universite.ldc.ui.MainGUI_LDC;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

/**
 * Simply creates a new {@link ImageJ} interface, containing aswell the plugin content (from src/main/java).<br>
 * Entry point for debug, instantly opens the {@code MainGUI_LDC}.
 */
public class TestPlugin {

	public static void main(final String[] args) {
		LegacyInjector.preinit();
		final ImageJ ij = new ImageJ();
		ij.launch(args);
		
		try {
			final MainGUI_LDC gui = new MainGUI_LDC(ij.context());
			gui.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			gui.setVisible(true);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
