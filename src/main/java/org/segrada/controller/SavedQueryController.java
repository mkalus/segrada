package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.SavedQuery;
import org.segrada.model.prototype.*;
import org.segrada.model.savedquery.GraphData;
import org.segrada.model.savedquery.GraphSavedQueryDataWorker;
import org.segrada.model.savedquery.SavedQueryDataWorker;
import org.segrada.model.savedquery.factory.SavedQueryDataWorkerFactory;
import org.segrada.rendering.export.Exporter;
import org.segrada.rendering.export.GEXFExporter;
import org.segrada.rendering.json.JSONConverter;
import org.segrada.service.NodeService;
import org.segrada.service.RelationService;
import org.segrada.service.SavedQueryService;
import org.segrada.service.base.SegradaService;
import org.segrada.session.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.HashMap;
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
 * Saved Query controller
 */
@Path("/saved_query")
@RequestScoped
public class SavedQueryController extends AbstractBaseController<ISavedQuery> {
	private static final Logger logger = LoggerFactory.getLogger(SavedQueryController.class);

	@Inject
	SavedQueryService service;

	@Inject
	NodeService nodeService;

	@Inject
	RelationService relationService;

	@Inject
	private Identity identity;

	@Inject
	private JSONConverter jsonConverter;

	@Inject
	private SavedQueryDataWorkerFactory savedQueryDataWorkerFactory;


