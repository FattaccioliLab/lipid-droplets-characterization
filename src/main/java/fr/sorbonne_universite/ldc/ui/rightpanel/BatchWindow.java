package fr.sorbonne_universite.ldc.ui.rightpanel;

import javax.swing.*;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fr.sorbonne_universite.ldc.model.LDCService;
import fr.sorbonne_universite.ldc.ui.MainGUI_LDC;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.concurrent.CancellationException;

/**
 * A batch mode window, offering to the user the possibility of processing a whole given directory with the current analysis settings.
 */
@SuppressWarnings("serial")
public class BatchWindow extends JFrame {

	@Parameter
	private LDCService selectedSettings;

	private JTextField inputDirectoryField;
	private JTextField outputFileField;
	private JProgressBar progressBar;
	private JButton launchButton;
	private JButton cancelButton;
	private JLabel loadingLabel;

	private File selectedDirectory;
	private File selectedOutputFile;
	
	// Reference to the current BatchWorker (null if no current worker)
	private SwingWorker<Void, Void> currentBatchWorker = null;

	/**
	 * Creates a Batch mode window. If the {@code parent} Main LDC window is closed, this window is also closed.
	 * @param ctx		The LDC plugin context.
	 * @param parent	The LDC window.
	 */
	public BatchWindow(Context ctx, MainGUI_LDC parent) {
		super("Batch Processing");
		ctx.inject(this);

		// If main LDC window is closed, batch is cancelled and window is closed
		parent.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancelProcessing();
				dispose();
			}
		});

		initializeComponents();
		layoutComponents();

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(600, 400);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Initializes UI components.
	 */
	private void initializeComponents() {
		// Input directory selection
		inputDirectoryField = new JTextField();
		inputDirectoryField.setEditable(false);
		JButton browseInputButton = new JButton("Browse...");
		browseInputButton.addActionListener(e -> selectInputDirectory());

		// Output file selection
		outputFileField = new JTextField();
		outputFileField.setEditable(false);
		JButton browseOutputButton = new JButton("Browse...");
		browseOutputButton.addActionListener(e -> selectOutputFile());

		// Launch and Cancel buttons
		launchButton = new JButton("Launch Batch");
		launchButton.addActionListener(e -> launchBatchProcessing());

		cancelButton = new JButton("Cancel");
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(e -> cancelProcessing());

		// Progress bar
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setValue(0);

		// Loading gif
		URL gifUrl = getClass().getResource("/loading.gif");
		loadingLabel = new JLabel("<html><img src='" + gifUrl + "' width='20' height='20'></html>");
		loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		loadingLabel.setVisible(false); // Hidden at start
	}

	/**
	 * Creates UI components layout.
	 */
	private void layoutComponents() {
		setLayout(new BorderLayout(10, 10));

		JTextArea instructionText = new JTextArea("Batch mode uses all parameters entered during the whole workflow, "
				+ "applies them to '.tif' and '.tiff' files in the selected source folder, "
				+ "and produces a single global export containing all results for the files "
				+ "into an output '.csv' file.");
		instructionText.setEditable(false);
		instructionText.setWrapStyleWord(true);
		instructionText.setLineWrap(true);
		instructionText.setOpaque(false);
		instructionText.setFont(UIManager.getFont("Label.font"));
		instructionText.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
		add(instructionText, BorderLayout.NORTH);

		// Main panel with form fields
		JPanel formPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Input directory row
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		formPanel.add(new JLabel("Input Directory:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		formPanel.add(inputDirectoryField, gbc);

		gbc.gridx = 2;
		gbc.weightx = 0;
		JButton browseInputButton = new JButton("Browse...");
		browseInputButton.addActionListener(e -> selectInputDirectory());
		formPanel.add(browseInputButton, gbc);

		// Output file row
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		formPanel.add(new JLabel("Output File:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		formPanel.add(outputFileField, gbc);

		gbc.gridx = 2;
		gbc.weightx = 0;
		JButton browseOutputButton = new JButton("Browse...");
		browseOutputButton.addActionListener(e -> selectOutputFile());
		formPanel.add(browseOutputButton, gbc);

		// Progress bar row
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		formPanel.add(new JLabel("Progress:"), gbc);

		gbc.gridx = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		formPanel.add(progressBar, gbc);

		// Loading gif + Buttons panel
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.add(loadingLabel);
		bottomPanel.add(launchButton);
		bottomPanel.add(cancelButton);

		// Add panels to frame
		add(formPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		// Add padding around the main content
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}

	/**
	 * Opens a dialog window to select the input directory.
	 */
	private void selectInputDirectory() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle("Select Input Directory");

		if (selectedDirectory != null) {
			fileChooser.setCurrentDirectory(selectedDirectory.getParentFile());
		}

		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedDirectory = fileChooser.getSelectedFile();
			inputDirectoryField.setText(selectedDirectory.getAbsolutePath());
		}
	}

	/**
	 * Opens a dialog window to select the output file.
	 */
	private void selectOutputFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("Select Output File");

		// Only CSV file
		fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
			}

			@Override
			public String getDescription() {
				return "CSV Files (*.csv)";
			}
		});

		if (selectedOutputFile != null) {
			fileChooser.setCurrentDirectory(selectedOutputFile.getParentFile());
			fileChooser.setSelectedFile(selectedOutputFile);
		}

		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			selectedOutputFile = fileChooser.getSelectedFile();

			// Automatically appends .csv if not present
			if (!selectedOutputFile.getName().toLowerCase().endsWith(".csv")) {
				selectedOutputFile = new File(selectedOutputFile.getAbsolutePath() + ".csv");
			}
			outputFileField.setText(selectedOutputFile.getAbsolutePath());
		}
	}

	/**
	 * Action on pressing the "Launch Batch" button, creates a {@link BatchWorker}, whose work is to process the whole input directory
	 * (by creating sub-workers).
	 */
	private void launchBatchProcessing() {
		if (selectedDirectory == null) {
			JOptionPane.showMessageDialog(this, "Please select an input directory first.", "Missing Input",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (selectedOutputFile == null) {
			JOptionPane.showMessageDialog(this, "Please select an output file first.", "Missing Output",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Disable launch button and enable cancel button
		launchButton.setEnabled(false);
		cancelButton.setEnabled(true);
		progressBar.setValue(0);
		loadingLabel.setVisible(true);

		// Create and execute the BatchWorker
		currentBatchWorker = selectedSettings.createBatchWorker(selectedDirectory, selectedOutputFile, this);
		currentBatchWorker.addPropertyChangeListener(evt -> {
        	
        	// When the worker is done
        	// i.e. when the worker "state" property becomes DONE
            if ("state".equals(evt.getPropertyName())
                    && SwingWorker.StateValue.DONE == evt.getNewValue()) {
            	
	            try {
	            	currentBatchWorker.get(); // Check for exceptions
					
					// Success
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(
							BatchWindow.this,
							"Batch processing completed successfully!\nResults saved to: " + selectedOutputFile.getName(),
							"Success",
							JOptionPane.INFORMATION_MESSAGE
						);
					});
	                
	            } catch (CancellationException ce) { 
					// Cancelled by user
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(
							BatchWindow.this,
							"Batch processing was cancelled.",
							"Cancelled",
							JOptionPane.WARNING_MESSAGE
						);
					});
	            } catch (Exception e) {
					// Error occurred
					e.printStackTrace();
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(
							BatchWindow.this,
							"Error during batch processing:\n" + e.getMessage(),
							"Error",
							JOptionPane.ERROR_MESSAGE
						);
					});
	            } finally {
	            	// Reset UI in all cases
					SwingUtilities.invokeLater(() -> {
						launchButton.setEnabled(true);
						cancelButton.setEnabled(false);
						loadingLabel.setVisible(false);
						currentBatchWorker = null;
					});
	            }
            }
        });
		currentBatchWorker.execute();
	}

	/**
	 * Action on pressing the "Cancel" button, shuts down the current {@link BatchWorker}.
	 */
	private void cancelProcessing() {
		// Cancel the worker
		if (currentBatchWorker != null && !currentBatchWorker.isDone()) {
			currentBatchWorker.cancel(true);
		}

		// Reset UI
		launchButton.setEnabled(true);
		cancelButton.setEnabled(false);
		progressBar.setValue(0);
		loadingLabel.setVisible(false);
	}

	/**
	 * Updates the progress bar value.
	 * 
	 * @param value Progress value between 0 and 100.
	 */
	public void updateProgress(int value) {
		progressBar.setValue(value);
	}
}