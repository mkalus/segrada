package org.segrada.test;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.segrada.service.repository.orientdb.init.OrientDbSchemaUpdater;

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
 * Test instance for OrientDB
 */
public class OrientDBTestInstance {
	protected static OrientGraphFactory graphFactory =
			new OrientGraphFactory("memory:segradatest", "admin", "admin").setupPool(1, 10);

	/**
	 * schema created already?
	 */
	private static boolean schemaCreated = false;

	/**
	 * get db instance
	 * @return
	 */
	public ODatabaseDocumentTx getDatabase() {
		return graphFactory.getDatabase();
	}

	/**
	 * run schema creator, if needed
	 */
	public void setUpSchemaIfNeeded() {
		if (!schemaCreated) {
			// run schema updater
			OrientDbSchemaUpdater orientDbSchemaUpdater = new OrientDbSchemaUpdater(graphFactory, "memory:segradatest");

			orientDbSchemaUpdater.initializeDatabase();
			orientDbSchemaUpdater.buildOrUpdateSchema();

			schemaCreated = true;
		}
	}
}
