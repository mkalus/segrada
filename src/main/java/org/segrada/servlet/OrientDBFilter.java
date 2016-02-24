package org.segrada.servlet;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.eclipse.jetty.server.Request;
import org.segrada.SegradaApplication;
import org.segrada.controller.LoginController;
import org.segrada.model.User;
import org.segrada.model.prototype.IUser;
import org.segrada.search.lucene.LuceneSearchEngine;
import org.segrada.service.repository.RememberMeRepository;
import org.segrada.service.repository.orientdb.init.OrientDbSchemaUpdater;
import org.segrada.session.ApplicationSettings;
import org.segrada.session.Identity;
import org.segrada.util.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

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
@Singleton
public class OrientDBFilter implements Filter {
	private final static Logger logger = LoggerFactory.getLogger(OrientDBFilter.class.getName());

	/**
	 * filter configuration
	 */
	public FilterConfig filterConfig;

	/**
	 * reference to injector
	 */
	private static Injector injector;

	/**
	 * excluded patterns
	 */
	private Pattern excludePatterns;

	/**
	 * set the injector - called by Bootstrap
	 */
	public static void setInjector(Injector injector)
	{
		OrientDBFilter.injector = injector;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// set server status
		SegradaApplication.setServerStatus(SegradaApplication.STATUS_UPDATING_DATABASE);

		this.filterConfig = filterConfig;
		logger.info("OrientDBFilter initialized - running db update");

		// get orientdb instance and password encoder
		OrientGraphFactory orientGraphFactory = injector.getInstance(OrientGraphFactory.class);
		PasswordEncoder passwordEncoder = injector.getInstance(PasswordEncoder.class);
		ApplicationSettings applicationSettings = injector.getInstance(ApplicationSettings.class);
		// get and run updater
		OrientDbSchemaUpdater updater = new OrientDbSchemaUpdater(orientGraphFactory, applicationSettings.getSetting("orientDB.url"));
		updater.initializeDatabase();
		updater.buildOrUpdateSchema();
		updater.populateWithData(passwordEncoder);

		// add exclude patterns
		String pattern = filterConfig.getInitParameter("excludePatterns");
		excludePatterns = Pattern.compile(pattern);

		// set server status
		SegradaApplication.setServerStatus(SegradaApplication.STATUS_RUNNING);

		// check for update of segrada
		SegradaUpdateChecker updateChecker = new SegradaUpdateChecker(injector.getInstance(OrientGraphFactory.class));
		updateChecker.checkForUpdate();
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		// exclude?
		String url = ((HttpServletRequest) servletRequest).getPathInfo();

		if (logger.isTraceEnabled())
			logger.trace("Filtering " + url);

		if (excludePatterns.matcher(url).find()) {
			filterChain.doFilter(servletRequest, servletResponse);
			return; // ignore matched entries
		}

		// create database instance
		ODatabaseDocumentTx db = injector.getInstance(ODatabaseDocumentTx.class);
		//OrientGraph graph = injector.getInstance(OrientGraph.class);

		if (logger.isTraceEnabled())
			logger.trace("DB instance created");

		// check if an identity has been set
		Identity identity = injector.getInstance(Identity.class);
		if (identity.getName()==null && !url.equals("/login")) {
			// no identity set -> how do we authentificate the user?
			ApplicationSettings applicationSettings = injector.getInstance(ApplicationSettings.class);
			String requireLogin = applicationSettings.getSetting("requireLogin");
			if (requireLogin == null || requireLogin.isEmpty() || !requireLogin.equalsIgnoreCase("true")) {
				// automatic login as first user in DB
				IUser user = docToUser(db.browseClass("User").next());

				identity.setUser(user);

				logger.info("Autologin as " + user.getName());
			} else {
				IUser user = null;
				// get cookie - to check for remember me function
				for (Cookie c : ((Request) servletRequest).getCookies()) {
					if (c.getName().equals(LoginController.REMEMBER_ME_COOKIE_NAME)) {
						RememberMeRepository rememberMeRepository = injector.getInstance(RememberMeRepository.class);
						String id = rememberMeRepository.validateTokenAndGetUserId(c.getValue());
						if (id != null) {
							ORID oid = new ORecordId(id);
							if (oid.isValid()) {
								user = docToUser(db.getRecord(oid));

								if (user != null) {
									identity.setUser(user);

									logger.info("Remember-Me-Token-Login as " + user.getName());
								}
							}
						}
						break;
					}
				}

				if (user == null) {
					HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
					httpServletResponse.sendRedirect("/login");
				}
			}
		}

		// do whatever has to be done
		try {
			filterChain.doFilter(servletRequest, servletResponse);
		} finally {
			// close database instance
			db.close();
			//graph.shutdown(); // do not shutdown graph!
			if (logger.isTraceEnabled())
				logger.trace("DB instance destroyed");
		}
	}

	/**
	 * convert document to user
	 * @param document ODocument of user
	 * @return IUser instance or null
	 */
	private static IUser docToUser(ODocument document) {
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

			return user;
		}

		return null;
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
			logger.warn("Could not shut down OrientGraphFactory properly.", e);
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
			logger.warn("Could not shut down LuceneSearchEngine properly.", e);
		}

		// remove injector
		injector = null;

		// set server status
		SegradaApplication.setServerStatus(SegradaApplication.STATUS_STOPPED);
	}
}
