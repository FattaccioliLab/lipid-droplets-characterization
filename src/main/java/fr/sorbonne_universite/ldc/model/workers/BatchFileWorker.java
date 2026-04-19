package fr.sorbonne_universite.ldc.model.workers;

import java.io.File;

import javax.swing.SwingWorker;

import fr.sorbonne_universite.ldc.model.LDCService;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;

/**
 * Created by a {@link BatchWorker}, applies the whole workflow to a specific input file, returns the corresponding
 * {@link ResultsTable} after doing the particle analysis on the file.
 */
public class BatchFileWorker extends SwingWorker<ResultsTable,Void>{
	
	private LDCService ldcPlugin;
	private File inputDirectory;
	private File inputFile;
	
	/**
	 * Creates a worker applying the workflow on the given {@code inputFile}.
     * @param ldcPlugin			The LDC plugin.
	 * @param inputDirectory 	The input directory root.
	 * @param inputFile			The file on which apply the analysis.
	 */
	public BatchFileWorker(LDCService ldcPlugin, File inputDirectory, File inputFile) {
		this.ldcPlugin = ldcPlugin;
		this.inputDirectory = inputDirectory;
		this.inputFile = inputFile;
	}
	
	@Override
	protected ResultsTable doInBackground() throws Exception {
		
		// Opens the image to treat
		ImagePlus image = IJ.openImage(inputFile.getAbsolutePath());
        if (image == null) return null;
        
        // PREPROCESSING
        if (isCancelled()) return null;
        ldcPlugin.applyEnhanceContrast(image.getProcessor());
        SwingWorker<Void, Void> worker = ldcPlugin.createApplyMedianWorker(image.getImageStack());
        worker.execute();
        try {
			worker.get();
		} catch (Exception e) {
			return null;
		}
        
        // THRESHOLDING
		if (isCancelled()) return null;
		ldcPlugin.previewAutoThreshold(image);
		ImagePlus mask = ldcPlugin.applyThreshold(image);
        
		// BINARY MASK OPERATION (only one can be done)
		if (isCancelled()) return null;
		ldcPlugin.applyMorphology(mask);
		
        // PARTICLE ANALYSIS
		if (isCancelled()) return null;
        SwingWorker<ResultsTable, Void> worker2 = ldcPlugin.createMeasuresProcessingWorker(image);
        worker2.execute();
        ResultsTable results = null;
        try {
        	results = worker2.get();
		} catch (Exception e) {
			return null;
		}
        
        // Adds the relative path of the file (or just filename if in root directory)
        String relativeFilePath = getRelativeFilePath();
        for (int row = 0; row < results.size(); row++) {
        	results.setLabel("", row); // Empty label for normal rows
        	results.setValue("Filename", row, relativeFilePath);
        }
    	
		return results;
	}

	/**
	 * Gets the relative path of a file from the input directory.
	 * 
	 * @return The relative path (e.g., "file.tif" or "subfolder/file.tif")
	 */
	private String getRelativeFilePath() {
		try {
			// Get absolute paths
			String rootPath = inputDirectory.getAbsolutePath();
			String filePath = inputFile.getAbsolutePath();

			// Check if file is inside the input directory
			if (filePath.startsWith(rootPath)) {
				// Remove the root path and leading separator
				String relativePath = filePath.substring(rootPath.length());

				// Remove leading slash or backslash
				if (relativePath.startsWith(File.separator)) {
					relativePath = relativePath.substring(1);
				}

				return relativePath;
			} else {
				// Fallback: just return filename if not in expected directory
				return inputFile.getName();
			}
		} catch (Exception e) {
			// In case of error, return just the filename
			return inputFile.getName();
		}
	}
	
}
