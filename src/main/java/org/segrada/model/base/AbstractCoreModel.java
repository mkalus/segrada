package org.segrada.model.base;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.segrada.model.prototype.ILocation;
import org.segrada.model.prototype.IPeriod;
import org.segrada.model.prototype.SegradaCoreEntity;

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
 * Abstract model that extends AbstractColoredModel and comments, periods and locations
 */
abstract public class AbstractCoreModel extends AbstractAnnotatedModel implements SegradaCoreEntity {
	/**
	 * referenced locations
	 */
	private transient List<ILocation> locations;

	/**
	 * referenced periods
	 */
	private transient List<IPeriod> periods;

	/**
	 * minimum entry of periods above
	 */
	private String minEntry = null;

	/**
	 * maximum entry of periods above
	 */
	private String maxEntry = null;

	/**
	 * minimum entry calendar of periods above
	 */
	private String minEntryCalendar = null;

	/**
	 * maximum entry calendar of periods above
	 */
	private String maxEntryCalendar = null;

	/**
	 * minimum JD date of periods above
	 */
	private Long minJD = 0L;

	/**
	 * maximum JD date of periods above
	 */
	private Long maxJD = Long.MAX_VALUE;

	@Override
	public List<ILocation> getLocations() {
		return locations;
	}

	@Override
	public void setLocations(List<ILocation> locations) {
		this.locations = locations;
	}

	@Override
	public List<IPeriod> getPeriods() {
		return periods;
	}

	@Override
	public void setPeriods(List<IPeriod> periods) {
		this.periods = periods;
	}

	@Override
	public String getMinEntry() {
		return minEntry;
	}

	@Override
	public void setMinEntry(String minEntry) {
		this.minEntry = minEntry;
	}

	@Override
	public String getMaxEntry() {
		return maxEntry;
	}

	@Override
	public void setMaxEntry(String maxEntry) {
		this.maxEntry = maxEntry;
	}

	@Override
	public String getMinEntryCalendar() {
		return minEntryCalendar;
	}

	@Override
	public void setMinEntryCalendar(String minEntryCalendar) {
		this.minEntryCalendar = minEntryCalendar;
	}

	@Override
	public String getMaxEntryCalendar() {
		return maxEntryCalendar;
	}

	@Override
	public void setMaxEntryCalendar(String maxEntryCalendar) {
		this.maxEntryCalendar = maxEntryCalendar;
	}

	@Override
	public Long getMinJD() {
		return minJD;
	}

	@Override
	public void setMinJD(Long minJD) {
		this.minJD = minJD;
	}

	@Override
	public Long getMaxJD() {
		return maxJD;
	}

	@Override
	public void setMaxJD(Long maxJD) {
		this.maxJD = maxJD;
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that, "created", "modified", "creator", "modifier", "tags", "comments", "files", "sourceReferences", "locations", "periods");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "created", "modified", "creator", "modifier", "tags", "comments", "files", "sourceReferences", "locations", "periods");
	}
}
