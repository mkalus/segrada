package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.TermQuery;
import org.segrada.model.Source;
import org.segrada.model.prototype.ISource;
import org.segrada.service.repository.SourceRepository;
import org.segrada.service.repository.orientdb.base.AbstractAnnotatedOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.OrientStringEscape;
import org.segrada.util.Sluggify;

import java.util.*;

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
 * OrientDb Source Repository
 */
public class OrientDbSourceRepository extends AbstractAnnotatedOrientDbRepository<ISource> implements SourceRepository {
	/**
	 * keep allowed sorting fields here
	 */
	private static final Set<String> allowedSorts = new HashSet<>(Arrays.asList(new String[]{"shortTitleAsc", "shortRef"}));

	/**
	 * Constructor
	 */
	@Inject
	public OrientDbSourceRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "Source";
	}

	@Override
	public ISource convertToEntity(ODocument document) {
		Source source = new Source();
		source.setShortTitle(document.field("shortTitle", String.class));
		source.setLongTitle(document.field("longTitle", String.class));
		source.setShortRef(document.field("shortRef", String.class));
		source.setUrl(document.field("url", String.class));
		source.setProductCode(document.field("productCode", String.class));
		source.setAuthor(document.field("author", String.class));
		source.setCitation(document.field("citation", String.class));
		source.setCopyright(document.field("copyright", String.class));
		source.setDescription(document.field("description", String.class));
		source.setDescriptionMarkup(document.field("descriptionMarkup", String.class));

		populateEntityWithBaseData(document, source);

		// populate with data
		populateEntityWithCreatedModified(document, source);
		populateEntityWithColored(document, source);
		populateEntityWithAnnotated(document, source);

		return source;
	}

	@Override
	public ODocument convertToDocument(ISource entity) {
		ODocument document = createOrLoadDocument(entity);

		document.field("shortTitle", entity.getShortTitle())
				.field("shortTitleasc", Sluggify.asciify(entity.getShortTitle()))
				.field("longTitle", entity.getLongTitle())
				.field("shortRef", entity.getShortRef())
				.field("url", entity.getUrl())
				.field("productCode", entity.getProductCode())
				.field("author", entity.getAuthor())
				.field("citation", entity.getCitation())
				.field("copyright", entity.getCopyright())
				.field("description", entity.getDescription())
				.field("descriptionMarkup", entity.getDescriptionMarkup());

		// populate with created/modified stuff
		populateODocumentWithCreatedModified(document, entity);
		populateODocumentWithColored(document, entity);
		populateODocumentWithAnnotated(document, entity);

		return document;
	}

	@Override
	protected String getDefaultOrder(boolean addOrderBy) {
		return (addOrderBy?" ORDER BY":"").concat(" shortTitleAsc");
	}

	@Override
	public ISource findByRef(String ref) {
		if (ref == null || "".equals(ref)) return null;

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Source where shortRef = ?");
		List<ODocument> result = db.command(query).execute(ref);

		// no pic found?
		if (result.isEmpty()) return null;

		// get first entity
		return convertToEntity(result.get(0));
	}

	@Override
	public List<ISource> findByTitle(String title) {
		if (title == null || "".equals(title)) return null;

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Source where longTitle LIKE ? OR shortTitle LIKE ?" + getDefaultOrder(true));
		List<ODocument> result = db.command(query).execute(title, title);

		List<ISource> list = new ArrayList<>();

		// populate set
		for (ODocument document : result)
			list.add(convertToEntity(document));

		return list;
	}

	@Override
	public boolean delete(ISource entity) {
		if (entity == null) return true;

		if (super.delete(entity)) {
			// delete source references pointing to me, too
			repositoryFactory.getDb().command(new OCommandSQL("delete from SourceReference where source = " + entity.getId())).execute();

			return true;
		}
		return false;
	}

	@Override
	public List<ISource> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
		List<ISource> hits = new ArrayList<>();

		// empty search term and returnWithoutTerm false
		if (!returnWithoutTerm && (term == null || term.equals(""))) return hits;

		initDb();

		// search for term
		List<ODocument> result;
		if (term != null && !term.isEmpty()) {
			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Source where " + createSearchTermFullText(term) + " LIMIT " + maximum);
			result = db.command(query).execute();
		} else { // no term, just find top X entries
			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Source " + getDefaultOrder(true) + " LIMIT " + maximum);
			result = db.command(query).execute();
		}

		// browse entities
		for (ODocument document : result) {
			hits.add(convertToEntity(document));
		}

		return hits;
	}
	
	@Override
	public PaginationInfo<ISource> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		// avoid NPEs
		if (filters == null) filters = new HashMap<>();

		// aggregate filters
		List<String> constraints = new ArrayList<>();
		// search term
		if (filters.containsKey("search")) {
			constraints.add(createSearchTermFullText((String) filters.get("search")));
		}
		if (filters.containsKey("shortRef")) {
			constraints.add("shortRef LIKE '%" + OrientStringEscape.escapeOrientSql((String) filters.get("shortRef")) + "%'");
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
		StringBuilder sb = new StringBuilder(" [longTitle,shortRef,shortTitle] LUCENE '");
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
