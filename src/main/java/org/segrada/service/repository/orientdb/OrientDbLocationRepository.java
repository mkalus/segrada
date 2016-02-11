package org.segrada.service.repository.orientdb;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.segrada.model.Location;
import org.segrada.model.prototype.ILocation;
import org.segrada.service.repository.LocationRepository;
import org.segrada.service.repository.orientdb.base.AbstractSegradaOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.util.AbstractLazyLoadedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
 * OrientDb Location Repository
 */
public class OrientDbLocationRepository extends AbstractSegradaOrientDbRepository<ILocation> implements LocationRepository {
	private static final Logger logger = LoggerFactory.getLogger(OrientDbLocationRepository.class);

	/**
	 * Constructor
	 */
	@Inject
	public OrientDbLocationRepository(OrientDbRepositoryFactory repositoryFactory) {
		super(repositoryFactory);
	}

	@Override
	public String getModelClassName() {
		return "Location";
	}

	@Override
	public ILocation convertToEntity(ODocument document) {
		Location location = new Location();
		location.setLongitude(document.field("longitude", Double.class));
		location.setLatitude(document.field("latitude", Double.class));
		location.setComment(document.field("comment", String.class));

		// populate with data
		populateEntityWithBaseData(document, location);
		populateEntityWithCreatedModified(document, location);

		// parent
		ORecordId parent = document.field("parent", ORecordId.class);
		if (parent != null) {
			initDb();

			location.setParentId(parent.getIdentity().toString());
			location.setParentModel(db.getMetadata().getSchema().getClassByClusterId(parent.getClusterId()).getName());

			//TODO: lazy load parent
		}

		// add distance, if set in document (added by some query results)
		if (document.field("$distance") != null)
			location.setDistance(document.field("$distance", Double.class));

		return location;
	}

	@Override
	public ODocument convertToDocument(ILocation entity) {
		ODocument document = createOrLoadDocument(entity);

		// populate with data
		document.field("latitude", entity.getLatitude())
				.field("longitude", entity.getLongitude())
				.field("comment", entity.getComment())
				.field("parent", new ORecordId(entity.getParentId()));

		// populate with created/modified stuff
		populateODocumentWithCreatedModified(document, (Location) entity);

		return document;
	}

	@Override
	public List<ILocation> findByParent(String id) {
		if (id == null || "".equals(id)) return null;

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select @rid as id from Location where parent = ?" + getDefaultOrder(true));
		List<ODocument> result = db.command(query).execute(new ORecordId(id));

		List<ILocation> list = new ArrayList<>();

		// populate set
		for (ODocument document : result)
			list.add(lazyLoadInstance(document.field("id", String.class)));

		return list;
	}

	/**
	 * lazy load instance
	 * @param id of entity
	 * @return lazy loading proxy for entity
	 */
	public ILocation lazyLoadInstance(final String id) {
		final LocationRepository repository = this;
		try {
			return (ILocation) java.lang.reflect.Proxy.newProxyInstance(
					ILocation.class.getClassLoader(),
					new Class[]{ILocation.class},
					new AbstractLazyLoadedObject() {
						@Override
						protected Object loadObject() {
							return repository.find(id);
						}
					}
			);
		} catch (Exception e) {
			logger.error("Could not lazy load ILocation " + id, e);
		}
		return null;
	}

	@Override
	public List<ILocation> findClosest(double latitude, double longitude, double radius) {
		// find closest
		List<ILocation> near = findNear(latitude, longitude, radius);

		// now check for the closest elements
		List<ILocation> list = new ArrayList<>();

		// closest distance flag
		double closest = -1;

		// find closest elements
		for (ILocation location : near) {
			// initialize closest
			if (closest == -1) closest = location.getDistance();
			if (location.getDistance() > closest) break; // break iteration
			// add to set
			list.add(location);
		}

		return list;
	}

	@Override
	public List<ILocation> findNear(double latitude, double longitude, double radius) {
		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select *,$distance from Location where [latitude,longitude,$spatial] NEAR [" +
				latitude + "," + longitude + ",{\"maxDistance\":" + radius + "}]");
		List<ODocument> result = db.command(query).execute();

		List<ILocation> list = new ArrayList<>();

		// populate set
		for (ODocument document : result)
			list.add(convertToEntity(document));

		return list;
	}

	@Override
	public List<ILocation> findWithin(double latitude1, double longitude1, double latitude2, double longitude2) {
		// switch values, if bounding box is negative
		if (latitude1 > latitude2) {
			double buffer = latitude1;
			latitude1 = latitude2;
			latitude2 = buffer;

			buffer = longitude1;
			longitude1 = longitude2;
			longitude2 = buffer;
		}

		initDb();

		// execute query
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from Location where [latitude,longitude] WITHIN [[" +
				latitude1 + "," + longitude1 + "], [" + latitude2 + "," + longitude2 + "]]");
		List<ODocument> result = db.command(query).execute();

		List<ILocation> list = new ArrayList<>();

		// populate set
		for (ODocument document : result)
			list.add(convertToEntity(document));

		return list;
	}
}
