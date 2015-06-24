package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.DynamicRepository;
import org.segrada.service.repository.orientdb.base.AbstractOrientDbRepository;

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
 * OrientDb Repository to dynamically load resources from other repositories
 */
public class OrientDbDynamicRepository implements DynamicRepository {
	/**
	 * map to repositories
	 */
	private final Map<String, AbstractOrientDbRepository> repositoryMap;

	/**
	 * Constructor
	 */
	@Inject
	public OrientDbDynamicRepository(Map<String, AbstractOrientDbRepository> repositoryMap) {
		this.repositoryMap = repositoryMap;
	}

	/**
	 * convert field in document to Segrada entity using dynamically allocated repositories
	 * @param document document to be converted
	 * @return converted entity
	 */
	public SegradaEntity convertToEntity(ODocument document) {
		AbstractOrientDbRepository repository = getRepository(document.getClassName());
		return repository==null?null:repository.convertToEntity(document);
	}

	@Override
	public SegradaEntity find(String id, String className) {
		AbstractOrientDbRepository repository = getRepository(className);
		return repository==null?null:repository.find(id);
	}

	@Override
	public AbstractOrientDbRepository getRepository(String className) {
		if (!repositoryMap.containsKey(className)) return null; // or throw exception? TODO
		return repositoryMap.get(className);
	}
}
