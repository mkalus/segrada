package org.segrada.config;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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
import org.segrada.service.*;
import org.segrada.service.binarydata.BinaryDataService;
import org.segrada.service.binarydata.BinaryDataServiceFile;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.session.ApplicationSettings;
import org.segrada.session.ApplicationSettingsProperties;
import org.segrada.util.PBKDF2WithHmacSHA1PasswordEncoder;
import org.segrada.util.PasswordEncoder;

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
	@Override
	protected void configure() {
		// bind settings
		bind(ApplicationSettings.class).to(ApplicationSettingsProperties.class);

		// bind password encoder
		bind(PasswordEncoder.class).to(PBKDF2WithHmacSHA1PasswordEncoder.class);

		// bind binary data service
		bind(BinaryDataService.class).to(BinaryDataServiceFile.class);
		// bind search engine
		bind(SearchEngine.class).to(LuceneSearchEngine.class);

		// bind repository factory
		bind(RepositoryFactory.class).to(OrientDbRepositoryFactory.class);

		// bind all services
		bind(ColorService.class);
		bind(CommentService.class);
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
	}

	@Provides
	@Singleton
	@Inject
	OrientGraphFactory provideOrientGraphFactory(ApplicationSettings settings) {
		return new OrientGraphFactory(
				settings.getSetting("orientDB.url"),
				settings.getSetting("orientDB.login"),
				settings.getSetting("orientDB.password"))
				.setupPool(10, 50);
	}

	@Provides @RequestScoped
	@Inject
	public ODatabaseDocumentTx provideDatabase(OrientGraphFactory orientGraphFactory) {
		//System.out.println("Available: " + orientGraphFactory.getAvailableInstancesInPool());
		//System.out.println("Created: " + orientGraphFactory.getCreatedInstancesInPool());
		return orientGraphFactory.getDatabase();
	}

	// lucene should be thread save in practice
	@Provides @Singleton @Inject
	public Directory provideLuceneDirectory(ApplicationSettings settings) {
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
		if (analyzer == null || analyzer.length() == 0) return new StandardAnalyzer(Version.LUCENE_47);

		// class for name
		try {
			// create new instance of my analyzer
			return (Analyzer) Class.forName(analyzer).newInstance();
		} catch (Exception e) {
			return new StandardAnalyzer(Version.LUCENE_47); // fallback 2
		}
	}
}
