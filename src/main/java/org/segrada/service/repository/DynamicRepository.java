package org.segrada.service.repository;

import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.prototype.SegradaRepository;

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
 * Dynamic Repository
 */
public interface DynamicRepository {
	/**
	 * find entity by id and class name
	 * @param id of document
	 * @param className of document
	 * @return entity or null
	 */
	SegradaEntity find(String id, String className);

	/**
	 * get repository
	 * @param className of repository, e.g. "Tag" or "Node"
	 * @return repository or null
	 */
	SegradaRepository getRepository(String className);
}
