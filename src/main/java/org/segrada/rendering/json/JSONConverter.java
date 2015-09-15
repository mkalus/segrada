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
		o.put("url", "/node/show/" + node.getUid());

		// picture?
		if (node.getPictogram() != null) {
			o.put("image", "/pictogram/file/" + node.getPictogram().getUid());
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
	public static JSONObject convertRelationToJSON(IRelation relation) throws JSONException {
		JSONObject o = new JSONObject();

		o.put("id", relation.getId());
		o.put("label", relation.getRelationType().getFromTitle());
		o.put("group", "relation");
		o.put("from", relation.getFromEntity().getId());
		o.put("to", relation.getToEntity().getId());
		o.put("url", "/relation/show/" + relation.getUid());

		if (relation.getColor() != null) {
			JSONObject color = new JSONObject();
			color.put("color", relation.getColorCode());
			o.put("color", color);
		}

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
}
