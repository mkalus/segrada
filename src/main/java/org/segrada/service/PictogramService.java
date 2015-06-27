package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.Pictogram;
import org.segrada.model.prototype.IPictogram;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.repository.PictogramRepository;
import org.segrada.service.repository.factory.RepositoryFactory;

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
 * Pictogram service
 */
public class PictogramService extends AbstractRepositoryService<IPictogram, PictogramRepository> {
	/**
	 * Constructor
	 */
	@Inject
	public PictogramService(RepositoryFactory repositoryFactory) {
		super(repositoryFactory, PictogramRepository.class);
	}

	@Override
	public IPictogram createNewInstance() {
		return new Pictogram();
	}

	@Override
	public Class<IPictogram> getModelClass() {
		return IPictogram.class;
	}

	/**
	 * Find entity by title
	 * @param title login name
	 * @return entity or null
	 */
	public IPictogram findByTitle(String title) {
		return repository.findByTitle(title);
	}
}
