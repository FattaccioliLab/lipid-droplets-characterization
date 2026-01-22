package mainGUI.utils;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Utility class for creating and configuring standardized JPanels.
 */
public class PanelUtils {

    /**
     * Helper to create standardized vertical panels with a titled border.
     * @param title     The title of the border.
     * @param maxHeight The maximum height constraint for the panel.
     * @return The configured JPanel.
     */
    public static JPanel createVerticalPanel(String title, int maxHeight) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
        return p;
    }
    
    /**
     * Helper to transform an existing JPanel into a container of standardized vertical panels with a titled border.
     * @param p     An existing JPanel that needs to be modified.
     * @param title     The title of the border.
     * @param maxHeight The maximum height constraint for the panel.
     */
    public static void createVerticalPanel(JPanel p, String title, int maxHeight) {
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
    }
	
}
