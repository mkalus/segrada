package org.segrada.service.repository;

import org.segrada.model.prototype.ILocation;
import org.segrada.service.repository.prototype.CRUDRepository;

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
 * Location Repository
 */
public interface LocationRepository extends CRUDRepository<ILocation> {
	/**
	 * find set by parent id
	 * @param id parent id
	 * @return list of locations associated with parent
	 */
	List<ILocation> findByParent(String id);

	/**
	 * find closest location(s) to a given coordinate
	 * @param latitude coordinate
	 * @param longitude coordinate
	 * @return closest location(s) - might be more than one
	 */
	List<ILocation> findClosest(double latitude, double longitude, double radius);

	/**
	 * find nearest locations in an area
	 * @param latitude coordinate
	 * @param longitude coordinate
	 * @param radius in km
	 * @return list of locations within area (sorted by distance from point)
	 */
	List<ILocation> findNear(double latitude, double longitude, double radius);

	/**
	 * find nearest locations within a bounding box
	 * @param latitude1 coordinate 1
	 * @param longitude1 coordinate 1
	 * @param latitude2 coordinate 2
	 * @param longitude2 coordinate 2
	 * @return set of locations within box
	 */
	List<ILocation> findWithin(double latitude1, double longitude1, double latitude2, double longitude2);
}
