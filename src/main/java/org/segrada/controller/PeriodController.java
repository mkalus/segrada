package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.Period;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IPeriod;
import org.segrada.model.prototype.SegradaCoreEntity;
import org.segrada.service.ColorService;
import org.segrada.service.NodeService;
import org.segrada.service.PeriodService;
import org.segrada.service.RelationService;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.base.SegradaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

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
 * Controller for periods
 */
@Path("/period")
@RequestScoped
public class PeriodController extends AbstractBaseController<IPeriod> {
	private static final Logger logger = LoggerFactory.getLogger(PeriodController.class);

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

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("PERIOD")
	public String index() {
		return "Not implemented.";
	}

	@POST
	@Path("/add/{model}/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("PERIOD_ADD")
	public Viewable add(
			@PathParam("model") String parentModel,
			@PathParam("uid") String parentUid,
			@FormParam("fromEntry") String fromEntry,
			@FormParam("toEntry") String toEntry,
			@FormParam("fromEntryCalendar") String fromEntryCalendar,
			@FormParam("toEntryCalendar") String toEntryCalendar,
			@FormParam("fromFuzzyFlagsCa") String fromFuzzyFlagsCa,
			@FormParam("fromFuzzyFlagsUncertain") String fromFuzzyFlagsUncertain,
			@FormParam("toFuzzyFlagsCa") String toFuzzyFlagsCa,
			@FormParam("toFuzzyFlagsUncertain") String toFuzzyFlagsUncertain,
			@FormParam("comment") String comment,
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
		@SuppressWarnings("unchecked")
		SegradaCoreEntity parent = null;
		if (parentService != null) {
			parent = (SegradaCoreEntity) parentService.findById(parentService.convertUidToId(parentUid));
			if (parent == null) return new Viewable("error", "Parent not found.");
		} else return new Viewable("error", "Parent definitions were incorrect.");

		// period or point of time?
		boolean hidePeriod = isPeriodValue == null || isPeriodValue.isEmpty() || !isPeriodValue.equals("1");
		if (hidePeriod) {
			toEntry = fromEntry;
			toFuzzyFlagsCa = fromFuzzyFlagsCa;
			toFuzzyFlagsUncertain = fromFuzzyFlagsUncertain;
			toEntryCalendar = fromEntryCalendar;
		}

		// pre validation errors
		Map<String, String> preValidationErrors = new HashMap<>();

		// create new entity
		IPeriod entity = new Period();
		if (id != null && !id.isEmpty()) entity.setId(id);
		entity.setParentId(service.convertUidToId(parentUid));
		entity.setParentModel(parentModel);
		entity.setFromEntryCalendar(fromEntryCalendar); // calendars first!
		entity.setToEntryCalendar(toEntryCalendar);
		try {
			entity.setFromEntry(fromEntry);
		} catch (Throwable e) {
			// setting date has failed: add error
			preValidationErrors.put("fromJD", "error.calendar.incorrect");
		}
		try {
			entity.setToEntry(toEntry);
		} catch (Throwable e) {
			// setting date has failed: add error
			preValidationErrors.put("toJD", "error.calendar.incorrect");
		}
		entity.setComment(comment);
		// fuzzy flags
		if (fromFuzzyFlagsCa != null) entity.addFuzzyFromFlag('c');
		if (fromFuzzyFlagsUncertain != null) entity.addFuzzyFromFlag('?');
		if (toFuzzyFlagsCa != null) entity.addFuzzyToFlag('c');
		if (toFuzzyFlagsUncertain != null) entity.addFuzzyToFlag('?');

		// validate entity
		Map<String, String> errors = validate(entity, preValidationErrors);

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

			clearCache(); // delete caches
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
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"PERIOD_EDIT", "PERIOD_EDIT_MINE"})
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({"PERIOD_EDIT", "PERIOD_EDIT_MINE"})
	public Response update(
			Period entity,
			@FormParam("fromFuzzyFlagsCa") String fromFuzzyFlagsCa,
			@FormParam("fromFuzzyFlagsUncertain") String fromFuzzyFlagsUncertain,
			@FormParam("toFuzzyFlagsCa") String toFuzzyFlagsCa,
			@FormParam("toFuzzyFlagsUncertain") String toFuzzyFlagsUncertain,
			@FormParam("isPeriod") String isPeriodValue
	) {
		// period or point of time?
		boolean hidePeriod = isPeriodValue == null || isPeriodValue.isEmpty() || !isPeriodValue.equals("1");
		if (hidePeriod) {
			entity.setToEntry(entity.getFromEntry());
			entity.setToEntryCalendar(entity.getFromEntryCalendar());
			toFuzzyFlagsCa = fromFuzzyFlagsCa;
			toFuzzyFlagsUncertain = fromFuzzyFlagsUncertain;
		}

		// fuzzy flags
		if (fromFuzzyFlagsCa != null) entity.addFuzzyFromFlag('c'); else entity.deleteFuzzyFromFlag('c');
		if (fromFuzzyFlagsUncertain != null) entity.addFuzzyFromFlag('?'); else entity.deleteFuzzyFromFlag('?');
		if (toFuzzyFlagsCa != null) entity.addFuzzyToFlag('c'); else entity.deleteFuzzyToFlag('c');
		if (toFuzzyFlagsUncertain != null) entity.addFuzzyToFlag('?'); else entity.deleteFuzzyToFlag('?');

		Response response = handleUpdate(entity, service);
		if (response.getStatus() != 200) { // redirect means that element has been saved successfully
			// update parent model, too - this will update the period of the model
			if (entity.getParentModel().equals("Node"))
				nodeService.save(nodeService.findById(entity.getParentId()));
			else if (entity.getParentModel().equals("Relation"))
				nodeService.save(nodeService.findById(entity.getParentId()));
			else logger.error("No such enriched model while updating period: " + entity.getModelName());

			// return "success" response
			Map<String, Object> model = new HashMap<>();
			model.put("entity", entity);
			return Response.ok(new Viewable(getBasePath() + "updateSuccess", model)).build();
		}
		return response;
	}

	@Override
	protected void validateExtra(Map<String, String> errors, IPeriod entity) {
		// only check existing entities
		if (entity.getId() != null && !entity.getId().isEmpty()) {
			try {
				entity.setFromEntry(entity.getFromEntry());
			} catch (Throwable e) {
				// setting date has failed: add error
				errors.put("fromJD", "error.calendar.incorrect");
			}
			try {
				entity.setToEntry(entity.getToEntry());
			} catch (Throwable e) {
				// setting date has failed: add error
				errors.put("toJD", "error.calendar.incorrect");
			}
		}
	}

	@GET
	@Path("/delete/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"PERIOD_DELETE", "PERIOD_DELETE_MINE"})
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
		} else logger.error("No such enriched model while deleting period: " + entity.getModelName());

		clearCache(); // delete caches

		// empty response
		return Response.ok().build();
	}
}
