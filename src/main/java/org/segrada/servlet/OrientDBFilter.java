package org.segrada.servlet;

import com.google.inject.Injector;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.segrada.SegradaApplication;
import org.segrada.model.User;
import org.segrada.model.prototype.IUser;
import org.segrada.search.lucene.LuceneSearchEngine;
import org.segrada.service.repository.orientdb.init.OrientDbSchemaUpdater;
import org.segrada.session.ApplicationSettings;
import org.segrada.session.Identity;
import org.segrada.util.PasswordEncoder;

import javax.servlet.*;
import java.io.IOException;
import java.util.logging.Logger;

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
 * Servlet filter loading and destroying orient db instance on each request
 */
public class OrientDBFilter implements Filter {
	private final static Logger logger = Logger.getLogger(OrientDBFilter.class.getName());

	/**
	 * filter configuration
	 */
	public FilterConfig filterConfig;

	/**
	 * reference to injector
	 */
	private Injector injector;

	/**
	 * set the injector - called by Bootstrap
	 */
	public void setInjector(Injector injector)
	{
		this.injector = injector;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// set server status
		SegradaApplication.setServerStatus(SegradaApplication.STATUS_UPDATING_DATABASE);

		this.filterConfig = filterConfig;
		logger.finer("OrientDBFilter initialized - running db update");

		// get orientdb instance and password encoder
		OrientGraphFactory orientGraphFactory = injector.getInstance(OrientGraphFactory.class);
		PasswordEncoder passwordEncoder = injector.getInstance(PasswordEncoder.class);
		ApplicationSettings applicationSettings = injector.getInstance(ApplicationSettings.class);
		// get and run updater
		OrientDbSchemaUpdater updater = new OrientDbSchemaUpdater(orientGraphFactory, applicationSettings.getSetting("orientDB.url"));
		updater.initializeDatabase();
		updater.buildOrUpdateSchema();
		updater.populateWithData(passwordEncoder);

		// set server status
		SegradaApplication.setServerStatus(SegradaApplication.STATUS_RUNNING);
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		// create database instance
		ODatabaseDocumentTx db = injector.getInstance(ODatabaseDocumentTx.class);
		//OrientGraph graph = injector.getInstance(OrientGraph.class);

		//*************************** start delete in production **********************
		//TODO: this is the preliminary autologin should be changed in production
		// check if an identity has been set
		Identity identity = injector.getInstance(Identity.class);
		if (identity.getName()==null) {
			ODocument document = db.browseClass("User").next();
			if (document != null) {
				IUser user = new User();

				user.setLogin(document.field("login", String.class));
				user.setPassword(document.field("password", String.class));
				user.setName(document.field("name", String.class));
				user.setRole(document.field("role", String.class));
				user.setLastLogin(document.field("lastLogin", Long.class));
				user.setActive(document.field("active", Boolean.class));
				user.setId(document.getIdentity().toString());
				user.setVersion(document.getVersion());
				user.setCreated(document.field("created", Long.class));
				user.setModified(document.field("modified", Long.class));

				identity.setUser(user);
			}
		}
		//**************************** end delete in production **********************

		// do whatever has to be done
		try {
			filterChain.doFilter(servletRequest, servletResponse);
		} finally {
			// close database instance
			db.close();
			//graph.shutdown(); // do not shutdown graph!
			//System.out.println("destroyed...");
		}
	}

	@Override
	public void destroy() {
		// set server status
		SegradaApplication.setServerStatus(SegradaApplication.STATUS_STOPPING);

		try {
			OrientGraphFactory orientGraphFactory = injector.getInstance(OrientGraphFactory.class);
			if (orientGraphFactory != null) {
				orientGraphFactory.close();
			}
		} catch (Exception e) {
			logger.warning("Could not shut down OrientGraphFactory properly.");
		}

		// shut down instance completely
		Orient.instance().shutdown();

		logger.info("OrientDB has been shut down.");

		// also shut down lucene
		try {
			LuceneSearchEngine luceneSearchEngine = injector.getInstance(LuceneSearchEngine.class);
			luceneSearchEngine.destroy();

			logger.info("LuceneSearchEngine has been shut down.");
		} catch (Exception e) {
			logger.warning("Could not shut down LuceneSearchEngine properly.");
		}

		// set server status
		SegradaApplication.setServerStatus(SegradaApplication.STATUS_OFF);
	}
}
