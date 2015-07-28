package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.Location;
import org.segrada.model.prototype.ILocation;
import org.segrada.model.prototype.SegradaCoreEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.ColorService;
import org.segrada.service.LocationService;
import org.segrada.service.NodeService;
import org.segrada.service.RelationService;
import org.segrada.service.base.AbstractRepositoryService;

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
 * Controller for locations
 */
@Path("/location")
@RequestScoped
public class LocationController extends AbstractBaseController<ILocation> {
	@Inject
	private LocationService service;

	@Inject
	private NodeService nodeService;

	@Inject
	private RelationService relationService;

	@Override
	protected String getBasePath() {
		return "/location/";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String index() {
		return "Not implemented.";
	}

	@POST
	@Path("/add/{model}/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable add(
			@PathParam("model") String parentModel,
			@PathParam("uid") String parentUid,
			@FormParam("lat") String latitudeValue,
			@FormParam("lng") String longitudeValue
	) {
		boolean error = false;

		// try to parse
		Double latitude = null, longitude = null;
		try {
			latitude = Double.parseDouble(latitudeValue);
		} catch (Exception e) {
			error = true;
		}
		try {
			longitude = Double.parseDouble(longitudeValue);
		} catch (Exception e) {
			error = true;
		}

		// find parent
		AbstractRepositoryService parentService = null;

		if (parentModel == null || parentUid == null || parentModel.isEmpty() || parentUid.isEmpty()) error = true;
		else if (parentModel.equals("Node")) parentService = this.nodeService;
		else if (parentModel.equals("Relation")) parentService = this.relationService;

		// try to find parent model
		SegradaCoreEntity parent = null;
		if (parentService != null) {
			parent = (SegradaCoreEntity) parentService.findById(parentService.convertUidToId(parentUid));
			if (parent == null) error = true;
		} else error = true;

		// ok, no error?
		if (!error) {
			ILocation location = new Location();
			location.setParentId(parentService.convertUidToId(parentUid));
			location.setParentModel(parentModel);
			location.setLongitude(longitude);
			location.setLatitude(latitude);

			// try to save
			if (!service.save(location)) error = true;
		}

		Map<String, Object> model = new HashMap<>();
		model.put("entity", parent);
		model.put("error", error);
		return new Viewable(getBasePath() + "show", model);
	}

	@GET
	@Path("/delete/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable delete(@PathParam("uid") String uid) {
		boolean error = false;

		SegradaCoreEntity parent = null;

		// find element
		ILocation location = service.findById(service.convertUidToId(uid));
		if (location == null) error = true;
		else {
			AbstractRepositoryService parentService = null;

			String parentModel = location.getParentModel();
			String parentId = location.getParentId();

			if (parentModel == null || parentId == null || parentModel.isEmpty() || parentId.isEmpty()) error = true;
			else if (parentModel.equals("Node")) parentService = this.nodeService;
			else if (parentModel.equals("Relation")) parentService = this.relationService;

			// try to delete element
			if (!service.delete(location)) error = true;

			// try to find parent model
			if (parentService != null) {
				parent = (SegradaCoreEntity) parentService.findById(parentId);
				if (parent == null) error = true;
			} else error = true;
		}

		Map<String, Object> model = new HashMap<>();
		model.put("entity", parent);
		model.put("error", error);
		return new Viewable(getBasePath() + "show", model);
	}
}
