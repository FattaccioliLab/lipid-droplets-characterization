package fr.sorbonne_universite.ldc.model.rightpanel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingWorker;

import fr.sorbonne_universite.ldc.model.workers.MeasuresProcessingWorker;
import fr.sorbonne_universite.ldc.model.workers.MeasuresPreviewWorker;
import fr.sorbonne_universite.ldc.ui.leftpanel.subpanels.PreprocessingPanel;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.measure.Calibration;
import ij.measure.ResultsTable;

/**
 * Provides measurements management operations. Indirectly used by the LDC {@link PreprocessingPanel} UI class.
 */
public class MeasurementsManager {

    /**
     * Creates a {@link SwingWorker}, that take care of processing and showing the measurements preview, if executed.
     * @param isCalibrated					Boolean that tell if the image there is a calibration given for the image
     * @param Calibration					Given calibration.
     * @param minSize 						Minimum particle size (μm² IsCalibrated is true, otherwise px²).
     * @param maxSize 						Maximum particle size (μm² IsCalibrated is true, otherwise px²).
     * @param minCircularity 				Minimum particle circularity.
     * @param maxCircularity 				Maximum particle circularity.
     * @param excludeOnEdgesEnabled 		Particle Analyzer option.
     * @param showAreaEnabled 				True if the 'Area' column must be shown in the results.
     * @param showMedianEnabled 			True if the 'Median' column must be shown in the results.
     * @param showMeanEnabled 				True if the 'Mean' column must be shown in the results.
     * @param showIntegratedDensityEnabled 	True if the 'IntegratedDensity' column must be shown in the results.
     * @param showCircularityEnabled 		True if the 'Circularity' column is shown must be the results.
     * @param img 							The current image to consider.
	 * @see MeasuresPreviewWorker
     */
	public SwingWorker<Void,Void> createMeasuresPreviewWorker(
    		boolean isCalibrated,
    		Calibration calibration,
			double minSize,
			double maxSize,
			double minCircularity,
			double maxCircularity,
			boolean excludeOnEdgesEnabled, 
			boolean showAreaEnabled,
			boolean showMedianEnabled,
			boolean showMeanEnabled,
			boolean showIntegratedDensityEnabled,
			boolean showCircularityEnabled,
			ImagePlus img){
		return new MeasuresPreviewWorker(
				isCalibrated,
				calibration,
				minSize,
				maxSize,
				minCircularity,
				maxCircularity,
				excludeOnEdgesEnabled, 
				showAreaEnabled,
				showMedianEnabled,
				showMeanEnabled,
				showIntegratedDensityEnabled,
				showCircularityEnabled, 
				img); 
	}
	
    /**
     * Creates a {@link SwingWorker}, that take care of processing measurements and showing them, if executed.
     * @param isCalibrated					Boolean that tell if the image there is a calibration given for the image
     * @param Calibration					Given calibration.
     * @param minSize 						Minimum particle size (μm² IsCalibrated is true, otherwise px²).
     * @param maxSize 						Maximum particle size (μm² IsCalibrated is true, otherwise px²).
     * @param minCircularity 				Minimum particle circularity.
     * @param maxCircularity 				Maximum particle circularity.
     * @param excludeOnEdgesEnabled 		Particle Analyzer option.
     * @param circularityThreshold 			Threshold on particle's circularity to define if they are isolated.
     * @param showAreaEnabled 				True if the 'Area' column must be shown in the results.
     * @param showMedianEnabled 			True if the 'Median' column must be shown in the results.
     * @param showMeanEnabled 				True if the 'Mean' column must be shown in the results.
     * @param showIntegratedDensityEnabled 	True if the 'IntegratedDensity' column must be shown in the results.
     * @param showCircularityEnabled 		True if the 'Circularity' column is shown must be the results.
     * @param img 							The current image to consider.
	 * @see MeasuresProcessingWorker
     */
	public SwingWorker<Void,Void> createMeasuresProcessingWorker(
    		boolean isCalibrated,
    		Calibration calibration,
			double minSize,
			double maxSize,
			double minCircularity,
			double maxCircularity,
			boolean excludeOnEdgesEnabled,
			double circularityThreshold,
			boolean showAreaEnabled,
			boolean showMedianEnabled,
			boolean showMeanEnabled,
			boolean showIntegratedDensityEnabled,
			boolean showCircularityEnabled,
			ImagePlus img){
		return new MeasuresProcessingWorker(
	    		isCalibrated,
	    		calibration,
				minSize,
				maxSize,
				minCircularity,
				maxCircularity,
				excludeOnEdgesEnabled,
				circularityThreshold,
				showAreaEnabled,
				showMedianEnabled,
				showMeanEnabled,
				showIntegratedDensityEnabled,
				showCircularityEnabled, 
				img); 
	}
	
