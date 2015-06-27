package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.File;
import org.segrada.model.prototype.IFile;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.rendering.markup.MarkupFilterFactory;
import org.segrada.search.SearchEngine;
import org.segrada.service.base.AbstractFullTextService;
import org.segrada.service.base.BinaryDataHandler;
import org.segrada.service.base.SearchTermService;
import org.segrada.service.binarydata.BinaryDataService;
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
public class FileService extends AbstractFullTextService<IFile, FileRepository> implements SearchTermService<IFile>, BinaryDataHandler<IFile> {
	/**
	 * reference to binary data service
	 */
	private final BinaryDataService binaryDataService;

	/**
	 * Constructor
	 */
	@Inject
	public FileService(RepositoryFactory repositoryFactory, SearchEngine searchEngine, BinaryDataService binaryDataService) {
		super(repositoryFactory, FileRepository.class, searchEngine);

		this.binaryDataService = binaryDataService;
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

	/**
	 * Find entities by title or file name
	 * @param titleOrFilename title or file name
	 * @return entity or null
	 */
	public List<IFile> findByTitleOrFilename(String titleOrFilename) {
		return repository.findByTitleOrFilename(titleOrFilename);
	}

	/**
	 * find files referencing entity with id
	 * @param id of entity referenced
	 * @return list of files
	 */
	public List<IFile> findByReference(String id) {
		return repository.findByReference(id);
	}

	/**
	 * find entities referencing file with id
	 * @param id of file referencing
	 * @return list of entities referenced by file
	 */
	public List<SegradaEntity> findByFile(String id) {
		return repository.findByFile(id);
	}

	/**
	 * Create new file connection (only once)
	 * @param file referencing
	 * @param entity referenced
	 */
	public void connectFileToEntity(IFile file, SegradaAnnotatedEntity entity) {
		repository.connectFileToEntity(file, entity);
	}

	/**
	 * Remove existing file connection
	 * @param file referencing
	 * @param entity referenced
	 */
	public void removeFileFromEntity(IFile file, SegradaAnnotatedEntity entity) {
		repository.removeFileFromEntity(file, entity);
	}

	@Override
	public void saveBinaryDataToService(IFile entity) {
		// cast to file?
		if (!(entity instanceof  File)) return; // sanity check
		File file = (File) entity;

		// nothing to save
		if (file.getData() == null) {
			return;
		}

		// save and/or replace data
		String identifier = binaryDataService.saveNewReference(entity, entity.getFilename(), entity.getMimeType(),
				file.getData(), entity.getFileIdentifier());

		// issue identifier, remove data
		if (identifier != null) {
			entity.setFileIdentifier(identifier);
			file.setData(null); // reset data
		}
	}

	@Override
	public void removeBinaryDataFromService(IFile entity) {
		binaryDataService.removeReference(entity.getFileIdentifier());
	}

	@Override
	public boolean save(IFile entity) {
		// new entity?
		boolean newEntity = entity.getId()==null;

		// map data to binary service
		saveBinaryDataToService(entity);

		// save to db
		if (super.save(entity)) {
			// update back reference
			if (newEntity) binaryDataService.updateReferenceId(entity.getFileIdentifier(), entity);
			return true;
		}

		// error while saving: delete file reference
		binaryDataService.removeReference(entity.getFileIdentifier());
		return false;
	}

	@Override
	public boolean delete(IFile entity) {
		removeBinaryDataFromService(entity);
		return super.delete(entity);
	}
}
