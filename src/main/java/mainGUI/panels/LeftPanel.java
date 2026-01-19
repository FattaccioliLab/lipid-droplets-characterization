package mainGUI.panels;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.scijava.Context;

@SuppressWarnings("serial")
public class LeftPanel extends JPanel {

    public LeftPanel(final Context ctx) {
    	
    	ctx.inject(this);
    	
        setLayout(new BorderLayout());
        
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.RED, 2),
            "Original Image"
        );
        border.setTitleJustification(TitledBorder.CENTER);
        setBorder(border);
        
        // placeholder label
        add(new JLabel("No image selected", SwingConstants.CENTER), BorderLayout.CENTER);
    }
    
}