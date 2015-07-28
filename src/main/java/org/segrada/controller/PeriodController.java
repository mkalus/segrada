package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.Period;
import org.segrada.model.prototype.IPeriod;
import org.segrada.model.prototype.SegradaCoreEntity;
import org.segrada.service.ColorService;
import org.segrada.service.NodeService;
import org.segrada.service.PeriodService;
import org.segrada.service.RelationService;
import org.segrada.service.base.AbstractRepositoryService;

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
 * Controller for periods
 */
@Path("/period")
@RequestScoped
public class PeriodController extends AbstractBaseController<IPeriod> {
	@Inject
	private PeriodService service;

	@Inject
	private NodeService nodeService;

	@Inject
	private RelationService relationService;

	@Override
	protected String getBasePath() {
		return "/period/";
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
			@FormParam("fromEntry") String fromEntry,
			@FormParam("toEntry") String toEntry,
			@FormParam("isPeriod") String isPeriodValue,
			@FormParam("id") String id
	) {
		// find parent
		AbstractRepositoryService parentService = null;

		if (parentModel == null || parentUid == null || parentModel.isEmpty() || parentUid.isEmpty())
			return new Viewable("error", "Parent not defined properly.");
		else if (parentModel.equals("Node")) parentService = this.nodeService;
		else if (parentModel.equals("Relation")) parentService = this.relationService;

		// try to find parent model
		SegradaCoreEntity parent = null;
		if (parentService != null) {
			parent = (SegradaCoreEntity) parentService.findById(parentService.convertUidToId(parentUid));
			if (parent == null) return new Viewable("error", "Parent not found.");
		} else return new Viewable("error", "Parent definitions were incorrect.");

		// period or point of time?
		boolean hidePeriod = isPeriodValue == null || isPeriodValue.isEmpty() || !isPeriodValue.equals("1");
		if (hidePeriod) toEntry = fromEntry;

		// create new entity
		IPeriod entity = new Period();
		if (id != null && !id.isEmpty()) entity.setId(id);
		entity.setParentId(service.convertUidToId(parentUid));
		entity.setParentModel(parentModel);
		entity.setFromEntry(fromEntry);
		entity.setToEntry(toEntry);

		// validate entity
		Map<String, String> errors = validate(entity);

		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("isNewEntity", entity.getId()==null|| entity.getId().isEmpty());

		// no validation errors: save entity
		if (errors.isEmpty()) {
			if (!service.save(entity))
				return new Viewable("error", "SAVE failed.");

			model.put("entity", null);
			model.put("errors", null);
			model.put("hidePeriod", false);

			// save parent service in order to save from/to date
			parentService.save(parent);
		} else {
			model.put("entity", entity);
			model.put("errors", errors);
			model.put("hidePeriod", hidePeriod);
		}

		// fill model map
		model.put("parent", parent);

		enrichModelForEditingAndSaving(model);

		// return viewable
		return new Viewable(getBasePath() + "form", model);
	}

	@GET
	@Path("/delete/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Response delete(@PathParam("uid") String uid) {
		// get entity
		IPeriod entity = service.findById(service.convertUidToId(uid));

		if (!service.delete(entity)) {
			return Response.ok(new Viewable("error", "DELETE failed.")).build();
		}

		// find parent
		AbstractRepositoryService parentService = null;

		String parentModel = entity.getParentModel();
		String parentId = entity.getParentId();

		if (parentModel.equals("Node")) parentService = this.nodeService;
		else if (parentModel.equals("Relation")) parentService = this.relationService;

		// update parent
		if (parentService != null) {
			if (!parentService.save(parentService.findById(parentId)))
				return Response.ok(new Viewable("error", "Parent update failed.")).build();
		}

		// empty response
		return Response.ok().build();
	}
}
