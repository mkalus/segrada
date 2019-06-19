package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.Color;
import org.segrada.model.prototype.IColor;
import org.segrada.service.ColorService;
import org.segrada.service.base.SegradaService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Copyright 2015-2019 Maximilian Kalus [segrada@auxnet.de]
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
 * Controller for colors
 */
@Path("/color")
@RequestScoped
public class ColorController extends AbstractBaseController<IColor> {
	@Inject
	private ColorService service;

	@Override
	protected String getBasePath() {
		return "/color/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("COLOR")
	public Viewable index() {
		return handleShowAll(service);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("COLOR")
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("COLOR_ADD")
	public Viewable add() {
		return handleForm(service.createNewInstance());
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"COLOR_EDIT", "COLOR_EDIT_MINE"})
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({"COLOR_ADD", "COLOR_EDIT", "COLOR_EDIT_MINE"})
	public Response update(Color entity) {
		return handleUpdate(entity, service);
	}

	@GET
	@Path("/delete/{uid}/{empty}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"COLOR_DELETE", "COLOR_DELETE_MINE"})
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}
}
