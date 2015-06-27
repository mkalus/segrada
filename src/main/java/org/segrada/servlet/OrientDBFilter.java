package org.segrada.servlet;

import com.google.inject.Injector;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.segrada.search.lucene.LuceneSearchEngine;
import org.segrada.service.repository.orientdb.init.OrientDbSchemaUpdater;
import org.segrada.session.ApplicationSettings;
import org.segrada.util.PasswordEncoder;

import javax.servlet.*;
import java.io.IOException;
import java.util.logging.Logger;

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
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		// create database instance
		ODatabaseDocumentTx db = injector.getInstance(ODatabaseDocumentTx.class);
		//OrientGraph graph = injector.getInstance(OrientGraph.class);

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
	}
}
