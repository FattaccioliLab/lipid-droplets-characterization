package mainGUI.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import ij.measure.ResultsTable;

import org.scijava.event.EventService;

import io.scif.services.DatasetIOService;
import model.LDCService;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;

@SuppressWarnings("serial")
public class RightPanel extends JPanel {

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
	
    // container panel for the data table
    private JPanel viewPanel;
    
    public RightPanel(Context ctx) {
    	super();
    	
    	setLayout(new BorderLayout());
    	
    	ctx.inject(this);
    	
    	JPanel controlBar = new JPanel();
    	
        // show measures button
        JButton resultsButton = new JButton("show results");
        resultsButton.addActionListener(e -> {
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
        controlBar.add(resultsButton);
        
        // generate histograms button
        JButton histogramsButton = new JButton("histograms");
        histogramsButton.addActionListener(e -> {
        	// TODO
        });
        controlBar.add(histogramsButton);
        
        // generate statistics button
        JButton statisticButton = new JButton("Statistics");
        statisticButton.addActionListener(e -> {
        	// TODO
        });
    	controlBar.add(statisticButton);
    	
    	// export csv button
        JButton exportButton = new JButton("export");
        exportButton.addActionListener(e -> {
        	// TODO
        });
    	controlBar.add(exportButton);
    	
    	add(controlBar, BorderLayout.NORTH);
    	
    	// table panel initialization
    	viewPanel = new JPanel();
    	viewPanel.setLayout(new BorderLayout());
         
    	// initial data display is empty
    	showTable(null);
    }
    
    
    public void showTable(ResultsTable rt) {
    	viewPanel.removeAll(); // reseting panel
    	
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
}