package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.Node;
import org.segrada.model.prototype.INode;
import org.segrada.search.SearchEngine;
import org.segrada.service.base.AbstractFullTextService;
import org.segrada.service.base.SearchTermService;
import org.segrada.service.repository.NodeRepository;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.prototype.PaginatingRepositoryOrService;
import org.segrada.service.util.PaginationInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

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
 * Node service
 */
public class NodeService extends AbstractFullTextService<INode, NodeRepository> implements SearchTermService<INode>, PaginatingRepositoryOrService<INode> {
	/**
	 * Constructor
	 */
	@Inject
	public NodeService(RepositoryFactory repositoryFactory, SearchEngine searchEngine) {
		super(repositoryFactory, NodeRepository.class, searchEngine);
	}

	@Override
	public INode createNewInstance() {
		return new Node();
	}

	@Override
	public Class<INode> getModelClass() {
		return INode.class;
	}

	/**
	 * Find entities by search term
	 * @param term search term (or empty)
	 * @param maximum maximum hits to return
	 * @param returnWithoutTerm true if you want to return top hits if no search term is supplied (otherwise empty)
	 * @return list of entities
	 */
	public List<INode> findBySearchTerm(String term, int maximum, boolean returnWithoutTerm) {
		return repository.findBySearchTerm(term, maximum, returnWithoutTerm);
	}

	/**
	 * find by search terms, but also contain by tags (used in reference search)
	 * @param term to search for
	 * @param maximum to show
	 * @param returnWithoutTerm show list even without search
	 * @param tagIds list of tag ids to contain search in
	 * @return list of entities
	 */
	public List<INode> findBySearchTermAndTags(@Nullable String term, int maximum, boolean returnWithoutTerm, @Nullable String[] tagIds) {
		return repository.findBySearchTermAndTags(term, maximum, returnWithoutTerm, tagIds);
	}

	@Override
	public List<INode> search(String term) {
		return findBySearchTerm(term, 10, true);
	}

	@Nullable
	@Override
	protected SearchIndexEntity prepareIndexEntity(INode entity) {
		SearchIndexEntity idxEntity = new SearchIndexEntity(entity.getUid());
		idxEntity.title = entity.getTitle();
		idxEntity.subTitles = entity.getAlternativeTitles();
		idxEntity.content = entity.getDescription();
		idxEntity.contentMarkup = entity.getDescriptionMarkup();
		idxEntity.weight = 1.1f;
		idxEntity.color = entity.getColor();
		if (entity.getPictogram() != null)
			idxEntity.iconFileIdentifier = entity.getPictogram().getFileIdentifier();

		// get tag ids and add them to entity
		TagRepository tagRepository = repositoryFactory.produceRepository(TagRepository.class);
		if (tagRepository != null)
			idxEntity.tagIds = tagRepository.findTagIdsConnectedToModel(entity, false);

		return idxEntity;
	}

	@Override
	public PaginationInfo<INode> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		return repository.paginate(page, entriesPerPage, filters);
	}
}
