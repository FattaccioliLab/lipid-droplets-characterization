package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanelSubPanel;

/**
 * A custom Swing component that draws an image histogram.
 * Supports both 8-bit (256 bins) and 16-bit (65536 bins) histograms.
 * 
 * <p>Does not have to follow the contracts needed by a {@link LeftPanelSubPanel}.</p>
 */
@SuppressWarnings("serial")
public class HistogramPanel extends JPanel {

    private int[] histogram;
    private int maxCount = 0;
    private int effectiveMaxBin;	//for nb of effective value/bin (value > 0) in the histogram
    
    // The current threshold range to highlight
    private int minThreshold = -1;
    private int maxThreshold = -1;

    public HistogramPanel() {
        // Standard ImageJ threshold window size style
        setPreferredSize(new Dimension(256, 100)); 
        setBackground(Color.WHITE);
    }

    /**
     * Updates the histogram data to display.
     * @param stats Array of integers representing pixel counts (size 256 or 65536).
     */
    public int setHistogram(int[] stats) {
        this.histogram = stats;
        
        this.effectiveMaxBin = 0; // Reset
        
        if (histogram != null) {
        	
            // 1. Find the height of the tallest bar (Y-axis scaling)
            for (int count : histogram) {
                if (count > maxCount) maxCount = count;
            }

            // 2. Find the last bin that actually has data (X-axis scaling)
            // We search backwards from the end.
            for (int i = histogram.length - 1; i >= 0; i--) {
                if (histogram[i] > 0) {
                    effectiveMaxBin = i;
                    break;
                }
            }
            
            // Safety: Ensure we don't zoom in crazy close on an empty/black image
            // If it's an 8-bit image, length is 256, so we default to 255.
            if (effectiveMaxBin < 255) effectiveMaxBin = 255; 
        }
        //System.out.println("pixel counts : "+stats.length+", maxCount : "+maxCount);
        repaint();
        return effectiveMaxBin;		//returning this value, so that ThresholdPanel can use it to determine the max value of a slider.
    }

    public void setThresholdRange(int min, int max) {
        this.minThreshold = min;
        this.maxThreshold = max;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (histogram == null || histogram.length == 0) {
            g.setColor(Color.GRAY);
            g.drawString("No Histogram Data", 20, 50);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        //int totalBins = 10000;//histogram.length; // Will be 256 (8-bit) or 65536 (16-bit), do
        
        // Instead of dividing by histogram.length (65536), 
        // we divide by effectiveMaxBin (e.g., 8291).
        // This "stretches" the relevant data to fill the panel width.
        double binScale = (double) effectiveMaxBin;
        
        // Draw the bars
        // We cannot draw 65536 bars on a 300px screen, so we loop through screen pixels (width)
        // instead of histogram bins.
        for (int x = 0; x < width; x++) {
            
        	// Map screen pixel (x) to the effective data range
            int startBin = (int) ((x / (double) width) * binScale);
            int endBin = (int) (((x + 1) / (double) width) * binScale);
            
            // Clamp endBin to prevent array index out of bounds
            if (endBin >= histogram.length) endBin = histogram.length - 1;

            // Since multiple bins might fit into 1 screen pixel (for 16-bit images),
            // we grab the MAX count in that range to represent the peak.
            int count = 0;
            for (int k = startBin; k <= endBin; k++) {
                 if (histogram[k] > count) count = histogram[k];
            }
            
            // Logarithmic Scaling for height (matches ImageJ native look)
            // Helps see small peaks against large backgrounds
            int barHeight = 0;
            if (maxCount > 0 && count > 0) {
                 barHeight = (int) (height * (Math.log(count) / Math.log(maxCount)));
            }
            
            // Color Logic
            // If the bin range corresponds to selected threshold -> RED
            // Note: startBin is the value on the X axis (pixel value)
            if (startBin >= minThreshold && startBin <= maxThreshold) {
                g.setColor(Color.RED); 
            } else {
                g.setColor(Color.BLACK); 
            }

            // Draw vertical line for this X position
            g.drawLine(x, height, x, height - barHeight);
        }
    }
}