package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;

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
 * Abstract OrientDb Base Repository - just provide OrientDB stuff
 */
abstract public class AbstractOrientDbBaseRepository {
	/**
	 * Injected repository factory
	 */
	protected final OrientDbRepositoryFactory repositoryFactory;

	/**
	 * database instance
	 */
	protected final ODatabaseDocumentTx db;

	/**
	 * Constructor
	 * @param repositoryFactory injected
	 */
	public AbstractOrientDbBaseRepository(OrientDbRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
		if (repositoryFactory != null)
			this.db = repositoryFactory.getDb();
		else this.db = null;
	}

	/**
	 * open database
	 */
	protected void initDb() {
		try {
			if (db.isClosed()) {
				db.open(repositoryFactory.getApplicationSettings().getSetting("orientDB.login"),
						repositoryFactory.getApplicationSettings().getSetting("orientDB.password"));
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not open database.", e);
		}
	}
}
