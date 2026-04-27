package fr.sorbonne_universite.ldc.ui.rightpanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;


import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.leftpanel.LeftPanel;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;

import org.scijava.event.EventService;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;

/**
 * The {@link JPanel} at the right in the {@link MainGUI_LDC}.
 */
@SuppressWarnings("serial")
public class RightPanel extends JPanel {
	
	private LeftPanel leftPanel;

	@Parameter
	private DatasetIOService datasetIOService;
	
	@Parameter(type = ItemIO.OUTPUT)
	private Dataset image;
	
	@Parameter
	private UIService uiService;
	
	@Parameter
	private ImageDisplayService displayService;
	
	@Parameter
	private EventService eventService;
    
	@Parameter
	private LDCService selectedSettings;

    private JPanel viewPanel; // container panel for the data table
    
    private ResultsTable currentTable; // reference for the table currently shown 
    private int nb_particle = 0; // total number of particle in the current table
    private int nb_isolated = 0;	// save the number of isolated particle showed with the given measures parameters
    
    private JLabel nbIsolatedLabel; // label that show the number of isolated particles on the total number of particles
        
    /**
     * Constructor for the {@link RightPanel}.
     * @param ctx			The LDC plugin context.
     * @param leftPanel		The instance of the {@link RightPanel} of the {@link MainGUI_LDC}.
     */
    public RightPanel(Context ctx, LeftPanel leftPanel) {
    	super();
    	ctx.inject(this);
    	this.leftPanel = leftPanel;
    	
    	setLayout(new BorderLayout());
    	
    	// formate : (top, left, bottom, right)
    	// --- Add a visual separator line and padding ---
        // A CompoundBorder applies two borders together: an outer line, and inner padding.
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY), // Outer: 1px gray line on the LEFT
            BorderFactory.createEmptyBorder(8, 10, 8, 8)                  // Inner:  padding
        ));
    	
    	
    	JPanel headerPanel = new JPanel();
        JPanel footerPanel = new JPanel();
    	
        // preview overlay button
        JButton previewButton = new JButton("Preview");
        previewButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
