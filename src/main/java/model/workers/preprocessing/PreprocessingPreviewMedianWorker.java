package model.workers.preprocessing;

import javax.swing.SwingWorker;

import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

/**
 * SwingWorker applying a median filter preview on an {@link ImageProcessor}.
 * <p>
 * When preview mode is enabled, the image is reset, snapshotted, and filtered
 * using a median filter with the specified radius. When disabled, the image
 * is restored to its original state.
 * </p>
 */
public class PreprocessingPreviewMedianWorker extends SwingWorker<Void, Void>{
	
	private ImageProcessor ip;
	private boolean isPreviewOn;
	private double radius;
	
    /**
     * Creates a {@link PreprocessingPreviewMedianWorker}.
     * @param ip The image processor to modify.
     * @param isPreviewOn Whether preview mode is enabled. If it is false it will reset the given image processor.
     * @param radius The radius of the median filter.
     */
	public PreprocessingPreviewMedianWorker(ImageProcessor ip, boolean isPreviewOn, double radius) {
		this.ip = ip;
		this.isPreviewOn = isPreviewOn;
		this.radius = radius;
	}

	@Override
    protected Void doInBackground() throws Exception {

        // Check cancel before starting
        if (isCancelled()) return null;

        if (isPreviewOn) {
            ip.reset(); 
            if (isCancelled()) return null;
            
            ip.snapshot(); 
            if (isCancelled()) return null;
            
            if(radius>0) {
                RankFilters rf = new RankFilters();
                rf.rank(ip, radius, RankFilters.MEDIAN);
            }
        } else {
            ip.reset();
        }
        return null;
    }
}
