package fr.sorbonne_universite.ldc.model.workers;

import javax.swing.SwingWorker;

import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

/**
 * SwingWorker applying a median filter preview on an {@link ImageProcessor}.
 */
public class PreprocessingPreviewMedianWorker extends SwingWorker<Void, Void>{
	
	private ImageProcessor ip;
	private double radius;
	
    /**
     * Creates a {@link PreprocessingPreviewMedianWorker}.
     * @param ip The image processor to modify.
     * @param radius The radius of the median filter.
     */
	public PreprocessingPreviewMedianWorker(ImageProcessor ip, double radius) {
		this.ip = ip;
		this.radius = radius;
	}

	@Override
    protected Void doInBackground() throws Exception {

        // Check cancel before starting
        if (isCancelled()) return null;

        if(radius>0) {
        	RankFilters rf = new RankFilters();
        	rf.rank(ip, radius, RankFilters.MEDIAN);
        }
        return null;
    }
}
