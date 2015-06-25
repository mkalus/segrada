package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.segrada.model.base.AbstractCoreModel;
import org.segrada.model.prototype.ILocation;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IPeriod;
import org.segrada.model.prototype.SegradaCoreEntity;
import org.segrada.service.repository.LocationRepository;
import org.segrada.service.repository.PeriodRepository;
import org.segrada.service.repository.orientdb.OrientDbLocationRepository;
import org.segrada.service.repository.orientdb.OrientDbPeriodRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.AbstractLazyLoadedObject;

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
		// determine periods and save range in my model
		if (entity.getPeriods() != null && !entity.getPeriods().isEmpty()) {
			Long min = Long.MAX_VALUE;
			Long max = 0L;
			String minEntry = null;
			String maxEntry = null;
			String minCalendar = null;
			String maxCalendar = null;

			for (IPeriod period : entity.getPeriods()) {
				// calculate from/to extent
				Long from = period.getFromJD();
				if (from != null && from > 0 && from < min) {
					min = from;
					minEntry = period.getFromEntry();
					minCalendar = period.getFromEntryCalendar();
				}

				Long to = period.getToJD();
				if (to != null && to < Long.MAX_VALUE && to > max) {
					max = to;
					maxEntry = period.getToEntry();
					maxCalendar = period.getToEntryCalendar();
				}
			}
			document.field("minJD", min);
			document.field("maxJD", max);
			document.field("minEntry", minEntry);
			document.field("maxEntry", maxEntry);
			document.field("minEntryCalendar", minCalendar);
			document.field("maxEntryCalendar", maxCalendar);
		} else {
			// reset fields
			document.field("minJD", 0L);
			document.field("maxJD", Long.MAX_VALUE);
			document.removeField("minEntry");
			document.removeField("maxEntry");
			document.removeField("minEntryCalendar");
			document.removeField("maxEntryCalendar");
		}

		// periods and locations are not saved here, because they have their own repositories
	}

	/**
	 * helper to change ODocument back to entity
	 * @param document to be converted
	 * @param entity converted
	 */
	protected void populateEntityWithCore(ODocument document, AbstractCoreModel entity) {
		if (entity.getId() != null) {
			entity.setLocations(lazyLoadLocations(entity));
			entity.setPeriods(lazyLoadPeriods(entity));
		}

		// get fields
		entity.setMinJD(document.field("minJD", Long.class));
		entity.setMaxJD(document.field("maxJD", Long.class));
		entity.setMinEntry(document.field("minEntry", String.class));
		entity.setMaxEntry(document.field("maxEntry", String.class));
		entity.setMinEntryCalendar(document.field("minEntryCalendar", String.class));
		entity.setMaxEntryCalendar(document.field("maxEntryCalendar", String.class));
	}

	/**
	 * lazily load locations for an entity
	 * @param entity connected as reference
	 * @return list of locations (proxy)
	 */
	@SuppressWarnings("unchecked")
	public List<ILocation> lazyLoadLocations(final SegradaCoreEntity entity) {
		try {
			return (List<ILocation>) java.lang.reflect.Proxy.newProxyInstance(
					List.class.getClassLoader(),
					new Class[]{List.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							LocationRepository locationRepository =
									repositoryFactory.produceRepository(OrientDbLocationRepository.class);

							return locationRepository.findByParent(entity.getId());
						}
					}
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * lazily load periods for an entity
	 * @param entity connected as reference
	 * @return list of periods (proxy)
	 */
	@SuppressWarnings("unchecked")
	public List<IPeriod> lazyLoadPeriods(final SegradaCoreEntity entity) {
		try {
			return (List<IPeriod>) java.lang.reflect.Proxy.newProxyInstance(
					List.class.getClassLoader(),
					new Class[]{List.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							PeriodRepository periodRepository =
									repositoryFactory.produceRepository(OrientDbPeriodRepository.class);

							return periodRepository.findByParent(entity.getId());
						}
					}
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