	@Override
	protected String getBasePath() {
		return "/saved_query/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("GRAPH")
	public Viewable index(
			@QueryParam("title") String title,
			@QueryParam("type") String type
	) {
		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("entities", service.findAllBy(identity.getUser(), type, title));

		return new Viewable(getBasePath() + "index", model);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("GRAPH")
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("GRAPH")
	public Viewable add() {
		return handleForm(service.createNewInstance());
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"GRAPH"})
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({"GRAPH"})
	public Response update(SavedQuery entity) {
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
	@Path("/save")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed("GRAPH")
	public Response save(
			@FormParam("uid") String uid,
			@FormParam("title") String title,
			@FormParam("type") String type,
			@FormParam("data") String data
	) {
		// data sanity?
		if (type == null || type.isEmpty() || data == null || data.isEmpty()) {
			logger.error("Empty data given - aborting save.");
			return Response.serverError().build();
		}

		// get logged in user
		IUser me = identity.getUser();

		// validate data before anything else
		SavedQueryDataWorker worker = savedQueryDataWorkerFactory.produceSavedQueryDataValidator(type);
		if (!worker.validateData(data)) {
			logger.error("Data validation failed: " + data);
			return Response.serverError().build();
		}

		// validation ok, now find entity
		ISavedQuery entity;

		// existing entity?
		if (uid != null && !uid.isEmpty()) {
			entity = service.findById(service.convertUidToId(uid));
			if (entity == null) {
				logger.error("Saved Query entity not found: " + uid);
				return Response.serverError().build();
			}
			if (!entity.getType().equals(type)) {
				logger.error("Saved Query entity types differ: " + uid + " has " + entity.getType() + " but request says " + type);
				return Response.serverError().build();
			}
			if (!entity.getUser().getUid().equals(me.getUid())) {
				logger.error("Access error for Saved Query " + uid);
				return Response.serverError().build();
			}
			logger.debug("Updating Saved Query: " + uid);
		} else {
			entity = service.createNewInstance();
			entity.setType(type);
			entity.setUser(me);
		}

		// updating title and data
		entity.setTitle(title);
		entity.setData(data);

		// save entity
		if (!service.save(entity)) {
			logger.error("Error saving Saved Query.");
			return Response.serverError().build();
		}

		// send ok
		return Response.ok(entity.getUid()).build();
	}

	@GET
	@Path("/find_by")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed("GRAPH")
	public Response findBy(
			@QueryParam("type") String type,
			@QueryParam("title") String title
	) {
		// convert to null if needed
		if (type != null && type.isEmpty()) type = null;
		if (title != null && title.isEmpty()) title = null;

		// find in repository
		JSONArray jsonList = new JSONArray();
		for (ISavedQuery savedQuery : service.findAllBy(identity.getUser(), type, title)) {
			try {
				jsonList.put(jsonConverter.convertSavedQueryToJSON(savedQuery));
			} catch (JSONException e) {
				logger.error("Error converting Saved Query to JSON (" + savedQuery.toString() + ").", e);
			}
		}

		return Response.ok(jsonList.toString()).build();
	}

	@POST
	@Path("/graph/{uid}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@RolesAllowed("GRAPH")
	public String postGraph(@PathParam("uid") String uid) {
		return graph(uid);
	}

	@GET
	@Path("/graph/{uid}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@RolesAllowed("GRAPH")
	public String getGraph(@PathParam("uid") String uid) {
		return graph(uid);
	}

	/**
	 * Handle graph creation
	 * @param uid of node
	 * @return json string to set data
	 */
	public String graph(String uid) {
		ISavedQuery savedQuery = service.findById(service.convertUidToId(uid));

		// not found
		if (savedQuery == null)
			return "{\"error\": \"Not found.\"}";

		// convert back to list of elements via validator
		SavedQueryDataWorker worker = savedQueryDataWorkerFactory.produceSavedQueryDataValidator(savedQuery.getType());
		if (worker == null) {
			logger.error("Saved query type " + savedQuery.getType() + " not supported.");
			return "{\"error\": \"Saved query type " + savedQuery.getType() + " not supported.\"}";
		}

		// extract data from representation
		Map<String, Iterable<SegradaEntity>> extractedData = worker.savedQueryToEntities(savedQuery.getData());
		Map<String, GraphData> dataMap;
		if (savedQuery.getType().equals("graph")) {
			// get coordinates
			dataMap = ((GraphSavedQueryDataWorker) worker).retrieveGraphDataFromData(savedQuery.getData());
		} else dataMap = null;

		// create nodes
		Iterable<SegradaEntity> entities = extractedData.get("nodes");
		JSONArray nodes = new JSONArray();
		for (SegradaEntity entity : entities) {
			try {
				JSONObject o;
				if (entity instanceof INode)
					o = jsonConverter.convertNodeToJSON((INode) entity);
				else if (entity instanceof ITag)
					o = jsonConverter.convertTagToJSON((ITag) entity);
				else throw new JSONException("Unsupported node type " + entity.toString());

				// add dataMap stuff, if applicable
				GraphData dataMapEntry = dataMap.get(entity.getId());
				if (dataMapEntry == null) dataMapEntry = dataMap.get(entity.getUid()); // try both variants to find coordinate
				if (dataMapEntry != null) {
					o.put("x", dataMapEntry.x);
					o.put("y", dataMapEntry.y);

					if (!dataMapEntry.physics) o.put("physics", false);
				}

				// o set, add to list
				nodes.put(o);
			} catch (JSONException e) {
				logger.warn("Could not convert to JSON: " + entity.toString(), e);
			}
		}

		// create edges
		Iterable<SegradaEntity> relations = extractedData.get("edges");
		JSONArray edges = new JSONArray();
		for (SegradaEntity entity : relations) {
			try {
				JSONObject o;

				if (entity instanceof IRelation)
					o = jsonConverter.convertRelationToJSON((IRelation) entity);
				else throw new JSONException("Unsupported edge type " + entity.toString());

				// o set, add to list
				edges.put(o);
			} catch (JSONException e) {
				logger.warn("Could not convert to JSON: " + entity.toString(), e);
			}
		}

		// create response object
		try {
			JSONObject response = new JSONObject();

			response.put("nodes", nodes);
			response.put("edges", edges);

			return response.toString();
		} catch (JSONException e) {
			return "{\"error\": \"" + JSONObject.quote(e.getMessage()) + "\"}";
		}
	}

	@GET
	@Path("/export/{uid}")
	@RolesAllowed("GRAPH")
	public Response export(@PathParam("uid") String uid) {
		Map<String, Iterable<SegradaEntity>> extractedData;
		String title;
		String id;

		if ("all".equals(uid)) {
			// extract data from representation
			extractedData = new HashMap<>();
			extractedData.put("nodes", (Iterable) nodeService.findAll());

			extractedData.put("edges", (Iterable) relationService.findAll());

			title = "";
			id = "graph";
		} else {
			ISavedQuery savedQuery = service.findById(service.convertUidToId(uid));

			// not found
			if (savedQuery == null)
				return Response.serverError().build();

			title = savedQuery.getTitle();
			id = savedQuery.getUid();

			// convert back to list of elements via validator
			SavedQueryDataWorker worker = savedQueryDataWorkerFactory.produceSavedQueryDataValidator(savedQuery.getType());
			if (worker == null) {
				logger.error("Saved query type " + savedQuery.getType() + " not supported.");
				return Response.serverError().build();
			}

			// extract data from representation
			extractedData = worker.savedQueryToEntities(savedQuery.getData());
		}

		// TODO: make this dynamic later on
		Exporter exporter = new GEXFExporter();

		// stream output
		StreamingOutput stream = outputStream -> exporter.export(outputStream, title, extractedData);

		return Response.ok(stream)
				.header("Content-Disposition", "attachment; filename=\"" + exporter.getFileName(id) + "\"")
				.type(exporter.getMediaType()).build();
	}
}
