package fr.sorbonne_universite.ldc.ui.leftpanel.subpanels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanelSubPanel;

/**
 * A custom Swing component that draws an image histogram.
 * Supports both 8-bit and 16-bit histograms dynamically.
 */
@SuppressWarnings("serial")
public class HistogramPanel extends JPanel {

    private int[] histogram;
    private int maxCount = 0;
    private int effectiveMaxBin;    
    
    // The current threshold range to highlight
    private int minThreshold = -1;
    private int maxThreshold = -1;

    // Mathematical bounds of the array
    private double histMin = 0;
    private double histMax = 255;

    public HistogramPanel() {
        setPreferredSize(new Dimension(256, 100)); 
        setBackground(Color.WHITE);
    }

    /**
     * Updates the histogram data to display, including its mathematical bounds.
     */
    public int setHistogram(int[] stats, double min, double max) {
        this.histogram = stats;
        this.histMin = min;
        this.histMax = max;
        
        this.effectiveMaxBin = 0; 
        this.maxCount = 0; // Reset max count!
        
        if (histogram != null) {
            
            for (int count : histogram) {
                if (count > maxCount) maxCount = count;
            }

            for (int i = histogram.length - 1; i >= 0; i--) {
                if (histogram[i] > 0) {
                    effectiveMaxBin = i;
                    break;
                }
            }
            
            // Safety to prevent division by zero on blank images
            if (effectiveMaxBin < 1) effectiveMaxBin = 1;
            
            // For 8-bit images, ensure the X-axis stays static 0-255 even if dark
            if (histMax <= 255 && effectiveMaxBin < 255) {
                effectiveMaxBin = 255;
            }
        }
        repaint();
        return effectiveMaxBin; 
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
        
        double binScale = (double) effectiveMaxBin;
        
        for (int x = 0; x < width; x++) {
            
            int startBin = (int) ((x / (double) width) * binScale);
            int endBin = (int) (((x + 1) / (double) width) * binScale);
            
            if (endBin >= histogram.length) endBin = histogram.length - 1;

            int count = 0;
            for (int k = startBin; k <= endBin; k++) {
                 if (histogram[k] > count) count = histogram[k];
            }
            
            int barHeight = 0;
            if (maxCount > 0 && count > 0) {
                 barHeight = (int) (height * (Math.log(count) / Math.log(maxCount)));
            }
            
            // Translate the array bin index (e.g. 0-255) into the 
            // REAL 16-bit pixel intensity value (e.g. 0-6000)
            double binIntensity = histMin + ((double) startBin / histogram.length) * (histMax - histMin);
            double nextBinIntensity = histMin + ((double) (startBin + 1) / histogram.length) * (histMax - histMin);

            // Check if this pixel intensity falls within our slider values
            if (nextBinIntensity >= minThreshold && binIntensity <= maxThreshold) {
                g.setColor(Color.RED); 
            } else {
                g.setColor(Color.BLACK); 
            }

            g.drawLine(x, height, x, height - barHeight);
        }
    }
}