package org.segrada.service.repository;

import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaTaggable;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.repository.prototype.PaginatingRepositoryOrService;
import org.segrada.service.repository.prototype.SearchTermRepository;

import javax.annotation.Nullable;
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
 * Tag Repository
 */
public interface TagRepository extends CRUDRepository<ITag>, SearchTermRepository<ITag>, PaginatingRepositoryOrService<ITag> {
	/**
	 * Find entity by title
	 * @param title login name
	 * @param useSlug true if slug title shall be used instead of normal title
	 * @return entity or null
	 */
	ITag findByTitle(String title, boolean useSlug);

	/**
	 * create new tags from those titles that do not exist
	 * @param titles tag titles list
	 * @return newly created tags
	 */
	List<ITag> createNewTagsByTitles(String[] titles);

	/**
	 * Find tags by tag list
	 * @param titles tag titles list
	 * @return list of tags (non-existing ones are skipped)
	 */
	List<ITag> findTagsByTitles(String[] titles);

	/**
	 * retrieve list of ids for tag names
	 * @param titles tag titles list
	 * @return array (non-existing entries are skipped)
	 */
	String[] findTagIdsByTitles(String[] titles);

	/**
	 * retrieve list of titles for tag names
	 * @param ids tag id list
	 * @return array (non-existing entries are skipped)
	 */
	String[] findTagTitlesByIds(String[] ids);

	/**
	 * get subordinate ids by parent
	 * @param id of parent node
	 * @return array of ids including parent
	 */
	String[] findTagIdsByParent(String id);

	/**
	 * find entities connected to tag
	 * @param id of parent
	 * @param traverse traverse subtree?
	 * @param classes classes to include (null = all classes)
	 * @return list of taggable entities
	 */
	List<SegradaTaggable> findByTag(String id, boolean traverse, @Nullable String[] classes);

	/**
	 * find tag titles connected to a certain entity
	 * @param entity instance
	 * @param onlyDirect set true if only directly connected tags should be returned, otherwise whole tree of tags
	 * @return array of tag titles
	 */
	String[] findTagTitlesConnectedToModel(SegradaTaggable entity, boolean onlyDirect);

	/**
	 * find tag ids connected to a certain entity
	 * @param entity instance
	 * @param onlyDirect set true if only directly connected tags should be returned, otherwise whole tree of tags
	 * @return array of tag ids
	 */
	String[] findTagIdsConnectedToModel(SegradaTaggable entity, boolean onlyDirect);

	/**
	 * get subordinate ids by parent tag (by title)
	 * @param title of parent node
	 * @return array of ids including parent
	 */
	String[] findTagIdsByParentTitle(String title);

	/**
	 * Connect two tags
	 * @param parent node
	 * @param child node
	 */
	void connectTag(ITag parent, SegradaTaggable child);

	/**
	 * Remove existing tag connection
	 * @param tagId id of tag
	 * @param child node that the tag is connected to
	 */
	void removeTag(String tagId, SegradaTaggable child);

	/**
	 * Remove existing tag connection
	 * @param tagId id of tag
	 * @param childId node id that the tag is connected to
	 */
	void removeTag(String tagId, String childId);

	/**
	 * Checks whether node is a parent of possibleChild
	 * @param node parent node
	 * @param possibleChild possible child node
	 * @return true if node is parent of possibleChild
	 */
	boolean isParentOf(ITag node, SegradaTaggable possibleChild);

	/**
	 * Checks whether node is a child of possibleParent
	 * @param node child node
	 * @param possibleParent possible parent node
	 * @return true if node is child of possibleParent
	 */
	boolean isChildOf(SegradaTaggable node, ITag possibleParent);

	/**
	 * Checks if nodeUid is tagged by tagUid
	 * @param tagUid parent node uid
	 * @param nodeUid possible child node uid
	 * @return true if tag is connected to node
	 */
	boolean isTagConnectedTo(String tagUid, String nodeUid);
}
