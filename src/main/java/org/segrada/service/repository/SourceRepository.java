package org.segrada.service.repository;

import org.segrada.model.prototype.ISource;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.repository.prototype.PaginatingRepositoryOrService;
import org.segrada.service.repository.prototype.SearchTermRepository;

import java.util.List;

/**
 * Copyright 2015-2019 Maximilian Kalus [segrada@auxnet.de]
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
 * Source Repository
 */
public interface SourceRepository extends CRUDRepository<ISource>, SearchTermRepository<ISource>, PaginatingRepositoryOrService<ISource> {
	/**
	 * Find entities by title
	 * @param ref reference title
	 * @return entity or null
	 */
	ISource findByRef(String ref);

	/**
	 * Find entities by title
	 * @param title short title
	 * @return entity or null
	 */
	List<ISource> findByTitle(String title);
}
