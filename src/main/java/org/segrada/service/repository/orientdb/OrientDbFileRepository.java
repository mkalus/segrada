package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.TermQuery;
import org.segrada.model.File;
import org.segrada.model.prototype.IFile;
import org.segrada.service.repository.FileRepository;
import org.segrada.service.repository.orientdb.base.AbstractAnnotatedOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.OrientStringEscape;

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
 * OrientDb File Repository
 */
public class OrientDbFileRepository extends AbstractAnnotatedOrientDbRepository<IFile> implements FileRepository {
	/**
	 * Constructor
	 */
	@Inject
	public OrientDbFileRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "File";
	}

	@Override
	public IFile convertToEntity(ODocument document) {
		File file = new File();
		file.setTitle(document.field("title", String.class));
		file.setFilename(document.field("filename", String.class));
		file.setDescription(document.field("description", String.class));
		file.setDescriptionMarkup(document.field("descriptionMarkup", String.class));
		file.setCopyright(document.field("copyright", String.class));
		file.setFullText(document.field("fullText", String.class));
		file.setMimeType(document.field("mimeType", String.class));
		file.setLocation(document.field("location", String.class));
		file.setIndexFullText(document.field("indexFullText", Boolean.class));
		file.setContainFile(document.field("containFile", Boolean.class));
		file.setFileSize(document.field("fileSize", Long.class));
		file.setFileIdentifier(document.field("fileIdentifier", String.class));

		// populate with data
		populateEntityWithBaseData(document, file);
		populateEntityWithCreatedModified(document, file);
		populateEntityWithColored(document, file);
		populateEntityWithAnnotated(document, file);

		return file;
	}

	@Override
	public ODocument convertToDocument(IFile entity) {
		ODocument document = createOrLoadDocument(entity);

		document.field("title", entity.getTitle())
				.field("filename", entity.getFilename())
				.field("description", entity.getDescription())
				.field("descriptionMarkup", entity.getDescriptionMarkup())
				.field("copyright", entity.getCopyright())
				.field("fullText", entity.getFullText())
				.field("mimeType", entity.getMimeType())
				.field("location", entity.getLocation())
				.field("indexFullText", entity.getIndexFullText())
				.field("containFile", entity.getContainFile())
				.field("fileSize", entity.getFileSize())
				.field("fileIdentifier", entity.getFileIdentifier());

		// populate with data
		populateODocumentWithCreatedModified(document, entity);
		populateODocumentWithColored(document, entity);
		populateODocumentWithAnnotated(document, entity);

		return document;
	}

	@Override
	public List<IFile> findByTitleOrFilename(String titleOrFilename) {
		if (titleOrFilename == null || "".equals(titleOrFilename)) return null;

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from File where title LIKE ? OR filename LIKE ?" + getDefaultOrder(true));
		List<ODocument> result = db.command(query).execute(titleOrFilename, titleOrFilename);

		List<IFile> list = new LinkedList<>();

		// populate set
		for (ODocument document : result)
			list.add(convertToEntity(document));

		return list;
	}

	@Override
	public List<IFile> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
		List<IFile> hits = new LinkedList<>();

		// empty search term and returnWithoutTerm false
		if (!returnWithoutTerm && (term == null || term.equals(""))) return hits;

		initDb();

		// search for term
		List<ODocument> result;
		if (term != null && term.length() > 0) {
			// create query term for lucene full text search
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String termPart : term.toLowerCase().split("\\s+")) {
				termPart = QueryParserUtil.escape(termPart);
				TermQuery termQuery1 = new TermQuery(new Term("title", termPart + "*"));
				TermQuery termQuery2 = new TermQuery(new Term("filename", termPart + "*"));
				if (first) first = false;
				else sb.append(" AND ");
				sb.append("(").append(termQuery1.toString()).append(" OR ")
						.append(termQuery2.toString()).append(")");
			}

			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from File where " + createSearchTermFullText(term) + " LIMIT " + maximum);
			result = db.command(query).execute();
		} else { // no term, just find top X entries
			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from File " + getDefaultOrder(true) + " LIMIT " + maximum);
			result = db.command(query).execute();
		}

		// browse entities
		for (ODocument document : result) {
			hits.add(convertToEntity(document));
		}

		return hits;
	}

	@Override
	public PaginationInfo<IFile> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		// avoid NPEs
		if (filters == null) filters = new HashMap<>();

		// aggregate filters
		List<String> constraints = new LinkedList<>();
		// search term
		if (filters.get("search") != null) {
			constraints.add(createSearchTermFullText((String) filters.get("search")));
		}
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
		StringBuilder sb = new StringBuilder(" [title, filename] LUCENE ");
		boolean first = true;
		for (String termPart : term.toLowerCase().split("\\s+")) {
			if (termPart.contains(":")) termPart = "\"" + termPart + "\"";
			else termPart = QueryParserUtil.escape(termPart);
			if (termPart.contains(".")) termPart = "\"" + termPart + "\"";
			else if (!termPart.startsWith("\"") || !termPart.endsWith("\"")) termPart += "*";
			TermQuery termQuery1 = new TermQuery(new Term("title", termPart));
			TermQuery termQuery2 = new TermQuery(new Term("filename", termPart));
			if (first) first = false;
			else sb.append(" AND [title, filename] LUCENE ");
			sb.append("(").append(termQuery1.toString()).append(" OR ").append(termQuery2.toString()).append(")");
		}

		return sb.toString();
	}

	@Override
	protected IFile processBeforeSaving(IFile entity) {
		if (entity.getTitle() == null || entity.getTitle().isEmpty())
			entity.setTitle(entity.getFilename());

		return super.processBeforeSaving(entity);
	}
}
