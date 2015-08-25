package org.segrada;

import org.segrada.util.ApplicationStatusChangedListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;
import java.util.Locale;
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

		setSize(400, 150);
		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent windowEvent) {
			}

			@Override
			public void windowClosing(WindowEvent windowEvent) {
				// stop if running
				if (SegradaApplication.getServerStatus() != SegradaApplication.STATUS_OFF)
					new Thread(){
						public void run(){
							try {
								SegradaApplication.stopServer();
								System.exit(0);
							} catch (Exception e) {
								showError(e.getMessage());
							}
						}
					}.start();
				else System.exit(0);
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

		setTitle(messages.getString("segrada"));

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

		// add elements
		Container pane = getContentPane();
		pane.add(statusText, BorderLayout.NORTH);
		pane.add(startStopButton, BorderLayout.CENTER);
		pane.add(browserButton, BorderLayout.SOUTH);

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
						}
					}
				}.start();
				break;
		}
	}

	/**
	 * open browser window
	 * @param actionEvent triggered
	 */
	private void openBrowser(ActionEvent actionEvent) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI("http://localhost:8080/"));
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		// start instance
		new SegradaLauncher();
	}

	@Override
	public void onApplicationStatusChanged(int newStatus, int oldStatus) {
		switch (newStatus) {
			case SegradaApplication.STATUS_OFF:
				statusText.setText(messages.getString("stopped"));
				startStopButton.setText(messages.getString("start"));
				startStopButton.setEnabled(true);
				browserButton.setEnabled(false);
				break;
			case SegradaApplication.STATUS_STARTING:
				statusText.setText(messages.getString("starting"));
				startStopButton.setEnabled(false);
				browserButton.setEnabled(false);
				break;
			case SegradaApplication.STATUS_UPDATING_DATABASE:
				statusText.setText(messages.getString("updatingDb"));
				startStopButton.setEnabled(false);
				browserButton.setEnabled(false);
				break;
			case SegradaApplication.STATUS_RUNNING:
				statusText.setText(messages.getString("running"));
				startStopButton.setText(messages.getString("stop"));
				startStopButton.setEnabled(true);
				browserButton.setEnabled(true);
				break;
			case SegradaApplication.STATUS_STOPPING:
				statusText.setText(messages.getString("stopping"));
				startStopButton.setEnabled(false);
				browserButton.setEnabled(false);
				break;
		}

		statusText.setForeground(Color.BLACK);
		validate();
		repaint();
	}
}
