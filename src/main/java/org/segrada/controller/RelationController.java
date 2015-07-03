package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.Relation;
import org.segrada.model.prototype.IRelation;
import org.segrada.service.RelationService;
import org.segrada.service.RelationTypeService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
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

	@Override
	protected String getBasePath() {
		return "/relation/";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index(@QueryParam("page") int page, @QueryParam("entriesPerPage") int entriesPerPage) {
		// TODO: do filters
		return handlePaginatedIndex(service, page, entriesPerPage, null);
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

		model.put("targetId", "#relations-by-type-" + relationTypeUid);
		model.put("baseUrl", "/relation/by_relation_type/" + relationTypeUid);

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
	public Viewable add() {
		return handleForm(service.createNewInstance());
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
}
