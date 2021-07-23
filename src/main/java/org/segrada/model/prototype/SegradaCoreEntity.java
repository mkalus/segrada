package org.segrada.model.prototype;

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
 * Core entities contain some relations
 */
public interface SegradaCoreEntity extends SegradaAnnotatedEntity {
	List<ILocation> getLocations();
	void setLocations(List<ILocation> locations);

	List<IPeriod> getPeriods();
	void setPeriods(List<IPeriod> periods);

	/**
	 * minimum and maximum entries of periods above
	 */
	String getMinEntry();
	void setMinEntry(String minEntry);

	String getMaxEntry();
	void setMaxEntry(String maxEntry);

	String getMinEntryCalendar();
	void setMinEntryCalendar(String minEntryCalendar);

	String getMaxEntryCalendar();
	void setMaxEntryCalendar(String maxEntryCalendar);

	Long getMinJD();
	void setMinJD(Long minJD);

	Long getMaxJD();
	void setMaxJD(Long maxJD);

	/**
	 * fuzzy flag handlers
	 */
	void addFuzzyMinFlag(char flag);
	void deleteFuzzyMinFlag(char flag);
	boolean hasFuzzyMinFlag(char flag);
	char[] getFuzzyMinFlags();

	void addFuzzyMaxFlag(char flag);
	void deleteFuzzyMaxFlag(char flag);
	boolean hasFuzzyMaxFlag(char flag);
	char[] getFuzzyMaxFlags();
}
