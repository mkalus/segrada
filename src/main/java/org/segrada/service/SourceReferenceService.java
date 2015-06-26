package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.SourceReference;
import org.segrada.model.prototype.ISourceReference;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.repository.SourceReferenceRepository;
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
 * Source Reference service
 */
public class SourceReferenceService extends AbstractRepositoryService<ISourceReference, SourceReferenceRepository> {
	/**
	 * Constructor
	 */
	@Inject
	public SourceReferenceService(RepositoryFactory repositoryFactory) {
		super(repositoryFactory, SourceReferenceRepository.class);
	}

	@Override
	public ISourceReference createNewInstance() {
		return new SourceReference();
	}

	@Override
	public Class<ISourceReference> getModelClass() {
		return ISourceReference.class;
	}
}
