package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.Node;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.ITag;
import org.segrada.rendering.json.JSONConverter;
import org.segrada.service.NodeService;
import org.segrada.service.RelationService;
import org.segrada.service.TagService;
import org.segrada.service.base.SegradaService;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

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
 * Controller for nodes
 */
@Path("/node")
@RequestScoped
public class NodeController extends AbstractColoredController<INode> {
	@Inject
	private NodeService service;

	@Inject
	private RelationService relationService;

	@Inject
	private TagService tagService;

	@Inject
	private JSONConverter jsonConverter;

	@Override
	protected String getBasePath() {
		return "/node/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("NODE")
	public Viewable index(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			@QueryParam("search") String search,
			@QueryParam("minEntry") String minEntry,
			@QueryParam("maxEntry") String maxEntry,
			@QueryParam("tags") List<String> tags,
			@QueryParam("sort") String sortBy, // titleasc, minJD, maxJD
			@QueryParam("dir") String sortOrder // asc, desc, none
	) {
		return getPaginatedView(page, entriesPerPage, reset, search, minEntry, maxEntry, tags, sortBy, sortOrder, null, null, null, null);
	}

	@GET
	@Path("/by_tag/{tagUid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"NODE", "TAG"})
	public Viewable byTag(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			@QueryParam("search") String search,
			@QueryParam("minEntry") String minEntry,
			@QueryParam("maxEntry") String maxEntry,
			@PathParam("tagUid") String tagUid,
			@QueryParam("withSubTags") String withSubTags,
			@QueryParam("sort") String sortBy, // titleasc, minJD, maxJD
			@QueryParam("dir") String sortOrder // asc, desc, none
	) {
		// get tag
		ITag tag = tagService.findById(tagService.convertUidToId(tagUid));
		if (tag == null) return new Viewable("error", "Tag not found");

		// predefine filters
		Map<String, Object> filters = new HashMap<>();
		filters.put("key", "Node/by_tag/" + tagUid + "Service"); // has to be named like this in order to make cache work properly
		if (withSubTags != null) filters.put("withSubTags", withSubTags.equals("1"));

		// tags to contain
		List<String> tags = new ArrayList<>(1);
		tags.add(tag.getTitle());

		// create model
		Map<String, Object> model = new HashMap<>();
		model.put("tag", tag);
		model.put("targetId", "#refs-by-tag-" + tag.getUid() + "-node");
		model.put("baseUrl", getBasePath() + "by_tag/" + tag.getUid());

		// reset keep
		String[] resetKeep = new String[]{"tags"};

		return getPaginatedView(page, entriesPerPage, reset, search, minEntry, maxEntry, tags, sortBy, sortOrder, resetKeep, "by_tag", model, filters);
	}

	/**
	 * create paginated view
	 */
	protected Viewable getPaginatedView(
			int page,
			int entriesPerPage,
			int reset,
			String search,
			String minEntry,
			String maxEntry,
			List<String> tags,
			String sortBy, // titleasc, minJD, maxJD
			String sortOrder, // asc, desc, none
			@Nullable String[] resetKeep,
			@Nullable String viewName,
			@Nullable Map<String, Object> model,
			@Nullable Map<String, Object> filters
	) {
		// filters:
		if (filters == null) filters = new HashMap<>();
		if (reset > 0) filters.put("reset", true);
		if (search != null) filters.put("search", search);
		if (minEntry != null) filters.put("minEntry", minEntry);
		if (maxEntry != null) filters.put("maxEntry", maxEntry);
		if (tags != null) {
			if (tags.isEmpty() && search != null) filters.put("tags", null);
			else if (!tags.isEmpty()) {
				String[] tagArray = new String[tags.size()];
				tags.toArray(tagArray);
				filters.put("tags", tagArray);
			}
		}
		// sorting
		if (sortBy != null && sortOrder != null) {
			filters.put("sort", sortBy);
			filters.put("dir", sortOrder);
		}
		// keep reset
		if (resetKeep != null)
			filters.put("resetKeep", resetKeep);

		// handle pagination
		return handlePaginatedIndex(service, page, entriesPerPage, filters, viewName, model);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("NODE")
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@PermitAll //TODO: ACL
	public String search(@QueryParam("s") String term, @QueryParam("tags") String tags) {
		// json array to hold hits
		JSONArray jsonArray = new JSONArray();

		// explode tags
		String[] tagIds;
		if (tags != null && !tags.isEmpty()) tagIds = tags.split(",");
		else tagIds = null;

		// search term finding
		for (INode node : service.findBySearchTermAndTags(term, 30, true, tagIds)) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", node.getId());
				jsonObject.put("uid", node.getUid());
				jsonObject.put("title", node.getTitle());

				jsonArray.put(jsonObject);
			} catch (JSONException e) {
				//IGNORE
			}
		}

		return jsonArray.toString();
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("NODE_ADD")
	public Viewable add() {
		return handleForm(service.createNewInstance());
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"NODE_EDIT", "NODE_EDIT_MINE"})
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({"NODE_ADD", "NODE_EDIT", "NODE_EDIT_MINE"})
	public Response update(Node entity) {
		return handleUpdate(entity, service);
	}

	@GET
	@Path("/delete/{uid}/{empty}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"NODE_DELETE", "NODE_DELETE_MINE"})
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}

	@POST
	@Path("/graph/{uid}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@RolesAllowed("GRAPH")
	public String postGraph(@PathParam("uid") String uid, String jsonData) {
		return graph(uid, jsonData);
	}

	@GET
	@Path("/graph/{uid}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@RolesAllowed("GRAPH")
	public String getGraph(@PathParam("uid") String uid, @QueryParam("data") String jsonData) {
		return graph(uid, jsonData);
	}

	/**
	 * Handle graph creation
	 * @param uid of node
	 * @param jsonData optional json data (can be null)
	 * @return json string to/add remove data
	 */
	protected String graph(String uid, String jsonData) {
		try {
			// get node
			INode node = service.findById(service.convertUidToId(uid));
			if (node == null)
				throw new Exception("Entity " + uid + " not found.");

			// get posted json data
			JSONObject data;
			if (jsonData != null && !jsonData.isEmpty()) data = new JSONObject(jsonData);
			else data = null;

			// create node list
			JSONArray nodes = new JSONArray(1);
			nodes.put(jsonConverter.convertNodeToJSON(node)); // add node

			// add edges between nodes that are on the canvas already
			JSONArray edges = new JSONArray();
			//TODO: create a service method that does this query on a db level
			if (data != null) {
				JSONArray nodeIds = data.getJSONArray("nodes");
				if (nodeIds != null && nodeIds.length() > 0) {
					// only if there are other elements on the canvas already

					// -> create set for faster finding of ids
					Set<String> nodeIdSet = new HashSet<>(nodeIds.length());
					for (int i = 0; i < nodeIds.length(); i++)
						nodeIdSet.add(nodeIds.getString(i));

					// -> create set for edges, too
					JSONArray edgeIds = data.getJSONArray("edges");
					Set<String> edgeIdSet = new HashSet<>();
					if (edgeIds != null)
						for (int i = 0; i < edgeIds.length(); i++)
							edgeIdSet.add(edgeIds.getString(i));

					// -> find my edges
					for (IRelation relation : relationService.findByRelation(node)) {
						// edge not on canvas?
						if (!edgeIdSet.contains(relation.getId())) {
							INode otherNode = relation.getToEntity().getId().equals(node.getId())?relation.getFromEntity():relation.getToEntity();
							// node on canvas?
							if (nodeIdSet.contains(otherNode.getId())) {
								// -> add edge
								edges.put(jsonConverter.convertRelationToJSON(relation));
							}
						}
					}
					// TODO: check for tag links of this node
				}
			}

			// create response object
			JSONObject response = new JSONObject();

			response.put("nodes", nodes);
			response.put("edges", edges);
			response.put("highlightNode", node.getId());

			return response.toString();
		} catch (Exception e) {
			return "{\"error\": \"" + JSONObject.quote(e.getMessage()) + "\"}";
		}
	}
}
