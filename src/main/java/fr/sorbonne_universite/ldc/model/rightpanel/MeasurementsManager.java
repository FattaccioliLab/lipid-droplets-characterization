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
     * @param minSize 						Minimum particle size (px²).
     * @param maxSize 						Maximum particle size (px²).
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
     * @param minSize 						Minimum particle size (px²).
     * @param maxSize 						Maximum particle size (px²).
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
     * @param rt 			The ResultsTable to generate statistics from.
     * @param cal 			The calibration of the current image to consider.
     * @param imgWidth 		The width of the current image to consider.
     * @param imgHeight 	The height of the current image to consider.
     * @return 				The ResultsTable containing the statistics.  
     */
    public ResultsTable calculateSummaryTable(ResultsTable rt, Calibration cal, double imgWidth, double imgHeight) {
    	
    	ResultsTable summaryRt = new ResultsTable();
    	
    	if (rt == null || rt.getCounter() == 0) {
    		IJ.showMessage("No data yet.");
    		return summaryRt;
    	}
    	
        double totalImageArea = 0;
        
        if (cal != null) {
        	// calculate the total area of the image
            double totalArea = cal.pixelWidth * cal.pixelHeight;
            totalImageArea = imgWidth * imgHeight * totalArea;
        }
        
        // Slice number -> List of indexes of line that correspond to the slice
        Map<Integer, List<Integer>> rowsBySlice = new HashMap<>();
        
        boolean hasSliceCol = rt.columnExists("Slice"); // check if there is a slice column in the table
        
        for (int i = 0; i < rt.getCounter(); i++) {
            // if there is no slice columns, we consider that every rows are in slice 1
            int slice = hasSliceCol ? (int) rt.getValue("Slice", i) : 1;
            rowsBySlice.computeIfAbsent(slice, k -> new ArrayList<>()).add(i);
        }
              
        // columns to compute statistics
        String[] headings = rt.getHeadings();
        
        // columns to ignores
        Set<String> ignoredCols = new HashSet<>(Arrays.asList(
            "Slice", "X", "Y", "is_isolated"
        ));
        
        // sorting
        List<Integer> sortedSlices = new ArrayList<>(rowsBySlice.keySet());
        Collections.sort(sortedSlices);
        
        // compute statistics for each list
        for (int slice : sortedSlices) {
        	
        	List<Integer> rows = rowsBySlice.get(slice);
        	int count = rows.size();
        	
        	summaryRt.incrementCounter();
            summaryRt.addValue("Slice", slice);
            summaryRt.addValue("Count", count);
            
            for (String col : headings) {
                // skip ignored columns
                if (ignoredCols.contains(col)) {
                    continue;
                }

                // check that the column exists
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
                    
                	// if there is valid values compute their statistics
                	if (n > 0) {
                		Collections.sort(values);
                		
                		// Min and Max
                		double min = values.get(0);
                		double max = values.get(n - 1);
                		
                		// Mean and Sum
                		double sum = 0;
                		for (double v : values) sum += v;
                		double mean = sum / n;
                		
                		// Median
                		double median;
                		if (n % 2 == 0) median = (values.get(n / 2 - 1) + values.get(n / 2)) / 2;
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
                    	
                    	// for area compute total area and %Area
                    	if (col.equals("Area")) {
                    		double pctArea = (totalImageArea > 0) ? (sum / totalImageArea) * 100.0 : 0;
                    		summaryRt.addValue("Total Area", sum);
                    		summaryRt.addValue("%Area", pctArea);
                        }
                    	
                    	// add the columns to the summary table
                    	summaryRt.addValue("Mean_" + col, mean);
                    	summaryRt.addValue("Median_" + col, median);
                    	summaryRt.addValue("StdDev_" + col, stdDev);
                    	summaryRt.addValue("CV_" + col, cv);
                    	summaryRt.addValue("Min_" + col, min);
                    	summaryRt.addValue("Max_" + col, max);
                    	
                	} else {
                        // handle case where all values are NaN
                		summaryRt.addValue("Mean_" + col, Double.NaN);
                        summaryRt.addValue("Median_" + col, Double.NaN);
                        summaryRt.addValue("StdDev_" + col, Double.NaN);
                        summaryRt.addValue("CV_" + col, Double.NaN);
                        summaryRt.addValue("Min_" + col, Double.NaN);
                        summaryRt.addValue("Max_" + col, Double.NaN);
                        
                        if (col.equals("Area")) {
                        	summaryRt.addValue("Total Area", Double.NaN);
                            summaryRt.addValue("%Area", Double.NaN);
                        }
                	}
                }
            }
        }

        return summaryRt;
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
