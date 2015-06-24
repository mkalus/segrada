package org.segrada.service.repository;

import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.model.util.IdModelTuple;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.repository.prototype.PaginatingRepository;
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
public interface TagRepository extends CRUDRepository<ITag>, SearchTermRepository<ITag>, PaginatingRepository<ITag> {
	/**
	 * Find entity by title
	 * @param title login name
	 * @return entity or null
	 */
	ITag findByTitle(String title);

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
	 * find tag ids connected to a certain entity
	 * @param id of entity
	 * @param model of entity
	 * @param onlyDirect set true if only directly connected tags should be returned, otherwise whole tree of tags
	 * @return array of tag ids
	 */
	String[] findTagIdsConnectedToModel(String id, String model, boolean onlyDirect);

	/**
	 * find tag ids connected to a certain entity
	 * @param idModelTuple entity represented s
	 * @param onlyDirect set true if only directly connected tags should be returned, otherwise whole tree of tags
	 * @return array of tag ids
	 */
	String[] findTagIdsConnectedToModel(IdModelTuple idModelTuple, boolean onlyDirect);

	/**
	 * find tag ids connected to a certain entity
	 * @param entity instance
	 * @param onlyDirect set true if only directly connected tags should be returned, otherwise whole tree of tags
	 * @return array of tag ids
	 */
	String[] findTagIdsConnectedToModel(SegradaAnnotatedEntity entity, boolean onlyDirect);

	/**
	 * get subordinate ids by parent tag (by title)
	 * @param title of parent node
	 * @return array of ids including parent
	 */
	String[] findTagIdsByParentTitle(String title);

	/**
	 * get tuples for tag
	 * @param id of tag
	 * @param classes to return - if null or empty, all classes including subtags will be returned
	 * @return list of ids and classes connected to this tag
	 */
	List<IdModelTuple> getConnectedIdModelTuplesOf(String id, @Nullable String[] classes);

	/**
	 * get tuples for tag
	 * @param tag root tag
	 * @param classes to return - if null or empty, all classes including subtags will be returned
	 * @return list of ids and classes connected to this tag
	 */
	List<IdModelTuple> getConnectedIdModelTuplesOf(ITag tag, @Nullable String[] classes);

	/**
	 * Connect two tags
	 * @param parent node
	 * @param child node
	 */
	void connectTag(ITag parent, SegradaAnnotatedEntity child);

	/**
	 * Remove existing tag connection
	 * @param tagId id of tag
	 * @param child node that the tag is connected to
	 */
	void removeTag(String tagId, SegradaAnnotatedEntity child);

	/**
	 * Checks whether node is a parent of possibleChild
	 * @param node parent node
	 * @param possibleChild possible child node
	 * @return true if node is parent of possibleChild
	 */
	boolean isParentOf(ITag node, SegradaAnnotatedEntity possibleChild);

	/**
	 * Checks whether node is a child of possibleParent
	 * @param node child node
	 * @param possibleParent possible parent node
	 * @return true if node is child of possibleParent
	 */
	boolean isChildOf(SegradaAnnotatedEntity node, ITag possibleParent);
}
