package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.segrada.model.SavedQuery;
import org.segrada.model.prototype.ISavedQuery;
import org.segrada.model.prototype.IUser;
import org.segrada.service.repository.SavedQueryRepository;
import org.segrada.service.repository.orientdb.base.AbstractSegradaOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.OrientStringEscape;
import org.segrada.util.Sluggify;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2016 Maximilian Kalus [segrada@auxnet.de]
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
 * OrientDb Saved Query Repository
 */
public class OrientDbSavedQueryRepository extends AbstractSegradaOrientDbRepository<ISavedQuery> implements SavedQueryRepository {
	/**
	 * Constructor
	 *
	 * @param repositoryFactory
	 */
	public OrientDbSavedQueryRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "SavedQuery";
	}

	@Override
	public ISavedQuery convertToEntity(ODocument document) {
		SavedQuery savedQuery = new SavedQuery();

		savedQuery.setType(document.field("type", String.class));
		savedQuery.setTitle(document.field("title", String.class));
		savedQuery.setDescription(document.field("description", String.class));
		savedQuery.setData(document.field("data", String.class));

		// populate with data
		populateEntityWithBaseData(document, savedQuery);
		populateEntityWithCreatedModified(document, savedQuery);

		// get creator/modifier from user
		// get creator/modifier
		ORecordId oUser = document.field("user", ORecordId.class);

		// push
		if (oUser != null) {
			savedQuery.setCreator(lazyLoadUser(oUser));
			savedQuery.setModifier(lazyLoadUser(oUser));
		}

		return savedQuery;
	}

	@Override
	public ODocument convertToDocument(ISavedQuery entity) {
		ODocument document = createOrLoadDocument(entity);

		// populate with data
		document.field("type", entity.getType())
				.field("title", entity.getTitle())
				.field("titleasc", Sluggify.sluggify(entity.getTitle()))
				.field("description", entity.getDescription())
				.field("data", entity.getData());

		// populate with data
		if (document.getIdentity().isNew()) { // only set in new documents
			document.field("created", entity.getCreated());
		}

		document.field("modified", entity.getModified())
				.field("user", entity.getUser()==null?null:new ORecordId(entity.getUser().getId()));

		return document;
	}

	@Override
	public PaginationInfo<ISavedQuery> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		// avoid NPEs
		if (filters == null) filters = new HashMap<>();

		// aggregate filters
		List<String> constraints = new ArrayList<>();
		// nothing here yet
		// TODO

		// sorting
		String customOrder = null;
		if (filters.get("sort") != null) {
			String field = (String) filters.get("sort");
			if (field.equalsIgnoreCase("title")) { // sanity check
				String dir = getDirectionFromString(filters.get("dir"));
				if (dir != null) customOrder = "title".concat(dir);
			}
		}

		// let helper do most of the work
		return super.paginate(page, entriesPerPage, constraints, customOrder);
	}

	@Override
	public List<ISavedQuery> findAllBy(@Nullable IUser user, @Nullable String type, @Nullable String title) {
		List<ISavedQuery> list = new ArrayList<>();

		initDb();

		// aggregate filters
		List<String> constraints = new ArrayList<>();
		if (user != null && user.getId() != null) constraints.add("user = " + user.getId());
		if (type != null) constraints.add("type = '" + OrientStringEscape.escapeOrientSql(type) + "'");
		if (title != null) constraints.add("title LIKE '" + OrientStringEscape.escapeOrientSql(title) + "%'");

		// build SQL query
		String sql = "";
		for (String constraint : constraints) {
			if (!sql.isEmpty()) sql += " AND ";
			sql += constraint;
		}
		if (!sql.isEmpty()) sql = " WHERE " + sql;

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from SavedQuery" + sql + getDefaultOrder(true));
		List<ODocument> result = db.command(query).execute();

		for (ODocument doc : result) {
			list.add(convertToEntity(doc));
		}

		return list;
	}

	@Override
	protected String getDefaultOrder(boolean addOrderBy) {
		return (addOrderBy?" ORDER BY":"").concat(" titleasc");
	}
}
