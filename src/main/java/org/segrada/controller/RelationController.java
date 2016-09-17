package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.Relation;
import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.ITag;
import org.segrada.rendering.json.JSONConverter;
import org.segrada.service.NodeService;
import org.segrada.service.RelationService;
import org.segrada.service.RelationTypeService;
import org.segrada.service.TagService;
import org.segrada.service.base.SegradaService;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
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

	@Inject
	private TagService tagService;

	@Inject
	private JSONConverter jsonConverter;

	@Override
	protected String getBasePath() {
		return "/relation/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("RELATION")
	public Viewable index(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			@QueryParam("search") String search, // empty dummy value to know when we have submitted form
			@QueryParam("minEntry") String minEntry,
			@QueryParam("maxEntry") String maxEntry,
			@QueryParam("tags") List<String> tags,
			@QueryParam("sort") String sortBy, // minJD, maxJD
			@QueryParam("dir") String sortOrder // asc, desc, none
	) {
		return getPaginatedView(page, entriesPerPage, reset, search, minEntry, maxEntry, tags, sortBy, sortOrder, null, null, null, null);
	}

	@GET
	@Path("/by_tag/{tagUid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"RELATION", "TAG"})
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
		filters.put("key", "Relation/by_tag/" + tagUid + "Service"); // has to be named like this in order to make cache work properly
		if (withSubTags != null) filters.put("withSubTags", withSubTags.equals("1"));

		// tags to contain
		List<String> tags = new ArrayList<>(1);
		tags.add(tag.getTitle());

		// create model
		Map<String, Object> model = new HashMap<>();
		model.put("tag", tag);
		model.put("targetId", "#refs-by-tag-" + tag.getUid() + "-relation");
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
	@Path("/by_relation_type/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"RELATION", "RELATION_TYPE"})
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
	@RolesAllowed({"RELATION", "NODE"})
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
	@RolesAllowed("RELATION")
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("RELATION_ADD")
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
	@RolesAllowed({"RELATION_EDIT", "RELATION_EDIT_MINE"})
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({"RELATION_ADD", "RELATION_EDIT", "RELATION_EDIT_MINE"})
	public Response update(Relation entity) {
		return handleUpdate(entity, service);
	}

	@GET
	@Path("/delete/{uid}/{empty}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"RELATION_DELETE", "RELATION_DELETE_MINE"})
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
			// we load the whole data of the entities to include pictograms etc.
			nodes.put(jsonConverter.convertNodeToJSON(nodeService.findById(relation.getFromEntity().getId()))); // add node 1
			nodes.put(jsonConverter.convertNodeToJSON(nodeService.findById(relation.getToEntity().getId()))); // add node 2

			// create edge list
			JSONArray edges = new JSONArray();
			edges.put(jsonConverter.convertRelationToJSON(relation)); // add relation

			// TODO: check for tag links of this relation

			// create response object
			JSONObject response = new JSONObject();

			response.put("nodes", nodes);
			response.put("edges", edges);
			response.put("highlightEdge", relation.getId());

			return response.toString();
		} catch (Exception e) {
			return "{\"error\": \"" + JSONObject.quote(e.getMessage()) + "\"}";
		}
	}
}
