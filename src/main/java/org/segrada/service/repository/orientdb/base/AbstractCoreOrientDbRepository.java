package org.segrada.service.repository.orientdb.base;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.segrada.model.base.AbstractCoreModel;
import org.segrada.model.prototype.ILocation;
import org.segrada.model.prototype.IPeriod;
import org.segrada.model.prototype.SegradaCoreEntity;
import org.segrada.service.repository.LocationRepository;
import org.segrada.service.repository.PeriodRepository;
import org.segrada.service.repository.orientdb.OrientDbLocationRepository;
import org.segrada.service.repository.orientdb.OrientDbPeriodRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.AbstractLazyLoadedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Copyright 2015-2019 Maximilian Kalus [segrada@auxnet.de]
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
	private static final Logger logger = LoggerFactory.getLogger(AbstractCoreOrientDbRepository.class);

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
	protected void populateODocumentWithCore(ODocument document, T entity) {
		// determine periods and save range in my model
		if (entity.getPeriods() != null && !entity.getPeriods().isEmpty()) {
			Long min = Long.MAX_VALUE;
			Long max = Long.MIN_VALUE;
			String minEntry = null;
			String maxEntry = null;
			String minCalendar = null;
			String maxCalendar = null;
			char[] minFuzzyFlags = null;
			char[] maxFuzzyFlags = null;

			for (IPeriod period : entity.getPeriods()) {
				// calculate from/to extent
				Long from = period.getFromJD();
				if (from != null && from != Long.MIN_VALUE && from < min) {
					min = from;
					minEntry = period.getFromEntry();
					minCalendar = period.getFromEntryCalendar();
					minFuzzyFlags = period.getFuzzyFromFlags();
				}

				Long to = period.getToJD();
				if (to != null && to != Long.MAX_VALUE && to > max) {
					max = to;
					maxEntry = period.getToEntry();
					maxCalendar = period.getToEntryCalendar();
					maxFuzzyFlags = period.getFuzzyToFlags();
				}
			}
			document.field("minJD", min);
			document.field("maxJD", max);
			document.field("minEntry", minEntry);
			document.field("maxEntry", maxEntry);
			document.field("minEntryCalendar", minCalendar);
			document.field("maxEntryCalendar", maxCalendar);
			document.field("minFuzzyFlags", new String(minFuzzyFlags==null?new char[0]:minFuzzyFlags));
			document.field("maxFuzzyFlags", new String(maxFuzzyFlags==null?new char[0]:maxFuzzyFlags));
		} else {
			// reset fields
			document.field("minJD", Long.MIN_VALUE);
			document.field("maxJD", Long.MAX_VALUE);
			document.removeField("minEntry");
			document.removeField("maxEntry");
			document.removeField("minEntryCalendar");
			document.removeField("maxEntryCalendar");
			document.removeField("minFuzzyFlags");
			document.removeField("maxFuzzyFlags");
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

		// set fuzzy flags
		String flags = document.field("minFuzzyFlags", String.class);
		if (flags != null && !flags.isEmpty()) {
			for (char flag : flags.toCharArray())
				entity.addFuzzyMinFlag(flag);
		}
		flags = document.field("maxFuzzyFlags", String.class);
		if (flags != null && !flags.isEmpty()) {
			for (char flag : flags.toCharArray())
				entity.addFuzzyMaxFlag(flag);
		}
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
					Thread.currentThread().getContextClassLoader(),
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
			logger.error("Could not lazy load ILocations for " + entity.toString(), e);
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
					Thread.currentThread().getContextClassLoader(),
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
			logger.error("Could not lazy load IPeriods for " + entity.toString(), e);
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
