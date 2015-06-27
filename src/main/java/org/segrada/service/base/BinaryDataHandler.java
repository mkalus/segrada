package org.segrada.service.base;

import org.segrada.model.prototype.SegradaEntity;

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
 * Service handling filess
 */
public interface BinaryDataHandler<BEAN extends SegradaEntity> {
	/**
	 * move/map/save binary data of entity to binary data service
	 *
	 * will remove old data from service if needed
	 *
	 * @param entity containing binary data
	 */
	void saveBinaryDataToService(BEAN entity);

	/**
	 * delete binary data from service
	 *
	 * @param entity containing binary data
	 */
	void removeBinaryDataFromService(BEAN entity);
}
