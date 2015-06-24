package org.segrada.service.repository;

import org.segrada.model.prototype.IComment;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.model.util.IdModelTuple;
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
	 * connect a comment with an entity
	 * @param comment to connect to
	 * @param entity to connect with
	 * @return true if connection did not fail
	 */
	boolean connectCommentWith(IComment comment, SegradaAnnotatedEntity entity);

	/**
	 * connect a comment with an entity
	 * @param comment to connect to
	 * @param idModelTuple to connect with
	 * @return true if connection did not fail
	 */
	boolean connectCommentWith(IComment comment, IdModelTuple idModelTuple);

	/**
	 * delete a comment connection
	 * @param comment connected comment
	 * @param entity which is connected to comment
	 * @return true if deletion did not fail
	 */
	boolean deleteCommentConnection(IComment comment, SegradaAnnotatedEntity entity);

	/**
	 * delete a comment connection
	 * @param comment connected comment
	 * @param idModelTuple which is connected to comment
	 * @return true if deletion did not fail
	 */
	boolean deleteCommentConnection(IComment comment, IdModelTuple idModelTuple);

	/**
	 * gets connected ids/models of a single comment
	 * @param comment to check
	 * @return list of entity ids/models that reference comment
	 */
	List<IdModelTuple> getConnectedIdModelTuplesOf(IComment comment);

	/**
	 * @param comment to check
	 * @return true if comment has any connections
	 */
	boolean hasConnections(IComment comment);
}
