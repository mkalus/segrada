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
import org.segrada.service.repository.prototype.PaginatingRepositoryOrService;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.TextExtractor;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * File service
 */
public class FileService extends AbstractFullTextService<IFile, FileRepository> implements SearchTermService<IFile>, BinaryDataHandler<IFile>, PaginatingRepositoryOrService<IFile> {
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
		SearchIndexEntity idxEntity = new SearchIndexEntity(entity.getUid());
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
		if (file.getData() == null) return;

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
	public InputStream getBinaryDataAsStream(IFile entity) {
		if (entity == null || entity.getFileIdentifier() == null || entity.getFileIdentifier().isEmpty())
			return null;

		try {
			return binaryDataService.getBinaryDataAsStream(entity.getFileIdentifier());
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public boolean save(IFile entity) {
		// new entity?
		boolean newEntity = entity.getId()==null;

		// enrich with full text, if applicable
		enrichWithFullText(entity);

		// file should be saved in database?
		if (entity.getContainFile()) {
			// map data to binary service
			saveBinaryDataToService(entity);
		} else {
			// remove old identifier, if it exists
			removeBinaryDataFromService(entity);

			// reset file identifier
			entity.setFileIdentifier(null);

			// do not replace metadata in any case
			newEntity = false;
		}

		// save to db
		if (super.save(entity)) {
			// update back reference
			if (newEntity) binaryDataService.updateReferenceId(entity.getFileIdentifier(), entity);
			return true;
		}

		// error while saving: delete file reference
		removeBinaryDataFromService(entity);
		return false;
	}

	@Override
	public boolean delete(IFile entity) {
		removeBinaryDataFromService(entity);
		return super.delete(entity);
	}

	/**
	 * extract and enrich file entity with full text, if needed
	 * @param entity to be enriched: must be File entity with data set
	 */
	private void enrichWithFullText(IFile entity) {
		// cast to file?
		if (!(entity instanceof  File)) return; // sanity check
		File file = (File) entity;

		// full text extraction of uploaded file?
		if (entity.getIndexFullText()) {
			byte[] data = file.getData();
			if (data != null && data.length > 0) { // uploaded?
				// extract text
				TextExtractor textExtractor = new TextExtractor();
				entity.setFullText(textExtractor.parseToString(new ByteArrayInputStream(data)));
			} else if (entity.getFullText() == null || entity.getFullText().isEmpty()) {
				// nothing uploaded, but file is present in system and has not been extracted yet
				String fileIdentifier = entity.getFileIdentifier();
				if (fileIdentifier != null && !fileIdentifier.isEmpty()) {
					TextExtractor textExtractor = new TextExtractor();
					try {
						entity.setFullText(textExtractor.parseToString(binaryDataService.getBinaryDataAsStream(fileIdentifier)));
					} catch (IOException e) {
						// fail silently
					}
				}
			}
		} else entity.setFullText(null); // remove full text
	}

	@Override
	public PaginationInfo<IFile> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		return repository.paginate(page, entriesPerPage, filters);
	}
}
