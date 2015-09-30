package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.segrada.service.repository.ConfigRepository;
import org.segrada.service.repository.orientdb.base.AbstractOrientDbBaseRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.util.OrientStringEscape;

import java.util.List;

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
 * OrientDb Config Repository
 */
public class OrientDbConfigRepository extends AbstractOrientDbBaseRepository implements ConfigRepository {
	/**
	 * Constructor
	 * @param repositoryFactory injected
	 */
	public OrientDbConfigRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getValue(String key) {
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Config where key = ?");
		List<ODocument> result = db.command(query).execute(key);

		// not found?
		if (result == null || result.isEmpty()) return null;

		// otherwise get document
		ODocument config = result.get(0);

		// and return value
		return config.field("value", String.class);
	}

	@Override
	public boolean hasValue(String key) {
		return getValue(key) != null;
	}

	@Override
	public void setValue(String key, String value) {
		key = OrientStringEscape.escapeOrientSql(key);
		// upsert command
		String query = "UPDATE Config SET key = '" + key + "', value = '" + OrientStringEscape.escapeOrientSql(value) + "' UPSERT WHERE key = '" + key + "'";
		db.command(new OCommandSQL(query)).execute();
	}

	@Override
	public void deleteValue(String key) {
		// delete command
		String query = "DELETE FROM Config WHERE key = '" + OrientStringEscape.escapeOrientSql(key) + "'";
		db.command(new OCommandSQL(query)).execute();
	}
}
