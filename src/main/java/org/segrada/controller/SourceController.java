package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.Source;
import org.segrada.service.SourceService;

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
 * Controller for sources
 */
@Path("/source")
@RequestScoped
public class SourceController extends AbstractBaseController {
	@Inject
	private SourceService service;

	@Override
	protected String getBasePath() {
		return "/source/";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index() {
		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("paginationInfo", service.paginate(1, 5, null));

		return new Viewable("source/index", model);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable show(@PathParam("uid") String uid) {
		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("entity", service.findById(service.convertUidToId(uid)));

		return new Viewable("source/show", model);
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	public Viewable add() {
		Map<String, Object> model = new HashMap<>();

		model.put("entity", service.createNewInstance());

		return new Viewable("source/add", model);
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_PLAIN)
	public String edit(@PathParam("uid") String uid) {
		return "Not implemented yet.";
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response update(@FormParam("backUrl") String backUrl, Source source) {
		return handleUpdate(backUrl, source, service);
	}

	@GET
	@Path("/delete/{uid}/{empty}")
	@Produces(MediaType.TEXT_HTML)
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}
}
