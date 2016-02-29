package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import org.segrada.model.Node;
import org.segrada.model.Relation;
import org.segrada.model.base.AbstractCoreModel;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.IRelationType;
import org.segrada.service.repository.NodeRepository;
import org.segrada.service.repository.RelationRepository;
import org.segrada.service.repository.RelationTypeRepository;
import org.segrada.service.repository.orientdb.base.AbstractCoreOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.FlexibleDateParser;
import org.segrada.util.OrientStringEscape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Nullable;
import java.util.*;

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
	 * keep allowed sorting fields here
	 */
	private static final Set<String> allowedSorts = new HashSet<>(Arrays.asList(new String[]{"minJD", "maxJD"}));

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
		// load relation link
		ODocument relationLink = getRelationLink(document, true);
		if (relationLink == null) {
			// this is an invalid entry: delete if from database
			document.delete(); // this is not elegant, but we can clean such entries on the fly by doing this
			return null; // exact error was logged below
		}

		Relation relation = new Relation();
		// get from/to - slim entities, just title and id
		relation.setFromEntity(getRelatedEntity(relationLink, "out"));
		relation.setToEntity(getRelatedEntity(relationLink, "in"));

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
	 * @param logError log null error?
	 * @return relation link or null
	 */
	private @Nullable ODocument getRelationLink(ODocument document, boolean logError) {
		Object relationLinkO = document.field("relationLink");
		if (relationLinkO == null) {
			if (logError)
				logger.error("Could not create relationLink entity while converting relation.");
			return null;
		}
		if (relationLinkO instanceof ODocument) return (ODocument) relationLinkO;
		else if (relationLinkO instanceof ORecordId) {
			ODocument doc = repositoryFactory.getDb().load((ORecordId) relationLinkO);
			if (doc == null)
				logger.error("Instance vanished from database: " + relationLinkO);
			return doc;
		} else {
			logger.error("Invalid class type: " + relationLinkO.getClass().getName());
			return null;
		}
	}

	/**
	 * helper to load nodes relation to document
	 * @param document relationLink document/link
	 * @param direction "in" or "out"
	 * @return INode instance or null
	 */
	private @Nullable INode getRelatedEntity(ODocument document, String direction) {
		Object nodeO = document.field(direction, ORecordId.class);
		if (nodeO == null) {
			logger.error("Could not create related entity while converting relation with direction " + direction);
			return null;
		}
		if (nodeO instanceof ORecordId) nodeO = repositoryFactory.getDb().load((ORecordId) nodeO);
		if (nodeO == null) {
			logger.error("Invalid record in direction " + direction + ": " + document.toString());
			return null;
		}

		//convert
		ODocument nodeDoc = (ODocument) nodeO;

		// slim node: just set title and id
		INode node = new Node();
		node.setTitle(nodeDoc.field("title"));
		node.setId(nodeDoc.getIdentity().toString());

		return node;

		/*
		old -not performant
		String id;
		if (relationO instanceof OIdentifiable) id = ((ORecordId)relationO).getIdentity().toString();
		else {
			logger.error("Invalid class type: " + relationO.getClass().getName());
			return null;
		}

		return nodeRepository.find(id);*/
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
		List<IRelation> list = new ArrayList<>();

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
		List<IRelation> list = new ArrayList<>();

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
	public boolean save(IRelation entity) {
		try {
			// sanity: we need from and to entity to do this!
			if (entity.getFromEntity() == null || entity.getToEntity() == null) {
				logger.error("From and/or to-entity not set in relation: Not saving!");
				return false;
			}

			initDb();

			// process before saving
			entity = processBeforeSaving(entity);

			// create document - call special method
			ODocument document = reallyConvertToDocument(entity);

			ODocument relationLink = getRelationLink(document, false);
			if (relationLink == null) { // no relation yet => create new one
				// create edge and set it to document
				List<OIdentifiable> edgeList =
						repositoryFactory.getDb().command(new OCommandSQL("create edge IsRelation from " + entity.getFromEntity().getId() + " to " + entity.getToEntity().getId())).execute();
				document.field("relationLink", (ORecordId) edgeList.get(0).getIdentity());

				if (logger.isTraceEnabled())
					logger.trace("Created brank new IsRelation edge: " + edgeList.get(0).toString());
			} else {
				// check whether relation has changed
				String oldFromId = relationLink.field("out", ORecordId.class).toString();
				String oldToId = relationLink.field("in", ORecordId.class).toString();

				String newFromId = entity.getFromEntity().getId();
				String newToId = entity.getToEntity().getId();

				// check equality
				if (!oldFromId.equals(newFromId) || !oldToId.equals(newToId)) {
					// delete old edge and create new one
					relationLink.delete();

					List<OrientEdge> edgeList =
							repositoryFactory.getDb().command(new OCommandSQL("create edge IsRelation from " + entity.getFromEntity().getId() + " to " + entity.getToEntity().getId())).execute();
					document.field("relationLink", (ORecordId) edgeList.get(0).getId());

					if (logger.isTraceEnabled())
						logger.trace("Replace IsRelation edge: " + edgeList.get(0).toString() + " (was: " + relationLink.toString() + ")");
				}
			}

			// save
			ODocument updated = db.save(document);

			// process after saving
			processAfterSaving(updated, entity);

			if (logger.isInfoEnabled())
				logger.info("Saved entity: " + entity.toString());

			return true;
		} catch (Exception e) {
			logger.error("Exception thrown while saving entity.", e);
		}

		return false;
	}

	@Override
	public void deleteByRelation(INode node) {
		// delete source reference pointing to relation node
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Relation where relationLink.in = " + node.getId() + " OR relationLink.out = " + node.getId());
		List<ODocument> result = db.command(query).execute();

		// remove relation and link
		for (ODocument document : result) {
			ODocument relationLink = getRelationLink(document, true);
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
			ODocument relationLink = getRelationLink(document, true);
			if (relationLink != null && db.load(relationLink.getIdentity()) != null) relationLink.delete();

			document.delete();
		}
	}
	
	@Override
	public PaginationInfo<IRelation> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
		// avoid NPEs
		if (filters == null) filters = new HashMap<>();

		// aggregate filters
		List<String> constraints = new ArrayList<>();
		// search term
		if (filters.containsKey("search")) {
			//constraints.add(createSearchTermFullText((String) filters.get("search")));
			//TODO: implement this one day
		}


		// location
		// TODO search locations and contain


		// period
		//Long minJD = null, maxJD = null;
		// get periods from session - not needed, since we always calculate from min/maxEntries
		//if (filters.containsKey("minJD")) minJD = (Long)filters.get("minJD");
		//if (filters.containsKey("maxJD")) maxJD = (Long)filters.get("maxJD");
		// parse periods from input
		if (filters.containsKey("minEntry")) { // parse from input
			FlexibleDateParser parser = new FlexibleDateParser();
			Long minJD = parser.inputToJd((String) filters.get("minEntry"), "G", false);
			if (minJD > Long.MIN_VALUE) constraints.add("minJD >= " + minJD);
		}
		if (filters.containsKey("maxEntry")) { // parse from input
			FlexibleDateParser parser = new FlexibleDateParser();
			Long maxJD = parser.inputToJd((String) filters.get("maxEntry"), "G", true);
			if (maxJD < Long.MAX_VALUE) constraints.add("maxJD <= " + maxJD);
		}


		// tags
		String tagSQL = buildTagFilterSQL((String[]) filters.get("tags"), filters.containsKey("withSubTags") && (boolean) filters.get("withSubTags"), false);
		if (!tagSQL.isEmpty()) constraints.add(tagSQL);

		// location type uid
		if (filters.containsKey("relationTypeUid")) {
			// convert to id
			String id = AbstractSegradaEntity.convertUidToOrientId((String) filters.get("relationTypeUid"));
			if (id != null) {
				constraints.add("relationType = " + id);
			}
			//TODO: test
		}
		// node uid
		if (filters.containsKey("nodeUid")) {
			// convert to id
			String id = AbstractSegradaEntity.convertUidToOrientId((String) filters.get("nodeUid"));
			if (id != null) {
				constraints.add("(relationLink.in = " + id + " OR relationLink.out = " + id + ")");
			}
			//TODO: test
		}

		// sorting
		String customOrder = null;
		if (filters.get("sort") != null) {
			String field = (String) filters.get("sort");
			if (allowedSorts.contains(field)) { // sanity check
				String dir = getDirectionFromString(filters.get("dir"));
				if (dir != null) customOrder = field.concat(dir);
			}
		}

		// let helper do most of the work
		return super.paginate(page, entriesPerPage, constraints, customOrder);
	}
}
