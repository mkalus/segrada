package org.segrada;

import org.segrada.servlet.SegradaUpdateChecker;
import org.segrada.util.ApplicationStatusChangedListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties; 
import java.util.ResourceBundle;

/**
 * Copyright 2015 Maximilian Kalus [segrada@auxnet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Application launcher
 */
public class SegradaLauncher extends JFrame implements ApplicationStatusChangedListener {
	/**
	 * elements
	 */
	private final JLabel statusText;
	private final JButton startStopButton;
	private final JButton browserButton;

	// directory chooser
	private final JLabel directoryText;
	private final JButton changeDirectoryButton;

	/**
	 * resource bundle
	 */
	private ResourceBundle messages;

	/**
	 * Constructor
	 */
	public SegradaLauncher() {
		super("Segrada");

		initI18N();

		setSize(400, 200);
		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent windowEvent) {
			}

			@Override
			public void windowClosing(WindowEvent windowEvent) {
				// stop if running
				int status = SegradaApplication.getServerStatus();
				if (status == SegradaApplication.STATUS_OFF || status == SegradaApplication.STATUS_STOPPED) {
					System.exit(0);
				} else {
					new Thread(){
						public void run(){
							try {
								SegradaApplication.stopServer();
								System.exit(0);
							} catch (Exception e) {
								showError(e.getMessage());
								e.printStackTrace();
							}
						}
					}.start();
				}
			}

			@Override
			public void windowClosed(WindowEvent windowEvent) {
			}

			@Override
			public void windowIconified(WindowEvent windowEvent) {
			}

			@Override
			public void windowDeiconified(WindowEvent windowEvent) {
			}

			@Override
			public void windowActivated(WindowEvent windowEvent) {
			}

