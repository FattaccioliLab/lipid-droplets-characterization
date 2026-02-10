package model.rightpanel;

import javax.swing.SwingWorker;

import mainGUI.panels.subpanels.leftpanel.PreprocessingPanel;
import model.workers.measures.MeasuresProcessingWorker;

/**
 * Provides measurements management operations. Indirectly used by the LDC {@link PreprocessingPanel} UI class.
 */
public class MeasurementsManager {

    /**
     * Creates a {@link SwingWorker}, that take care of processing measurements and showing them, if executed.
     * @param minSize minimum particle size (px).
     * @param maxSize maximum particle size (px).
     * @param minCircularity minimum particle circularity.
     * @param maxCircularity maximum particle circularity.
     * @param excludeOnEdgesEnabled Particle Analyzer option.
     * @param showAreaEnabled True if the 'Area' column must be shown in the results.
     * @param showMedianEnabled True if the 'Median' column must be shown in the results.
     * @param showMeanEnabled True if the 'Mean' column must be shown in the results.
     * @param showIntegratedDensityEnabled True if the 'IntegratedDensity' column must be shown in the results.
     * @param showCircularityEnabled True if the 'Circularity' column is shown must be the results.
	 * @see MeasuresProcessingWorker
     */
	public SwingWorker<Void,Void> createMeasuresProcessingWorker(double minSize, double maxSize, double minCircularity, double maxCircularity, boolean excludeOnEdgesEnabled, 
			boolean showAreaEnabled, boolean showMedianEnabled, boolean showMeanEnabled, boolean showIntegratedDensityEnabled, boolean showCircularityEnabled){
		return new MeasuresProcessingWorker(minSize, maxSize, minCircularity, maxCircularity, excludeOnEdgesEnabled, 
				showAreaEnabled, showMedianEnabled, showMeanEnabled, showIntegratedDensityEnabled, showCircularityEnabled); 
	}
}
