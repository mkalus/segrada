package org.segrada;

import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.segrada.servlet.SegradaGuiceServletContextListener;
import org.segrada.util.ApplicationStatusChangedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.*;

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
 * Segrada application standalone server
 */
public class SegradaApplication {
	private static final Logger logger = LoggerFactory.getLogger(SegradaApplication.class);

	/**
	 * server statuses
	 */
	public static final int STATUS_OFF = 0;
	public static final int STATUS_STARTING = 1;
	public static final int STATUS_UPDATING_DATABASE = 2;
	public static final int STATUS_RUNNING = 3;
	public static final int STATUS_STOPPING = 4;
	public static final int STATUS_STOPPED = 5;

	/**
	 * current server status
	 */
	private static int serverStatus = STATUS_OFF;

	/**
	 * context root
	 */
	private static String contextRoot = "/";

	/**
	 * port to listen to
	 */
	private static int port = 8080;

	/**
	 * prepare context data - get port and contextRoot from environmental variables or properties
	 */
	public static void prepareServerContext() {
		// get context and from environmental variables
		Map<String, String> var = System.getenv();
		if (var.containsKey("SEGRADA_SERVER_PORT")) {
			try {
				String propPort = var.get("SEGRADA_SERVER_PORT");
				if (propPort != null)
					port = Integer.parseInt(propPort);
			} catch (Exception e) {
				logger.warn("Environment variable SEGRADA_SERVER_PORT contains illegal value " + var.get("SEGRADA_SERVER_PORT") + " - ignoring.");
			}
		}
		if (var.containsKey("SEGRADA_SERVER_CONTEXT")) {
			String propCtx = var.get("SEGRADA_SERVER_CONTEXT");
			if (propCtx != null)
				contextRoot = var.get("SEGRADA_SERVER_CONTEXT");
		}

		// get properties from file
		Properties env = System.getProperties();
		if (env.getProperty("server.port") != null) {
			try {
				String propPort = env.getProperty("server.port");
				port = Integer.parseInt(propPort);
			} catch (Exception e) {
				logger.warn("Property server.port contains illegal value " + env.getProperty("server.port") + " - ignoring.");
			}
		}
		if (env.getProperty("server.context") != null) {
			contextRoot = env.getProperty("server.context");
		}
	}

	public static String getContextRoot() {
		return contextRoot;
	}

	public static int getPort() {
		return port;
	}

	/**
	 * listeners for application status changes
	 */
	private static List<ApplicationStatusChangedListener> applicationStatusChangedListeners;

	/**
	 * @return serverStatus
	 */
	public static int getServerStatus() {
		return serverStatus;
	}

	/**
	 * set new server status
	 * @param serverStatus new status
	 */
	public static void setServerStatus(int serverStatus) {
		// keep old status
		int oldStatus = SegradaApplication.serverStatus;

		// set new status
		SegradaApplication.serverStatus = serverStatus;

		// call listeners
		if (applicationStatusChangedListeners != null)
			for (ApplicationStatusChangedListener listener : applicationStatusChangedListeners)
				listener.onApplicationStatusChanged(serverStatus, oldStatus);
	}

	/**
	 * add a new listener to application status changes
	 * @param listener new listener
	 */
	public static void addApplicationStatusChangedListener(ApplicationStatusChangedListener listener) {
		if (applicationStatusChangedListeners == null)
			applicationStatusChangedListeners = new LinkedList<>();

		applicationStatusChangedListeners.add(listener);
	}

	/**
	 * server instance
	 */
	private static Server server;

	/**
	 * start server
	 * @throws Exception
	 */
	public static void startServer() throws Exception {
		if (server != null)
			throw new Exception("Server instance already created.");

		// prepare server context
		prepareServerContext();

		// set server starting
		setServerStatus(STATUS_STARTING);

		// create server
		server = new Server(port);

		// Create a servlet context
		ServletContextHandler sch = new ServletContextHandler(server, contextRoot, ServletContextHandler.SESSIONS);

		// set source main path
		sch.setResourceBase("src/main/webapp");

		// Create the SessionHandler (wrapper) to handle the sessions
		// Add our Guice listener that includes our bindings
		sch.addEventListener(new SegradaGuiceServletContextListener());

		// Then add GuiceFilter and configure the server to
		// reroute all requests through this filter.
		sch.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

		// Must add DefaultServlet for embedded Jetty.
		// Failing to do this will cause 404 errors.
		// This is not needed if web.xml is used instead.
		sch.addServlet(DefaultServlet.class, "/*");

		// stop when JVM shuts down
		server.setStopAtShutdown(true);

		// start server
		server.start();
		server.join();
	}

	/**
	 * stop server instance
	 * @throws Exception
	 */
	public static void stopServer() throws Exception {
		if (server == null) throw new Exception("Server instance not created.");

		server.stop();
		server = null;
	}

	/**
	 * starter for command line version
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		startServer();
	}
}
