package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.segrada.model.Pictogram;
import org.segrada.model.User;
import org.segrada.model.prototype.IPictogram;
import org.segrada.model.prototype.IUser;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.AbstractLazyLoadedObject;
import org.segrada.session.Identity;

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
 * Abstract OrientDb Repository for created/modified entities
 */
abstract public class AbstractSegradaOrientDbRepository<T extends SegradaEntity> extends AbstractOrientDbRepository<T> {
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
		document.field("created", entity.getCreated())
				.field("modified", entity.getModified())
				.field("creator", entity.getCreator()==null?null:new ORecordId(entity.getCreator().getId()))
				.field("modifier", entity.getModifier() == null?null:new ORecordId(entity.getModifier().getId()));
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
					IUser.class.getClassLoader(),
					new Class[]{IUser.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							return convertToUser(db.getRecord(id));
						}
					}
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * static version of convertToEntity in order to not have to create OrientDbUserRepository instance
	 * @param document to be converted to instance
	 * @return instance
	 */
	protected IUser convertToUser(ODocument document) {
		IUser user = new User();

		user.setLogin(document.field("login", String.class));
		user.setPassword(document.field("password", String.class));
		user.setName(document.field("name", String.class));
		user.setRole(document.field("role", String.class));
		user.setLastLogin(document.field("lastLogin", Long.class));
		user.setActive(document.field("active", Boolean.class));

		populateEntityWithBaseData(document, user);
		populateEntityWithCreatedModified(document, user);

		return user;
	}


	/**
	 * static version of convertToEntity in order to not have to create pictogram repository instance
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
			if (identity != null && identity.isAuthenticated())
				entity.setCreator(identity.getUser());
		}

		// modified date
		entity.setModified(System.currentTimeMillis());

		// identity known?
		if (identity != null && identity.isAuthenticated())
			entity.setModifier(identity.getUser());
		else entity.setModifier(null);

		return entity;
	}
}
