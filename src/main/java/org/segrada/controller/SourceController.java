package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.model.Source;
import org.segrada.service.ColorService;
import org.segrada.service.SourceService;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
public class SourceController {
	@Inject
	private SourceService service;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String index() {
		return "Not implemented yet.";
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	public Viewable add() {
		Map<String, Object> model = new HashMap<>();

		model.put("source", service.createNewInstance());

		return new Viewable("source/add", model);
	}

	//https://jersey.java.net/documentation/latest/bean-validation.html

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String update(Source source, String backUrl) {
		System.out.println("source:" + source);
		System.out.println("backUrl:" + backUrl); //TODO

		return "redirect:/source/add";
	}
}
