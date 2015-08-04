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

public class EncodePopup implements ActionListener, PropertyChangeListener, WindowListener {

	private JFrame window; // Main window
	private ImagePanel hostImage, previewImage; // Image panels
	private JButton generatePreviewButton, encodeButton, cancelButton; // Buttons
	private JTextField passwordText, hostImageText, stegoText, outputImageText; // Text boxes
	private JButton hostImageBrowse, stegoBrowse, outputImageBrowse; // File path browse buttons
	private String password, hostImagePath, stegoPath, outputImagePath; // Remember info while encoding
	private JCheckBox passwordCheckBox; // Enable or disable password
	private JProgressBar progressBar; // Progress bar

	private String lastPassword, lastHostImagePath, lastStegoPath; // Do not re-encode if the preview will not change
	private EncodeWorker encodeWorker; // Worker thread
	private boolean usePassword; // Should the password be used?
	private boolean workerBusy; // Is the worker busy?
	private boolean encodePending; // Was the encode button pushed?

	/**
	 * Create a popup window for encoding
	 */
	public EncodePopup() {
		buildWindow();
		initialize();
	}

	private void buildWindow() {
		// Create window
		window = new JFrame("Encode");
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(this);
        window.setSize(1000,600);
		window.setMinimumSize(new Dimension(600, 400));
		window.setLocationRelativeTo(null);

		// Create panels
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel imagePanel = new JPanel(new GridLayout(1, 2));
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel passwordSelectorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel hostImageSelectorPanel = new JPanel(new BorderLayout());
		JPanel stegoSelectorPanel = new JPanel(new BorderLayout());
		JPanel outputImageSelectorPanel = new JPanel(new BorderLayout());
		JPanel progressBarPanel = new JPanel(new GridLayout(1, 1));

		// Build the host image panel
		JPanel hostImagePanel = new JPanel(new BorderLayout());
		JLabel hostImageLabel = new JLabel("Original Host Image");
		hostImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		hostImagePanel.add(hostImageLabel, BorderLayout.NORTH);
		hostImage = new ImagePanel();
		hostImage.loadText("No host image loaded");
		hostImagePanel.add(hostImage, BorderLayout.CENTER);
		imagePanel.add(hostImagePanel);

		// Build the preview image panel
		JPanel previewImagePanel = new JPanel(new BorderLayout());
		JLabel previewImageLabel = new JLabel("Output Image Preview");
		previewImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		previewImagePanel.add(previewImageLabel, BorderLayout.NORTH);
		previewImage = new ImagePanel();
		previewImage.loadText("No host image loaded");
		previewImagePanel.add(previewImage);
		imagePanel.add(previewImagePanel);

		// Build the button panel
		generatePreviewButton = new JButton("Generate Preview");
		generatePreviewButton.addActionListener(this);
		buttonPanel.add(generatePreviewButton);
		encodeButton = new JButton("Encode");
		encodeButton.addActionListener(this);
		buttonPanel.add(encodeButton);
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

		// Build the stego selector panel
		stegoSelectorPanel.add(new JLabel("File to Hide: "), BorderLayout.WEST);
		stegoText = new JTextField();
		stegoSelectorPanel.add(stegoText, BorderLayout.CENTER);
		stegoBrowse = new JButton("Browse");
		stegoBrowse.addActionListener(this);
		stegoSelectorPanel.add(stegoBrowse, BorderLayout.EAST);
		controlPanel.add(stegoSelectorPanel);

		// Build the output image selector panel
		outputImageSelectorPanel.add(new JLabel("Output Image: "), BorderLayout.WEST);
		outputImageText = new JTextField();
		outputImageSelectorPanel.add(outputImageText, BorderLayout.CENTER);
		outputImageBrowse = new JButton("Browse");
		outputImageBrowse.addActionListener(this);
		outputImageSelectorPanel.add(outputImageBrowse, BorderLayout.EAST);
		controlPanel.add(outputImageSelectorPanel);

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
		stegoPath = "";
		outputImagePath = "";
		password = "";
		lastHostImagePath = "";
		lastStegoPath = "";
		lastPassword = "";
		usePassword = false;
		workerBusy = false;
		encodePending = false;
	}

