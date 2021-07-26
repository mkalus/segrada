package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.TermQuery;
import org.segrada.model.Node;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.INode;
import org.segrada.service.repository.NodeRepository;
import org.segrada.service.repository.RelationRepository;
import org.segrada.service.repository.orientdb.base.AbstractCoreOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.FlexibleDateParser;
import org.segrada.util.OrientStringEscape;
import org.segrada.util.Sluggify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
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
	 * keep allowed sorting fields here
	 */
	private static final Set<String> allowedSorts = new HashSet<>(Arrays.asList(new String[]{"titleasc", "minJD", "maxJD"}));

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
		populateEntityWithAnnotated(document, node);
		populateEntityWithCore(document, node);

		return node;
	}

	@Override
	public ODocument convertToDocument(INode entity) {
		ODocument document = createOrLoadDocument(entity);

		// fields to document
		document.field("title", entity.getTitle())
				.field("titleasc", Sluggify.asciify(entity.getTitle()))
				.field("alternativeTitles", entity.getAlternativeTitles())
				.field("description", entity.getDescription())
				.field("descriptionMarkup", entity.getDescriptionMarkup());

		// populate with data
		populateODocumentWithCreatedModified(document, entity);
		populateODocumentWithColored(document, entity);
		populateODocumentWithAnnotated(document, entity);
		populateODocumentWithCore(document, entity);

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
		return (addOrderBy?" ORDER BY":"").concat(" titleasc");
	}

	@Override
	public List<INode> findBySearchTerm(@Nullable String term, int maximum, boolean returnWithoutTerm) {
		return findBySearchTermAndTags(term, maximum, returnWithoutTerm, null);
	}

	@Override
	public List<INode> findBySearchTermAndTags(@Nullable String term, int maximum, boolean returnWithoutTerm, @Nullable String[] tagIds) {
		List<INode> hits = new ArrayList<>();

		// empty search term and returnWithoutTerm false
		if (!returnWithoutTerm && (term == null || term.equals(""))) return hits;

		initDb();

		// contain by tag ids - case
		StringBuilder queryString;
		if (tagIds != null && tagIds.length > 0) {
			queryString = new StringBuilder("select * from ( traverse out('IsTagOf') from [");
			boolean first = true;
			for (String tagId : tagIds) {
				// parse tag id - avoid sql injections
				Matcher matcher = AbstractSegradaEntity.PATTERN_ORIENTID.matcher(tagId);
				if (matcher.find()) {
					if (first) first = false;
					else queryString.append(",");
					queryString.append(tagId);
				} else {
					logger.warn("Could not parse to tagId: " + tagId);
				}
			}
			queryString.append("]) where @class = 'Node'");

			// with search term
			if (term != null && !term.isEmpty()) {
				String escapedTerm = OrientStringEscape.escapeOrientSql(term);
				queryString.append("and (title LIKE '%")
						.append(escapedTerm).append("%' OR alternativeTitles LIKE '%")
						.append(escapedTerm).append("%')");
			}

			queryString.append(" LIMIT ").append(maximum);
		} else { // no tags, do search in normal way
			String where;
			if (term != null && !term.isEmpty()) where = "where " + createSearchTermFullText(term); // create search term
			else where = getDefaultOrder(true); // no term, just find top X entries

			// create query
			queryString = new StringBuilder().append("select * from Node ").append(where).append(" LIMIT ").append(maximum);
		}

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(queryString.toString());
		List<ODocument> result = db.command(query).execute();

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
		List<String> constraints = new ArrayList<>();
		// search term
		if (filters.get("search") != null) {
			constraints.add(createSearchTermFullText((String) filters.get("search")));
		}


		// location
		// TODO search locations and contain


		// period
		// parse periods from input
		if (filters.containsKey("minEntry")) { // parse from input
			FlexibleDateParser parser = new FlexibleDateParser();
			Long minJD = parser.inputToJd((String) filters.get("minEntry"), "G", false);
			if (minJD > Long.MIN_VALUE) constraints.add("minJD >= " + minJD);
		}
		if (filters.containsKey("maxEntry")) { // parse from input
			FlexibleDateParser parser = new FlexibleDateParser();
			Long maxJD = parser.inputToJd((String) filters.get("maxEntry"), "G", true);
			if (maxJD < Long.MAX_VALUE) constraints.add("maxJD <= " + maxJD);
		}

		// tags
		String tagSQL = buildTagFilterSQL((String[]) filters.get("tags"), filters.containsKey("withSubTags") && (boolean) filters.get("withSubTags"), false);
		if (!tagSQL.isEmpty()) constraints.add(tagSQL);

		// sorting
		String customOrder = null;
		if (filters.get("sort") != null) {
			String field = (String) filters.get("sort");
			if (allowedSorts.contains(field)) { // sanity check
				String dir = getDirectionFromString(filters.get("dir"));
				if (dir != null) customOrder = field.concat(dir);
			}
		}

		// let helper do most of the work
		return super.paginate(page, entriesPerPage, constraints, customOrder);
	}

	/**
	 * create search term for full text search
	 * @param term term(s) to search for
	 * @return search term part
	 */
	private static String createSearchTermFullText(String term) {
		// create query term for lucene full text search
		StringBuilder sb = new StringBuilder(" [title, alternativeTitles] LUCENE '");
		boolean first = true;
		for (String termPart : term.toLowerCase().split("\\s+")) {
			if (termPart.contains(":")) termPart = "\"" + termPart + "\"";
			else termPart = QueryParserUtil.escape(termPart);
			if (termPart.contains(".")) termPart = "\"" + termPart + "\"";
			else if (!termPart.startsWith("\"") || !termPart.endsWith("\"")) termPart += "*";
			if (first) first = false;
			else sb.append(' ');
			sb.append(termPart);
		}
		sb.append('\'');

		return sb.toString();
	}
}
