package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.segrada.model.prototype.ISavedQuery;
import org.segrada.model.prototype.IUser;
import org.segrada.model.prototype.SegradaEntity;
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
	@Produces("application/json")
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

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("GRAPH")
	public Response show(@PathParam("uid") String uid) {
		ISavedQuery savedQuery = service.findById(service.convertUidToId(uid));

		// not found
		if (savedQuery == null) return Response.status(404).build();

		// convert back to list of elements via validator
		SavedQueryDataWorker worker = savedQueryDataWorkerFactory.produceSavedQueryDataValidator(savedQuery.getType());
		if (worker == null) {
			logger.error("Saved query type " + savedQuery.getType() + " not supported.");
			return Response.serverError().build();
		}

		// extract data from representation
		Map<String, List<SegradaEntity>> extractedData = worker.savedQueryToEntities(savedQuery.getData());
		if (savedQuery.getType().equals("graph")) {
			// get coordinates
			Map<String, GraphCoordinate> coordinateMap = ((GraphSavedQueryDataWorker) worker).retrieveCoordinatesFromData(savedQuery.getData());
		}

		//TODO
		return Response.ok().build();
	}
}
