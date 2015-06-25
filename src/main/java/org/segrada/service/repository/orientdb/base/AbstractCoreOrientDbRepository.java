package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.segrada.model.base.AbstractCoreModel;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.SegradaCoreEntity;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;

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
 * Abstract OrientDb Repository for core entities (locations, periods)
 */
abstract public class AbstractCoreOrientDbRepository<T extends SegradaCoreEntity> extends AbstractAnnotatedOrientDbRepository<T> {
	/**
	 * Constructor
	 */
	public AbstractCoreOrientDbRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	/**
	 * helper method to convert entity to document
	 * @param document to be converted
	 * @param entity converted
	 */
	protected void populateODocumentWithCore(ODocument document, AbstractCoreModel entity) {
		//TODO
	}

	/**
	 * helper to change ODocument back to entity
	 * @param document to be converted
	 * @param entity converted
	 */
	protected void populateEntityWithCore(ODocument document, AbstractCoreModel entity) {
		//TODO
	}


	@Override
	public boolean delete(T entity) {
		if (super.delete(entity)) {
			// delete connected locations and periods
			db.command(new OCommandSQL("delete from Location where parent = " + entity.getId())).execute();
			db.command(new OCommandSQL("delete from Period where parent = " + entity.getId())).execute();

			return true;
		}
		return false;
	}
}
