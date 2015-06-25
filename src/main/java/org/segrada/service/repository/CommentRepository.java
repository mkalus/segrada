package org.segrada.service.repository;

import org.segrada.model.prototype.IComment;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.prototype.CRUDRepository;

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
 * Comment Repository
 */
public interface CommentRepository extends CRUDRepository<IComment> {
	/**
	 * find comments by reference id
	 * @param id of entity referencing comments
	 * @return list of comments
	 */
	List<IComment> findByReference(String id);

	/**
	 * gets connected entities of a single comment
	 * @param id of comment
	 * @return list of entities that reference comment
	 */
	List<SegradaEntity> findByComment(String id);


	/**
	 * Create new comment connection (only once)
	 * @param comment referencing
	 * @param entity referenced
	 */
	void connectCommentToEntity(IComment comment, SegradaAnnotatedEntity entity);

	/**
	 * Remove existing comment connection
	 * @param comment referencing
	 * @param entity referenced
	 */
	void removeCommentFromEntity(IComment comment, SegradaAnnotatedEntity entity);

	/**
	 * Checks for comment connection
	 * @param comment referencing
	 * @param entity referenced
	 */
	boolean isCommentOf(IComment comment, SegradaAnnotatedEntity entity);

	/**
	 * @param comment to check
	 * @return true if comment has any connections
	 */
	boolean hasConnections(IComment comment);
}
