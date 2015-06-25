package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.segrada.model.Comment;
import org.segrada.model.prototype.IComment;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.model.util.IdModelTuple;
import org.segrada.service.repository.CommentRepository;
import org.segrada.service.repository.orientdb.base.AbstractAnnotatedOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;

import java.util.LinkedList;
import java.util.List;

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
 * OrientDb Comment Repository
 */
public class OrientDbCommentRepository extends AbstractAnnotatedOrientDbRepository<IComment> implements CommentRepository {
	/**
	 * Constructor
	 */
	@Inject
	public OrientDbCommentRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "Comment";
	}

	@Override
	public IComment convertToEntity(ODocument document) {
		Comment comment = new Comment();
		comment.setText(document.field("text", String.class));
		comment.setMarkup(document.field("markup", String.class));

		// populate with data
		populateEntityWithBaseData(document, comment);
		populateEntityWithCreatedModified(document, comment);
		populateEntityWithColored(document, comment);
		populateEntityWithAnnotated(document, comment);

		return comment;
	}

	@Override
	public ODocument convertToDocument(IComment entity) {
		ODocument document = createOrLoadDocument(entity);

		// populate with data
		document.field("text", entity.getText())
				.field("markup", entity.getMarkup());

		// populate with data
		populateODocumentWithCreatedModified(document, entity);
		populateODocumentWithColored(document, entity);
		populateODocumentWithAnnotated(document, entity);

		return document;
	}

	@Override
	public List<IComment> findByReference(String id) {
		List<IComment> list = new LinkedList<>();

		// empty?
		if (id == null) return list;

		initDb();

		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select out as comment from IsCommentOf where in = " + id);
		List<ODocument> result = db.command(query).execute();

		for (ODocument document : result) {
			list.add(convertToEntity(document.field("comment", ODocument.class)));
		}

		return list;
	}

	@Override
	public boolean connectCommentWith(IComment comment, SegradaAnnotatedEntity entity) {
		return !(entity == null || entity.getId() == null)
				&& connectCommentWith(comment, new IdModelTuple(entity.getId(), entity.getModelName()));
	}

	@Override
	public boolean connectCommentWith(IComment comment, IdModelTuple idModelTuple) {
		// only saved elements
		if (comment.getId() == null || idModelTuple == null || idModelTuple.id == null) return false;

		initDb();

		// connection already set?
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment.getId() + " AND in = " + idModelTuple.id);
		List<ODocument> result = db.command(query).execute();
		if (result.size() > 0) return false;

		// add edge
		db.command(new OCommandSQL("create edge IsCommentOf from " + comment.getId() + " to " + idModelTuple.id)).execute();
		return true;
	}

	@Override
	public boolean deleteCommentConnection(IComment comment, SegradaAnnotatedEntity entity) {
		return !(entity == null || entity.getId() == null)
				&& deleteCommentConnection(comment, new IdModelTuple(entity.getId(), entity.getModelName()));
	}

	@Override
	public boolean deleteCommentConnection(IComment comment, IdModelTuple idModelTuple) {
		// only saved elements
		if (comment.getId() == null || idModelTuple == null || idModelTuple.id == null) return false;

		initDb();

		// connection already set?
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid as id from IsCommentOf where out = " + comment.getId() + " AND in = " + idModelTuple.id);
		List<ODocument> result = db.command(query).execute();
		if (result.size() == 0) return false;

		String id = result.get(0).field("id", String.class);

		// delete edge
		db.command(new OCommandSQL("delete edge " + id)).execute();
		return true;
	}

	@Override
	public List<IdModelTuple> getConnectedIdModelTuplesOf(IComment comment) {
		List<IdModelTuple> list = new LinkedList<>();

		// only saved elements
		if (comment == null || comment.getId() == null) return list;

		initDb();

		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select in.@rid as id, in.@class as class from IsCommentOf where out = " + comment.getId());
		List<ODocument> result = db.command(query).execute();

		for (ODocument document : result) {
			list.add(new IdModelTuple(document.field("id", String.class), document.field("class", String.class)));
		}

		return list;
	}

	@Override
	public boolean hasConnections(IComment comment) {
		// only saved elements
		if (comment == null || comment.getId() == null) return false;

		initDb();

		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid from IsCommentOf where out = " + comment.getId() + " LIMIT 1");
		List<ODocument> result = db.command(query).execute();
		return result.size() != 0;
	}
}
