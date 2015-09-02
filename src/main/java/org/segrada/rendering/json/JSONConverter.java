package org.segrada.rendering.json;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IRelation;

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
 * Static JSON converter for classes
 */
public class JSONConverter {
	/**
	 * convert node to json object
	 * @param node to be converted
	 * @return json object
	 * @throws JSONException
	 */
	public static JSONObject convertNodeToJSON(INode node) throws JSONException {
		JSONObject o = new JSONObject();

		o.put("id", node.getId());
		o.put("label", node.getTitle());
		o.put("group", "node");

		return o;
	}

	/**
	 * convert relation to json object
	 * @param relation to be converted
	 * @return json object
	 * @throws JSONException
	 */
	public static JSONObject convertRelationToJSON(IRelation relation) throws JSONException {
		JSONObject o = new JSONObject();

		o.put("id", relation.getId());
		o.put("label", relation.getRelationType().getFromTitle());
		o.put("group", "relation");
		o.put("from", relation.getFromEntity().getId());
		o.put("to", relation.getToEntity().getId());

		return o;
	}
}
