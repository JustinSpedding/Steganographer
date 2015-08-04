import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// By: Justin Spedding & Andrew Miller

public class MainGUI implements WindowListener, ActionListener {

	private JFrame window; // Main window
	private JButton encodeButton, decodeButton, analyzeButton; // Buttons
	private JMenuItem encodeMenuItem, decodeMenuItem, analyzeMenuItem, exitMenuItem; // File menu items
	private JMenuItem markerMenuItem; // Settings menu items
	private JMenuItem helpMenuItem; // Help menu items
	private String helpText; // The text from the help file

	/**
	 * Creates the main launcher GUI
	 */
	public MainGUI() {
		buildWindow();
		initialize();
	}

	private void buildWindow() {
		// Create window
		window = new JFrame("Steganographer " + Steganographer.version);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(this);
		window.setSize(200,200);
		window.setMinimumSize(new Dimension(200, 200));
		window.setLocationRelativeTo(null);

		// Create menu bar
		JMenuBar menuBar = new JMenuBar();

		// Create file menu
		JMenu fileMenu = new JMenu("File");
		encodeMenuItem = new JMenuItem("Encode...");
		encodeMenuItem.addActionListener(this);
		fileMenu.add(encodeMenuItem);
		decodeMenuItem = new JMenuItem("Decode...");
		decodeMenuItem.addActionListener(this);
		fileMenu.add(decodeMenuItem);
		analyzeMenuItem = new JMenuItem("Analyze...");
		analyzeMenuItem.addActionListener(this);
		fileMenu.add(analyzeMenuItem);
		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(this);
		fileMenu.add(exitMenuItem);

		// Create settings menu
		JMenu settingsMenu = new JMenu("Settings");
		markerMenuItem = new JMenuItem("Set marker...");
		markerMenuItem.addActionListener(this);
		settingsMenu.add(markerMenuItem);

	    // Create help menu
	    JMenu helpMenu = new JMenu("Help");
	    helpMenuItem = new JMenuItem("View help");
		helpMenuItem.addActionListener(this);
		helpMenu.add(helpMenuItem);

		// Complete menu bar
		menuBar.add(fileMenu);
		menuBar.add(settingsMenu);
		menuBar.add(helpMenu);

		// Create Panels
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel buttonPanel = new JPanel(new GridLayout(3, 1));

		// Build title panel
		titlePanel.add(new JLabel("Steganographer " + Steganographer.version));

		// Build button panel
		JPanel encodePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		encodeButton = new JButton("Encode");
		encodeButton.addActionListener(this);
		encodePanel.add(encodeButton);
		buttonPanel.add(encodePanel);
		JPanel decodePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		decodeButton = new JButton("Decode");
		decodeButton.addActionListener(this);
		decodePanel.add(decodeButton);
		buttonPanel.add(decodePanel);
		JPanel analyzePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		analyzeButton = new JButton("Analyze");
		analyzeButton.addActionListener(this);
		analyzePanel.add(analyzeButton);
		buttonPanel.add(analyzePanel);

		// Finish
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			
		}
		window.setJMenuBar(menuBar);
		mainPanel.add(titlePanel, BorderLayout.NORTH);
		mainPanel.add(buttonPanel, BorderLayout.CENTER);
		window.add(mainPanel);
		window.setVisible(true);
	}

	private void initialize() {
		// Load help file
		try {
			InputStream in = getClass().getResourceAsStream(File.separator + "help.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		    StringBuilder out = new StringBuilder();
		    String newLine = System.getProperty("line.separator");
		    String line;
		    while ((line = reader.readLine()) != null) {
		        out.append(line);
		        out.append(newLine);
		    }
			helpText = out.toString();
		} catch (IOException e) {
			helpText = "Help documentation not found.";
		}
	}

	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		if (obj.equals(encodeButton) || obj.equals(encodeMenuItem)) {
			new EncodePopup();
		} else if (obj.equals(decodeButton) || obj.equals(decodeMenuItem)) {
			new DecodePopup();
		} else if (obj.equals(analyzeButton) || obj.equals(analyzeMenuItem)) {
			new AnalyzePopup();
		} else if (obj.equals(exitMenuItem)) {
			exit();
		} else if (obj.equals(markerMenuItem)) {
			String marker = JOptionPane.showInputDialog(null, "Enter a new marker:\n(Current marker = \"" + Steganographer.getMarker() + "\")");
			if (marker != null) {
				Steganographer.setMarker(marker);
			}
		} else if (obj.equals(helpMenuItem)) {
			JOptionPane.showMessageDialog(null, helpText);
		}
	}

	public void windowActivated(WindowEvent arg0) {

	}

	public void windowClosed(WindowEvent arg0) {

	}

	public void windowClosing(WindowEvent arg0) {
		exit();
	}

	public void windowDeactivated(WindowEvent arg0) {

	}

	public void windowDeiconified(WindowEvent arg0) {

	}

	public void windowIconified(WindowEvent arg0) {

	}

	public void windowOpened(WindowEvent arg0) {

	}

	public void exit() {
		if (Steganographer.getWorkerCount() == 0 || JOptionPane.showConfirmDialog(null, "One or more encode/decode operations are currently running.\nBy closing this window, all of these operations will be stopped.\nAre you sure you want to exit?", "Close?",  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			window.setVisible(false);
			System.exit(0);
		}
	}
}
