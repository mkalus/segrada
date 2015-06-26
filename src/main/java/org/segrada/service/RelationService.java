package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.Relation;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IRelation;
import org.segrada.search.SearchEngine;
import org.segrada.service.base.AbstractFullTextService;
import org.segrada.service.repository.RelationRepository;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.factory.RepositoryFactory;

import javax.annotation.Nullable;

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
 * Relation service
 */
public class RelationService extends AbstractFullTextService<IRelation, RelationRepository> {
	/**
	 * Constructor
	 */
	@Inject
	public RelationService(RepositoryFactory repositoryFactory, SearchEngine searchEngine) {
		super(repositoryFactory, RelationRepository.class, searchEngine);
	}

	@Override
	public IRelation createNewInstance() {
		return new Relation();
	}

	@Override
	public Class<IRelation> getModelClass() {
		return IRelation.class;
	}

	@Nullable
	@Override
	protected SearchIndexEntity prepareIndexEntity(IRelation entity) {
		SearchIndexEntity idxEntity = new SearchIndexEntity(entity.getId());
		String fromTitle = entity.getFromEntity().getTitle();
		String toTitle = entity.getToEntity().getTitle();

		idxEntity.title = fromTitle + " => " + entity.getRelationType().getFromTitle() + " => " + toTitle;
		idxEntity.subTitles = entity.getRelationType().getFromTitle() + " " + entity.getRelationType().getToTitle();
		idxEntity.content = entity.getDescription();
		idxEntity.contentMarkup = entity.getDescriptionMarkup();
		idxEntity.weight = 5f; // relatively important

		// get tag ids and add them to entity
		TagRepository tagRepository = repositoryFactory.produceRepository(TagRepository.class);
		if (tagRepository != null)
			idxEntity.tagIds = tagRepository.findTagIdsConnectedToModel(entity, false);

		return idxEntity;
	}
}
