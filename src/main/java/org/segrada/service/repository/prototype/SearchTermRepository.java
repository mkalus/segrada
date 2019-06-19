package org.segrada.service.repository.prototype;

import org.segrada.model.prototype.SegradaEntity;

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
 * Base Repository for search term methods
 */
public interface SearchTermRepository<T extends SegradaEntity> {
	/**
	 * Find entities by search term
	 * @param term search term
	 * @param maximum maximum hits to return
	 * @param returnWithoutTerm true if you want to return list of entries without any search term provided
	 * @return list of entities
	 */
	List<T> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm);
}
