package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.segrada.model.Relation;
import org.segrada.model.prototype.*;
import org.segrada.service.repository.NodeRepository;
import org.segrada.service.repository.RelationRepository;
import org.segrada.service.repository.RelationTypeRepository;
import org.segrada.service.repository.orientdb.base.AbstractCoreOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Nullable;
import java.util.LinkedList;
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
	private static final Logger logger = LoggerFactory.getLogger(OrientDbRelationRepository.class);

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
		Relation relation = new Relation();

		// load relation link
		ODocument relationLink = getRelationLink(document);
		if (relationLink == null) return null; // exact error was logged below

		// load node repository
		NodeRepository nodeRepository = repositoryFactory.produceRepository(OrientDbNodeRepository.class);
		if (nodeRepository == null) {
			logger.error("Could not create NodeRepository while converting relation.");
			return null;
		}

		// get from/to
		relation.setFromEntity(getRelatedEntity(relationLink, "out", nodeRepository));
		relation.setToEntity(getRelatedEntity(relationLink, "in", nodeRepository));

		// set relation type
		ORecordId relationType = document.field("relationType", ORecordId.class);
		if (relationType != null) {
			RelationTypeRepository relationTypeRepository = repositoryFactory.produceRepository(OrientDbRelationTypeRepository.class);
			if (relationTypeRepository != null)
				relation.setRelationType(relationTypeRepository.find(relationType.getIdentity().toString()));
			else logger.error("Could not produce RelationTypeRepository while converting relation.");
		}

		// rest is easy
		relation.setDescription(document.field("description", String.class));
		relation.setDescriptionMarkup(document.field("descriptionMarkup", String.class));

		// populate with data
		populateEntityWithBaseData(document, relation);
		populateEntityWithCreatedModified(document, relation);
		populateEntityWithColored(document, relation);
		populateEntityWithAnnotated(document, relation);
		populateEntityWithCore(document, relation);

		return relation;
	}

	/**
	 * helper to load relation link document
	 * @param document of relation
	 * @return relation link or null
	 */
	private @Nullable ODocument getRelationLink(ODocument document) {
		Object relationLinkO = document.field("relationLink");
		if (relationLinkO == null) {
			logger.error("Could not create relationLink entity while converting relation.");
			return null;
		}
		if (relationLinkO instanceof ODocument) return (ODocument) relationLinkO;
		else if (relationLinkO instanceof ORecordId) {
			return repositoryFactory.getDb().load((ORecordId) relationLinkO);
		} else {
			logger.error("Invalid class type: " + relationLinkO.getClass().getName());
			return null;
		}
	}

	/**
	 * helper to load nodes relation to document
	 * @param document relationLink document/link
	 * @param direction "in" or "out"
	 * @param nodeRepository reference to node repository
	 * @return INode instance or null
	 */
	private @Nullable INode getRelatedEntity(ODocument document, String direction, NodeRepository nodeRepository) {
		Object relationO = document.field(direction, ORecordId.class);
		if (relationO == null) {
			logger.error("Could not create related entity while converting relation with direction " + direction);
			return null;
		}
		String id;
		if (relationO instanceof ORecordId) id = ((ORecordId)relationO).getIdentity().toString();
		else if (relationO instanceof ODocument) id = ((ODocument)relationO).getIdentity().toString();
		else {
			logger.error("Invalid class type: " + relationO.getClass().getName());
			return null;
		}

		return nodeRepository.find(id);
	}

	@Override
	public ODocument convertToDocument(IRelation entity) {
		// not implemented - this is intended!
		throw new NotImplementedException();
	}

	/**
	 * actual helper function - protected since we do not want to create orphan relation nodes
	 * @param entity to be converted
	 * @return converted document
	 */
	protected ODocument reallyConvertToDocument(IRelation entity) {
		ODocument document = createOrLoadDocument(entity);

		// fields to document
		document.field("description", entity.getDescription())
				.field("descriptionMarkup", entity.getDescriptionMarkup());

		if (entity.getRelationType() != null && entity.getRelationType().getId() != null)
			document.field("relationType", new ORecordId(entity.getRelationType().getId()));

		// populate with data
		populateODocumentWithCreatedModified(document, entity);
		populateODocumentWithColored(document, entity);
		populateODocumentWithAnnotated(document, entity);
		populateODocumentWithCore(document, entity);

		// relation (= edge) itself is not created - this is done in save method!
		return document;
	}

	@Override
	public List<IRelation> findByRelation(INode node) {
		List<IRelation> list = new LinkedList<>();

		// no NPEs
		if (node == null) return list;

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Relation where relationLink.in = " + node.getId() + " OR relationLink.out = " + node.getId());
		List<ODocument> result = db.command(query).execute();

		// populate set
		for (ODocument document : result)
			list.add(convertToEntity(document));

		return list;
	}

	@Override
	public List<IRelation> findByRelationType(IRelationType relationType) {
		List<IRelation> list = new LinkedList<>();

		// no NPEs
		if (relationType == null) return list;

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Relation where relationType = " + relationType.getId());
		List<ODocument> result = db.command(query).execute();

		// populate set
		for (ODocument document : result)
			list.add(convertToEntity(document));

		return list;
	}

	@Override
	public void deleteByRelation(INode node) {
		// delete source reference pointing to relation node
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Relation where relationLink.in = " + node.getId() + " OR relationLink.out = " + node.getId());
		List<ODocument> result = db.command(query).execute();

		// remove relation and link
		for (ODocument document : result) {
			ODocument relationLink = getRelationLink(document);
			if (relationLink != null && db.load(relationLink.getIdentity()) != null) relationLink.delete();

			document.delete();
		}
	}

	@Override
	public void deleteByRelationType(IRelationType relationType) {
		// delete source reference pointing to relation type
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Relation where relationType = " + relationType.getId());
		List<ODocument> result = db.command(query).execute();

		// remove relation and link
		for (ODocument document : result) {
			ODocument relationLink = getRelationLink(document);
			if (relationLink != null && db.load(relationLink.getIdentity()) != null) relationLink.delete();

			document.delete();
		}
	}

	@Override
	public PaginationInfo<IRelation> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		//TODO
		return null;
	}
}
