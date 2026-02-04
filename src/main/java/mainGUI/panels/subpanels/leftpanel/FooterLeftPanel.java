package mainGUI.panels.subpanels.leftpanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.scijava.Context;

import mainGUI.panels.LeftPanel;

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
		super(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        ctx.inject(this);

        prevButton = new JButton("Prev");
        prevButton.setEnabled(false);
        nextButton = new JButton("Next");
        
        PreprocessingPanel ppp = leftPanel.getPreprocessingPanel();
        ThresholdingPanel tp = leftPanel.getThresholdingPanel();

        //leftPanel.goToPrevStep(); // Delegate to LeftPanel


        nextButton.addActionListener(e -> {
            if(leftPanel.isProcessing()) return;
            
            //leftPanel.goToNextStep();

            ppp.setVisible(false);
            tp.setVisible(true);
            tp.updateThresholdLogic();
            
            prevButton.setEnabled(true);
            nextButton.setEnabled(false);
            revalidate();
            repaint();
        });

        prevButton.addActionListener(e -> {
            if(leftPanel.isProcessing()) return;
            //leftPanel.goToNextStep();

            ppp.setVisible(true);
            tp.setVisible(false);
            prevButton.setEnabled(false);
            nextButton.setEnabled(true);
            revalidate();
            repaint();
        });

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
