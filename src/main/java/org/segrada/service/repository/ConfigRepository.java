package org.segrada.service.repository;

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
 * Config Repository - just a simple key/value store
 */
public interface ConfigRepository extends SegradaRepository {
	/**
	 * get value by key
	 * @param key to find value in
	 * @return value or null
	 */
	String getValue(String key);

	/**
	 * check key existence
	 * @param key to find
	 * @return true if key exists
	 */
	boolean hasValue(String key);

	/**
	 * update/set value
	 * @param key to set
	 * @param value to set
	 */
	void setValue(String key, String value);

	/**
	 * delete value
	 * @param key to delete
	 */
	void deleteValue(String key);
}