			@Override
			public void windowDeactivated(WindowEvent windowEvent) {
			}
		});
		// Get the size of the screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		// Determine the new location of the window
		int w = getSize().width;
		int h = getSize().height;
		int x = (dim.width-w)/2;
		int y = (dim.height-h)/2;

		// Move the window
		setLocation(x, y);

		// add change listener
		SegradaApplication.addApplicationStatusChangedListener(this);

		Font fatFont = new Font("sans-serif", Font.BOLD, 16);

		setTitle(messages.getString("segrada").concat(" ").concat(SegradaUpdateChecker.currentVersion));

		// create elements
		statusText = new JLabel(messages.getString("stopped"));
		statusText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		statusText.setFont(fatFont);

		startStopButton = new JButton(messages.getString("start"));
		startStopButton.setFont(fatFont);
		startStopButton.addActionListener(this::buttonClicked);

		browserButton = new JButton(messages.getString("openApplication"));
		browserButton.addActionListener(this::openBrowser);
		browserButton.setEnabled(false);
		browserButton.setFont(fatFont);

		// create directory chooser
		String directory = System.getProperty("savePath");
		if (directory == null || directory.isEmpty()) directory = messages.getString("defaultDirectoryLabel");
		directoryText = new JLabel(directory);
		directoryText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		changeDirectoryButton = new JButton(messages.getString("changeDirectory"));
		changeDirectoryButton.addActionListener(this::changeDirectory);

		JPanel directoryChooser = new JPanel(new GridLayout(1,2));
		directoryChooser.add(directoryText);
		directoryChooser.add(changeDirectoryButton);

		// bottom panel for bottom stuff
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(0,1));
		bottomPanel.add(directoryChooser);
		bottomPanel.add(browserButton);

		// add elements
		Container pane = getContentPane();
		pane.add(statusText, BorderLayout.NORTH);
		pane.add(startStopButton, BorderLayout.CENTER);
		pane.add(bottomPanel, BorderLayout.SOUTH);

		setVisible(true);
	}

	/**
	 * Initialize I18N
	 */
	private void initI18N() {
		Locale locale = Locale.getDefault();
		messages = ResourceBundle.getBundle("launcher/Messages", locale);
	}

	/**
	 * called on button clicked
	 * @param actionEvent triggered
	 */
	private void buttonClicked(ActionEvent actionEvent) {
		switch (SegradaApplication.getServerStatus()) {
			case SegradaApplication.STATUS_OFF:
				new Thread(){
					public void run(){
						try {
							SegradaApplication.startServer();
						} catch (Exception e) {
							showError(e.getMessage());
							e.printStackTrace();
						}
					}
				}.start();
				break;
			case SegradaApplication.STATUS_RUNNING:
				new Thread(){
					public void run(){
						try {
							SegradaApplication.stopServer();
						} catch (Exception e) {
							showError(e.getMessage());
							e.printStackTrace();
						}
					}
				}.start();
				break;
			case SegradaApplication.STATUS_STOPPED:
				restartApplication();
				break;
			default:
				break;
		}
	}

	public void restartApplication()
	{
		try {
			final File currentJar = new File(SegradaLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());

			/* is it a jar file? */
			if(!currentJar.getName().endsWith(".jar"))
				return;

			/* Build command: java -jar application.jar */
			final ArrayList<String> command = new ArrayList<String>();
			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());

			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
			System.exit(0);
		} catch (Exception e) {
			statusText.setText("Error: " + e.getMessage());
		}
	}

	/**
	 * open browser window
	 * @param actionEvent triggered
	 */
	private void openBrowser(ActionEvent actionEvent) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

		// context and port has been set in Segrada Application
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI("http://localhost:" + String.valueOf(SegradaApplication.getPort()) + SegradaApplication.getContextRoot()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * open browser window
	 * @param actionEvent triggered
	 */
	private void changeDirectory(ActionEvent actionEvent) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle(messages.getString("directoryChooserTitle"));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			// get chosen file
			String directory = chooser.getSelectedFile().toString();

			// set label
			directoryText.setText(directory);

			// set environment variables
			System.setProperty("savePath", directory);
			System.setProperty("orientDB.url", "plocal:" + directory + File.separator + "db");
		}
	}

	/**
	 * show error message
	 * @param error message
	 */
	private void showError(String error) {
		statusText.setText(error);
		statusText.setForeground(Color.RED);
	}

	/**
	 * starter for gui version
	 * @param args
	 */
	public static void main(String[] args) {
		// start headless instance?
		if (args.length > 0 && args[0].equalsIgnoreCase("headless"))
			try {
				SegradaApplication.main(args);
			} catch (Exception e) {

			}
		else // start launcher instance
			new SegradaLauncher();
	}

	@Override
	public void onApplicationStatusChanged(int newStatus, int oldStatus) {
		switch (newStatus) {
			case SegradaApplication.STATUS_STOPPED:
				statusText.setText(messages.getString("stopped"));
				startStopButton.setText(messages.getString("restart"));
				startStopButton.setEnabled(true);
				browserButton.setEnabled(false);
				changeDirectoryButton.setEnabled(false);
				break;
			case SegradaApplication.STATUS_STARTING:
				statusText.setText(messages.getString("starting"));
				startStopButton.setEnabled(false);
				browserButton.setEnabled(false);
				changeDirectoryButton.setEnabled(false);
				break;
			case SegradaApplication.STATUS_UPDATING_DATABASE:
				statusText.setText(messages.getString("updatingDb"));
				startStopButton.setEnabled(false);
				browserButton.setEnabled(false);
				changeDirectoryButton.setEnabled(false);
				break;
			case SegradaApplication.STATUS_RUNNING:
				statusText.setText(messages.getString("running"));
				startStopButton.setText(messages.getString("stop"));
				startStopButton.setEnabled(true);
				browserButton.setEnabled(true);
				changeDirectoryButton.setEnabled(false);
				break;
			case SegradaApplication.STATUS_STOPPING:
				statusText.setText(messages.getString("stopping"));
				startStopButton.setEnabled(false);
				browserButton.setEnabled(false);
				changeDirectoryButton.setEnabled(false);
				break;
			default:
				break;
		}

		statusText.setForeground(Color.BLACK);
		validate();
		repaint();
	}
}
