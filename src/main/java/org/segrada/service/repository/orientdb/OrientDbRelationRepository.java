package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.IRelationType;
import org.segrada.service.repository.RelationRepository;
import org.segrada.service.repository.orientdb.base.AbstractCoreOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;

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
 * OrientDb Relation Repository
 */
public class OrientDbRelationRepository extends AbstractCoreOrientDbRepository<IRelation> implements RelationRepository {
	/**
	 * Constructor
	 */
	@Inject
	public OrientDbRelationRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "Relation";
	}

	@Override
	public IRelation convertToEntity(ODocument document) {
		//TODO
		return null;
	}

	@Override
	public ODocument convertToDocument(IRelation entity) {
		//TODO
		return null;
	}

	@Override
	public List<IRelation> findByRelation(INode node) {
		//TODO
		return null;
	}

	@Override
	public List<IRelation> findByRelationType(IRelationType relationType) {
		//TODO
		return null;
	}

	@Override
	public void deleteByRelation(INode node) {
		//TODO
	}

	@Override
	public void deleteByRelationType(IRelationType relationType) {
		//TODO
	}

	@Override
	public PaginationInfo<IRelation> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		//TODO
		return null;
	}
}
