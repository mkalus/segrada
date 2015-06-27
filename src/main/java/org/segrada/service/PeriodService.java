package org.segrada.service;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.segrada.model.Period;
import org.segrada.model.prototype.IPeriod;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.repository.PeriodRepository;
import org.segrada.service.repository.factory.RepositoryFactory;

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
 * Period service
 */
public class PeriodService extends AbstractRepositoryService<IPeriod, PeriodRepository> {
	/**
	 * Constructor
	 */
	@Inject
	public PeriodService(RepositoryFactory repositoryFactory) {
		super(repositoryFactory, PeriodRepository.class);
	}

	@Override
	public IPeriod createNewInstance() {
		return new Period();
	}

	@Override
	public Class<IPeriod> getModelClass() {
		return IPeriod.class;
	}

	/**
	 * find set by parent id
	 * @param id parent id
	 * @return list of locations associated with parent
	 */
	public List<IPeriod> findByParent(String id) {
		return repository.findByParent(id);
	}

	/**
	 * find entities within a certain time frame
	 * @param jdStart start time (can be null to set it to 0)
	 * @param jdEnd end time (can be null to set it to maximum time)
	 * @return list of hits sorted by starttime/endtime
	 */
	public List<IPeriod> findWithin(Long jdStart, Long jdEnd) {
		return repository.findWithin(jdStart, jdEnd);
	}

	/**
	 * find entities within a certain time frame
	 * @param start start time (can be null to set it to 0)
	 * @param end end time (can be null to set it to maximum time)
	 * @return list of hits sorted by starttime/endtime
	 */
	public List<IPeriod> findWithin(DateTime start, DateTime end) {
		return repository.findWithin(start, end);
	}
}
