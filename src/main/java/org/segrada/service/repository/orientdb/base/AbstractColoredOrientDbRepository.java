package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.segrada.model.base.AbstractColoredModel;
import org.segrada.model.prototype.IPictogram;
import org.segrada.model.prototype.SegradaColoredEntity;
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
 * Abstract OrientDb Repository for colored entities
 */
abstract public class AbstractColoredOrientDbRepository<T extends SegradaColoredEntity> extends AbstractSegradaOrientDbRepository<T> {
	/**
	 * Constructor
	 */
	public AbstractColoredOrientDbRepository(ODatabaseDocumentTx db, ApplicationSettings applicationSettings,
	                                         Identity identity) {
		super(db, applicationSettings, identity);
	}

	/**
	 * helper method to convert entity to document
	 * @param document to be converted
	 * @param entity converted
	 */
	protected void populateODocumentWithColored(ODocument document, AbstractColoredModel entity) {
		// set color
		document.field("color", entity.getColor());

		// set pictogram
		if (entity.getPictogram() != null) {
			document.field("pictogram", new ORecordId(entity.getPictogram().getId()));
		} else document.removeField("pictogram");
	}

	/**
	 * helper to change ODocument back to entity
	 * @param document to be converted
	 * @param entity converted
	 */
	protected void populateEntityWithColored(ODocument document, AbstractColoredModel entity) {
		// set color
		entity.setColor(document.field("color", Integer.class));

		// set pictogram
		ORecordId pictogram = document.field("pictogram", ORecordId.class);
		if (pictogram != null) {
			entity.setPictogram(lazyLoadPictogram(pictogram));
		}
	}

	/**
	 * lazy load user
	 * @param id record id of user
	 * @return user instance or null
	 */
	protected IPictogram lazyLoadPictogram(final ORecordId id) {
		try {
			return (IPictogram) java.lang.reflect.Proxy.newProxyInstance(
					IPictogram.class.getClassLoader(),
					new Class[]{IPictogram.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							return convertToPictogram(db.getRecord(id));
						}
					}
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