    /**
     * Export as a CSV file the table passed in parameter. 
     * @param rt 		The ResultsTable to export as a CSV file.
     * @param path 		The path of the file as a String.
     */
    public void exportResultsTable(ResultsTable rt, String path) {
    	
    	// check if there is data to export
    	if (rt == null || rt.getCounter() == 0) {
    		IJ.showMessage("No data to export.");
    		return;
    	}
    	
    	// force CSV extension
    	if (!path.toLowerCase().endsWith(".csv")) {
            path += ".csv";
        }

    	// try to save the file
    	if (!rt.save(path)) {
    		IJ.showMessage("An error occured while saving file.");
    	}
    }
    
    /**
     * Calculate the summary ResultTable generated by ImageJ.
     * @param rt            The ResultsTable to generate statistics from.
     * @param cal           The calibration of the current image to consider.
     * @param imgWidth      The width of the current image to consider.
     * @param imgHeight     The height of the current image to consider.
     * @return              The ResultsTable containing the statistics.  
     */
    public ResultsTable calculateSummaryTable(ResultsTable rt, Calibration cal, double imgWidth, double imgHeight) {
        
        ResultsTable summaryRt = new ResultsTable();
        
        if (rt == null || rt.getCounter() == 0) {
            IJ.showMessage("No data yet.");
            return summaryRt;
        }
        
        // Slice number -> List of indexes of line that correspond to the slice
        Map<Integer, List<Integer>> rowsBySlice = new HashMap<>();
        List<Integer> allRows = new ArrayList<>(); // to store all rows for overall statistics
        
        boolean hasSliceCol = rt.columnExists("Slice"); // check if there is a slice column
        
        for (int i = 0; i < rt.getCounter(); i++) {
            int slice = hasSliceCol ? (int) rt.getValue("Slice", i) : 1;
            rowsBySlice.computeIfAbsent(slice, k -> new ArrayList<>()).add(i);
            allRows.add(i);
        }
        
        // columns to compute statistics
        String[] headings = rt.getHeadings();
        
        // columns to ignore
        Set<String> ignoredCols = new HashSet<>(Arrays.asList(
            "Slice", "is_isolated"
        ));
        
        // sorting slices
        List<Integer> sortedSlices = new ArrayList<>(rowsBySlice.keySet());
        Collections.sort(sortedSlices);
        
        // compute and add overall statistics
        addStatisticsRows(summaryRt, rt, allRows, "", headings, ignoredCols);
        
        // compute and add statistics for each slice
        for (int slice : sortedSlices) {
            addStatisticsRows(summaryRt, rt, rowsBySlice.get(slice), String.valueOf(slice), headings, ignoredCols);
        }

        return summaryRt;
    }

