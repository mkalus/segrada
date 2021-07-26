package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.service.ConfigService;
import org.segrada.servlet.SegradaUpdateChecker;
import org.segrada.session.ApplicationSettings;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
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
 * Base controller (root)
 */
@Path("/")
@RequestScoped
public class MainController {
	@Inject
	private ConfigService service;

	@Inject
	private ApplicationSettings applicationSettings;

	@GET
	@Produces(MediaType.TEXT_HTML)
	@PermitAll
	public Viewable index() {
		// get version update variable
		String versionUpdate = service.getValue("versionUpdate");
		if (versionUpdate == null) versionUpdate = "";

		// test application if login is allowed
		String requireLogin = applicationSettings.getSetting("requireLogin");
		boolean showLogout = requireLogin != null && !requireLogin.isEmpty() && requireLogin.equalsIgnoreCase("true");

		// map engine defined in settings
		String mapEngine = applicationSettings.getSetting("map.engine");
		if (mapEngine == null) mapEngine = "ol"; // open layers as default

		// create model map
		Map<String, Object> model = new HashMap<>();
		model.put("version", SegradaUpdateChecker.currentVersion);
		model.put("versionUpdate", versionUpdate);
		model.put("showLogout", showLogout);
		model.put("mapEngine", mapEngine);

		return new Viewable("home", model);
	}
}
