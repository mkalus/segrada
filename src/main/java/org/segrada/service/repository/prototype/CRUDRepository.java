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
 * Base Repository interface for CRUD operations
 */
public interface CRUDRepository<T extends SegradaEntity> extends SegradaRepository {
	/**
	 * Save an entity
	 * @param entity to save
	 * @return true if saved
	 */
	boolean save(T entity);

	/**
	 * Count entities
	 * @return number of entities in db
	 */
	long count();

	/**
	 * Find all entities
	 * @return list of all entities
	 */
	Iterable<T> findAll();

	/**
	 * Find next entries starting from uid (or first if uid is null) up to number entries
	 * @param uid to start with or null for first entry
	 * @param number maximum number of entries
	 * @return list of entries or null if none exist
	 */
	List<T> findNextEntriesFrom(String uid, int number);

	/**
	 * find single instance by id
	 * @param id of entity to find
	 * @return entity or null
	 */
	T find(String id);

	/**
	 * Delete single entity
	 * @param entity to delete
	 * @return true if deleted
	 */
	boolean delete(T entity);

	/**
	 * convert generic uid to specific repository id
	 * @param uid generic uid e.g. 13-9
	 * @return id specific to db, e.g. #13:9
	 */
	String convertUidToId(String uid);
}
