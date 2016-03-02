package org.segrada.model.base;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.segrada.model.prototype.ILocation;
import org.segrada.model.prototype.IPeriod;
import org.segrada.model.prototype.SegradaCoreEntity;
import org.segrada.model.util.FuzzyFlag;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

import static org.segrada.model.util.FuzzyFlag.*;

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
	private String minEntry;

	/**
	 * maximum entry of periods above
	 */
	private String maxEntry;

	/**
	 * minimum entry calendar of periods above
	 */
	private String minEntryCalendar;

	/**
	 * maximum entry calendar of periods above
	 */
	private String maxEntryCalendar;

	/**
	 * minimum JD date of periods above
	 */
	private Long minJD = Long.MIN_VALUE;

	/**
	 * maximum JD date of periods above
	 */
	private Long maxJD = Long.MAX_VALUE;

	private EnumSet<FuzzyFlag> minFuzzyFlags;

	private EnumSet<FuzzyFlag> maxFuzzyFlags;

	@Override
	public @Nullable List<ILocation> getLocations() {
		return locations;
	}

	@Override
	public void setLocations(List<ILocation> locations) {
		this.locations = locations;
	}

	@Override
	public @Nullable List<IPeriod> getPeriods() {
		return periods;
	}

	@Override
	public void setPeriods(List<IPeriod> periods) {
		this.periods = periods;
	}

	@Override
	public @Nullable String getMinEntry() {
		return minEntry;
	}

	@Override
	public void setMinEntry(String minEntry) {
		this.minEntry = minEntry;
	}

	@Override
	public @Nullable String getMaxEntry() {
		return maxEntry;
	}

	@Override
	public void setMaxEntry(String maxEntry) {
		this.maxEntry = maxEntry;
	}

	@Override
	public @Nullable String getMinEntryCalendar() {
		return minEntryCalendar;
	}

	@Override
	public void setMinEntryCalendar(String minEntryCalendar) {
		this.minEntryCalendar = minEntryCalendar;
	}

	@Override
	public @Nullable String getMaxEntryCalendar() {
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
	public void addFuzzyMinFlag(char flag) {
		minFuzzyFlags = addFuzzyFlag(flag, minFuzzyFlags);
	}

	@Override
	public void deleteFuzzyMinFlag(char flag) {
		minFuzzyFlags = deleteFuzzyFlag(flag, minFuzzyFlags);
	}

	@Override
	public boolean hasFuzzyMinFlag(char flag) {
		return hasFuzzyFlag(flag, minFuzzyFlags);
	}

	@Override
	public char[] getFuzzyMinFlags() {
		return getFuzzyFlags(minFuzzyFlags);
	}

	@Override
	public void addFuzzyMaxFlag(char flag) {
		maxFuzzyFlags = addFuzzyFlag(flag, maxFuzzyFlags);
	}

	@Override
	public void deleteFuzzyMaxFlag(char flag) {
		maxFuzzyFlags = deleteFuzzyFlag(flag, maxFuzzyFlags);
	}

	@Override
	public boolean hasFuzzyMaxFlag(char flag) {
		return hasFuzzyFlag(flag, maxFuzzyFlags);
	}

	@Override
	public char[] getFuzzyMaxFlags() {
		return getFuzzyFlags(maxFuzzyFlags);
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that, "created", "modified", "creator", "modifier", "tags", "comments", "files", "sourceReferences", "locations", "periods", "minFuzzyFlags", "maxFuzzyFlags");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "created", "modified", "creator", "modifier", "tags", "comments", "files", "sourceReferences", "locations", "periods", "minFuzzyFlags", "maxFuzzyFlags");
	}
}
