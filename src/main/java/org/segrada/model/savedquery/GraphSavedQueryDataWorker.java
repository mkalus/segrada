package org.segrada.model.savedquery;

import com.google.inject.Inject;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.NodeService;
import org.segrada.service.RelationService;
import org.segrada.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * Validator for saved graph data (JSON)
 */
public class GraphSavedQueryDataWorker implements SavedQueryDataWorker {
	private static final Logger logger = LoggerFactory.getLogger(GraphSavedQueryDataWorker.class);

	@Inject
	private NodeService nodeService;

	@Inject
	private TagService tagService;

	@Inject
	private RelationService relationService;

	@Override
	public boolean validateData(String data) {
		if (data == null || data.isEmpty()) return false;

		// should be a JSON object
		try {
			JSONObject o = new JSONObject(data);

			// check data integrity of nodes array
			JSONArray nodes = (JSONArray) o.get("nodes");
			if (nodes.length() == 0) return false; // at least one node should be in the graph

			if (!structureValid(nodes)) return false;
			//TODO: we might want to check for nodes not existing etc, but this makes more sense during reverse validation

			// check data integrity of edges array
			JSONArray edges = (JSONArray) o.get("edges");
			if (!structureValid(edges)) return false;
		} catch (JSONException e) {
			logger.error("Error converting data to JSON " + data, e);
			return false;
		}

		return true;
	}

	/**
	 * helper to analyse json arrays
	 * @param jsonArray to analyse
	 * @return true if valid structure
	 */
	private boolean structureValid(JSONArray jsonArray) {
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject structure = jsonArray.getJSONObject(i);
				// check structures existing
				if (!structure.has("group")) return false;
				if (!(structure.has("id") || structure.has("uid"))) return false;
			}
		} catch (JSONException e) {
			logger.error("Error converting data to JSON " + jsonArray.toString(), e);
			return false;
		}

		return true;
	}

	@Override
	public Map<String, Iterable<SegradaEntity>> savedQueryToEntities(String data) {
		try {
			Map<String, Iterable<SegradaEntity>> returnData = new HashMap<>();

			JSONObject o = new JSONObject(data);

			// first get nodes
			JSONArray nodes = (JSONArray) o.get("nodes");
			List<SegradaEntity> entities = new LinkedList<>();

			for (int i = 0; i < nodes.length(); i++) {
				JSONObject node = nodes.getJSONObject(i);

				if ("node".equals(node.get("group"))) {
					// get node

					// get id/uid
					String id = node.has("id")?node.getString("id"):nodeService.convertUidToId(node.getString("uid"));
					INode entity = nodeService.findById(id);
					if (entity != null) {
						entities.add(entity);
					} else
						logger.warn("Node with id " + id + " not found in db - skipping for graph creation.");
				} else if ("tag".equals(node.get("group"))) {
					// get tag

					// get id/uid
					String id = node.has("id")?node.getString("id"):tagService.convertUidToId(node.getString("uid"));
					ITag entity = tagService.findById(id);
					if (entity != null) {
						entities.add(entity);
					} else
						logger.warn("Tag with id " + id + " not found in db - skipping for graph creation.");
				} else
					throw new JSONException("Unsupported node type in " + node.toString());
			}

			// add entity list to map
			returnData.put("nodes", entities);


			// now get edges
			JSONArray edges = (JSONArray) o.get("edges");
			List<SegradaEntity> relations = new LinkedList<>();
			for (int i = 0; i < edges.length(); i++) {
				JSONObject edge = edges.getJSONObject(i);

				if (!"relation".equals(edge.get("group")))
					throw new JSONException("Unsupported relation type in " + edge.toString());

				// get id/uid
				String id = edge.has("id")?edge.getString("id"):relationService.convertUidToId(edge.getString("uid"));
				// find relation
				IRelation relation = relationService.findById(id);
				if (relation != null) {
					relations.add(relation);
				} else
					logger.warn("Relation with id " + id + " not found in db - skipping for graph creation.");
			}
			// add relation list to map
			returnData.put("edges", relations);


			return returnData;
		} catch (JSONException e) {
			logger.error("Error converting data to JSON in savedQueryToEntities " + data, e);
			return null;
		}
	}

	/**
	 * get graph data from JSON data
	 * @param data JSON string
	 * @return map of ids to graph data entries
	 */
	public Map<String, GraphData> retrieveGraphDataFromData(String data) {
		try {
			Map<String, GraphData> returnData = new HashMap<>();

			JSONObject o = new JSONObject(data);

			JSONArray nodes = (JSONArray) o.get("nodes");
			for (int i = 0; i < nodes.length(); i++) {
				JSONObject node = nodes.getJSONObject(i);

				if (node.has("x") && node.has("y")) {
					String id = node.has("id")?node.getString("id"):node.getString("uid");

					boolean physics = !node.has("physics") || node.getBoolean("physics");

					returnData.put(id, new GraphData(node.getInt("x"), node.getInt("y"), physics));
				}
			}

			return returnData;
		} catch (JSONException e) {
			logger.error("Error converting data to JSON in retrieveGraphDataFromData " + data, e);
			return null;
		}
	}
}
