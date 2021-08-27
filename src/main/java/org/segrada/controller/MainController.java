package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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
		if (mapEngine == null) mapEngine = "leaflet"; // leaflet as default

		// create model map
		Map<String, Object> model = new HashMap<>();
		model.put("version", SegradaUpdateChecker.currentVersion);
		model.put("versionUpdate", versionUpdate);
		model.put("showLogout", showLogout);
		model.put("mapEngine", mapEngine);

		try {
			model.put("mapSettings", defineMapSettings(mapEngine));
		} catch (JSONException e) {
			// ignore silently
		}

		return new Viewable("home", model);
	}

	// define map settings as JSON string
	protected String defineMapSettings(String mapEngine) throws JSONException {
		if (!mapEngine.equals("leaflet")) return "";

		// define map settings
		JSONObject mapSettings = new JSONObject();

		// from settings
		mapSettings.put("provider", applicationSettings.getSettingOrDefault("map.provider", "Stamen.TerrainBackground"));
		mapSettings.put("zoom", applicationSettings.getSettingAsInteger("map.defaultZoom", 13));
		mapSettings.put("lat", applicationSettings.getSettingAsInteger("map.defaultLat", 0));
		mapSettings.put("lng", applicationSettings.getSettingAsInteger("map.defaultLng", 0));

		// options
		JSONObject mapOptions = new JSONObject();
		mapSettings.put("options", mapOptions);

		for (Map.Entry<String, String> entry : applicationSettings.getAllSettingsStartingWith("map.options.").entrySet()) {
			mapOptions.put(entry.getKey(), entry.getValue());
		}

		return mapSettings.toString();
	}
}
