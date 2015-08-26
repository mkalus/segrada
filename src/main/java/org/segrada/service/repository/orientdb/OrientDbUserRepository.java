package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.segrada.model.prototype.IUser;
import org.segrada.service.repository.UserRepository;
import org.segrada.service.repository.orientdb.base.AbstractSegradaOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.OrientStringEscape;
import org.segrada.util.Sluggify;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
 * User Repository implementation
 */
public class OrientDbUserRepository extends AbstractSegradaOrientDbRepository<IUser> implements UserRepository {
	/**
	 * Constructor
	 */
	@Inject
	public OrientDbUserRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "User";
	}

	@Override
	public IUser convertToEntity(ODocument document) {
		return convertToUser(document);
	}

	@Override
	public ODocument convertToDocument(IUser entity) {
		ODocument document = createOrLoadDocument(entity);

		// populate with data
		document.field("login", entity.getLogin())
				.field("password", entity.getPassword())
				.field("name", entity.getName())
				.field("nameasc", Sluggify.asciify(entity.getName()))
				.field("role", entity.getRole())
				.field("created", entity.getCreated())
				.field("modified", entity.getModified())
				.field("lastLogin", entity.getLastLogin())
				.field("active", entity.getActive());

		return document;
	}

	@Override
	public IUser findByLogin(String login) {
		if (login == null) return null;

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from User where login LIKE ?");
		List<ODocument> result = db.command(query).execute(login.toLowerCase());

		// no user found?
		if (result.size() == 0) return null;

		// get first user
		return convertToEntity(result.get(0));
	}

	@Override
	public PaginationInfo<IUser> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		// avoid NPEs
		if (filters == null) filters = new HashMap<>();

		// aggregate filters
		List<String> constraints = new LinkedList<>();
		// search term
		if (filters.get("search") != null) {
			String term = "'" + OrientStringEscape.escapeOrientSql((String) filters.get("search")) + "'";
			constraints.add("(login LIKE " + term + " OR name LIKE " + term + ")");
		}

		// let helper do most of the work
		return super.paginate(page, entriesPerPage, constraints);
	}

	@Override
	protected String getDefaultOrder(boolean addOrderBy) {
		return (addOrderBy?" ORDER BY":"").concat(" name");
	}
}
