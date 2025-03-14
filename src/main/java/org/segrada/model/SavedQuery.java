package org.segrada.model;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.model.base.AbstractSegradaEntity;
import org.segrada.model.prototype.ISavedQuery;
import org.segrada.model.prototype.IUser;

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
 * Saved Query model implementation (abstract)
 */
public class SavedQuery extends AbstractSegradaEntity implements ISavedQuery {
	private static final long serialVersionUID = 1L;

	/**
	 * Type
	 */
	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String type = "query";

	/**
	 * Main title
	 */
	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String title = "";

	/**
	 * Text
	 */
	@NotNull(message = "error.notNull")
	private String description = "";

	/**
	 * JSON data
	 */
	@NotNull(message = "error.notNull")
	@Size(min=1, message = "error.notEmpty")
	private String data = "{}";

	/**
	 * JSON representation of the data - mini cache for data retrieval
	 */
	private JSONObject rawData;

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
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
	public String getData() {
		return data;
	}

	@Override
	public void setData(String data) {
		this.data = data;

		this.rawData = null; // reset data
	}

	@Override
	public JSONObject getJSONData() {
		if (rawData == null) {
			if (data != null && data.length() != 0) {
				if (data.charAt(0) == '{') {
					try {
						rawData = new JSONObject(data);
					} catch (JSONException e) {
						rawData = new JSONObject(); // set empty on error
					}
				} else if (data.charAt(0) == '[') {
					rawData = new JSONObject();
					try {
						JSONArray a = new JSONArray(data);
						rawData.put("queryParts", a);
					} catch (JSONException e) {
						// ignore
					}
				}
			} else
				this.rawData = new JSONObject();
		}

		return rawData;
	}

	@Override
	public IUser getUser() {
		return getCreator();
	}

	@Override
	public void setUser(IUser user) {
		setCreator(user);
	}

	@Override
	public JSONObject toJSON() {
		JSONObject jsonObject = super.toJSON();

		try {
			jsonObject.put("type", type);
			jsonObject.put("title", title);
			jsonObject.put("description", description);
			jsonObject.put("data", getJSONData());
		} catch (Exception e) {
			// ignore
		}

		return jsonObject;
	}
}
