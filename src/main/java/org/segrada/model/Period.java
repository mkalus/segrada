package org.segrada.model;

import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.IPeriod;
import org.segrada.util.FlexibleDateParser;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
 * Period model implementation
 */
public class Period extends AbstractSegradaEntity implements IPeriod {
	private static final long serialVersionUID = 1L;

	@NotNull(message = "error.notNull")
	private String parentId;

	@NotNull(message = "error.notNull")
	private String parentModel;

	private String fromEntry = "";

	private String toEntry = "";

	@NotNull(message = "error.notNull")
	private Long fromJD = Long.MIN_VALUE;

	@NotNull(message = "error.notNull")
	private Long toJD = Long.MAX_VALUE;

	@NotNull(message = "error.notNull")
	@Pattern(regexp = "[GJ]", message = "error.calendar")
	private String fromEntryCalendar = "G";

	@NotNull(message = "error.notNull")
	@Pattern(regexp = "[GJ]", message = "error.calendar")
	private String toEntryCalendar = "G";

	/**
	 * dummy check to make sure that from is lower or equal to to
	 * @return true of from is lower or equal to
	 */
	@AssertTrue(message = "error.calendar.fromTo")
	public boolean getFromLowerEqualThanTo() {
		if (this.fromJD == null || this.toJD == null) return true; // no NPEs
		return this.fromJD <= this.toJD;
	}

	/**
	 * dummy check to make sure that from is lower or equal to to
	 * @return true of from is lower or equal to
	 */
	@AssertTrue(message = "error.calendar.empty")
	public boolean getNonEmpty() {
		if (this.fromJD == null || this.toJD == null) return true; // no NPEs
		// not allowed empty values in both entries
		if (this.fromJD == Long.MIN_VALUE && this.toJD == Long.MAX_VALUE) return false;
		return true;
	}

	@Override
	public String getParentId() {
		return parentId;
	}

	@Override
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	@Override
	public String getParentModel() {
		return parentModel;
	}

	@Override
	public void setParentModel(String parentModel) {
		this.parentModel = parentModel;
	}

	@Override
	public String getType() {
		if (fromJD == null && toJD == null) return "period"; // should not happen manually
		if (fromJD == null || toJD == null) return "period";
		return fromJD.equals(toJD)?"moment":"period";
	}

	@Override
	public String getFromEntry() {
		return fromEntry;
	}

	@Override
	public void setFromEntry(String fromEntry) {
		this.fromEntry = fromEntry;
		parseFromTime();
	}

	@Override
	public String getToEntry() {
		return toEntry;
	}

	@Override
	public void setToEntry(String toEntry) {
		this.toEntry = toEntry;
		parseToTime();
	}

	@Override
	public String getToEntryCalendar() {
		return toEntryCalendar;
	}

	@Override
	public void setToEntryCalendar(String toEntryCalendar) {
		this.toEntryCalendar = toEntryCalendar;
		parseToTime();
	}

	@Override
	public String getFromEntryCalendar() {
		return fromEntryCalendar;
	}

	@Override
	public void setFromEntryCalendar(String fromEntryCalendar) {
		this.fromEntryCalendar = fromEntryCalendar;
		parseFromTime();
	}

	@Override
	public Long getFromJD() {
		return fromJD;
	}

	@Override
	public void setFromJD(Long fromJD) {
		this.fromJD = fromJD;
	}

	@Override
	public Long getToJD() {
		return toJD;
	}

	@Override
	public void setToJD(Long toJD) {
		this.toJD = toJD;
	}

	/**
	 * parse from time into jd
	 */
	protected void parseFromTime() {
		if (getFromEntry() == null) setFromJD(Long.MIN_VALUE);
		else {
			// parse time to jd
			FlexibleDateParser parser = new FlexibleDateParser();
			setFromJD(parser.inputToJd(getFromEntry(), getFromEntryCalendar(), false));
		}
	}

	/**
	 * parse to time into jd
	 */
	protected void parseToTime() {
		if (getToEntry() == null) setToJD(Long.MAX_VALUE);
		else {
			// parse time to jd
			FlexibleDateParser parser = new FlexibleDateParser();
			setToJD(parser.inputToJd(getToEntry(), getToEntryCalendar(), true));
		}
	}

	@Override
	public String getTitle() {
		return (getFromEntry() == null && getToEntry() == null)?
				"---":
				((getFromEntry()!=null?getFromEntry():"") + "-"
						+ (getToEntry()!=null?getToEntry():""));
	}
}
