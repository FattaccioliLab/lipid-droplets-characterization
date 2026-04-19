package fr.sorbonne_universite.ldc.model.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.rightpanel.BatchWindow;
import ij.measure.ResultsTable;

/**
 * Manages the creation of sub-workers ({@link BatchFileWorker}), for processing
 * the files from a given output directory.<br>
 * Ends by merging results and exporting them to a newly created global '.csv'.
 */
public class BatchWorker extends SwingWorker<Void, Void> {

	private LDCService ldcPlugin;
	private File inputDirectory;
	private File outputFile;
	private BatchWindow bw;

	// For sub-workers management
	private List<BatchFileWorker> workers = new ArrayList<>();
	private List<Future<ResultsTable>> futures = new ArrayList<>();
	
	/**
	 * Creates a BatchWorker.
     * @param ldcPlugin			The LDC plugin, used within {@link BatchFileWorker} for the API calls.
	 * @param inputDirectory	The input directory where it has to find '.tif' and '.tiff' files.
	 * @param outputFile     	The output '.csv' where it writes the results.
	 * @param bw             	A reference to the {@link BatchWindow} creating this
	 *                       	worker, needed to update the progress bar.
	 */
	public BatchWorker(LDCService ldcPlugin, File inputDirectory, File outputFile, BatchWindow bw) {
		this.ldcPlugin = ldcPlugin;
		this.inputDirectory = inputDirectory;
		this.outputFile = outputFile;
		this.bw = bw;
	}

	@Override
	protected Void doInBackground() throws Exception {

		// Gets all tif files
		List<File> files = getAllTifFiles(inputDirectory);
		int totalFiles = files.size();

		// Creates sub-workers : 1 for each file
		for (File inputFile : files) {
			workers.add(new BatchFileWorker(ldcPlugin, inputDirectory, inputFile));
		}

		// Cancellation check
		if (isCancelled()) {
			cancelAllTasks();
			return null;
		}

		// Launches all workers inside the forkJoinPool
		ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
		for (BatchFileWorker worker : workers) {
			Future<ResultsTable> future = forkJoinPool.submit(() -> {
				return worker.doInBackground();
			});
			futures.add(future);
		}

		// Global results table (with 7 first lines empty at start)
		ResultsTable globalResults = new ResultsTable();
	    for (int i = 0; i < 7; i++) {
	        globalResults.incrementCounter();
	    }
	    globalResults.setLabel("Mean", 0);
	    globalResults.setLabel("Median", 1);
	    globalResults.setLabel("SD", 2);
	    globalResults.setLabel("CV", 3);
	    globalResults.setLabel("Min", 4);
	    globalResults.setLabel("Max", 5);
	    globalResults.setLabel("Nb of isolated|total droplets", 6);
	    
	    // Gets all results
	    int completed = 0;
		for (Future<ResultsTable> future : futures) {
			ResultsTable fileResults = future.get(); // Blocking

			if (fileResults != null) mergeResults(globalResults, fileResults);

			completed++;
			int progress = (completed * 100) / totalFiles;
			if (bw != null) bw.updateProgress(progress);

			// Cancellation check
			if (isCancelled()) {
				cancelAllTasks();
				return null;
			}
		}
		
		// Calculates and fills global statistics
	    calculateSummaryStatistics(globalResults);

		// CSV export
		globalResults.save(outputFile.getAbsolutePath());

		return null;
	}

	/**
	 * Gets all '.tif' and '.tiff' files from the given {@code directory}.
	 * 
	 * @param directory The directory in which we recursively serch for input files.
	 * @return The list of '.tif' and '.tiff' inside the {@code directory}.
	 */
	private List<File> getAllTifFiles(File directory) {
		List<File> tifFiles = new ArrayList<>();

		if (directory == null || !directory.exists() || !directory.isDirectory()) {
			return tifFiles;
		}

		File[] files = directory.listFiles();
		if (files == null) return tifFiles;

		for (File file : files) {
			if (file.isDirectory()) {
				// Recursion inside sub-folders
				tifFiles.addAll(getAllTifFiles(file));
			} else if (file.getName().toLowerCase().endsWith(".tif") || file.getName().toLowerCase().endsWith(".tiff")) {
				tifFiles.add(file);
			}
		}

		return tifFiles;
	}

