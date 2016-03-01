package org.segrada.service.repository;

import org.segrada.model.prototype.ISourceReference;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.util.PaginationInfo;

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
 * Source Reference Repository
 */
public interface SourceReferenceRepository extends CRUDRepository<ISourceReference> {
	/**
	 * Find entities by source id
	 * @param id source id
	 * @param page page
	 * @param entriesPerPage entries per page
	 * @param referencedClass class to limit to (or null)
	 * @return list of entities or null
	 */
	PaginationInfo<ISourceReference> findBySource(String id, int page, int entriesPerPage, String referencedClass);

	/**
	 * Find entities by reference id
	 * @param id reference id
	 * @param page page
	 * @param entriesPerPage entries per page
	 * @param referencedClass class to limit to (or null)
	 * @return list of entities or null
	 */
	PaginationInfo<ISourceReference> findByReference(String id, int page, int entriesPerPage, String referencedClass);
}