	private void updateStatus() {
		outputImagePath = outputImageText.getText(); // Update the output image path
		if (!hostImageText.getText().equals(hostImagePath) || !stegoText.getText().equals(stegoPath) || !passwordText.getText().equals(password) || passwordCheckBox.isSelected() != usePassword) {
			if (!hostImageText.getText().equals(hostImagePath)) { // Update original image if the host image path changed
				hostImagePath = hostImageText.getText();
				try {
					hostImage.loadImage(ImageIO.read(new File(hostImagePath))); // Load the image
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
			stegoPath = stegoText.getText(); // Update the file path
			previewImage.loadText("Press \"Generate Preview\"..."); // Preview must be re-generated
		}
	}

	private void generatePreview() {
		if (!workerBusy) {
			try {
				updateStatus();
				if (!lastHostImagePath.equals(hostImagePath) || !lastStegoPath.equals(stegoPath) || !lastPassword.equals(password)) {
					lastHostImagePath = hostImagePath;
					lastStegoPath = stegoPath;
					lastPassword = password;
					previewImage.loadText("Generating preview...");
					encodeWorker = new EncodeWorker(hostImagePath, stegoPath, password); // Create a worker thread for encoding
					encodeWorker.addPropertyChangeListener(this);
					workerBusy = true; // The encode worker is busy
					encodeWorker.execute();
				}
			} catch (IOException e) {
				previewImage.loadText("Invalid host image or stego path.");
			} catch (ImageOverflowException e) {
				previewImage.loadText("Internal error: Image overflow");
			} catch (CannotEncodeException e) {
				previewImage.loadText("Host image not big enough.");
			}
		} else {
			printBusyMessage();
		}
	}

	private void writePreviewImage() {
		if (previewImage.isImage()) {
			try {
				ImageIO.write(previewImage.getImage(), "bmp" , new File(outputImagePath)); // Write the preview to a new image file
				JOptionPane.showMessageDialog(null, "Done!");
			} catch (Exception j) {
				progressBar.setString("Failed to save output image.");
				JOptionPane.showMessageDialog(null, "Cannot Encode: The output image file is invalid.");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Cannot encode: Invalid host image or file path.");
		}
	}

	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		if (obj.equals(generatePreviewButton)) {
			generatePreview();
		} else if (obj.equals(encodeButton)) {
			if (!workerBusy) {
				updateStatus();
				if (Steganographer.checkOutputImagePath(outputImagePath)) {
					if (!lastHostImagePath.equals(hostImagePath) || !lastStegoPath.equals(stegoPath)) {
						generatePreview(); // Generate the preview
						encodePending = true; // Remember to write the preview to the file when done
						Steganographer.addWorker();
					} else {
						writePreviewImage(); // Preview already generated, so just write it
					}
				} else {
					JOptionPane.showMessageDialog(null, "Invalid output image path.");
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
		} else if (obj.equals(passwordCheckBox)) {
			if (!workerBusy) {
				passwordText.setEnabled(passwordCheckBox.isSelected());
				updateStatus();
			} else {
				passwordCheckBox.setSelected(usePassword);
				printBusyMessage();
			}
		} else if (obj.equals(stegoBrowse)) {
			if (!workerBusy) {
				String temp = FileBrowsers.browseFile();
				if (temp != null) {
					stegoText.setText(temp);
					updateStatus();
				}
			} else {
				printBusyMessage();
			}
		} else if (obj.equals(outputImageBrowse)) {
			if (!workerBusy) {
				String temp = FileBrowsers.browseOutputImage();
				if (temp != null) {
					outputImageText.setText(temp);
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
			progressBar.setString("Encoding: " + progress + "%");
			if (progress == 100 && !encodeWorker.isCancelled()) {
				workerBusy = false;
				progressBar.setString("Done!");
				try {
					previewImage.loadImage(encodeWorker.get());
				} catch (ExecutionException | InterruptedException | CancellationException e) {
					progressBar.setString("Operation failed!");
					previewImage.loadText("Internal error: Something unusual went wrong...");
				}
				if (encodePending) {
					writePreviewImage();
					Steganographer.removeWorker();
					encodePending = false;
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
		if (!encodePending || JOptionPane.showConfirmDialog(null, "A file is currently being encoded.\nAre you sure you want to close?", "Close?",  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			if(encodeWorker != null && !encodeWorker.isDone()) {
				encodeWorker.cancel(true);
				Steganographer.removeWorker();
			}
			window.setVisible(false);
			window.dispose();
		}
	}
}
