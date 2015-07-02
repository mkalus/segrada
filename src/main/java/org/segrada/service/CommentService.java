package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.Comment;
import org.segrada.model.prototype.IComment;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.search.SearchEngine;
import org.segrada.service.base.AbstractFullTextService;
import org.segrada.service.repository.CommentRepository;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.factory.RepositoryFactory;

import javax.annotation.Nullable;
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
 * Comment service
 */
public class CommentService extends AbstractFullTextService<IComment, CommentRepository> {
	/**
	 * Constructor
	 */
	@Inject
	public CommentService(RepositoryFactory repositoryFactory, SearchEngine searchEngine) {
		super(repositoryFactory, CommentRepository.class, searchEngine);
	}

	@Override
	public Comment createNewInstance() {
		return new Comment();
	}

	@Override
	public Class<IComment> getModelClass() {
		return IComment.class;
	}

	@Nullable
	@Override
	protected SearchIndexEntity prepareIndexEntity(IComment entity) {
		SearchIndexEntity idxEntity = new SearchIndexEntity(entity.getUid());
		idxEntity.title = null;
		idxEntity.subTitles = null;
		idxEntity.content = entity.getText();
		idxEntity.contentMarkup = entity.getMarkup();
		idxEntity.weight = 1f; // not so important

		// get tag ids and add them to entity
		TagRepository tagRepository = repositoryFactory.produceRepository(TagRepository.class);
		if (tagRepository != null)
			idxEntity.tagIds = tagRepository.findTagIdsConnectedToModel(entity, false);

		return idxEntity;
	}

	/**
	 * find comments by reference id
	 * @param id of entity referencing comments
	 * @return list of comments
	 */
	public List<IComment> findByReference(String id) {
		return repository.findByReference(id);
	}

	/**
	 * gets connected entities of a single comment
	 * @param id of comment
	 * @return list of entities that reference comment
	 */
	public List<SegradaEntity> findByComment(String id) {
		return repository.findByComment(id);
	}


	/**
	 * Create new comment connection (only once)
	 * @param comment referencing
	 * @param entity referenced
	 */
	public void connectCommentToEntity(IComment comment, SegradaAnnotatedEntity entity) {
		repository.connectCommentToEntity(comment, entity);
	}

	/**
	 * Remove existing comment connection
	 * @param comment referencing
	 * @param entity referenced
	 */
	public void removeCommentFromEntity(IComment comment, SegradaAnnotatedEntity entity) {
		repository.removeCommentFromEntity(comment, entity);
	}
}
