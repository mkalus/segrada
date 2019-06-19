package org.segrada.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.segrada.model.base.AbstractColoredModel;
import org.segrada.model.prototype.IRelationType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
 * Relation type model implementation
 */
public class RelationType extends AbstractColoredModel implements IRelationType {
	private static final long serialVersionUID = 1L;

	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String fromTitle = "";

	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String toTitle = "";

	private String[] fromTags;

	private String[] toTags;

	private String fromTagIds;
	private String toTagIds;

	/**
	 * Tag list
	 */
	private transient String[] tags;

	/**
	 * Description
	 */
	@NotNull(message = "error.notNull")
	private String description = "";

	/**
	 * Markup of description field
	 */
	@NotNull(message = "error.notNull")
	private String descriptionMarkup = "default";

	@Override
	public String getFromTitle() {
		return fromTitle;
	}

	@Override
	public void setFromTitle(String fromTitle) {
		this.fromTitle = fromTitle;
	}

	@Override
	public String getToTitle() {
		return toTitle;
	}

	@Override
	public void setToTitle(String toTitle) {
		this.toTitle = toTitle;
	}

	@Override
	public String[] getFromTags() {
		return fromTags;
	}

	@Override
	public void setFromTags(String[] fromTags) {
		this.fromTags = fromTags;
	}

	@Override
	public String[] getToTags() {
		return toTags;
	}

	@Override
	public void setToTags(String[] toTags) {
		this.toTags = toTags;
	}

	@Override
	public String getFromTagIds() {
		return fromTagIds;
	}

	public void setFromTagIds(String fromTagIds) {
		this.fromTagIds = fromTagIds;
	}

	@Override
	public String getToTagIds() {
		return toTagIds;
	}

	public void setToTagIds(String toTagIds) {
		this.toTagIds = toTagIds;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescriptionMarkup() {
		return descriptionMarkup;
	}

	@Override
	public void setDescriptionMarkup(String descriptionMarkup) {
		this.descriptionMarkup = descriptionMarkup;
	}

	@Override
	public String[] getTags() {
		return tags;
	}

	@Override
	public void setTags(String[] tags) {
		this.tags = tags;
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that, "tags", "created", "modified", "creator", "modifier");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "tags", "created", "modified", "creator", "modifier");
	}

	@Override
	public String getTitle() {
		return getFromTitle() + "⇒" + getToTitle();
	}
}
