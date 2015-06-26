package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.File;
import org.segrada.model.prototype.IFile;
import org.segrada.rendering.markup.MarkupFilterFactory;
import org.segrada.search.SearchEngine;
import org.segrada.service.base.AbstractFullTextService;
import org.segrada.service.base.SearchTermService;
import org.segrada.service.repository.FileRepository;
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
 * File service
 */
public class FileService extends AbstractFullTextService<IFile, FileRepository> implements SearchTermService<IFile> {
	/**
	 * Constructor
	 */
	@Inject
	public FileService(RepositoryFactory repositoryFactory, SearchEngine searchEngine) {
		super(repositoryFactory, FileRepository.class, searchEngine);
	}

	@Override
	public File createNewInstance() {
		return new File();
	}

	@Override
	public Class<IFile> getModelClass() {
		return IFile.class;
	}

	/**
	 * Find entities by search term
	 * @param term search term (or empty)
	 * @param maximum maximum hits to return
	 * @param returnWithoutTerm true if you want to return top hits if no search term is supplied (otherwise empty)
	 * @return list of entities
	 */
	public List<IFile> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
		return repository.findBySearchTerm(term, maximum, returnWithoutTerm);
	}

	@Override
	public List<IFile> search(String term) {
		return findBySearchTerm(term, 10, true);
	}

	@Nullable
	@Override
	protected SearchIndexEntity prepareIndexEntity(IFile entity) {
		SearchIndexEntity idxEntity = new SearchIndexEntity(entity.getId());
		idxEntity.title = entity.getTitle();
		idxEntity.subTitles = entity.getFilename();

		// resolve description text
		String description = entity.getDescription();
		if (description == null) description = "";
		// to plain text
		description = MarkupFilterFactory.produce(entity.getDescriptionMarkup()).toPlain(description);
		// add full text?
		if (entity.getIndexFullText() && entity.getFullText() != null && !entity.getFullText().isEmpty())
			description += " ".concat(entity.getFullText());

		idxEntity.content = description;
		idxEntity.contentMarkup = "plain";
		idxEntity.weight = 5f; // relatively important

		// get tag ids and add them to entity
		TagRepository tagRepository = repositoryFactory.produceRepository(TagRepository.class);
		if (tagRepository != null)
			idxEntity.tagIds = tagRepository.findTagIdsConnectedToModel(entity, false);

		return idxEntity;
	}
}
