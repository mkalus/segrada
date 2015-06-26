package org.segrada.service.base;

import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.repository.prototype.SegradaRepository;

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
abstract public class AbstractRepositoryService<BEAN extends SegradaEntity, REPOSITORY extends CRUDRepository<BEAN>> implements SegradaService<BEAN> {
	/**
	 * reference to factory
	 */
	protected final RepositoryFactory repositoryFactory;

	/**
	 * reference to repository
	 */
	protected final REPOSITORY repository;

	/**
	 * Constructor
	 */
	@SuppressWarnings("unchecked")
	public AbstractRepositoryService(RepositoryFactory repositoryFactory, Class clazz) {
		this.repositoryFactory = repositoryFactory;
		this.repository = (REPOSITORY) repositoryFactory.produceRepository(clazz);
	}

	@Override
	public BEAN findById(String id) {
		return repository.find(id);
	}

	@Override
	public boolean save(BEAN entity) {
		return repository.save(entity);
	}

	@Override
	public boolean delete(BEAN entity) {
		return repository.delete(entity);
	}

	@Override
	public List<BEAN> findAll() {
		return repository.findAll();
	}

	@Override
	public long count() {
		return repository.count();
	}
}