	/**
	 * Merges a {@code toAdd} table into a {@code global} table.
	 * 
	 * @param global The destination table.
	 * @param toAdd  Its content will be added at the end of {@code global}.
	 */
	private void mergeResults(ResultsTable global, ResultsTable toAdd) {

		for (int row = 0; row < toAdd.size(); row++) {
			global.incrementCounter();

			for (int col = 0; col <= toAdd.getLastColumn(); col++) {
				String heading = toAdd.getColumnHeading(col);
				
				// Ignore if no valid heading
				if (heading == null || heading.trim().isEmpty())
					continue;

				// Ignore empty column
				if (!toAdd.columnExists(col))
					continue;

				// Try in first decoding as a double value (most frequent)
				double value = toAdd.getValueAsDouble(col, row);
				
				if (!Double.isNaN(value)) {
					global.addValue(heading, value);
				} else { // If not ok, then decode as a String
					String valueS = toAdd.getStringValue(col, row);
					if (valueS != null) global.addValue(heading, valueS);
				}
			}
		}
	}

	/**
	 * Cancels all running {@link BatchFileWorker} tasks and futures.
	 */
	private void cancelAllTasks() {
		// Cancel workers
		for (BatchFileWorker worker : workers)
			worker.cancel(true);

		// Cancel futures
		for (Future<ResultsTable> future : futures)
			future.cancel(true);
	}
	
	/**
	 * Calculates and fills summary statistics in the first 7 rows of the ResultsTable.
	 * 
	 * @param rt The ResultsTable with placeholder rows 0-6 for statistics
	 */
	private void calculateSummaryStatistics(ResultsTable rt) {
	    if (rt == null || rt.size() <= 6) return;

	    // Clear filename for stats rows
	    for (int i = 0; i < 7; i++) {
	        rt.setValue("Filename", i, "");
	    }

	    int dataStartRow = 8;
	    int dropletsCount = 0;
	    int isolatedDropletsCount = 0;

	    for (int col = 0; col <= rt.getLastColumn(); col++) {
	        String heading = rt.getColumnHeading(col);
	        rt.setValue(heading, 6, ""); // number of isolated|total droplets case empty for the column

	        // Ignore invalid columns
	        if (heading == null || heading.trim().isEmpty()
	                || !rt.columnExists(col)
	                || heading.equals("Filename")) {
	            continue;
	        }

	        List<Double> values = new ArrayList<>();

	        double min = Double.MAX_VALUE;
	        double max = -Double.MAX_VALUE;
	        double sum = 0;
	        double sumSquares = 0;
	        int count = 0;

	        for (int row = dataStartRow; row < rt.size(); row++) {
	            try {
	                double value = rt.getValueAsDouble(col, row);

	                if (!Double.isNaN(value)) {
	                    min = Math.min(min, value);
	                    max = Math.max(max, value);
	                    sum += value;
	                    sumSquares += value * value;
	                    values.add(value);
	                    count++;
	                    if (heading.equals("is_isolated")) {
	                    	if (value == 1.0) isolatedDropletsCount++;
	                    	dropletsCount++;
	                    }
	                }

	            } catch (Exception e) {
	                // ignore
	            }
	        }

	        if (count > 0) {
	        	double mean = sum / count;
	        	double variance = (count > 1) ? (sumSquares - (sum * sum) / count) / (count - 1) : 0;
	        	double stdDev = Math.sqrt(Math.max(0, variance));

	            // MEDIAN
	            Collections.sort(values);
	            double median;
	            if (count % 2 == 0) {
	                median = (values.get(count / 2 - 1) + values.get(count / 2)) / 2.0;
	            } else {
	                median = values.get(count / 2);
	            }

	            // CV
	            double cv = (mean != 0) ? stdDev / mean : 0;

	            rt.setValue(heading, 0, mean);
	            rt.setValue(heading, 1, median);
	            rt.setValue(heading, 2, stdDev);
	            rt.setValue(heading, 3, cv);
	            rt.setValue(heading, 4, min);
	            rt.setValue(heading, 5, max);
	        }
	    }
	    
	    // Adds the number of "isolated|total" droplets in the is_isolated column, at line 6
	    if (rt.getColumnIndex("is_isolated") >= 0) {
	        String stat = isolatedDropletsCount+"|"+dropletsCount;
	        rt.setValue("is_isolated", 6, stat);
	    }
	    
	}

}
