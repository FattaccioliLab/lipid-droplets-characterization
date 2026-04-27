package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.scijava.Context;

import fr.sorbonne_universite.ldc.ui.MainGUI_LDC;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;

/**
 * Creates the bottom panel of the {@link LeftPanel}, containing navigation buttons (Prev/Next).
 * * @return The constructed JPanel.
 */
@SuppressWarnings("serial")
public class FooterLeftPanel extends JPanel{

    private JButton nextButton;
    private JButton prevButton;
	
	public FooterLeftPanel(Context ctx, LeftPanel leftPanel) {
		
		// Initialization of the panel layout
		super(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        //setAlignmentX(Component.LEFT_ALIGNMENT);
        
        ctx.inject(this);

        prevButton = new JButton("Prev");
        prevButton.setEnabled(false);
        nextButton = new JButton("Next");

        nextButton.addActionListener(e -> {
            if(leftPanel.isProcessing()) return;
            leftPanel.goToNextStep();
            
        });
        nextButton.setFocusPainted(false);

        prevButton.addActionListener(e -> {
            if(leftPanel.isProcessing()) return;
            leftPanel.goToPrevStep();
            
        });
        prevButton.setFocusPainted(false);

        add(prevButton);
        add(nextButton);
	}
	
    /**
     * Enables, or disables, UI components (navigation buttons) of this footer panel. 
     * Also depends on the current {@code navigationIndex}.
     * @param enable 			true : enables inputs, false : disables inputs.
     * @param navigationIndex	The current navigation index.
     */
    public void enableUIComponents(boolean enable, int navigationIndex) {
    	prevButton.setEnabled(enable && navigationIndex > MainGUI_LDC.PREPROCESSING_STEP);
    	nextButton.setEnabled(enable && navigationIndex < MainGUI_LDC.ANALYSIS_PARAMETERS_STEP);
    }
}
