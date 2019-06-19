package org.segrada.rendering.json;

import com.google.inject.Inject;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.ISavedQuery;
import org.segrada.model.prototype.ITag;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

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
 * Static JSON converter for classes
 */
public class JSONConverter {
	@Inject
	private ServletContext servletContext;

	/**
	 * cached base path
	 */
	private String base;

	/**
	 * convert node to json object
	 * @param node to be converted
	 * @return json object
	 * @throws JSONException
	 */
	public JSONObject convertNodeToJSON(INode node) throws JSONException {
		JSONObject o = new JSONObject();

		o.put("id", node.getId());
		o.put("label", node.getTitle());
		o.put("group", "node");
		o.put("url", getBase() + "node/show/" + node.getUid());

		// picture?
		if (node.getPictogram() != null) {
			o.put("image", base + "pictogram/file/" + node.getPictogram().getUid());
			o.put("shape", "image");

			// additional color?
			if (node.getColor() != null) {
				JSONObject font = new JSONObject();
				// color according to brightness
				font.put("color", calculateBrightness(node.getColor())<130?"#ffffff":"#000000");
				font.put("background", node.getColorCode());
				o.put("font", font);
			}
		}
		// no picture, but color
		else if (node.getColor() != null) {
			JSONObject icon = new JSONObject();
			icon.put("color", node.getColorCode());
			o.put("icon", icon);
		}

		return o;
	}

	/**
	 * convert relation to json object
	 * @param relation to be converted
	 * @return json object
	 * @throws JSONException
	 */
	public JSONObject convertRelationToJSON(IRelation relation) throws JSONException {
		JSONObject o = new JSONObject();

		o.put("id", relation.getId());
		o.put("label", relation.getRelationType().getFromTitle());
		o.put("group", "relation");
		o.put("from", relation.getFromEntity().getId());
		o.put("to", relation.getToEntity().getId());
		o.put("url", getBase() + "relation/show/" + relation.getUid());

		if (relation.getColor() != null) {
			JSONObject color = new JSONObject();
			color.put("color", relation.getColorCode());
			o.put("color", color);
		}

		return o;
	}

	/**
	 * convert saved query to json object
	 * @param savedQuery to be converted
	 * @return json object
	 * @throws JSONException
	 */
	public JSONObject convertSavedQueryToJSON(ISavedQuery savedQuery) throws JSONException {
		JSONObject o = new JSONObject();

		o.put("id", savedQuery.getId());
		o.put("uid", savedQuery.getUid());
		o.put("title", savedQuery.getTitle());
		o.put("type", savedQuery.getType());

		return o;
	}

	/**
	 * convert tag to json object
	 * @param tag to be converted
	 * @return json object
	 * @throws JSONException
	 */
	public JSONObject convertTagToJSON(ITag tag) throws JSONException {
		JSONObject o = new JSONObject();

		o.put("id", tag.getId());
		o.put("label", tag.getTitle());
		o.put("group", "tag");
		o.put("url", getBase() + "tag/show/" + tag.getUid());

		return o;
	}

	/**
	 * return JSON object representing an edge between a tag and a target
	 * @param tagId as source
	 * @param targetId as target
	 * @return json object
	 * @throws JSONException
	 */
	public JSONObject createTagEntityConnection(String tagId, String targetId) throws JSONException {
		JSONObject o = new JSONObject();

		o.put("id", tagId + '-' + targetId);
		o.put("group", "tagEdge");
		o.put("from", tagId);
		o.put("to", targetId);

		return o;
	}

	/**
	 * calculate brightness from color (brightness  =  sqrt(.299 R² + .587 G² + .114 B² ))
	 * inspired by http://themergency.com/calculate-text-color-based-on-background-color-brightness/
	 * @param color input color
	 * @return brightness 0-255
	 */
	private static int calculateBrightness(int color) {
		double red = (double) ((color >> 16) & 0x000000FF);
		double green = (double) ((color >> 8) & 0x000000FF);
		double blue = (double) (color & 0x000000FF);

		return (int) Math.sqrt(
				red * red * .241 + green * green * .691 + blue * blue + 0.68
		);
	}

	/**
	 * get base path
	 * @return cached base path
	 */
	private String getBase() {
		// create base, if not exist
		if (base == null) {
			// soft fail servletContext
			if (servletContext != null) {
				base = servletContext.getContextPath();
				if (!base.endsWith("/")) base = base + "/";
			} else base = "/";
		}

		return base;
	}
}
