package org.segrada.service.repository.orientdb.factory;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.prototype.SegradaRepository;
import org.segrada.session.ApplicationSettings;
import org.segrada.session.Identity;

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
@RequestScoped
public class OrientDbRepositoryFactory implements RepositoryFactory {
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
	public <T extends SegradaRepository> T produceRepository(Class<T> clazz) {
		SegradaRepository repository = repositoryMap.get(clazz);
		if (repository != null) return (T) repository;

		try {
			// instantiate new class
			Constructor<T> constructor = clazz.getConstructor(OrientDbRepositoryFactory.class);
			// cache in map
			T repositoryInstance = constructor.newInstance(this);
			repositoryMap.put(clazz, repositoryInstance);

			return repositoryInstance;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
