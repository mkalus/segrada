package org.segrada.service.base;

import org.segrada.model.prototype.SegradaEntity;

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
 * Base service class
 */
public interface SegradaService<BEAN extends SegradaEntity> {
	/**
	 * Create a new instance of BEAN
	 * @return new instance
	 */
	BEAN createNewInstance();

	/**
	 * get class reference of model class
	 * @return class
	 */
	Class<BEAN> getModelClass();

	BEAN findById(String id);

	boolean save(BEAN entity);

	boolean delete(BEAN entity);

	List<BEAN> findAll();

	long count();

	/**
	 * convert generic uid to specific repository id
	 * @param uid generic id
	 * @return id or null (if not matched)
	 */
	String convertUidToId(String uid);
}
