package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.TermQuery;
import org.segrada.model.prototype.IPictogram;
import org.segrada.service.repository.PictogramRepository;
import org.segrada.service.repository.orientdb.base.AbstractSegradaOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.util.Sluggify;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2015-2019 Maximilian Kalus [segrada@auxnet.de]
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
 * OrientDb Pictogram Repository
 */
public class OrientDbPictogramRepository extends AbstractSegradaOrientDbRepository<IPictogram> implements PictogramRepository {
	/**
	 * Constructor
	 */
	@Inject
	public OrientDbPictogramRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "Pictogram";
	}

	@Override
	public IPictogram convertToEntity(ODocument document) {
		return convertToPictogram(document);
	}

	@Override
	public ODocument convertToDocument(IPictogram entity) {
		ODocument document = createOrLoadDocument(entity);

		// fields to document
		document.field("title", entity.getTitle())
				.field("titleasc", Sluggify.asciify(entity.getTitle()))
				.field("fileIdentifier", entity.getFileIdentifier());

		// populate with data
		populateODocumentWithCreatedModified(document, entity);

		return document;
	}

	@Override
	public IPictogram findByTitle(String title) {
		if (title == null) return null;

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Pictogram where title LIKE ?");
		List<ODocument> result = db.command(query).execute(title);

		// no pic found?
		if (result.isEmpty()) return null;

		// get first entity
		return convertToEntity(result.get(0));
	}

	@Override
	public List<IPictogram> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
		List<IPictogram> hits = new ArrayList<>();

		// empty search term and returnWithoutTerm false
		if (!returnWithoutTerm && (term == null || term.equals(""))) return hits;

		initDb();

		// search for term
		List<ODocument> result;
		if (term != null && !term.isEmpty()) {
			// create query term for lucene full text search
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String termPart : term.toLowerCase().split("\\s+")) {
				termPart = QueryParserUtil.escape(termPart);
				TermQuery termQuery = new TermQuery(new Term("title", termPart + "*"));
				if (first) first = false;
				else sb.append(" AND ");
				sb.append(termQuery.toString());
			}

			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Pictogram where title LUCENE ? LIMIT " + maximum);
			result = db.command(query).execute(sb.toString());
		} else { // no term, just find top X entries
			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Pictogram LIMIT " + maximum);
			result = db.command(query).execute();
		}

		// browse entities and populate list
		for (ODocument document : result)
			hits.add(convertToEntity(document));

		return hits;
	}

	@Override
	public boolean delete(IPictogram entity) {
		if (super.delete(entity)) {
			initDb();

			// update connected entities
			String query = "update Node set pictogram = null where pictogram = " + entity.getId();
			db.command(new OCommandSQL(query)).execute();

			// update connected relations
			query = "update Relation set pictogram = null where pictogram = " + entity.getId();
			db.command(new OCommandSQL(query)).execute();

			// update connected files
			query = "update File set pictogram = null where pictogram = " + entity.getId();
			db.command(new OCommandSQL(query)).execute();

			// update connected relation types
			query = "update RelationType set pictogram = null where pictogram = " + entity.getId();
			db.command(new OCommandSQL(query)).execute();

			// update connected sources
			query = "update Source set pictogram = null where pictogram = " + entity.getId();
			db.command(new OCommandSQL(query)).execute();

			return true;
		}
		return false;
	}

	@Override
	protected String getDefaultOrder(boolean addOrderBy) {
		return (addOrderBy?" ORDER BY":"").concat(" title");
	}
}
