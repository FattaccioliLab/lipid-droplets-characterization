package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.scijava.Context;

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
		super(new FlowLayout(FlowLayout.CENTER, 0, 0));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
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
     * Enables (or disables) the footer's 'Next' button.
     * @param enabled true : enables, false : disables
     */
    public void setNextButtonEnabled(boolean enabled) { nextButton.setEnabled(enabled); }
    
    /**
     * Enables (or disables) the footer's 'Prev' button.
     * @param enabled true : enables, false : disables
     */
    public void setPrevButtonEnabled(boolean enabled) { prevButton.setEnabled(enabled); }
}
