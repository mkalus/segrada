package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.model.prototype.*;
import org.segrada.model.savedquery.GraphCoordinate;
import org.segrada.model.savedquery.GraphSavedQueryDataWorker;
import org.segrada.model.savedquery.SavedQueryDataWorker;
import org.segrada.model.savedquery.factory.SavedQueryDataWorkerFactory;
import org.segrada.rendering.json.JSONConverter;
import org.segrada.service.SavedQueryService;
import org.segrada.session.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
 * Saved Query controller
 */
@Path("/saved_query")
@RequestScoped
public class SavedQueryController {
	private static final Logger logger = LoggerFactory.getLogger(SavedQueryController.class);

	@Inject
	SavedQueryService service;

	@Inject
	private Identity identity;

	@Inject
	private JSONConverter jsonConverter;

	@Inject
	private SavedQueryDataWorkerFactory savedQueryDataWorkerFactory;

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("GRAPH")
	public String index() {
		return "Not implemented.";
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
		Map<String, List<SegradaEntity>> extractedData = worker.savedQueryToEntities(savedQuery.getData());
		Map<String, GraphCoordinate> coordinateMap;
		if (savedQuery.getType().equals("graph")) {
			// get coordinates
			coordinateMap = ((GraphSavedQueryDataWorker) worker).retrieveCoordinatesFromData(savedQuery.getData());
		} else coordinateMap = null;

		// create nodes
		List<SegradaEntity> entities = extractedData.get("nodes");
		JSONArray nodes = new JSONArray(entities.size());
		for (SegradaEntity entity : entities) {
			try {
				JSONObject o;
				if (entity instanceof INode)
					o = jsonConverter.convertNodeToJSON((INode) entity);
				else if (entity instanceof ITag)
					o = jsonConverter.convertTagToJSON((ITag) entity);
				else throw new JSONException("Unsupported node type " + entity.toString());

				// add coordinate, if applicable
				GraphCoordinate coordinate = coordinateMap.get(entity.getId());
				if (coordinate == null) coordinate = coordinateMap.get(entity.getUid()); // try both variants to find coordinate
				if (coordinate != null) {
					o.put("x", coordinate.x);
					o.put("y", coordinate.y);
				}

				// o set, add to list
				nodes.put(o);
			} catch (JSONException e) {
				logger.warn("Could not convert to JSON: " + entity.toString(), e);
			}
		}

		// create edges
		List<SegradaEntity> relations = extractedData.get("edges");
		JSONArray edges = new JSONArray(relations.size());
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
}
