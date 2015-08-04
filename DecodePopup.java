import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// By: Justin Spedding & Andrew Miller

public class DecodePopup implements ActionListener, PropertyChangeListener, WindowListener {

	private JFrame window; // Main window
	private ImagePanel hostImage; // Image panel
	private JButton updateButton, decodeButton, cancelButton; // Buttons
	private JTextField passwordText, hostImageText, outputDirText; // Text boxes
	private JButton hostImageBrowse, outputDirBrowse; // File path browse buttons
	private String password, hostImagePath, outputDirPath; // Remember info while decoding
	private JCheckBox passwordCheckBox; // Enable or disable password
	private JLabel statusText; // Status label
	private JProgressBar progressBar; // Progress bar

	private DecodeWorker decodeWorker; // Worker thread
	private boolean usePassword; // Should the password be used?
	private boolean workerBusy; // Is the worker busy?

	public DecodePopup() {
		buildWindow();
		initialize();
	}

	/**
	 * Create a popup window for decoding
	 */
	private void buildWindow() {
		// Create window
		window = new JFrame("Decode");
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
		JPanel passwordSelectorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel hostImageSelectorPanel = new JPanel(new BorderLayout());
		JPanel outputDirSelectorPanel = new JPanel(new BorderLayout());
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
		statusText = new JLabel("Press 'Update' if you manually edited any of the file paths.");
		statusPanel.add(statusText);
		controlPanel.add(statusPanel);

		// Build the button panel
		updateButton = new JButton("Update");
		updateButton.addActionListener(this);
		buttonPanel.add(updateButton);
		decodeButton = new JButton("Decode");
		decodeButton.addActionListener(this);
		buttonPanel.add(decodeButton);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		controlPanel.add(buttonPanel);

		// Build the password selector panel
		passwordCheckBox = new JCheckBox("Use Password", false);
		passwordCheckBox.addActionListener(this);
		passwordSelectorPanel.add(passwordCheckBox);
		passwordText = new JTextField("", 15);
		passwordText.setEnabled(false);
		passwordSelectorPanel.add(passwordText);
		controlPanel.add(passwordSelectorPanel);

		// Build the host image selector panel
		hostImageSelectorPanel.add(new JLabel("Host Image: "), BorderLayout.WEST);
		hostImageText = new JTextField();
		hostImageSelectorPanel.add(hostImageText, BorderLayout.CENTER);
		hostImageBrowse = new JButton("Browse");
		hostImageBrowse.addActionListener(this);
		hostImageSelectorPanel.add(hostImageBrowse, BorderLayout.EAST);
		controlPanel.add(hostImageSelectorPanel);

		// Build the output directory selector panel
		outputDirSelectorPanel.add(new JLabel("Output Directory: "), BorderLayout.WEST);
		outputDirText = new JTextField();
		outputDirSelectorPanel.add(outputDirText, BorderLayout.CENTER);
		outputDirBrowse = new JButton("Browse");
		outputDirBrowse.addActionListener(this);
		outputDirSelectorPanel.add(outputDirBrowse, BorderLayout.EAST);
		controlPanel.add(outputDirSelectorPanel);

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
		outputDirPath = "";
		password = "";
		usePassword = false;
		workerBusy = false;
	}

	private void updateStatus() {
		outputDirPath = outputDirText.getText(); // Update the output directory path
		if (!hostImageText.getText().equals(hostImagePath) || !passwordText.getText().equals(password)) {
			hostImagePath = hostImageText.getText(); // Update the host image path
			try {
				hostImage.loadImage(ImageIO.read(new File(hostImagePath)));
			} catch (IOException e) {
				hostImage.loadText("Invalid host image path.");
			}
		}
		usePassword = passwordCheckBox.isSelected(); // Update the password usage
		if (usePassword) {
			password = passwordText.getText(); // Update the password if it is being used
		} else {
			password = ""; // Clear the password if it is not being used
		}
		if (hostImage.isImage()) {
			try {
				statusText.setText(Steganographer.getInfo(hostImage.getImage(), password)); // Load the info about hidden messages inside the image
			} catch (ImageOverflowException e) {
				statusText.setText("Internal error: Image overflow.");
			}
		} else {
			statusText.setText("Invalid host image path.");
		}
	}

	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		if (obj.equals(updateButton)) {
			if (!workerBusy) {
				updateStatus();
			} else {
				printBusyMessage();
			}
		} else if (obj.equals(decodeButton)) {
			if (!workerBusy) {
				updateStatus();
				if (Steganographer.checkOutputDirPath(outputDirPath)) {
					try {
						decodeWorker = new DecodeWorker(hostImagePath, outputDirPath, password); // Create a worker thread for decoding
						decodeWorker.addPropertyChangeListener(this);
						Steganographer.addWorker();
						workerBusy = true;
						decodeWorker.execute();
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Invalid host image path.");
					} catch (ImageOverflowException e) {
						JOptionPane.showMessageDialog(null, "Internal error: Image overflow");
					} catch (CannotDecodeException e) {
						JOptionPane.showMessageDialog(null, "The host image does not contain a valid stego.");
					}
				} else {
					JOptionPane.showMessageDialog(null, "Invalid output directory path.");
				}
			} else {
				printBusyMessage();
			}
		} else if (obj.equals(cancelButton)) {
			exit();
		} else if (obj.equals(passwordCheckBox)) {
			if (!workerBusy) {
				passwordText.setEnabled(passwordCheckBox.isSelected());
				updateStatus();
			} else {
				passwordCheckBox.setSelected(usePassword);
				printBusyMessage();
			}
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
		} else if (obj.equals(outputDirBrowse)) {
			if (!workerBusy) {
				String temp = FileBrowsers.browseOutputDir();
				if (temp != null) {
					outputDirText.setText(temp);
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
			progressBar.setString("Decoding: " + progress + "%");
			if (progress == 100 && !decodeWorker.isCancelled()) {
				Steganographer.removeWorker();
				workerBusy = false;
				progressBar.setString("Done!");
				JOptionPane.showMessageDialog(null, "Done!");
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
		if (!workerBusy || JOptionPane.showConfirmDialog(null, "A file is currently being decoded.\nAre you sure you want to close?", "Close?",  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			if(decodeWorker != null && !decodeWorker.isDone()) {
				decodeWorker.cancel(true);
				Steganographer.removeWorker();
			}
			window.setVisible(false);
			window.dispose();
		}
	}
}
