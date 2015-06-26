package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.Tag;
import org.segrada.model.prototype.ITag;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.base.SearchTermService;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.factory.RepositoryFactory;

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
 * Tag service
 */
public class TagService extends AbstractRepositoryService<ITag, TagRepository> implements SearchTermService<ITag> {
	/**
	 * Constructor
	 */
	@Inject
	public TagService(RepositoryFactory repositoryFactory) {
		super(repositoryFactory, TagRepository.class);
	}

	@Override
	public ITag createNewInstance() {
		return new Tag();
	}

	@Override
	public Class<ITag> getModelClass() {
		return ITag.class;
	}

	/**
	 * Find entities by search term
	 * @param term search term (or empty)
	 * @param maximum maximum hits to return
	 * @param returnWithoutTerm true if you want to return top hits if no search term is supplied (otherwise empty)
	 * @return list of entities
	 */
	public List<ITag> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
		return repository.findBySearchTerm(term, maximum, returnWithoutTerm);
	}

	@Override
	public List<ITag> search(String term) {
		return findBySearchTerm(term, 10, true);
	}
}
