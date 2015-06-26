package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.TermQuery;
import org.segrada.model.Node;
import org.segrada.model.prototype.INode;
import org.segrada.service.repository.NodeRepository;
import org.segrada.service.repository.RelationRepository;
import org.segrada.service.repository.orientdb.base.AbstractCoreOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.OrientStringEscape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
 * OrientDb Node Repository
 */
public class OrientDbNodeRepository extends AbstractCoreOrientDbRepository<INode> implements NodeRepository {
	private static final Logger logger = LoggerFactory.getLogger(OrientDbNodeRepository.class);

	/**
	 * Constructor
	 */
	@Inject
	public OrientDbNodeRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "Node";
	}

	@Override
	public INode convertToEntity(ODocument document) {
		Node node = new Node();

		node.setTitle(document.field("title", String.class));
		node.setAlternativeTitles(document.field("alternativeTitles", String.class));
		node.setDescription(document.field("description", String.class));
		node.setDescriptionMarkup(document.field("descriptionMarkup", String.class));
		populateEntityWithBaseData(document, node);

		// populate with data
		populateEntityWithCreatedModified(document, node);
		populateEntityWithColored(document, node);
		populateEntityWithCore(document, node);

		return node;
	}

	@Override
	public ODocument convertToDocument(INode entity) {
		ODocument document = createOrLoadDocument(entity);

		// fields to document
		document.field("title", entity.getTitle())
				.field("alternativeTitles", entity.getAlternativeTitles())
				.field("description", entity.getDescription())
				.field("descriptionMarkup", entity.getDescriptionMarkup());

		// populate with data
		populateODocumentWithCreatedModified(document, (Node) entity);
		populateODocumentWithColored(document, (Node) entity);
		populateODocumentWithCore(document, (Node) entity);

		return document;
	}

	@Override
	public boolean delete(INode entity) {
		if (super.delete(entity)) {
			// delete connected relations
			RelationRepository relationRepository = repositoryFactory.produceRepository(OrientDbRelationRepository.class);
			if (relationRepository == null) {
				logger.error("Could not produce RelationRepository while deleting node.");
				return false;
			}
			relationRepository.deleteByRelation(entity);

			return true;
		}
		return false;
	}

	@Override
	protected String getDefaultOrder(boolean addOrderBy) {
		return (addOrderBy?" ORDER BY":"").concat(" title");
	}

	@Override
	public List<INode> findBySearchTerm(@Nullable String term, int maximum, boolean returnWithoutTerm) {
		List<INode> hits = new LinkedList<>();

		// empty search term and returnWithoutTerm false
		if (!returnWithoutTerm && (term == null || term.equals(""))) return hits;

		initDb();

		// search for term
		List<ODocument> result;
		if (term != null && term.length() > 0) {
			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Node where " + createSearchTermFullText(term) + " LIMIT " + maximum);
			result = db.command(query).execute();
		} else { // no term, just find top X entries
			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Node " + getDefaultOrder(true) + " LIMIT " + maximum);
			result = db.command(query).execute();
		}

		// browse entities
		for (ODocument document : result) {
			hits.add(convertToEntity(document));
		}

		return hits;
	}

	@Override
	public PaginationInfo<INode> paginate(int page, int entriesPerPage, @Nullable Map<String, Object> filters) {
		// avoid NPEs
		if (filters == null) filters = new HashMap<>();

		// aggregate filters
		List<String> constraints = new LinkedList<>();
		// search term
		if (filters.get("search") != null) {
			constraints.add(createSearchTermFullText((String) filters.get("search")));
		}
		// location
		// TODO search locations and contain
		// period
		//TODO minJD/maxJD
		// tags
		if (filters.get("tags") != null) {
			StringBuilder sb = new StringBuilder(" in('IsTagOf').title IN [ ");
			boolean first = true;
			for (String tag : (String[]) filters.get("tags")) {
				if (first) first = false;
				else sb.append(",");
				sb.append("'").append(OrientStringEscape.escapeOrientSql(tag)).append("'");
			}

			constraints.add(sb.append("]").toString());
		}

		// let helper do most of the work
		return super.paginate(page, entriesPerPage, constraints);
	}

	/**
	 * create search term for full text search
	 * @param term term(s) to search for
	 * @return search term part
	 */
	private String createSearchTermFullText(String term) {
		// create query term for lucene full text search
		StringBuilder sb = new StringBuilder(" [title, alternativeTitles] LUCENE ");
		boolean first = true;
		for (String termPart : term.toLowerCase().split("\\s+")) {
			if (termPart.contains(":")) termPart = "\"" + termPart + "\"";
			else termPart = QueryParserUtil.escape(termPart);
			if (termPart.contains(".")) termPart = "\"" + termPart + "\"";
			else if (!termPart.startsWith("\"") || !termPart.endsWith("\"")) termPart += "*";
			TermQuery termQuery1 = new TermQuery(new Term("title", termPart));
			TermQuery termQuery2 = new TermQuery(new Term("alternativeTitles", termPart));
			if (first) first = false;
			else sb.append(" AND [title, alternativeTitles] LUCENE ");
			sb.append("(").append(termQuery1.toString()).append(" OR ").append(termQuery2.toString()).append(")");
		}

		return sb.toString();
	}
}
