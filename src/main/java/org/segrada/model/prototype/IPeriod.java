package org.segrada.model.prototype;

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
 * Period model interface
 */
public interface IPeriod extends SegradaEntity {
	String getParentId();
	void setParentId(String parent);

	String getParentModel();
	void setParentModel(String parentModel);

	String getType();

	String getFromEntry();
	void setFromEntry(String fromEntry);

	String getToEntry();
	void setToEntry(String toEntry);

	Long getFromJD();
	void setFromJD(Long fromJD);

	Long getToJD();
	void setToJD(Long toJD);

	String getFromEntryCalendar();
	void setFromEntryCalendar(String fromEntryCalendar);

	String getToEntryCalendar();
	void setToEntryCalendar(String toEntryCalendar);
}
