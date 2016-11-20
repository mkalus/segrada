package org.segrada.model.prototype;

import org.codehaus.jettison.json.JSONObject;

/**
 * Copyright 2016 Maximilian Kalus [segrada@auxnet.de]
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
 * Saved Query model interface
 */
public interface ISavedQuery extends SegradaEntity {
	String getType();
	void setType(String type);

	void setTitle(String title);

	String getDescription();
	void setDescription(String description);

	String getData();
	void setData(String data);

	// data conversion to JSON
	JSONObject getJSONData();

	IUser getUser();
	void setUser(IUser user);
}
