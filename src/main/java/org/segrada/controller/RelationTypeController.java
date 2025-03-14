package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONArray;
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.RelationType;
import org.segrada.model.prototype.IRelationType;
import org.segrada.service.RelationTypeService;
import org.segrada.service.base.SegradaService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
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
 * Controller for relation types
 */
@Path("/relation_type")
@RequestScoped
public class RelationTypeController extends AbstractColoredController<IRelationType> {
	@Inject
	private RelationTypeService service;

	@Override
	protected String getBasePath() {
		return "/relation_type/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("RELATION_TYPE")
	public Viewable index(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			@QueryParam("search") String search,
			@QueryParam("tags") List<String> tags,
			@QueryParam("sort") String sortBy, // fromToTitle
			@QueryParam("dir") String sortOrder // asc, desc, none
	) {
		// filters:
		Map<String, Object> filters = new HashMap<>();
		if (reset > 0) filters.put("reset", true);
		if (search != null) filters.put("search", search);
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

		// handle pagination
		return handlePaginatedIndex(service, page, entriesPerPage, filters);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("RELATION_TYPE")
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("RELATION_TYPE_ADD")
	public Viewable add() {
		return handleForm(service.createNewInstance());
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"RELATION_TYPE_EDIT", "RELATION_TYPE_EDIT_MINE"})
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({"RELATION_TYPE_ADD", "RELATION_TYPE_EDIT", "RELATION_TYPE_EDIT_MINE"})
	public Response update(RelationType entity) {
		return handleUpdate(entity, service);
	}

	@GET
	@Path("/delete/{uid}/{empty}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"RELATION_TYPE_DELETE", "RELATION_TYPE_DELETE_MINE"})
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}

	@GET
	@Path("/{uid}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@RolesAllowed("RELATION_TYPE")
	public String get(@PathParam("uid") String uid) {
		IRelationType relationType = service.findById(service.convertUidToId(uid));
		if (relationType == null) {
			throw new NotFoundException();
		}

		return relationType.toJSON().toString();
	}


	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@RolesAllowed("RELATION_TYPE")
	public String list() {
		// json array to hold hits
		JSONArray jsonArray = new JSONArray();

		for (IRelationType relationType : service.findAll()) {
			jsonArray.put(relationType.toJSON());
		}

		return jsonArray.toString();
	}
}
