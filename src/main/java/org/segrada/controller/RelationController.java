package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.Relation;
import org.segrada.model.prototype.IRelation;
import org.segrada.rendering.json.JSONConverter;
import org.segrada.service.NodeService;
import org.segrada.service.RelationService;
import org.segrada.service.RelationTypeService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Controller for relations
 */
@Path("/relation")
@RequestScoped
public class RelationController extends AbstractColoredController<IRelation> {
	@Inject
	private RelationService service;

	@Inject
	private RelationTypeService relationTypeService;

	@Inject
	private NodeService nodeService;

	@Override
	protected String getBasePath() {
		return "/relation/";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			//@QueryParam("search") String search,
			@QueryParam("minEntry") String minEntry,
			@QueryParam("maxEntry") String maxEntry,
			@QueryParam("tags") List<String> tags,
			@QueryParam("sort") String sortBy, // minJD, maxJD
			@QueryParam("dir") String sortOrder // asc, desc, none
	) {
		// filters:
		Map<String, Object> filters = new HashMap<>();
		if (reset > 0) filters.put("reset", true);
		//if (search != null) filters.put("search", search);
		if (minEntry != null) filters.put("minEntry", minEntry);
		if (maxEntry != null) filters.put("maxEntry", maxEntry);
		if (tags != null) {
			if (tags.size() == 0) filters.put("tags", null);
			else {
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

		// handle pagination
		return handlePaginatedIndex(service, page, entriesPerPage, filters);
	}

	@GET
	@Path("/by_relation_type/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable byRelationType(@PathParam("uid") String relationTypeUid, @QueryParam("page") int page, @QueryParam("entriesPerPage") int entriesPerPage) {
		Map<String, Object> filters = new HashMap<>();

		filters.put("key", "RelationByType" + relationTypeUid); // session key
		filters.put("relationTypeUid", relationTypeUid);

		// add target id
		Map<String, Object> model = new HashMap<>();

		model.put("relationTypeUid", relationTypeUid);
		model.put("targetId", "#relations-by-type-" + relationTypeUid);
		model.put("baseUrl", "/relation/by_relation_type/" + relationTypeUid);

		return handlePaginatedIndex(service, page, entriesPerPage, filters, null, model);
	}

	@GET
	@Path("/by_node/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable byNode(@PathParam("uid") String nodeUid, @QueryParam("page") int page, @QueryParam("entriesPerPage") int entriesPerPage) {
		Map<String, Object> filters = new HashMap<>();

		filters.put("key", "RelationByNode" + nodeUid); // session key
		filters.put("nodeUid", nodeUid);

		// add target id
		Map<String, Object> model = new HashMap<>();

		model.put("nodeUid", nodeUid);
		model.put("hasNode", true);
		model.put("targetId", "#relations-by-node-" + nodeUid);
		model.put("baseUrl", "/relation/by_node/" + nodeUid);

		return handlePaginatedIndex(service, page, entriesPerPage, filters, null, model);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	public Viewable add(
			@QueryParam("relationTypeUid") String relationTypeUid,
			@QueryParam("fromEntityUid") String fromEntityUid,
			@QueryParam("toEntityUid") String toEntityUid
	) {
		IRelation entity = service.createNewInstance();

		// prefill form
		if (relationTypeUid != null)
			entity.setRelationType(relationTypeService.findById(relationTypeService.convertUidToId(relationTypeUid)));
		if (fromEntityUid != null)
			entity.setFromEntity(nodeService.findById(nodeService.convertUidToId(fromEntityUid)));
		if (toEntityUid != null)
			entity.setToEntity(nodeService.findById(nodeService.convertUidToId(toEntityUid)));

		return handleForm(entity);
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response update(Relation entity) {
		return handleUpdate(entity, service);
	}

	@GET
	@Path("/delete/{uid}/{empty}")
	@Produces(MediaType.TEXT_HTML)
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}

	@Override
	protected void enrichModelForEditingAndSaving(Map<String, Object> model) {
		super.enrichModelForEditingAndSaving(model);

		model.put("relationTypes", relationTypeService.findAll());
	}

	@POST
	@Path("/graph/{uid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String postGraph(@PathParam("uid") String uid, String jsonData) {
		return graph(uid, jsonData);
	}

	@GET
	@Path("/graph/{uid}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getGraph(@PathParam("uid") String uid, @QueryParam("data") String jsonData) {
		return graph(uid, jsonData);
	}

	/**
	 * Handle graph creation
	 * @param uid of relation
	 * @param jsonData optional json data (can be null)
	 * @return
	 */
	protected String graph(String uid, String jsonData) {
		try {
			// get node
			IRelation relation = service.findById(service.convertUidToId(uid));
			if (relation == null)
				throw new Exception("Entity " + uid + " not found.");

			// get posted json data
			JSONObject data;
			if (jsonData != null && !jsonData.isEmpty()) data = new JSONObject(jsonData);
			else data = null;

			// create node list
			JSONArray nodes = new JSONArray(2);
			nodes.put(JSONConverter.convertNodeToJSON(relation.getFromEntity())); // add node 1
			nodes.put(JSONConverter.convertNodeToJSON(relation.getToEntity())); // add node 2

			// create edge list
			JSONArray edges = new JSONArray();
			edges.put(JSONConverter.convertRelationToJSON(relation)); // add relation

			// create response object
			JSONObject response = new JSONObject();

			response.put("nodes", nodes);
			response.put("edges", edges);
			//response.put("removeNodes", new JSONArray());
			//response.put("removeEdges", new JSONArray());
			//response.put("highlightNode", null);
			response.put("highlightEdge", relation.getId());

			return response.toString();
		} catch (Exception e) {
			return "{\"error\": \"" + JSONObject.quote(e.getMessage()) + "\"}";
		}
	}
}
