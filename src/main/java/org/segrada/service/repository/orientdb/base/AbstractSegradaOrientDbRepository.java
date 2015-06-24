package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.segrada.model.User;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.IUser;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.util.AbstractLazyLoadedObject;
import org.segrada.session.ApplicationSettings;
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
	 * Injected identity that keeps the logged in user
	 */
	protected final Identity identity;

	public AbstractSegradaOrientDbRepository(ODatabaseDocumentTx db, ApplicationSettings applicationSettings,
	                                         Identity identity) {
		super(db, applicationSettings);

		this.identity = identity;
	}

	/**
	 * helper method to convert entity to document
	 * @param document to be converted
	 * @param entity converted
	 */
	protected void populateODocumentWithCreatedModified(ODocument document, AbstractSegradaEntity entity) {
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
	protected void populateEntityWithCreatedModified(ODocument document, AbstractSegradaEntity entity) {
		entity.setCreated((Long) document.field("created", Long.class));
		entity.setModified((Long) document.field("modified", Long.class));

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
	protected static IUser convertToUser(ODocument document) {
		IUser user = new User();

		user.setLogin((String) document.field("login", String.class));
		user.setPassword((String) document.field("password", String.class));
		user.setName((String) document.field("name", String.class));
		user.setRole((String) document.field("role", String.class));
		user.setCreated((Long) document.field("created", Long.class));
		user.setModified((Long) document.field("modified", Long.class));
		user.setLastLogin((Long) document.field("lastLogin", Long.class));
		user.setActive((Boolean) document.field("active", Boolean.class));
		user.setId(document.getIdentity().toString());
		user.setVersion(document.getVersion());

		return user;
	}

	/**
	 * populate data with created/modified and user created/modified
	 * @param entity to be saved
	 * @return modified entity
	 */
	@Override
	protected T processBeforeSaving(T entity) {
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
