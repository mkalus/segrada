package org.segrada.service.repository.factory;

import org.segrada.service.repository.prototype.SegradaRepository;

import javax.annotation.Nullable;

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
 * Repository to dynamically create repositories during runtime
 */
public interface RepositoryFactory {
	/**
	 * produce repository by class name, can be base interface class
	 * @param clazz class or interface class
	 * @param <T> class of type SegradaRepository
	 * @return repository or null
	 */
	@Nullable <T extends SegradaRepository> T produceRepository(Class<T> clazz);

	/**
	 * produce repository by model nyme
	 * @param modelName model name
	 * @param <T> class of type SegradaRepository
	 * @return repository or null
	 */
	@Nullable <T extends SegradaRepository> T produceRepository(String modelName);
}
