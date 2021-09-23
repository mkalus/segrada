package org.segrada.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.ITag;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
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
 * Tag model implementation
 */
public class Tag extends AbstractSegradaEntity implements ITag {
	private static final long serialVersionUID = 1L;

	/**
	 * Main title
	 */
	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String title;

	/**
	 * Synonyms
	 */
	@NotNull(message = "error.notNull")
	private String synonyms = "";

	/**
	 * Tag list - parents
	 */
	private transient String[] tags;

	/**
	 * Tag list - children
	 */
	private transient String[] childTags;


	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getSynonyms() {
		return synonyms;
	}

	@Override
	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}

	@Override
	public @Nullable String[] getTags() {
		return tags;
	}

	@Override
	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public String[] getChildTags() {
		return childTags;
	}

	public void setChildTags(String[] childTags) {
		this.childTags = childTags;
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that, "tags", "childTags", "created", "modified", "creator", "modifier");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "tags", "childTags", "created", "modified", "creator", "modifier");
	}

	@Override
	public JSONObject toJSON() {
		JSONObject jsonObject = super.toJSON();

		try {
			jsonObject.put("title", title);
			if (synonyms != null && !synonyms.equals("")) jsonObject.put("synonyms", synonyms);

			if (tags != null && tags.length > 0) {
				JSONArray tagsList = new JSONArray(tags.length);
				for (String tag : tags) {
					tagsList.put(tag);
				}
				jsonObject.put("parentTags", tagsList);
			}

			if (childTags != null && childTags.length > 0) {
				JSONArray tagsList = new JSONArray(childTags.length);
				for (String tag : childTags) {
					tagsList.put(tag);
				}
				jsonObject.put("childTags", tagsList);
			}
		} catch (Exception e) {
			// ignore
		}

		return jsonObject;
	}
}
