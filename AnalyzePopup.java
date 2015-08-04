import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;

// By: Justin Spedding & Andrew Miller

public class AnalyzePopup implements ActionListener, PropertyChangeListener, WindowListener {

	private JFrame window; // Main window
	private ImagePanel hostImage; // Image panel
	private JButton analyzeButton, cancelButton; // Buttons
	private JTextField hostImageText; // Text boxes
	private JButton hostImageBrowse; // File path browse buttons
	private String hostImagePath; // Remember info while decoding
	private JLabel statusText; // Status label
	private JProgressBar progressBar; // Progress bar

	private AnalyzeWorker analyzeWorker; // Worker thread
	private boolean workerBusy; // Is the worker busy?

	public AnalyzePopup() {
		buildWindow();
		initialize();
	}

	/**
	 * Create a popup window for analysis
	 */
	private void buildWindow() {
		// Create window
		window = new JFrame("Analyze");
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(this);
		window.setSize(800,600);
		window.setMinimumSize(new Dimension(500, 400));
		window.setLocationRelativeTo(null);

		// Create panels
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel imagePanel = new JPanel(new GridLayout(1, 1));
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel hostImageSelectorPanel = new JPanel(new BorderLayout());
		JPanel progressBarPanel = new JPanel(new GridLayout(1, 1));

		// Build host image panel
		JPanel hostImagePanel = new JPanel(new BorderLayout());
		JLabel hostImageLabel = new JLabel("Host Image");
		hostImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		hostImagePanel.add(hostImageLabel, BorderLayout.NORTH);
		hostImage = new ImagePanel();
		hostImage.loadText("No host image loaded");
		hostImagePanel.add(hostImage, BorderLayout.CENTER);
		imagePanel.add(hostImagePanel);

		// Build the status panel
		statusText = new JLabel("Press 'Analyze' to analyze the host image.");
		statusPanel.add(statusText);
		controlPanel.add(statusPanel);

		// Build the button panel
		analyzeButton = new JButton("Analyze");
		analyzeButton.addActionListener(this);
		buttonPanel.add(analyzeButton);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		controlPanel.add(buttonPanel);

		// Build the host image selector panel
		hostImageSelectorPanel.add(new JLabel("Host Image: "), BorderLayout.WEST);
		hostImageText = new JTextField();
		hostImageSelectorPanel.add(hostImageText, BorderLayout.CENTER);
		hostImageBrowse = new JButton("Browse");
		hostImageBrowse.addActionListener(this);
		hostImageSelectorPanel.add(hostImageBrowse, BorderLayout.EAST);
		controlPanel.add(hostImageSelectorPanel);

		// Build the progress bar panel
		progressBar = new JProgressBar();
		progressBar.setString("");
		progressBar.setStringPainted(true);
		progressBarPanel.add(progressBar);
		controlPanel.add(progressBarPanel);

		// Finish
		mainPanel.add(imagePanel, BorderLayout.CENTER);
		mainPanel.add(controlPanel, BorderLayout.SOUTH);
		window.add(mainPanel);
		window.setVisible(true);
	}

	private void initialize() {
		hostImagePath = "";
		workerBusy = false;
	}

	private void updateStatus() {
		if (!hostImageText.getText().equals(hostImagePath)) {
			hostImagePath = hostImageText.getText(); // Update the host image path
			statusText.setText("Press 'Analyze' to analyze the host image.");
			try {
				hostImage.loadImage(ImageIO.read(new File(hostImagePath)));
			} catch (IOException e) {
				hostImage.loadText("Invalid host image path.");
			}
		}
	}

	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		if (obj.equals(analyzeButton)) {
			if (!workerBusy) {
				updateStatus();
				try {
					analyzeWorker = new AnalyzeWorker(hostImagePath); // Create a worker thread for analysis
					analyzeWorker.addPropertyChangeListener(this);
					workerBusy = true;
					statusText.setText("Analyzing...");
					analyzeWorker.execute();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Invalid host image path.");
				}
			} else {
				printBusyMessage();
			}
		} else if (obj.equals(cancelButton)) {
			exit();
		} else if (obj.equals(hostImageBrowse)) {
			if (!workerBusy) {
				String temp = FileBrowsers.browseHostImage();
				if (temp != null) {
					hostImageText.setText(temp);
					updateStatus();
				}
			} else {
				printBusyMessage();
			}
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		try {
			int progress = (Integer) event.getNewValue();
			progressBar.setValue(progress);
			progressBar.setString("Analyzing: " + progress + "%");
			if (progress == 100 && !analyzeWorker.isCancelled()) {
				workerBusy = false;
				progressBar.setString("Done!");
				try {
					if (analyzeWorker.get()) {
						statusText.setText("This image most likely DOES contain a stego.");
					} else {
						statusText.setText("This image most likely does NOT contain a stego.");
					}
				} catch (InterruptedException | ExecutionException | CancellationException e) {
					progressBar.setString("Operation Failed!");
					statusText.setText("Internal error: Something unusual went wrong...");
				}
			}
		} catch (ClassCastException e) {
			// Do nothing
		}
	}

	private void printBusyMessage() {
		JOptionPane.showMessageDialog(null, "Please wait for the current process to complete.");
	}

	public void windowActivated(WindowEvent e) {

	}

	public void windowClosed(WindowEvent e) {

	}

	public void windowClosing(WindowEvent e) {
		exit();
	}

	public void windowDeactivated(WindowEvent e) {

	}

	public void windowDeiconified(WindowEvent e) {

	}

	public void windowIconified(WindowEvent e) {

	}

	public void windowOpened(WindowEvent e) {

	}

	private void exit() {
		if(analyzeWorker != null && !analyzeWorker.isDone()) {
			analyzeWorker.cancel(true);
		}
		window.setVisible(false);
		window.dispose();
	}
}
