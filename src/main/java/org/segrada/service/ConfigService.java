package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.service.repository.ConfigRepository;
import org.segrada.service.repository.factory.RepositoryFactory;

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
 * Config service
 */
public class ConfigService {
	/**
	 * reference to factory
	 */
	protected final RepositoryFactory repositoryFactory;

	/**
	 * reference to repository
	 */
	protected final ConfigRepository repository;

	/**
	 * Constructor
	 */
	@Inject
	public ConfigService(RepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
		this.repository = repositoryFactory.produceRepository(ConfigRepository.class);
	}

	/**
	 * get value by key
	 * @param key to find value in
	 * @return value or null
	 */
	public String getValue(String key) {
		return repository.getValue(key);
	}

	/**
	 * check key existence
	 * @param key to find
	 * @return true if key exists
	 */
	public boolean hasValue(String key) {
		return repository.hasValue(key);
	}

	/**
	 * update/set value
	 * @param key to set
	 * @param value to set
	 */
	public void setValue(String key, String value) {
		repository.setValue(key, value);
	}

	/**
	 * delete value
	 * @param key to delete
	 */
	public void deleteValue(String key) {
		repository.deleteValue(key);
	}
}
