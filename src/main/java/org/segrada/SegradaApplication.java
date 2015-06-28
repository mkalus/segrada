package org.segrada;

import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.segrada.servlet.EmptyServlet;
import org.segrada.servlet.SegradaGuiceServletContextListener;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

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
public class SegradaApplication {
	public static void main(String[] args) throws Exception {
		// create server
		Server server = new Server(8080);

		// Create a servlet context
		ServletContextHandler sch = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);

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
		//sch.addServlet(EmptyServlet.class, "/*");
		// TODO: needed?

		// stop when JVM shuts down
		server.setStopAtShutdown(true);

		// start server
		server.start();
		server.join();
	}
}
