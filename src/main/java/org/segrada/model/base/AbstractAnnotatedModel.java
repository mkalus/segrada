package org.segrada.model.base;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.model.prototype.IComment;
import org.segrada.model.prototype.IFile;
import org.segrada.model.prototype.ISourceReference;
import org.segrada.model.prototype.SegradaAnnotatedEntity;

import javax.annotation.Nullable;
import java.util.List;

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
 * Abstract model that extends AbstractColoredModel and keeps comments, files, sources, references and tags
 */
abstract public class AbstractAnnotatedModel extends AbstractColoredModel implements SegradaAnnotatedEntity {
	/**
	 * Tag list
	 */
	private transient String[] tags;

	/**
	 * comment list
	 */
	private transient List<IComment> comments;

	/**
	 * comment list
	 */
	private transient List<IFile> files;

	/**
	 * comment list
	 */
	private transient List<ISourceReference> sourceReferences;


	@Override
	public @Nullable String[] getTags() {
		return tags;
	}

	@Override
	public void setTags(String[] tags) {
		this.tags = tags;
	}

	@Override
	public @Nullable List<IComment> getComments() {
		return comments;
	}

	@Override
	public void setComments(List<IComment> comments) {
		this.comments = comments;
	}

	@Override
	public @Nullable List<IFile> getFiles() {
		return files;
	}

	@Override
	public void setFiles(List<IFile> files) {
		this.files = files;
	}

	@Override
	public @Nullable List<ISourceReference> getSourceReferences() {
		return sourceReferences;
	}

	@Override
	public void setSourceReferences(List<ISourceReference> sourceReferences) {
		this.sourceReferences = sourceReferences;
	}

	@Override
	public boolean equals(Object that) {
		return that != null && this.getClass() == that.getClass() && EqualsBuilder.reflectionEquals(this, that, "created", "modified", "creator", "modifier", "tags", "comments", "files", "sourceReferences");
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "created", "modified", "creator", "modifier", "tags", "comments", "files", "sourceReferences");
	}

	public JSONObject toJSON() {
		JSONObject jsonObject = super.toJSON();

		try {
			if (tags != null && tags.length > 0) {
				JSONArray tagsList = new JSONArray(tags.length);
				for (String tag : tags) {
					tagsList.put(tag);
				}
				jsonObject.put("tags", tagsList);
			}
			// TODO: comments?
			// TODO: files?
			// TODO: sourceReferences?
		} catch (Exception e) {
			// ignore
		}

		return jsonObject;
	}
}
