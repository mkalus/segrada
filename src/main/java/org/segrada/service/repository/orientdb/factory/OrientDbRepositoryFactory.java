package org.segrada.service.repository.orientdb.factory;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.prototype.SegradaRepository;
import org.segrada.session.ApplicationSettings;
import org.segrada.session.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

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
 * Repository to dynamically create OrientDb repositories during runtime
 */
public class OrientDbRepositoryFactory implements RepositoryFactory {
	private static final Logger logger = LoggerFactory.getLogger(OrientDbRepositoryFactory.class);

	/**
	 * database instance
	 */
	protected final ODatabaseDocumentTx db;

	/**
	 * application settings instance
	 */
	protected final ApplicationSettings applicationSettings;

	/**
	 * Injected identity that keeps the logged in user
	 */
	protected final Identity identity;

	/**
	 * map to cache repositories
	 */
	private final Map<Class, SegradaRepository> repositoryMap = new HashMap<>();

	/**
	 * Constructor
	 */
	@Inject
	public OrientDbRepositoryFactory(ODatabaseDocumentTx db, ApplicationSettings applicationSettings, Identity identity) {
		this.db = db;
		this.applicationSettings = applicationSettings;
		this.identity = identity;
	}

	/**
	 * @return get db instance
	 */
	public ODatabaseDocumentTx getDb() {
		return db;
	}

	/**
	 * @return get application settings instance
	 */
	public ApplicationSettings getApplicationSettings() {
		return applicationSettings;
	}

	/**
	 * @return get user identity instance
	 */
	public Identity getIdentity() {
		return identity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public @Nullable <T extends SegradaRepository> T produceRepository(Class<T> clazz) {
		if (clazz == null) return null;
		// if we have a base class, e.g. FileRepository -> map it to OrientDbFileRepository
		if (clazz.isInterface()) {
			try { // create OrientDb class for this entity
				clazz = (Class<T>) Class.forName("org.segrada.service.repository.orientdb.OrientDb" + clazz.getSimpleName());
			} catch (Exception e) {
				logger.error("Error while producing repository from interface " + clazz.getName(), e);
				return null;
			}
		}

		// already in cache?
		SegradaRepository repository = repositoryMap.get(clazz);
		if (repository != null) return (T) repository;

		try {
			// instantiate new class
			Constructor<T> constructor = clazz.getConstructor(OrientDbRepositoryFactory.class);
			// cache to map
			T repositoryInstance = constructor.newInstance(this);
			repositoryMap.put(clazz, repositoryInstance);

			return repositoryInstance;
		} catch (Exception e) {
			logger.error("Error while producing repository from class " + clazz.getName(), e);
			return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public @Nullable <T extends SegradaRepository> T produceRepository(String modelName) {
		if (modelName == null) return null;
		try {
			Class c = Class.forName("org.segrada.service.repository.orientdb.OrientDb" + modelName + "Repository");
			return (T) produceRepository(c);
		} catch (ClassNotFoundException e) {
			// testing environment
			if (modelName.startsWith("Mock")) {
				logger.debug("Working in testing environment: Finding Mock repository.");
				for (Class repositoryClass : repositoryMap.keySet())
					if (repositoryClass.getName().contains("Mock"))
						return (T) repositoryMap.get(repositoryClass);
			}

			logger.error("Error while producing repository from model name " + modelName, e);
			return null;
		}
	}

	/**
	 * add repository instance to cache - used for testing
	 * @param clazz to map repository under
	 * @param repository to map
	 */
	public void addRepository(Class clazz, SegradaRepository repository) {
		repositoryMap.put(clazz, repository);
	}
}
