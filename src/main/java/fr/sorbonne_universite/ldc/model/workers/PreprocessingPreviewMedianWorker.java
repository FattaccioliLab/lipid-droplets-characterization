package fr.sorbonne_universite.ldc.model.workers;

import javax.swing.SwingWorker;

import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

/**
 * SwingWorker applying a median filter preview on an {@link ImageProcessor}.
 */
public class PreprocessingPreviewMedianWorker extends SwingWorker<Void, Void>{
	
	/** The image processor of the current slice to apply the preview on. */
	private ImageProcessor ip;
	
	/** The median filter radius in pixels. */
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

	
	/**
	 * Applies the median filter to a single {@link ImageProcessor} (the current slice preview),
	 * using ImageJ's {@link RankFilters#MEDIAN} rank filter.
	 *
	 * <p>Does nothing if the radius is zero or negative.
	 * Cancellation is checked before execution.</p>
	 *
	 * @return {@code null} on completion or cancellation.
	 * @throws Exception if an unexpected error occurs during processing.
	 */
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
