package org.segrada.service.repository;

import org.segrada.model.prototype.IFile;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.repository.prototype.PaginatingRepository;
import org.segrada.service.repository.prototype.SearchTermRepository;

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
 * File Repository
 */
public interface FileRepository extends CRUDRepository<IFile>, SearchTermRepository<IFile>, PaginatingRepository<IFile> {
	/**
	 * Find entities by title or file name
	 * @param titleOrFilename title or file name
	 * @return entity or null
	 */
	List<IFile> findByTitleOrFilename(String titleOrFilename);

	/**
	 * find files referencing entity with id
	 * @param id of entity referenced
	 * @return list of files
	 */
	List<IFile> findByReference(String id);

	/**
	 * find entities referencing file with id
	 * @param id of file referencing
	 * @return list of entities referenced by file
	 */
	List<SegradaEntity> findByFile(String id);

	/**
	 * Create new file connection (only once)
	 * @param file referencing
	 * @param entity referenced
	 */
	void connectFileToEntity(IFile file, SegradaAnnotatedEntity entity);

	/**
	 * Remove existing file connection
	 * @param file referencing
	 * @param entity referenced
	 */
	void removeFileFromEntity(IFile file, SegradaAnnotatedEntity entity);

	/**
	 * Checks for file connection
	 * @param file referencing
	 * @param entity referenced
	 */
	boolean isFileOf(IFile file, SegradaAnnotatedEntity entity);
}