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
 * OrientDb Source Repository
 */
public class OrientDbSourceRepository extends AbstractAnnotatedOrientDbRepository<ISource> implements SourceRepository {
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
	public ISource findByRef(String ref) {
		if (ref == null || "".equals(ref)) return null;

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Source where shortRef = ?");
		List<ODocument> result = db.command(query).execute(ref);

		// no pic found?
		if (result.size() == 0) return null;

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

		List<ISource> list = new LinkedList<>();

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
		List<ISource> hits = new LinkedList<>();

		// empty search term and returnWithoutTerm false
		if (!returnWithoutTerm && (term == null || term.equals(""))) return hits;

		initDb();

		// search for term
		List<ODocument> result;
		if (term != null && term.length() > 0) {
			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Source where " + createSearchTermFullText(term) + " LIMIT " + maximum);
			//System.out.println(query);
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
		List<String> constraints = new LinkedList<>();
		// search term
		if (filters.containsKey("search")) {
			constraints.add(createSearchTermFullText((String) filters.get("search")));
		}
		if (filters.containsKey("shortRef")) {
			constraints.add("shortRef LIKE '%" + OrientStringEscape.escapeOrientSql((String) filters.get("shortRef")) + "%'");
		}
		// tags
		if (filters.containsKey("tags")) {
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
		StringBuilder sb = new StringBuilder(" [longTitle,shortRef,shortTitle] LUCENE ");
		boolean first = true;
		for (String termPart : term.toLowerCase().split("\\s+")) {
			if (termPart.contains(":")) termPart = "\"" + termPart + "\"";
			else termPart = QueryParserUtil.escape(termPart);
			if (termPart.contains(".")) termPart = "\"" + termPart + "\"";
			else if (!termPart.startsWith("\"") || !termPart.endsWith("\"")) termPart += "*";
			TermQuery termQuery1 = new TermQuery(new Term("longTitle", termPart));
			TermQuery termQuery2 = new TermQuery(new Term("shortRef", termPart));
			TermQuery termQuery3 = new TermQuery(new Term("shortTitle", termPart));
			if (first) first = false;
			else sb.append(" AND [longTitle,shortRef,shortTitle] LUCENE ");
			sb.append("(").append(termQuery1.toString()).append(" OR (")
					.append(termQuery2.toString()).append(" OR ")
					.append(termQuery3.toString()).append("))");
		}
		//System.out.println(sb.toString());
		return sb.toString();
	}
}
