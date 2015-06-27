package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.Source;
import org.segrada.model.prototype.ISource;
import org.segrada.search.SearchEngine;
import org.segrada.service.base.AbstractFullTextService;
import org.segrada.service.base.SearchTermService;
import org.segrada.service.repository.SourceRepository;
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
 * Source service
 */
public class SourceService extends AbstractFullTextService<ISource, SourceRepository> implements SearchTermService<ISource> {
	/**
	 * Constructor
	 */
	@Inject
	public SourceService(RepositoryFactory repositoryFactory, SearchEngine searchEngine) {
		super(repositoryFactory, SourceRepository.class, searchEngine);
	}

	@Override
	public ISource createNewInstance() {
		return new Source();
	}

	@Override
	public Class<ISource> getModelClass() {
		return ISource.class;
	}

	/**
	 * Find entities by search term
	 * @param term search term (or empty)
	 * @param maximum maximum hits to return
	 * @param returnWithoutTerm true if you want to return top hits if no search term is supplied (otherwise empty)
	 * @return list of entities
	 */
	public List<ISource> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
		return repository.findBySearchTerm(term, maximum, returnWithoutTerm);
	}

	@Override
	public List<ISource> search(String term) {
		return findBySearchTerm(term, 10, true);
	}

	@Nullable
	@Override
	protected SearchIndexEntity prepareIndexEntity(ISource entity) {
		SearchIndexEntity idxEntity = new SearchIndexEntity(entity.getId());
		idxEntity.title = entity.getShortTitle();
		String subTitles = entity.getCitation();
		if (subTitles == null || subTitles.isEmpty()) subTitles = entity.getTitle();
		if (subTitles == null || subTitles.isEmpty()) subTitles = entity.getAuthor();
		idxEntity.subTitles = subTitles;
		idxEntity.content = entity.getDescription();
		idxEntity.contentMarkup = entity.getDescriptionMarkup();
		idxEntity.weight = 1f; // not so important

		// get tag ids and add them to entity
		TagRepository tagRepository = repositoryFactory.produceRepository(TagRepository.class);
		if (tagRepository != null)
			idxEntity.tagIds = tagRepository.findTagIdsConnectedToModel(entity, false);

		return idxEntity;
	}

	/**
	 * Find entities by title
	 * @param ref reference title
	 * @return entity or null
	 */
	public ISource findByRef(String ref) {
		return repository.findByRef(ref);
	}

	/**
	 * Find entities by title
	 * @param title short title
	 * @return entity or null
	 */
	public List<ISource> findByTitle(String title) {
		return repository.findByTitle(title);
	}
}
