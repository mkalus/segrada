package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.TermQuery;
import org.segrada.model.Tag;
import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaTaggable;
import org.segrada.model.util.IdModelTuple;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.orientdb.base.AbstractSegradaOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.AbstractLazyLoadedObject;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.OrientStringEscape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
 * Tag Repository implementation
 */
public class OrientDbTagRepository extends AbstractSegradaOrientDbRepository<ITag> implements TagRepository {
	private static final Logger logger = LoggerFactory.getLogger(OrientDbTagRepository.class);

	/**
	 * Constructor
	 */
	@Inject
	public OrientDbTagRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "Tag";
	}

	@Override
	public ITag convertToEntity(ODocument document) {
		ITag tag = new Tag();

		tag.setTitle(document.field("title", String.class));

		if (tag.getId() != null) {
			// set tags
			tag.setTags(lazyLoadTags(tag));
		}

		// populate with data
		populateEntityWithBaseData(document, tag);
		populateEntityWithCreatedModified(document, tag);

		return tag;
	}

	@Override
	public ODocument convertToDocument(ITag entity) {
		ODocument document = createOrLoadDocument(entity);

		// populate with data
		document.field("title", entity.getTitle());

		// populate with data
		populateODocumentWithCreatedModified(document, entity);

		return document;
	}

	@Override
	protected String getDefaultOrder(boolean addOrderBy) {
		return (addOrderBy?" ORDER BY":"").concat(" title");
	}

	@Override
	public ITag findByTitle(String title) {
		if (title == null) return null;

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Tag where title LIKE ?");
		List<ODocument> result = db.command(query).execute(title);

		// no entry found?
		if (result.size() == 0) return null;

		// get first entity
		return convertToEntity(result.get(0));
	}

	@Override
	public List<ITag> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
		List<ITag> hits = new LinkedList<>();

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
				TermQuery termQuery = new TermQuery(new Term("title", termPart + "*"));
				if (first) first = false;
				else sb.append(" AND ");
				sb.append(termQuery.toString());
			}

			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Tag where title LUCENE ? LIMIT " + maximum);
			result = db.command(query).execute(sb.toString());
		} else { // no term, just find top X entries
			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Tag " + getDefaultOrder(true) + " LIMIT " + maximum);
			result = db.command(query).execute();
		}

		// browse entities
		for (ODocument document : result) {
			hits.add(convertToEntity(document));
		}

		return hits;
	}

	@Override
	public List<ITag> createNewTagsByTitles(String[] titles) {
		List<ITag> list = new LinkedList<>();

		for (ODocument document : findTagDocumentsByCriteria("title", titles, true)) {
			list.add(convertToEntity(document));
		}

		return list;
	}

	@Override
	public List<ITag> findTagsByTitles(String[] titles) {
		List<ITag> list = new LinkedList<>();

		for (ODocument document : findTagDocumentsByCriteria("title", titles, false)) {
			list.add(convertToEntity(document));
		}

		return list;
	}

	@Override
	public String[] findTagIdsByTitles(String[] titles) {
		if (titles == null || titles.length == 0) return new String[]{};

		List<ODocument> docs = findTagDocumentsByCriteria("title", titles, false);

		String[] list = new String[docs.size()];
		int i = 0;

		for (ODocument document : docs) {
			list[i++] = document.getIdentity().toString();
		}

		return list;
	}

	@Override
	public String[] findTagTitlesByIds(String[] ids) {
		if (ids == null || ids.length == 0) return new String[]{};

		List<ODocument> docs = findTagDocumentsByCriteria("@rid", ids, false);

		String[] list = new String[docs.size()];
		int i = 0;

		for (ODocument document : docs) {
			list[i++] = document.field("title");
		}

		return list;
	}

	/**
	 * find tag documents (raw query interface)
	 * @param field field like title or id
	 * @param searchCriteria titles list
	 * @param onlyNonExistent if true, only non existing titles will be returned (which have been created)
	 * @return list of documents
	 */
	protected List<ODocument> findTagDocumentsByCriteria(String field, String[] searchCriteria, boolean onlyNonExistent) {
		initDb();

		List<ODocument> list = new LinkedList<>();

		// avoid NPEs
		if (searchCriteria == null || searchCriteria.length == 0) return list;

		// find each tag
		for (String tag : searchCriteria) {
			tag = tag.trim();
			if (tag.length() == 0) continue; // skip empty tags

			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Tag where " + field + " LIKE ?");
			List<ODocument> result = db.command(query).execute(tag);

			// no tag found and do we want to create new tags?
			if (onlyNonExistent) {
				if (result.size() == 0) { // only if nothing is found
					Tag tagEntity = new Tag();
					tagEntity.setTitle(tag);
					this.save(tagEntity);

					list.add(convertToDocument(tagEntity));
				}
			} else {
				if (result.size() != 0)
					list.add(result.get(0));
				else logger.info("findTagsByTagList: Skipping unknown tag " + tag);
			}
		}

		return list;
	}

	public String[] findTagIdsByParent(String id) {
		List<IdModelTuple> list = getConnectedIdModelTuplesOf(id, new String[]{"Tag"});

		// aggregate ids
		String[] tagIds = new String[list.size()];
		int i = 0;

		for (IdModelTuple tuple : list)
			tagIds[i++] = tuple.id;

		return tagIds;
	}

	@Override
	public String[] findTagIdsByParentTitle(String title) {
		// empty list
		if (title == null) return new String[]{};

		// get tag
		ITag tag = findByTitle(title);
		if (tag == null) return new String[]{};

		// call findTagIdsByParent to resolve rest
		return findTagIdsByParent(tag.getId());
	}

	@Override
	public List<IdModelTuple> getConnectedIdModelTuplesOf(String id, @Nullable String[] classes) {
		List<IdModelTuple> list = new LinkedList<>();

		// avoid NPEs
		if (id == null) return list;

		initDb();

		// create where statement
		String where;
		if (classes != null && classes.length > 0) {
			StringBuilder sb = new StringBuilder(" where");
			for (int i = 0; i < classes.length; i++) {
				if (i > 0) sb.append(" OR ");
				sb.append(" @class = '").append(OrientStringEscape.escapeOrientSql(classes[i])).append('\'');
			}
			where = sb.toString();
		} else where = "";

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @RID as id, @class as class from ( traverse out('IsTagOf') from " + id + " )" + where);
		List<ODocument> result = db.command(query).execute();

		for (ODocument document : result)
			list.add(new IdModelTuple(document.field("id", String.class), document.field("class", String.class)));

		return list;
	}

	@Override
	public List<IdModelTuple> getConnectedIdModelTuplesOf(ITag tag, @Nullable String[] classes) {
		return getConnectedIdModelTuplesOf(tag.getId(), classes);
	}

	@Override
	public String[] findTagIdsConnectedToModel(String id, String model, boolean onlyDirect) {
		// avoid NPEs
		if (id == null) return new String[0];

		initDb();

		// only directly connected tags?
		List<ODocument> result;
		if (onlyDirect) {
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select out.@rid as id from IsTagOf where out.@class = 'Tag' AND in = " + id);
			result = db.command(query).execute();
		} else {
			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @RID as id from ( traverse in('IsTagOf') from " + id + ") where @class = 'Tag'");
			result = db.command(query).execute();
		}

		String[] results = new String[result.size()];
		int i = 0;

		for (ODocument document : result) {
			results[i++] = document.field("id", String.class);
		}

		return results;
	}

	@Override
	public String[] findTagIdsConnectedToModel(IdModelTuple idModelTuple, boolean onlyDirect) {
		return findTagIdsConnectedToModel(idModelTuple.id, idModelTuple.model, onlyDirect);
	}

	@Override
	public String[] findTagIdsConnectedToModel(SegradaTaggable entity, boolean onlyDirect) {
		return findTagIdsConnectedToModel(entity.getId(), entity.getModelName(), onlyDirect);
	}

	@Override
	public void connectTag(ITag parent, SegradaTaggable child) {
		initDb();

		// check of childId is actually parent of parentId (circular tag path not allowed)
		if (child instanceof ITag) {
			ITag testChild = (ITag) child;
			if (isParentOf(testChild, parent))
				throw new RuntimeException("Circular connection of tags: " + parent + "=>" + child);
		}

		// no doubly connected tags:
		// does the child have a vertex to parent already?
		Iterable<ODocument> spath = db.command(new OSQLSynchQuery<>(
				"select shortestPath(" + parent.getId() + "," + child.getId() + ",'OUT')")).execute();
		ODocument sp = spath.iterator().next();
		List path = sp.field("shortestPath");

		if (!path.isEmpty()) return;

		// add edge
		db.command(new OCommandSQL("create edge IsTagOf from " + parent.getId() + " to " + child.getId())).execute();
	}

	@Override
	public void removeTag(String tagId, SegradaTaggable child) {
		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @RID as id from IsTagOf where out.@class = 'Tag' AND out = " +  tagId + " AND in = " + child.getId() + " LIMIT 1");
		List<ODocument> result = db.command(query).execute();

		if (result.size() > 0) {
			// remove edge
			db.command(new OCommandSQL("delete edge " + result.get(0).field("id", String.class))).execute();
		}
	}

	@Override
	public boolean isParentOf(ITag node, SegradaTaggable possibleChild) {
		return isChildOf(possibleChild, node);
	}

	@Override
	public boolean isChildOf(SegradaTaggable node, ITag possibleParent) {
		// sanity assertion
		if (node == null || possibleParent == null) return false;
		if (node.equals(possibleParent)) return false;

		initDb();

		// check shortest path
		Iterable<ODocument> spath = db.command(new OSQLSynchQuery<>(
				"select shortestPath(" + possibleParent.getId() + "," + node.getId() + ",'OUT')")).execute();
		ODocument sp = spath.iterator().next();
		List path = sp.field("shortestPath");

		return !path.isEmpty();
	}

	@Override
	public PaginationInfo<ITag> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		//TODO
		return null;
	}
}
