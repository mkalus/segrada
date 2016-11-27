package org.segrada.service.base;

import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.prototype.CRUDRepository;

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
 * Abstract CRUD service
 */
public abstract class AbstractRepositoryService<T extends SegradaEntity, E extends CRUDRepository<T>> implements SegradaService<T> {
	/**
	 * reference to factory
	 */
	protected final RepositoryFactory repositoryFactory;

	/**
	 * reference to repository
	 */
	protected final E repository;

	/**
	 * Constructor
	 */
	@SuppressWarnings("unchecked")
	public AbstractRepositoryService(RepositoryFactory repositoryFactory, Class clazz) {
		this.repositoryFactory = repositoryFactory;
		this.repository = (E) repositoryFactory.produceRepository(clazz);
	}

	@Override
	public T findById(String id) {
		return repository.find(id);
	}

	@Override
	public boolean save(T entity) {
		return repository.save(entity);
	}

	@Override
	public boolean delete(T entity) {
		return repository.delete(entity);
	}

	@Override
	public Iterable<T> findAll() {
		return repository.findAll();
	}

	@Override
	public List<T> findNextEntriesFrom(String uid, int number) {
		return repository.findNextEntriesFrom(uid, number);
	}

	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public String convertUidToId(String uid) {
		return repository.convertUidToId(uid);
	}
}