    /**
     * Helper method to calculate statistics and add the summary rows for a given set of rows.
     * @param summaryRt			The Result Table of the summary to add rows to.
     * @param rt				The Result Table to generate statistics from.
     * @param rows				The set of rows to generate summary.
     * @param sliceLabel		The slice to generate summary.
     * @param headings			The headings of the summary table.
     * @param ignoredCols		The set of columns to ignore.
     */
    private void addStatisticsRows(ResultsTable summaryRt, ResultsTable rt, List<Integer> rows, String sliceLabel, String[] headings, Set<String> ignoredCols) {
        
        // maps to store the computed statistic for each column heading
        Map<String, Double> means = new HashMap<>();
        Map<String, Double> medians = new HashMap<>();
        Map<String, Double> sds = new HashMap<>();
        Map<String, Double> cvs = new HashMap<>();
        Map<String, Double> mins = new HashMap<>();
        Map<String, Double> maxs = new HashMap<>();

        for (String col : headings) {
            // skip ignored columns
            if (ignoredCols.contains(col)) continue;

            int colIndex = rt.getColumnIndex(col);
            if (colIndex != ResultsTable.COLUMN_NOT_FOUND) {
                List<Double> values = new ArrayList<>();
                float[] colData = rt.getColumn(colIndex);
                
                // collect valid values
                for (int row : rows) {
                    double val = colData[row];
                    if (!Double.isNaN(val)) {
                        values.add(val);
                    }
                }

                int n = values.size();
                
                // compute statistics if values exist
                if (n > 0) {
                    Collections.sort(values);
                    
                    // Min and Max
                    double min = values.get(0);
                    double max = values.get(n - 1);
                    
                    // Mean
                    double sum = 0;
                    for (double v : values) sum += v;
                    double mean = sum / n;
                    
                    // Median
                    double median;
                    if (n % 2 == 0) median = (values.get(n / 2 - 1) + values.get(n / 2)) / 2.0;
                    else median = values.get(n / 2);
                
                    // Standard Deviation
                    double stdDev = 0;
                    if (n > 1) {
                        double sumSqDiff = 0;
                        for (double v : values) sumSqDiff += (v - mean) * (v - mean);
                        stdDev = Math.sqrt(sumSqDiff / (n - 1));
                    }
                    
                    // Coefficient of Variation
                    double cv = (mean != 0) ? (stdDev / mean) * 100 : 0;
                    
                    // adding the data the maps
                    means.put(col, mean);
                    medians.put(col, median);
                    sds.put(col, stdDev);
                    cvs.put(col, cv);
                    mins.put(col, min);
                    maxs.put(col, max);
                } else {
                    // handle missing values
                    means.put(col, Double.NaN);
                    medians.put(col, Double.NaN);
                    sds.put(col, Double.NaN);
                    cvs.put(col, Double.NaN);
                    mins.put(col, Double.NaN);
                    maxs.put(col, Double.NaN);
                }
            }
        }

        // define the sequence of statistics rows
        String[] statNames = {"Mean", "Median", "SD", "CV", "Min", "Max"};
        @SuppressWarnings("unchecked")
        Map<String, Double>[] statMaps = new Map[]{means, medians, sds, cvs, mins, maxs};

        // construct the actual rows in the ResultsTable
        for (int i = 0; i < statNames.length; i++) {
            summaryRt.incrementCounter();
            
            // add Label and Slice columns
            summaryRt.addValue("Label", statNames[i]);
            summaryRt.addValue("Slice", sliceLabel); 

            // add calculated values for each feature column
            for (String col : headings) {
                if (ignoredCols.contains(col)) continue;
                
                Double val = statMaps[i].get(col);
                if (val != null) {
                    summaryRt.addValue(col, val);
                } else {
                    summaryRt.addValue(col, Double.NaN);
                }
            }
        }
    }
    
    /**
     * Generate a list containing histograms for each columns of the ResultsTable.
     * @param rt 		ResultsTable to generated histograms from.
     * @return 			List that contain ImagePlus Object of histograms for each column of the table.
     */
    public List<ImagePlus> generateHistograms(ResultsTable rt){
    	
    	List<ImagePlus> plots = new ArrayList<>();
    	
    	// check if the table is null or empty
    	if (rt == null || rt.getCounter() == 0) {
    		IJ.showMessage("No data to generate histograms");
    		return plots;
    	}
    	
    	String[] headings = rt.getHeadings();
    	
    	Set<String> ignored = new HashSet<>(Arrays.asList("Slice"));
    	
    	// compute histograms of each columns
    	for (String col : headings) {
    		if (ignored.contains(col) || !rt.columnExists(col)) continue; // skip if ignored
    		
    		double[] data = rt.getColumn(col);
            if (data == null || data.length == 0) continue; // skip if no data

            // compute MIN MAX statistics
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (double v : data) {
                if (v < min) min = v;
                if (v > max) max = v;
            }
            
            // avoid division by zero
            if (min == max) {
                min -= 1;
                max += 1;
            }

            // bins creation
            int nBins = 20; // arbitrary value
            double binWidth = (max - min) / nBins;
            double[] xValues = new double[nBins];
            double[] yValues = new double[nBins];

            // initialization center X
            for (int i = 0; i < nBins; i++) {
                xValues[i] = min + (i + 0.5) * binWidth;
            }

            // fill frequencies
            for (double v : data) {
                int bin = (int) ((v - min) / binWidth);
                if (bin >= nBins) bin = nBins - 1;
                if (bin < 0) bin = 0;
                yValues[bin]++;
            }

            // create plot ImageJ
            Plot plot = new Plot("Distribution: " + col, col, "Frequency");
            plot.add("bar", xValues, yValues);
            plot.setColor(Color.BLUE);
            plot.setStyle(0, "blue, fill, 1.0"); 
            
            plots.add(plot.getImagePlus());
        }
    	return plots;
    }
}
