package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.RelationType;
import org.segrada.model.prototype.IRelationType;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.base.SearchTermService;
import org.segrada.service.repository.RelationTypeRepository;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.prototype.PaginatingRepositoryOrService;
import org.segrada.service.util.PaginationInfo;

import java.util.List;
import java.util.Map;

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
 * Relation type service
 */
public class RelationTypeService extends AbstractRepositoryService<IRelationType, RelationTypeRepository> implements SearchTermService<IRelationType>, PaginatingRepositoryOrService<IRelationType> {
	/**
	 * Constructor
	 */
	@Inject
	public RelationTypeService(RepositoryFactory repositoryFactory) {
		super(repositoryFactory, RelationTypeRepository.class);
	}

	@Override
	public IRelationType createNewInstance() {
		return new RelationType();
	}

	@Override
	public Class<IRelationType> getModelClass() {
		return IRelationType.class;
	}

	/**
	 * Find entities by search term
	 * @param term search term (or empty)
	 * @param maximum maximum hits to return
	 * @param returnWithoutTerm true if you want to return top hits if no search term is supplied (otherwise empty)
	 * @return list of entities
	 */
	public List<IRelationType> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
		return repository.findBySearchTerm(term, maximum, returnWithoutTerm);
	}

	@Override
	public List<IRelationType> search(String term) {
		return findBySearchTerm(term, 10, true);
	}

	@Override
	public PaginationInfo<IRelationType> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		return repository.paginate(page, entriesPerPage, filters);
	}
}
