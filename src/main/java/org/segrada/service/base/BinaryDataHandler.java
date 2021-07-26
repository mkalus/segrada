package org.segrada.service.base;

import org.segrada.model.prototype.SegradaEntity;

import java.io.InputStream;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
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
 * Service handling filess
 */
public interface BinaryDataHandler<T extends SegradaEntity> {
	/**
	 * move/map/save binary data of entity to binary data service
	 *
	 * will remove old data from service if needed
	 *
	 * @param entity containing binary data
	 */
	void saveBinaryDataToService(T entity);

	/**
	 * delete binary data from service
	 *
	 * @param entity containing binary data
	 */
	void removeBinaryDataFromService(T entity);

	/**
	 * return data as stream
	 * @param entity containing binary data
	 * @return input stream for data
	 */
	InputStream getBinaryDataAsStream(T entity);

	/**
	 * return data as stream
	 * @param fileIdentifier referencing binary data
	 * @return input stream for data
	 */
	InputStream getBinaryDataAsStream(String fileIdentifier);
}
