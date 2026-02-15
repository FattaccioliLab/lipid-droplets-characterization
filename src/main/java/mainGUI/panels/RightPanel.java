package mainGUI.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
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

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;

import org.scijava.event.EventService;

import io.scif.services.DatasetIOService;
import model.LDCService;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;

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
    
    public RightPanel(Context ctx, LeftPanel leftPanel) {
    	super();
      ctx.inject(this);
      this.leftPanel = leftPanel;
    	
    	setLayout(new BorderLayout());
    	
    	JPanel headerPanel = new JPanel();
        JPanel footerPanel = new JPanel();
    	
        // show measures button
        JButton resultsButton = new JButton("show results");
        resultsButton.addActionListener(e -> {
          leftPanel.getParticleAnalysisParamsPanel().updateInputValues(); // consider updated analysis input values, if not updated
          
        	ResultsTable rt = ResultsTable.getResultsTable();
        	rt.reset();
        	SwingWorker<Void,Void> measuresWorker = selectedSettings.createMeasuresProcessingWorker();
        	
        	// adding property change listener to the worker to show the table when the asynchronous task is completed
        	measuresWorker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("state".equals(evt.getPropertyName()) && SwingWorker.StateValue.DONE == evt.getNewValue()) {
                        showTable(ResultsTable.getResultsTable());
                    }
                }
            });
        	
        	measuresWorker.execute();
        });
        headerPanel.add(resultsButton);
        
        // generate histograms button
        JButton histogramsButton = new JButton("histograms");
        histogramsButton.addActionListener(e -> {
          leftPanel.getParticleAnalysisParamsPanel().updateInputValues(); // consider updated analysis input values, if not updated
          
        	// check if the table is null or empty
        	if (currentTable == null || currentTable.getCounter() == 0) {
        		IJ.showMessage("No data to plot");
        		return ;
        	}
        	
        	List<ImagePlus> plots = selectedSettings.generateHistograms(currentTable);
        	showHistograms(plots);
        });
        headerPanel.add(histogramsButton);
        
        // generate statistics button
        JButton statisticButton = new JButton("Statistics");
        statisticButton.addActionListener(e -> {
        	showTable(selectedSettings.calculateSummaryTable(currentTable));
        });
    	footerPanel.add(statisticButton);
    	
    	// export csv button
        JButton exportButton = new JButton("export");
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
    		JLabel l = new JLabel("no data yet");
    		l.setHorizontalAlignment(JLabel.CENTER);
    		viewPanel.add(l, BorderLayout.CENTER);
    		add(viewPanel , BorderLayout.CENTER);
    		viewPanel.revalidate();
            viewPanel.repaint();
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
        
    	for (int i = 0; i < rowCount; i++) {
    		data[i][0] = i + 1;
    		for (int j = 0; j < originalHeadings.length; j++) {
    			String colName = originalHeadings[j];
    			if (rt.columnExists(colName)) {
                    data[i][j+1] = rt.getValue(colName, i);
                } else {
                    data[i][j+1] = "-";
                }
    		}
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