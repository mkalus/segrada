package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.service.util.PaginationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

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
 * Abstract OrientDb Repository
 */
abstract public class AbstractOrientDbRepository<T extends SegradaEntity> implements CRUDRepository<T> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractOrientDbRepository.class);

	/**
	 * Injected repository factory
	 */
	protected final OrientDbRepositoryFactory repositoryFactory;

	/**
	 * database instance
	 */
	protected final ODatabaseDocumentTx db;

	/**
	 * Constructor
	 * @param repositoryFactory injected
	 */
	public AbstractOrientDbRepository(OrientDbRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
		this.db = repositoryFactory.getDb();
	}

	/**
	 * open database
	 */
	protected void initDb() {
		try {
			if (db.isClosed()) {
				db.open(repositoryFactory.getApplicationSettings().getSetting("orientDB.login"),
						repositoryFactory.getApplicationSettings().getSetting("orientDB.password"));
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not open database.", e);
		}
	}

	/**
	 * Convert document to entity
	 * @param document to be converted
	 * @return converted entity
	 */
	abstract public T convertToEntity(ODocument document);

	/**
	 * Convert entity to document
	 * @param entity to be converted
	 * @return converted document
	 */
	abstract public ODocument convertToDocument(T entity);

	/**
	 * get class reference of model class
	 * @return class name
	 */
	abstract public String getModelClassName();

	/**
	 * populate with basic data
	 * @param document source
	 * @param entity target entity
	 */
	protected void populateEntityWithBaseData(ODocument document, SegradaEntity entity) {
		entity.setId(document.getIdentity().toString());
		entity.setVersion(document.getVersion());
	}

	/**
	 * helper to create or load document from entity
	 * @param entity to create document from
	 * @return created document
	 */
	protected ODocument createOrLoadDocument(T entity) {
		ODocument document;

		// load from db?
		if (entity.getId() != null) {
			initDb();

			document = db.load(new ORecordId(entity.getId()));

			// correct class?
			if (document != null && !document.getClassName().equals(getModelClassName()))
				throw new RuntimeException("Record id " + entity.getId() + " should be of type "
						+ getModelClassName() + ", but was of class " + document.getClassName() + ".");
		} else { // create new entry
			document = new ODocument(getModelClassName());
		}

		return document;
	}

	/**
	 * process entity before saving it to db - can be overwritten to change stuff before saving entity
	 * @param entity to be saved
	 * @return changed entity
	 */
	protected T processBeforeSaving(T entity) {
		// do nothing by default
		return entity;
	}

	/**
	 * process entity after saving - can be overwritten to change stuff after saving entity
	 * @param updated data from OrientDB with updated data
	 * @param entity to be changed
	 * @return changed entity
	 */
	protected T processAfterSaving(ODocument updated, T entity) {
		// set id automatically
		if (entity.getId() == null) entity.setId(updated.getIdentity().toString());
		else if (!entity.getId().equals(updated.getIdentity().toString())) {
			String message = "Error while updating entity " + entity.toString() + ": New identity "
					+ updated.getIdentity().toString() + " saved to db.";
			logger.error(message);
			throw new RuntimeException(message); // not nice but should make it clear that we have a problem
		}

		// set version
		entity.setVersion(updated.getVersion());

		return entity;
	}

	/**
	 * Implementation to save entity to database
	 * @param entity to be saved
	 * @return true if saving succeeded
	 */
	public boolean save(T entity) {
		try {
			initDb();

			// process before saving
			entity = processBeforeSaving(entity);

			// save
			ODocument updated = db.save(convertToDocument(entity));

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

	/**
	 * Count all entities
	 * @return number of entities
	 */
	public long count() {
		try {
			initDb();

			// create query
			String sql = "select count(*) from ".concat(getModelClassName()).concat(getDefaultQueryParameters());

			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(sql);
			List<ODocument> list = db.command(query).execute();
			ODocument doc = list.get(0);

			return doc.field("count", Long.class);
		} catch (Exception e) {
			logger.error("Exception thrown while counting entities.", e);
		}
		return 0L;
	}

	/**
	 * Get all entities
	 * @return linked list of entities
	 */
	public List<T> findAll() {
		List<T> entities = new LinkedList<>();

		try {
			initDb();

			// create query
			String sql = "select * from ".concat(getModelClassName()).concat(getDefaultQueryParameters()).concat(getDefaultOrder());

			// execute query
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(sql);
			List<ODocument> list = db.command(query).execute();

			for (ODocument document : list) {
				entities.add(convertToEntity(document));
			}
		} catch (Exception e) {
			logger.error("Exception thrown while fetching all entities.", e);
		}

		return entities;
	}

	/**
	 * find single entity by Orient Id
	 * @param id string representation of orient db, e.g. "#11:1"
	 * @return entity or null
	 */
	public T find(@Nullable String id) {
		// sanity check to return null when looking for null
		if (id == null || id.length() == 0) return null;

		try {
			initDb();

			ODocument document = (ODocument) db.load(new ORecordId(id));
			if (document == null) return null;

			// not correct class? => return null
			if (!document.getClassName().equals(getModelClassName())) {
				logger.warn("Entity with id " + id + " is not of type " + getModelClassName());
				return null;
			}

			// correct class => convert to correct entity
			return convertToEntity(document);
		} catch (Exception e) {
			logger.error("Exception thrown while fetching one entity.", e);
		}
		return null;
	}

	/**
	 * delete record from database
	 * @param entity to be deleted
	 * @return true if deletion succeeded
	 */
	public boolean delete(@Nullable T entity) {
		if (entity == null) return true;

		try {
			initDb();

			if (logger.isInfoEnabled())
				logger.info("Deleting entity : " + entity.toString());

			// delete connected edges, if there are any
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select from E where in = " + entity.getId() + " OR out = " + entity.getId());
			for (ODocument edge : (List<ODocument>) db.command(query).execute())
					edge.delete();
			// The following code renders an NPE
			//repositoryFactory.getDb().command(new OCommandSQL("delete edge where in = " + entity.getId() + " OR out = " + entity.getId())).execute();

			return db.delete(new ORecordId(entity.getId())) != null;
		} catch (Exception e) {
			logger.warn("Could not delete entry (search engine entry deleted if applicable): " + entity.getId());
		}
		return false;
	}

	/**
	 * Return sql query parameters like "WHERE active = true - can be overwritten
	 * @param addWhere
	 * @return
	 */
	protected String getDefaultQueryParameters(boolean addWhere) {
		return "";
	}

	/**
	 * Return default sortby parameter
	 * @param addOrderBy
	 * @return
	 */
	protected String getDefaultOrder(boolean addOrderBy) {
		return "";
	}

	/**
	 * shorthand with addWhere true
	 * @return
	 */
	protected String getDefaultQueryParameters() {
		return getDefaultQueryParameters(true);
	}

	/**
	 * shorthand with addOrderBy true
	 * @return
	 */
	protected String getDefaultOrder() {
		return getDefaultOrder(true);
	}

	/**
	 * pagination helper function
	 * @param page to show
	 * @param entriesPerPage maximum entries per page
	 * @param constraints constraint list to concatenate
	 * @param customOrder custom order string or null for default
	 * @return PaginationInfo containing hits
	 */
	protected PaginationInfo<T> paginate(int page, int entriesPerPage, List<String> constraints, String customOrder) {
		// create constraint string
		StringBuilder sb = new StringBuilder();
		if (constraints.size() > 0) {
			boolean first = true;
			for (String constraint : constraints) {
				sb.append(first?" WHERE ":" AND ");
				sb.append(constraint);
				if (first) first = false;
			}
		}
		String constraint = " from ".concat(getModelClassName()).concat(sb.toString());

		List<T> entities = new LinkedList<>();

		try {
			initDb();

			// first, do a count of the entities
			String sql = "select count(*) as count".concat(constraint);
			if (logger.isTraceEnabled()) logger.trace(sql);
			int total = ((ODocument) db.query(new OSQLSynchQuery<ODocument>(sql)).get(0)).field("count", Integer.class);

			if (total == 0)
				return new PaginationInfo<>(
						1, // page
						1, // pages
						0, // total entries
						entriesPerPage, // per page
						entities // list of entities
				);

			// calculate pages
			if (entriesPerPage < 1) entriesPerPage = 10; // sanity
			int pages = total / entriesPerPage + (total % entriesPerPage == 0?0:1);

			// make sure we are inside the bounds
			if (page < 1) page = 1;
			else if (page > pages) page = pages;

			// prepare skip/limit strings
			int skip = (page-1) * entriesPerPage;
			String skipLimit = (skip>0?" SKIP ".concat(Integer.toString(skip)).concat(" "):"")
					.concat(" LIMIT ").concat(Integer.toString(entriesPerPage));

			// custom order?
			if (customOrder != null && !customOrder.isEmpty()) customOrder = " ORDER BY ".concat(customOrder);
			else customOrder = getDefaultOrder(); // no, just use default order

			// create query itself and fetch entities
			sql = "select *".concat(constraint).concat(customOrder).concat(skipLimit);
			if (logger.isTraceEnabled()) logger.trace(sql);

			// execute query
			List<ODocument> list = db.command(new OSQLSynchQuery<>(sql)).execute();

			entities.addAll(list.stream().map(this::convertToEntity).collect(Collectors.toList()));

			/**
			 * return pagination list
			 */
			return new PaginationInfo<>(
					page, // page
					pages, // pages
					total, // total entries
					entriesPerPage, // per page
					entities // list of entities
			);
		} catch (Exception e) {
			logger.error("Exception thrown while fetching paginated entities.", e);

			return null;
		}
	}

	@Override
	public String convertUidToId(String uid) {
		return AbstractSegradaEntity.convertUidToOrientId(uid);
	}

	/**
	 * Helper method to get direction from string object
	 * @param dir object in filter cache (should be string)
	 * @return direction or null, if not valid
	 */
	protected String getDirectionFromString(Object dir) {
		if (dir == null) return null;

		String testDir = dir.toString();

		if (testDir.equalsIgnoreCase("asc")) return " ASC";
		if (testDir.equalsIgnoreCase("desc")) return " DESC";

		return null;
	}
}
