package org.segrada.service.repository;

import org.joda.time.DateTime;
import org.segrada.model.prototype.IPeriod;
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
 * Period Repository
 */
public interface PeriodRepository extends CRUDRepository<IPeriod> {
	/**
	 * find set by parent id
	 * @param id parent id
	 * @return list of locations associated with parent
	 */
	List<IPeriod> findByParent(String id);

	/**
	 * find entities within a certain time frame
	 * @param jdStart start time (can be null to set it to 0)
	 * @param jdEnd end time (can be null to set it to maximum time)
	 * @return list of hits sorted by starttime/endtime
	 */
	List<IPeriod> findWithin(Long jdStart, Long jdEnd);

	/**
	 * find entities within a certain time frame
	 * @param start start time (can be null to set it to 0)
	 * @param end end time (can be null to set it to maximum time)
	 * @return list of hits sorted by starttime/endtime
	 */
	List<IPeriod> findWithin(DateTime start, DateTime end);
}