//        previewButton.setPreferredSize(new Dimension(80, 25));
        previewButton.addActionListener(e -> {
        	ImagePlus img = leftPanel.getCurrentImage();
        	// check if there is an image
        	if (img == null) {
        		IJ.showMessage("Please open an image first (File > Open)");
        		return ;
        	}
        	
        	this.leftPanel.updateAnalysisParametersInputValues(); // consider updated analysis input values, if not updated
            
        	// --- Fetch the current img's binary mask ---
            String binaryTitle = img.getShortTitle() + "_Binary";
            ImagePlus binaryImg = ij.WindowManager.getImage(binaryTitle);
            
            if (binaryImg == null) {
                IJ.showMessage("Please complete the thresholding step first to generate a mask.");
                return;
            }
            // ----------------------------------------------
        	
        	
        	// launch the preview worker on the binary mask
        	SwingWorker<Void,Void> previewWorker = selectedSettings.createMeasuresPreviewWorker(binaryImg);
        	previewWorker.execute();
        });;
        headerPanel.add(previewButton);

        // show measures button
        JButton resultsButton = new JButton("Show results");
        resultsButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        resultsButton.addActionListener(e -> {
        	ImagePlus img = leftPanel.getCurrentImage();
        	// check if there is an image
        	if (leftPanel.getCurrentImage() == null) {
        		IJ.showMessage("Please open an image first (File > Open)");
        		return ;
        	}
        	
        	// --- Fetch the current img's binary mask ---
            String binaryTitle = img.getShortTitle() + "_Binary";
            ImagePlus binaryImg = ij.WindowManager.getImage(binaryTitle);
            
            if (binaryImg == null) {
                IJ.showMessage("Please complete the thresholding step first to generate a mask.");
                return;
            }
            // ----------------------------------------------
        	
        	this.leftPanel.updateAnalysisParametersInputValues(); // consider updated analysis input values, if not updated
          
        	SwingWorker<ResultsTable,Void> measuresWorker = selectedSettings.createMeasuresProcessingWorker(img, binaryImg);
        	
        	// adding property change listener to the worker to show the table when the asynchronous task is completed
        	measuresWorker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                    	try {
                    		showTable(measuresWorker.get());
                    	} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
                    }
                }
            });
        	measuresWorker.execute();
        });
        headerPanel.add(resultsButton);
        
        // generate histograms button
        JButton histogramsButton = new JButton("Histograms");
        histogramsButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        histogramsButton.addActionListener(e -> {
        	this.leftPanel.updateAnalysisParametersInputValues(); // consider updated analysis input values, if not updated
          
        	// check if the table is null or empty
        	if (currentTable == null || currentTable.getCounter() == 0) {
        		IJ.showMessage("No data to plot");
        		return ;
        	}
        	
        	List<ImagePlus> plots = selectedSettings.generateHistograms(currentTable);
        	showHistograms(plots);
        });
        headerPanel.add(histogramsButton);
        
        // label for the number of isolated particles
        nbIsolatedLabel = new JLabel("Isolated: 0 | Total: 0");
        nbIsolatedLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        headerPanel.add(nbIsolatedLabel);
        
        // generate statistics button
        JButton statisticButton = new JButton("Statistics");
        statisticButton.addActionListener(e -> {
        	ImagePlus img = leftPanel.getCurrentImage();
        	// check if there is an image
        	if (leftPanel.getCurrentImage() == null) {
        		IJ.showMessage("Please open an image first (File > Open)");
        		return ;
        	}
        	
        	// --- Fetch the current img's binary mask ---
            String binaryTitle = img.getShortTitle() + "_Binary";
            ImagePlus binaryImg = ij.WindowManager.getImage(binaryTitle);
            
            if (binaryImg == null) {
                IJ.showMessage("Please complete the thresholding step first to generate a mask.");
                return;
            }
            // ----------------------------------------------
        	
        	this.leftPanel.updateAnalysisParametersInputValues(); // consider updated analysis input values, if not updated
          
        	SwingWorker<ResultsTable,Void> measuresWorker = selectedSettings.createMeasuresProcessingWorker(img, binaryImg);
        	ImagePlus currentImg = leftPanel.getCurrentImage();
        	
        	// adding property change listener to the worker to show the table when the asynchronous task is completed
        	measuresWorker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                        try {
							showTable(
									selectedSettings.calculateSummaryTable(
											measuresWorker.get(),
											currentImg.getWidth(),
											currentImg.getHeight()));
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
                    }
                }
            });
        	measuresWorker.execute();

        });
    	footerPanel.add(statisticButton);
    	
    	// export csv button
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> {
        	// check if the table is null or empty
        	if (currentTable == null || currentTable.getCounter() == 0) {
        		IJ.showMessage("No data to export.");
        		return;
        	}
        	
        	// setup file chooser window
        	JFileChooser fileChooser = new JFileChooser();
        	fileChooser.setDialogTitle("Save data as CSV");
        	fileChooser.setSelectedFile(new File("Results.csv"));
        	fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        	int userSelection = fileChooser.showSaveDialog(this);
        		
        	if (userSelection == JFileChooser.APPROVE_OPTION) {
        		// getting path
        		String path = fileChooser.getSelectedFile().getAbsolutePath();
        		
        		selectedSettings.exportResultsTable(currentTable, path);
            }
        });
        footerPanel.add(exportButton);
        
        // Batch mode button, creating batch window
        JButton batchButton = new JButton("Batch mode");
        batchButton.addActionListener(e -> {
           	if (leftPanel.getWorkflowIndex() < 2) {
        		IJ.showMessage("Please complete thresholding before batch mode.");
        	} else {
        		new BatchWindow(ctx, leftPanel.getMainGUI());
        	}
        });
        footerPanel.add(batchButton);
    	
    	add(headerPanel, BorderLayout.NORTH);
    	add(footerPanel, BorderLayout.SOUTH);
    	
    	// table panel initialization
    	viewPanel = new JPanel();
    	viewPanel.setLayout(new BorderLayout());
         
    	// initial data display is empty
    	showTable(null);
    }
    
    
    /**
     * Write and add show the data of a ResultsTable in the center panel of the right panel.
     * @param rt ResutsTable
     */
    private void showTable(ResultsTable rt) {
    	viewPanel.removeAll(); // reseting panel
    	
    	// check if the table is null or empty
    	if (rt == null || rt.getCounter() == 0) {
    		
	    	// there is no data yet
	    	JLabel l = new JLabel("No data yet");
	    	l.setHorizontalAlignment(JLabel.CENTER);
	    	viewPanel.add(l, BorderLayout.CENTER);
	    	add(viewPanel , BorderLayout.CENTER);
	    	viewPanel.revalidate();
	        viewPanel.repaint();
	            
	        // if it is because there is no output for the given parameters
    		if (rt != null && rt.getCounter() == 0) {
	    		IJ.showMessage("No output for the given parameters");
    		}
    		
    		return;
    	}

    	// creation of JTable with data from the ResultsTable
    	
    	this.currentTable = rt; // update current table 

        viewPanel.removeAll();
 
    	String[] originalHeadings = rt.getHeadings();
    	
    	// adding new columns at the beginning for the id
        String[] columns = new String[originalHeadings.length + 1];
        columns[0] = " ";
        System.arraycopy(originalHeadings, 0, columns, 1, originalHeadings.length);

        int rowCount = rt.getCounter();
        Object[][] data = new Object[rowCount][columns.length];
        
        nb_particle = rt.getCounter();
        nb_isolated = 0;
        
    	for (int i = 0; i < rowCount; i++) {
    		data[i][0] = i + 1;
    		for (int j = 0; j < originalHeadings.length; j++) {
    			String colName = originalHeadings[j];
    			if (rt.columnExists(colName)) {
    				if ((colName.equals("Label") || colName.equals("Slice"))) {
    					data[i][j+1] = rt.getStringValue(colName, i);
    				}else {
    					data[i][j+1] = rt.getValue(colName, i);    					
    				}
                    if (colName.equals("is_isolated")) {
                    	nb_isolated += rt.getValue(colName, i); // is_isolated column is 1 or 0
                    }
                } else {
                    data[i][j+1] = "-";
                }
    		}
    	}
    	
    	if (rt.columnExists("is_isolated")){
        	nbIsolatedLabel.setText("Isolated: " + nb_isolated + " | Total: " + nb_particle);
    	}else {
    		nbIsolatedLabel.setText("");
    	}
    	
    	DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    	
    	JTable measuresTable = new JTable(model);
    	measuresTable.setAutoCreateRowSorter(true);

    	JScrollPane scrollTable = new JScrollPane(measuresTable);
    	viewPanel.add(scrollTable, BorderLayout.CENTER);
    	
    	viewPanel.revalidate();
        viewPanel.repaint();
    }
    
    
    /**
     * Show histograms in the viewPanel.
     * @param plots List of ImagePlus histograms. 
     */
    private void showHistograms(List<ImagePlus> plots) {
    	viewPanel.removeAll();
    	
    	// check that plots isn't null or empty 
    	if (plots == null || plots.isEmpty()) {
    		JLabel l = new JLabel("No histograms generated.");
    		l.setHorizontalAlignment(JLabel.CENTER);
    		viewPanel.add(l, BorderLayout.CENTER);
    	} else {
    		
    		JPanel gridPanel = new JPanel(new GridLayout(0, 1, 5, 5)); 
    		
    		for (ImagePlus imp : plots) {
    			// conversion of ImagePlus to ImageIcon
    			ImageIcon icon = new ImageIcon(imp.getImage());
    			JLabel label = new JLabel(icon);
    			
    			label.setBorder(BorderFactory.createTitledBorder(imp.getTitle()));
    			label.setHorizontalAlignment(JLabel.CENTER);
    			
    			// add double click listener to open histogram into a new window
    			label.addMouseListener(new MouseAdapter() {
    		        @Override
    		        public void mouseClicked(MouseEvent e) {
    		            if (e.getClickCount() == 2) {
    		            	ImagePlus popup = imp.duplicate();
    		                popup.setTitle(imp.getTitle());
    		                popup.show();
    		            }
    		        }
    		    });
    			
    			gridPanel.add(label);
    		}
    		
    		JScrollPane scrollPane = new JScrollPane(gridPanel);
    		scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
    		
    		viewPanel.add(scrollPane, BorderLayout.CENTER);
    	}

        viewPanel.revalidate();
        viewPanel.repaint();
    }
}