package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.Tag;
import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaTaggable;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.base.SearchTermService;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.factory.RepositoryFactory;
import org.segrada.service.repository.prototype.PaginatingRepositoryOrService;
import org.segrada.service.util.PaginationInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

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
 * Tag service
 */
public class TagService extends AbstractRepositoryService<ITag, TagRepository> implements SearchTermService<ITag>, PaginatingRepositoryOrService<ITag> {
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

	/**
	 * Find entity by title
	 * @param title login name
	 * @param fuzzySearch true if fuzzy search for title shall be tried (e.g. sluggified)
	 * @return entity or null
	 */
	public ITag findByTitle(String title, boolean fuzzySearch) {
		return repository.findByTitle(title, fuzzySearch);
	}

	/**
	 * Find tags by tag list
	 * @param titles tag titles list
	 * @return list of tags (non-existing ones are skipped)
	 */
	public List<ITag> findTagsByTitles(String[] titles) {
		return repository.findTagsByTitles(titles);
	}

	/**
	 * find entities connected to tag
	 * @param id of parent
	 * @param traverse traverse subtree?
	 * @param classes classes to include (null = all classes)
	 * @return list of taggable entities
	 */
	public List<SegradaTaggable> findByTag(String id, boolean traverse, @Nullable String[] classes) {
		return repository.findByTag(id, traverse, classes);
	}

	/**
	 * find tag titles connected to a certain entity
	 * @param entity instance
	 * @param onlyDirect set true if only directly connected tags should be returned, otherwise whole tree of tags
	 * @return array of tag titles
	 */
	public String[] findTagTitlesConnectedToModel(SegradaTaggable entity, boolean onlyDirect) {
		return repository.findTagTitlesConnectedToModel(entity, onlyDirect);
	}

	/**
	 * Connect two tags
	 * @param parent node
	 * @param child node
	 */
	public void connectTag(ITag parent, SegradaTaggable child) {
		repository.connectTag(parent, child);
	}

	/**
	 * Remove existing tag connection
	 * @param tagId id of tag
	 * @param child node that the tag is connected to
	 */
	public void removeTag(String tagId, SegradaTaggable child) {
		repository.removeTag(tagId, child);
	}

	/**
	 * Remove existing tag connection
	 * @param tagId id of tag
	 * @param childId id of child
	 */
	public void removeTag(String tagId, String childId) {
		repository.removeTag(tagId, childId);
	}

	/**
	 * Checks if nodeUid is tagged by tagUid
	 * @param tagUid parent node uid
	 * @param nodeUid possible child node uid
	 * @return true if tag is connected to node
	 */
	public boolean isTagConnectedTo(String tagUid, String nodeUid) {
		return repository.isTagConnectedTo(tagUid, nodeUid);
	}

	@Override
	public PaginationInfo<ITag> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		return repository.paginate(page, entriesPerPage, filters);
	}
}
