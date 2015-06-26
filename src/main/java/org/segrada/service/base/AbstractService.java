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
 * Abstract service
 */
abstract public class AbstractService<BEAN extends SegradaEntity> {
	/**
	 * Create a new instance of BEAN
	 * @return new instance
	 */
	abstract public BEAN createNewInstance();

	/**
	 * get class reference of model class
	 * @return class
	 */
	abstract public Class<BEAN> getModelClass();

	abstract public BEAN findById(String id);

	abstract public boolean save(BEAN entity);

	abstract public boolean delete(BEAN entity);

	abstract public List<BEAN> findAll();

	abstract public long count();
}
