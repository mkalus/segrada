package org.segrada.service.repository;

import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.IRelationType;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.repository.prototype.PaginatingRepository;

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
 * Relation Repository
 */
public interface RelationRepository extends CRUDRepository<IRelation>, PaginatingRepository<IRelation> {
	/**
	 * find by related entity
	 * @param node connected entity
	 * @return list of relations connected to node (in or out)
	 */
	List<IRelation> findByRelation(INode node);

	/**
	 * find by relation type
	 * @param relationType of relation
	 * @return list of relations of a certain type
	 */
	List<IRelation> findByRelationType(IRelationType relationType);

	/**
	 * delete relations by node connection
	 * @param node connected entity
	 */
	void deleteByRelation(INode node);

	/**
	 * delete relations by type
	 * @param relationType of relation
	 */
	void deleteByRelationType(IRelationType relationType);
}
