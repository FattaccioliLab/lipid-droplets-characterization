package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.scijava.Context;

import fr.sorbonne_universite.ldc.ui.MainGUI_LDC;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanelSubPanel;

/**
 * Creates the bottom panel of the {@link LeftPanel}, containing navigation buttons (Prev/Next).
 */
@SuppressWarnings("serial")
public class FooterLeftPanel extends JPanel implements LeftPanelSubPanel{

    private JButton nextButton;
    private JButton prevButton;
    
    private LeftPanel leftPanel;
	
	public FooterLeftPanel(Context ctx, LeftPanel leftPanel) {
		
		// Initialization of the panel layout
		super(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        //setAlignmentX(Component.LEFT_ALIGNMENT);
        
        ctx.inject(this);
        
        this.leftPanel = leftPanel;

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
	
    // =========================================================================
    // ENABLING / DISABLING UI COMPONENTS
    // =========================================================================
	
    /**
     * Enables, or disables, UI components (navigation buttons) of this footer panel.
     * 
     * <ul>
     * 	<li>If the {@link LeftPanel} navigation index is currently at preprocessing, the prev button is necessarily disabled.</li>
     * 	<li>If the {@link LeftPanel} navigation index is currently at particle analysis, the next button is necessarily disabled.</li>
     * </ul>
     * 
     * @param enable 			true : enables inputs, false : disables inputs.
     */
	@Override
    public void enableUIComponents(boolean enable) {
    	prevButton.setEnabled(enable && leftPanel.getNavigationIndex() > MainGUI_LDC.PREPROCESSING_STEP);
    	nextButton.setEnabled(enable && leftPanel.getNavigationIndex() < MainGUI_LDC.ANALYSIS_PARAMETERS_STEP);
    }
}
