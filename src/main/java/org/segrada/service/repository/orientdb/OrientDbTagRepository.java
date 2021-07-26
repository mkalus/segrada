package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.TermQuery;
import org.segrada.model.Tag;
import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaTaggable;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.orientdb.base.AbstractOrientDbRepository;
import org.segrada.service.repository.orientdb.base.AbstractSegradaOrientDbRepository;
import org.segrada.service.repository.orientdb.exception.CircularConnectionException;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.OrientStringEscape;
import org.segrada.util.Sluggify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

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

		// populate with data
		populateEntityWithBaseData(document, tag);
		populateEntityWithCreatedModified(document, tag);

		if (tag.getId() != null) {
			// set tags
			String[] tags = getTags(document);
			if (tags != null && tags.length > 0) {
				tag.setTags(tags);
			}
		}

		return tag;
	}

	@Override
	public ODocument convertToDocument(ITag entity) {
		ODocument document = createOrLoadDocument(entity);

		// populate with data
		document.field("title", entity.getTitle())
				.field("titleasc", Sluggify.sluggify(entity.getTitle())); // sluggify correct!!

		// populate with data
		populateODocumentWithCreatedModified(document, entity);

		return document;
	}

	@Override
	protected ITag processAfterSaving(ODocument updated, ITag entity) {
		entity = super.processAfterSaving(updated, entity);

		// connect tags
		updateEntityTags(entity);
		// connect child tags, if needed
		updateChildTags(entity);

		return entity;
	}

	/**
	 * Connect child tags
	 * @param entity to update
	 */
	private void updateChildTags(ITag entity) {
		//TODO: test this method!

		// if null, then ignore
		if (entity.getChildTags() == null) return;

		// create new tags, if needed
		createNewTagsByTitles(entity.getChildTags());

		// find all tags by title
		List<ITag> tags = findTagsByTitles(entity.getChildTags());

		// keeps added ids
		Set<String> addedIds = new HashSet<>();

		// add all tags as child of entity
		for (ITag tag : tags) {
			connectTag(entity, tag);
			addedIds.add(tag.getId());
		}

		// now find all tag ids and see if there are some that have been deleted
		List<SegradaTaggable> children = findByTag(entity.getId(), false, new String[]{"Tag"});
		for (SegradaTaggable child : children) {
			if (!addedIds.contains(child.getId())) // not in set connected - delete
				removeTag(entity.getId(), child);
		}
	}

	@Override
	protected String getDefaultOrder(boolean addOrderBy) {
		return (addOrderBy?" ORDER BY":"").concat(" title");
	}

	@Override
	public ITag findByTitle(String title, boolean useSlug) {
		if (title == null) return null;

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Tag where title" + (useSlug?"asc":"") + " LIKE ?");
		List<ODocument> result = db.command(query).execute(title);

		// no entry found?
		if (result.isEmpty()) return null;

		// get first entity
		return convertToEntity(result.get(0));
	}

	@Override
	public List<ITag> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
		List<ITag> hits = new ArrayList<>();

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
		List<ITag> list = new ArrayList<>();

		for (ODocument document : findTagDocumentsByCriteria("title", titles, true)) {
			list.add(convertToEntity(document));
		}

		return list;
	}

	@Override
	public List<ITag> findTagsByTitles(String[] titles) {
		List<ITag> list = new ArrayList<>();

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

		List<ODocument> list = new ArrayList<>();

		// avoid NPEs
		if (searchCriteria == null || searchCriteria.length == 0) return list;

		// find each tag
		for (String tag : searchCriteria) {
			tag = tag.trim();
			if (tag.isEmpty()) continue; // skip empty tags

			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Tag where " + field + " LIKE ?");
			List<ODocument> result = db.command(query).execute(tag);

			// no tag found and do we want to create new tags?
			if (onlyNonExistent) {
				if (result.isEmpty()) { // only if nothing is found
					Tag tagEntity = new Tag();
					tagEntity.setTitle(tag);
					this.save(tagEntity);

					list.add(convertToDocument(tagEntity));
				}
			} else {
				if (!result.isEmpty())
					list.add(result.get(0));
				else logger.info("findTagsByTagList: Skipping unknown tag " + tag);
			}
		}

		return list;
	}

	@Override
	public String[] findTagIdsByParent(String id) {
		List<SegradaTaggable> list = findByTag(id, true, new String[]{"Tag"});

		// aggregate ids
		String[] tagIds = new String[list.size()];
		int i = 0;

		for (SegradaTaggable entity : list)
			tagIds[i++] = entity.getId();

		return tagIds;
	}

	@Override
	public List<SegradaTaggable> findByTag(String id, boolean traverse, @Nullable String[] classes) {
		List<SegradaTaggable> list = new ArrayList<>();

		// avoid NPEs
		if (id == null) return list;

		initDb();

		// execute query
		List<ODocument> result;
		if (traverse) {
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

			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from ( traverse out('IsTagOf') from " + id + " )" + where);
			result = db.command(query).execute();
		} else {
			// create where statement
			String where;
			if (classes != null && classes.length > 0) {
				StringBuilder sb = new StringBuilder("(");
				for (int i = 0; i < classes.length; i++) {
					if (i > 0) sb.append(" OR ");
					sb.append(" in.@class = '").append(OrientStringEscape.escapeOrientSql(classes[i])).append('\'');
				}
				where = sb.append(") AND ").toString();
			} else where = "";

			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select in from IsTagOf where " + where + " out = " + id);
			result = db.command(query).execute();
		}

 		for (ODocument document : result) {
			// get dynamic repository
			ODocument doc;
		    if (traverse) doc = document;
		    else doc = document.field("in");
			AbstractOrientDbRepository repository = repositoryFactory.produceRepository(doc.getClassName());
			if (repository != null)
				list.add((SegradaTaggable) repository.convertToEntity(doc));
			else logger.error("Could not convert document of class " + doc.getClassName() + " to entity.");
		}

		return list;
	}

	@Override
	public String[] findTagIdsByParentTitle(String title) {
		// empty list
		if (title == null) return new String[]{};

		// get tag
		ITag tag = findByTitle(title, false);
		if (tag == null) return new String[]{};

		// call findTagIdsByParent to resolve rest
		return findTagIdsByParent(tag.getId());
	}

	@Override
	public String[] findTagTitlesConnectedToModel(SegradaTaggable entity, boolean onlyDirect) {
		return findTagXConnectedToModel(entity, onlyDirect, "title");
	}

	@Override
	public String[] findTagIdsConnectedToModel(SegradaTaggable entity, boolean onlyDirect) {
		return findTagXConnectedToModel(entity, onlyDirect, "@RID");
	}

	/**
	 * worker class for boh methods above
	 * @param entity instance
	 * @param onlyDirect set true if only directly connected tags should be returned, otherwise whole tree of tags
	 * @param field to retrieve
	 * @return array of tag field entries
	 */
	private String[] findTagXConnectedToModel(@Nullable SegradaTaggable entity, boolean onlyDirect, String field) {
		// avoid NPEs
		if (entity == null || entity.getId() == null) return new String[0];

		initDb();
		// workaround for testing TODO: remove?
		ODatabaseRecordThreadLocal.INSTANCE.set(db);

		// only directly connected tags?
		List<ODocument> result;
		OSQLSynchQuery<ODocument> query;
		if (onlyDirect) {
			query = new OSQLSynchQuery<>("select out." + field + " as field from IsTagOf where out.@class = 'Tag' AND in = " + entity.getId());
		} else {
			// execute query
			query = new OSQLSynchQuery<>("select " + field + " as field from ( traverse in('IsTagOf') from " + entity.getId() + ") where @class = 'Tag'");
		}
		result = db.command(query).execute();

		String[] results = new String[result.size()];
		int i = 0;

		for (ODocument document : result) {
			results[i++] = document.field("field", String.class);
		}

		return results;
	}

	@Override
	public void connectTag(ITag parent, SegradaTaggable child) {
		if (parent == null || parent.getId() == null || child == null || child.getId() == null) {
			logger.warn("connectTag with null warning: " + parent + " => " + child);
			return;
		}

		initDb();

		// check of childId is actually parent of parentId (circular tag path not allowed)
		if (child instanceof ITag) {
			ITag testChild = (ITag) child;
			if (isParentOf(testChild, parent))
				throw new CircularConnectionException("Circular connection of tags: " + parent + "=>" + child);
		}

		// no doubly connected tags:
		// does the child have a vertex to parent already?
		Iterable<ODocument> spath = db.command(new OSQLSynchQuery<>(
				"select shortestPath(" + parent.getId() + "," + child.getId() + ",'OUT')")).execute();
		ODocument sp = spath.iterator().next();
		List path = sp.field("shortestPath");

		if (!path.isEmpty() && path.size() <= 2) return;

		// add edge
		db.command(new OCommandSQL("create edge IsTagOf from " + parent.getId() + " to " + child.getId())).execute();
	}

	@Override
	public void removeTag(String tagId, SegradaTaggable child) {
		removeTag(tagId, child.getId());
	}

	@Override
	public void removeTag(String tagId, String childId) {
		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @RID as id from IsTagOf where out.@class = 'Tag' AND out = " +  tagId + " AND in = " + childId + " LIMIT 1");
		List<ODocument> result = db.command(query).execute();

		if (!result.isEmpty()) {
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
	public boolean isTagConnectedTo(String tagUid, String nodeUid) {
		//TODO: Test!

		// sanity assertion
		if (tagUid == null || nodeUid == null) return false;
		if (tagUid.equals(nodeUid)) return false;

		// check shortest path
		Iterable<ODocument> spath = db.command(new OSQLSynchQuery<>(
				"select count(*) from IsTagOf where in = " + nodeUid + " and out = " + tagUid)).execute();
		ODocument sp = spath.iterator().next();
		Long count = sp.field("count");

		return count==1;
	}

	@Override
	public PaginationInfo<ITag> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		// avoid NPEs
		if (filters == null) filters = new HashMap<>();

		// aggregate filters
		List<String> constraints = new ArrayList<>();
		// search term
		if (filters.get("search") != null) {
			constraints.add("title LIKE '%" + OrientStringEscape.escapeOrientSql((String)filters.get("search")) + "%'");
		}
		// tags
		if (filters.get("tags") != null) {
			StringBuilder sb = new StringBuilder(" in('IsTagOf').title IN [ ");
			boolean first = true;
			for (String tag : (String[]) filters.get("tags")) {
				if (first) first = false;
				else sb.append(',');
				sb.append('\'').append(OrientStringEscape.escapeOrientSql(tag)).append('\'');
			}

			constraints.add(sb.append("]").toString());
		}

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
}
