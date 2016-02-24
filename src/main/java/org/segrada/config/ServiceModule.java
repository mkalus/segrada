package org.segrada.config;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.servlet.RequestScoped;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.segrada.search.SearchEngine;
import org.segrada.search.lucene.LuceneSearchEngine;
import org.segrada.search.solr.SolrSearchEngine;
import org.segrada.service.*;
import org.segrada.service.base.AbstractFullTextService;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.binarydata.BinaryDataService;
import org.segrada.service.binarydata.BinaryDataServiceFile;
import org.segrada.service.repository.RememberMeRepository;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.orientdb.OrientRememberMeRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.ApplicationSettings;
import org.segrada.session.ApplicationSettingsProperties;
import org.segrada.util.PBKDF2WithHmacSHA1PasswordEncoder;
import org.segrada.util.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.segrada.util.Preconditions.checkNotNull;

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
 * Bind services
 */
public class ServiceModule extends AbstractModule {
	private static final Logger logger = LoggerFactory.getLogger(ServiceModule.class);

	@Override
	protected void configure() {
		// bind settings
		bind(ApplicationSettings.class).to(ApplicationSettingsProperties.class);

		// bind password encoder
		bind(PasswordEncoder.class).to(PBKDF2WithHmacSHA1PasswordEncoder.class);

		// bind binary data service
		bind(BinaryDataService.class).to(BinaryDataServiceFile.class);

		// bind repository factory
		bind(RepositoryFactory.class).to(OrientDbRepositoryFactory.class);

		// bind all services
		bind(ColorService.class);
		bind(CommentService.class);
		bind(ConfigService.class);
		bind(FileService.class);
		bind(LocationService.class);
		bind(NodeService.class);
		bind(PeriodService.class);
		bind(PictogramService.class);
		bind(RelationService.class);
		bind(RelationTypeService.class);
		bind(SourceReferenceService.class);
		bind(SourceService.class);
		bind(TagService.class);
		bind(UserService.class);
		bind(UserGroupService.class);

		// bind remember me service
		bind(RememberMeRepository.class).to(OrientRememberMeRepository.class);

		// create mapped binding for services which support indexing
		MapBinder<String, AbstractFullTextService> fullTextServiceMapBinder
				= MapBinder.newMapBinder(binder(), String.class, AbstractFullTextService.class);
		fullTextServiceMapBinder.addBinding("Comment").to(CommentService.class);
		fullTextServiceMapBinder.addBinding("File").to(FileService.class);
		fullTextServiceMapBinder.addBinding("Node").to(NodeService.class);
		fullTextServiceMapBinder.addBinding("Source").to(SourceService.class);
		fullTextServiceMapBinder.addBinding("Relation").to(RelationService.class);

		// create mapped binding for services that can be annotated
		MapBinder<String, AbstractRepositoryService> annotatedServices
				= MapBinder.newMapBinder(binder(), String.class, AbstractRepositoryService.class);
		annotatedServices.addBinding("Comment").to(CommentService.class);
		annotatedServices.addBinding("File").to(FileService.class);
		annotatedServices.addBinding("Node").to(NodeService.class);
		annotatedServices.addBinding("Source").to(SourceService.class);
		annotatedServices.addBinding("Relation").to(RelationService.class);
	}

	@Provides
	@Singleton
	@Inject
	public SearchEngine provideSearchEngine(ApplicationSettings settings, Directory luceneDirectory, Analyzer luceneAnalyzer) {
		// solr has been set?
		String solrServer = settings.getSetting("solr.server");
		if (solrServer != null && !solrServer.isEmpty()) {
			// define search engine for solr server
			return new SolrSearchEngine(settings, luceneAnalyzer);
		}
		return new LuceneSearchEngine(luceneDirectory, luceneAnalyzer);
	}

	@Provides
	@Singleton
	@Inject
	public OrientGraphFactory provideOrientGraphFactory(ApplicationSettings settings) {
		if (logger.isInfoEnabled())
			logger.info("Providing OrientGraphFactory: " + settings.getSetting("orientDB.url"));

		return new OrientGraphFactory(
				settings.getSetting("orientDB.url"),
				settings.getSetting("orientDB.login"),
				settings.getSetting("orientDB.password"))
				.setupPool(10, 50);
	}

	@Provides @RequestScoped
	@Inject
	public ODatabaseDocumentTx provideDatabase(OrientGraphFactory orientGraphFactory) {
		if (logger.isTraceEnabled())
			logger.trace("Getting database.");

		//System.out.println("Available: " + orientGraphFactory.getAvailableInstancesInPool());
		//System.out.println("Created: " + orientGraphFactory.getCreatedInstancesInPool());
		return orientGraphFactory.getDatabase();
	}

	// lucene should be thread save in practice
	@Provides @Singleton @Inject
	public Directory provideLuceneDirectory(ApplicationSettings settings) {
		if (logger.isInfoEnabled())
			logger.info("Providing LuceneDirectory: " + settings.getSetting("savePath") + java.io.File.separator + "lucene");

		// construct save path from settings
		String savePath = checkNotNull(settings.getSetting("savePath"), "savePath");

		if (!savePath.endsWith(java.io.File.separator)) savePath += java.io.File.separator;
		savePath += "lucene" + java.io.File.separator;

		try {
			return new SimpleFSDirectory(new java.io.File(savePath));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// lucene should be thread save in practice
	@Provides @Singleton @Inject
	public Analyzer provideLuceneAnalyzer(ApplicationSettings settings) {
		// get analyzer from properties
		String analyzer = checkNotNull(settings.getSetting("lucene.analyzer"), "lucene.analyzer");

		// fallback 1
		if (analyzer == null || analyzer.isEmpty()) return new StandardAnalyzer(Version.LUCENE_47);

		// class for name
		try {
			// create new instance of my analyzer
			return (Analyzer) Class.forName(analyzer).newInstance();
		} catch (Exception e) {
			return new StandardAnalyzer(Version.LUCENE_47); // fallback 2
		}
	}
}
