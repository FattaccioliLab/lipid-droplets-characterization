package fr.sorbonne_universite.ldc.model.workers;

import javax.swing.SwingWorker;

import ij.IJ;
import ij.ImageStack;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

/**
 * SwingWorker applying a median filter using ImageJ's {@link RankFilters} on a whole {@link ImageStack}, 
 * or on an individual {@link ImageProcessor} among the given stack.
 * <p>
 * Supports cancellation during the stack loop.
 * </p>
 */
public class PreprocessingApplyMedianWorker extends SwingWorker<Void, Void>{
	
	private boolean medianFilterEnabled;
	private double radius;
	private boolean processAll;
	private int targetSlice;
	private ImageStack stack;
	
	/**
	 * Creates a {@code PreprocessingApplyMedianWorker}.
	 * @param medianFilterEnabled Indicates if the median filter is enabled.
	 * @param radius The median filter radius (px).
	 * @param stack The array of images to process (slices).
	 * @param processAll True to process all slices.
	 * @param targetSlice The specific slice to process if not all.
	 */
	public PreprocessingApplyMedianWorker(boolean medianFilterEnabled, double radius, 
			ImageStack stack, boolean processAll, int targetSlice) {
		this.medianFilterEnabled = medianFilterEnabled;
		this.radius = radius;
		this.stack = stack;
		this.processAll = processAll;
		this.targetSlice = targetSlice;
	}

    @Override
    protected Void doInBackground() throws Exception {
    	IJ.showStatus("Applying filters...");
    	
        // Check cancel before starting
        if (isCancelled()) return null;
    	
        if (medianFilterEnabled) {
            final RankFilters rf = new RankFilters();
            final int n = stack.getSize();

            if (processAll) {
                for (int z = 1; z <= n; z++) {
                    // CRITICAL: Check cancellation inside the loop!
                    if (isCancelled()) break; 
                    
                    rf.rank(stack.getProcessor(z), radius, RankFilters.MEDIAN);
                    IJ.showProgress(z, n);
                }
            } else if (targetSlice >= 1 && targetSlice <= n) {
                rf.rank(stack.getProcessor(targetSlice), radius, RankFilters.MEDIAN);
            }
            IJ.showProgress(1.0);
        }
        return null;
    }
}
