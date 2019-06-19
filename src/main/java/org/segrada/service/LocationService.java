package org.segrada.service;

import com.google.inject.Inject;
import org.segrada.model.Location;
import org.segrada.model.prototype.ILocation;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.repository.LocationRepository;
import org.segrada.service.repository.factory.RepositoryFactory;

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
 * Location service
 */
public class LocationService extends AbstractRepositoryService<ILocation, LocationRepository> {
	/**
	 * Constructor
	 */
	@Inject
	public LocationService(RepositoryFactory repositoryFactory) {
		super(repositoryFactory, LocationRepository.class);
	}

	@Override
	public ILocation createNewInstance() {
		return new Location();
	}

	@Override
	public Class<ILocation> getModelClass() {
		return ILocation.class;
	}

	/**
	 * find set by parent id
	 * @param id parent id
	 * @return list of locations associated with parent
	 */
	public List<ILocation> findByParent(String id) {
		return repository.findByParent(id);
	}

	/**
	 * find closest location(s) to a given coordinate
	 * @param latitude coordinate
	 * @param longitude coordinate
	 * @return closest location(s) - might be more than one
	 */
	public List<ILocation> findClosest(double latitude, double longitude, double radius) {
		return repository.findClosest(latitude, longitude, radius);
	}

	/**
	 * find nearest locations in an area
	 * @param latitude coordinate
	 * @param longitude coordinate
	 * @param radius in km
	 * @return list of locations within area (sorted by distance from point)
	 */
	public List<ILocation> findNear(double latitude, double longitude, double radius) {
		return repository.findNear(latitude, longitude, radius);
	}

	/**
	 * find nearest locations within a bounding box
	 * @param latitude1 coordinate 1
	 * @param longitude1 coordinate 1
	 * @param latitude2 coordinate 2
	 * @param longitude2 coordinate 2
	 * @return set of locations within box
	 */
	public List<ILocation> findWithin(double latitude1, double longitude1, double latitude2, double longitude2) {
		return repository.findWithin(latitude1, longitude1, latitude2, longitude2);
	}
}
