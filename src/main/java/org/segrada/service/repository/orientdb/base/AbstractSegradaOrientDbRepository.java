package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.record.ridbag.ORidBag;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.jetbrains.annotations.NotNull;
import org.segrada.model.Pictogram;
import org.segrada.model.User;
import org.segrada.model.UserGroup;
import org.segrada.model.prototype.*;
import org.segrada.service.repository.TagRepository;
import org.segrada.service.repository.orientdb.OrientDbTagRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.AbstractLazyLoadedObject;
import org.segrada.session.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
 * Abstract OrientDb Repository for created/modified entities
 */
abstract public class AbstractSegradaOrientDbRepository<T extends SegradaEntity> extends AbstractOrientDbRepository<T> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractSegradaOrientDbRepository.class);

	/**
	 * Constructor
	 */
	public AbstractSegradaOrientDbRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	/**
	 * helper method to convert entity to document
	 * @param document to be converted
	 * @param entity converted
	 */
	protected void populateODocumentWithCreatedModified(ODocument document, SegradaEntity entity) {
		// only set in new documents
		if (document.getIdentity().isNew()) {
			document.field("created", entity.getCreated())
					.field("creator", entity.getCreator()==null?null:new ORecordId(entity.getCreator().getId()));
		}

		document.field("modified", entity.getModified())
				.field("modifier", entity.getModifier() == null ? null : new ORecordId(entity.getModifier().getId()));
	}

	/**
	 * helper to change ODocument back to entity
	 * @param document to be converted
	 * @param entity converted
	 */
	protected void populateEntityWithCreatedModified(ODocument document, SegradaEntity entity) {
		entity.setCreated(document.field("created", Long.class));
		entity.setModified(document.field("modified", Long.class));

		// get creator/modifier
		ORecordId oCreator = document.field("creator", ORecordId.class);
		ORecordId oModifier = document.field("modifier",  ORecordId.class);

		// push
		if (oCreator != null) entity.setCreator(lazyLoadUser(oCreator));
		if (oModifier != null) entity.setModifier(lazyLoadUser(oModifier));
	}

	/**
	 * lazy load user
	 * @param id record id of user
	 * @return user instance or null
	 */
	protected IUser lazyLoadUser(final ORecordId id) {
		try {
			return (IUser) java.lang.reflect.Proxy.newProxyInstance(
					Thread.currentThread().getContextClassLoader(),
					new Class[]{IUser.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							return convertToUser(db.getRecord(id));
						}
					}
			);
		} catch (Exception e) {
			logger.error("Could not lazy load IUser " + id, e);
		}
		return null;
	}

	/**
	 * lazy load user group
	 * @param id record id of user group
	 * @return user instance or null
	 */
	protected IUserGroup lazyLoadUserGroup(final ORecordId id) {
		try {
			return (IUserGroup) java.lang.reflect.Proxy.newProxyInstance(
					Thread.currentThread().getContextClassLoader(),
					new Class[]{IUserGroup.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							return convertToUserGroup(db.getRecord(id));
						}
					}
			);
		} catch (Exception e) {
			logger.error("Could not lazy load IUserGroup " + id, e);
		}
		return null;
	}

	/**
	 * extracts tag titles from a document containing in_IsTagOf references to tags
	 * @param document raw document
	 * @return list of tags or null
	 */
	public String[] getTags(@NotNull ODocument document) {
		// set tags
		ORidBag tags = document.field("in_IsTagOf", ORidBag.class);
		if (tags != null && !tags.isEmpty()) {
			List<String> list = new ArrayList<>();
			Iterator it = tags.iterator();
			while (it.hasNext()) {
				ODocument o = (ODocument)it.next();
				if (o != null && o.containsField("out")) {
					Object out = o.field("out");
					if (out instanceof ODocument) {
						String title = ((ODocument)out).field("title", String.class);
						if (title != null) {
							list.add(title);
						}
					}
				}
			}
			if (!list.isEmpty()) {
				String[] strTags = new String[list.size()];
				list.toArray(strTags);
				return strTags;
			}
		}

		return null;
	}

	/**
	 * update tag connections of a given entity
	 * will delete old tag connections, create new tags and update them
	 * @param entity to update tags from
	 */
	protected void updateEntityTags(SegradaTaggable entity) {
		// avoid NPEs
		if (entity.getTags() == null) entity.setTags(new String[] {});

		TagRepository tagRepository = repositoryFactory.produceRepository(OrientDbTagRepository.class);
		if (tagRepository == null) return;

		// create new tags, if needed
		tagRepository.createNewTagsByTitles(entity.getTags());

		// find all tags by title
		List<ITag> tags = tagRepository.findTagsByTitles(entity.getTags());

		// keeps added ids
		Set<String> addedIds = new HashSet<>();

		// add all tags to entity
		for (ITag tag : tags) {
			tagRepository.connectTag(tag, entity);
			addedIds.add(tag.getId());
		}

		// now find all tag ids and see if there are some that have been deleted
		String[] tagIds = tagRepository.findTagIdsConnectedToModel(entity, true);
		for (String id : tagIds) {
			if (!addedIds.contains(id)) // not in set connected - delete
				tagRepository.removeTag(id, entity);
		}
	}

	/**
	 * general version of convertToEntity in order to not have to create OrientDbUserRepository instance
	 * @param document to be converted to instance
	 * @return instance
	 */
	protected IUser convertToUser(ODocument document) {
		IUser user = new User();

		user.setLogin(document.field("login", String.class));
		user.setPassword(document.field("password", String.class));
		user.setName(document.field("name", String.class));
		user.setGroup(lazyLoadUserGroup(document.field("group", ORecordId.class)));
		user.setLastLogin(document.field("lastLogin", Long.class));
		user.setActive(document.field("active", Boolean.class));

		populateEntityWithBaseData(document, user);
		populateEntityWithCreatedModified(document, user);

		return user;
	}

	/**
	 * general version of convertToEntity in order to not have to create OrientDbUserGroupRepository instance
	 * @param document to be converted to instance
	 * @return instance
	 */
	public IUserGroup convertToUserGroup(ODocument document) {
		UserGroup userGroup = new UserGroup();

		userGroup.setTitle(document.field("title", String.class));
		userGroup.setDescription(document.field("description", String.class));
		userGroup.setSpecial(document.field("special", String.class));
		Map<String, Integer> roles = document.field("roles", OType.EMBEDDEDMAP);
		for (Object key : roles.keySet()) {
			try {
				String realKey = (String) key;
				Integer value = roles.get(key);

				userGroup.setRole(realKey, value);
			} catch (NumberFormatException e) {
				logger.error("NumberFormatException while converting group right " + roles.get(key) + " in role " + key + ", group " + userGroup.getTitle());
				// ignore error by not adding this role to the group
			}
		}

		// populate with data
		populateEntityWithBaseData(document, userGroup);
		populateEntityWithCreatedModified(document, userGroup);

		return userGroup;
	}

	/**
	 * general version of convertToEntity in order to not have to create pictogram repository instance
	 * @param document to be converted to instance
	 * @return instance
	 */
	protected IPictogram convertToPictogram(ODocument document) {
		IPictogram pictogram = new Pictogram();

		pictogram.setTitle(document.field("title", String.class));
		pictogram.setFileIdentifier(document.field("fileIdentifier", String.class));

		// populate with data
		populateEntityWithBaseData(document, pictogram);
		populateEntityWithCreatedModified(document, pictogram);

		return pictogram;
	}

	/**
	 * populate data with created/modified and user created/modified
	 * @param entity to be saved
	 * @return modified entity
	 */
	@Override
	protected T processBeforeSaving(T entity) {
		Identity identity = repositoryFactory.getIdentity();

		// new entry?
		if (entity.getId() == null) {
			entity.setCreated(System.currentTimeMillis());

			// identity known?
			if (identity != null && identity.isAuthenticated() && !identity.getUser().getId().isEmpty()) // getId must be "" in order to work properly!!
				entity.setCreator(identity.getUser());
		}

		// modified date
		entity.setModified(System.currentTimeMillis());

		// identity known?
		if (identity != null && identity.isAuthenticated() && !identity.getUser().getId().isEmpty()) // getId must be "" in order to work properly!!
			entity.setModifier(identity.getUser());
		else entity.setModifier(null);

		return entity;
	}
}
