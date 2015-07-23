package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.prototype.ISource;
import org.segrada.model.prototype.ISourceReference;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.service.SourceReferenceService;
import org.segrada.service.SourceService;
import org.segrada.service.base.AbstractRepositoryService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
 * Controller for source references
 */
@Path("/source_reference")
@RequestScoped
public class SourceReferenceController extends AbstractBaseController<ISourceReference> {
	@Inject
	private SourceReferenceService service;

	@Inject
	private SourceService sourceService;

	@Inject
	private Map<String, AbstractRepositoryService> annotatedServices;

	@Override
	protected String getBasePath() {
		return "/source_reference/";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String index() {
		return "Not implemented.";
	}

	/**
	 * show files by reference
	 * @param referenceUid uid of reference
	 * @param referenceModel reference model, e.g. "node"
	 * @param errors json encoded errors
	 * @return view
	 */
	@GET
	@Path("/by_reference/{model}/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable byReference(
			@PathParam("uid") String referenceUid,
			@PathParam("model") String referenceModel,
			@QueryParam("errors") String errors // json object of errors
	) {
		// get reference by uid
		SegradaAnnotatedEntity referenceEntity;
		referenceModel = referenceModel.substring(0,1).toUpperCase() + referenceModel.substring(1);
		AbstractRepositoryService referenceService = annotatedServices.get(referenceModel);
		if (referenceService == null) {
			return new Viewable("error", "referenceService not found");
		} else {
			referenceEntity = (SegradaAnnotatedEntity) referenceService.findById(service.convertUidToId(referenceUid));
		}

		// get references
		List<ISourceReference> entities = service.findByReference(service.convertUidToId(referenceUid));

		// create model map
		Map<String, Object> model = new HashMap<>();
		model.put("uid", referenceUid);
		model.put("model", referenceModel);
		model.put("referenceEntity", referenceEntity);
		model.put("entities", entities);
		model.put("targetId", "#files-by-ref-" + referenceUid);

		if (errors != null) {
			try {
				JSONObject errorData = new JSONObject(errors);
				Map<String, String> errorMessages = new HashMap<>();
				Iterator it = errorData.keys();
				while (it.hasNext()) {
					String key = (String) it.next();
					errorMessages.put(key, errorData.getString(key));
				}
				model.put("errors", errorMessages);
			} catch (Exception e) {
				//TODO: log
			}
		}

		return new Viewable("source_reference/by_reference", model);
	}

	/**
	 * show references by source
	 * @param sourceUid uid of source
	 * @param errors json encoded errors
	 * @return view
	 */
	@GET
	@Path("/by_source/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable bySource(
			@PathParam("uid") String sourceUid,
			@QueryParam("errors") String errors // json object of errors
	) {
		// get source by uid
		ISource source = sourceService.findById(sourceService.convertUidToId(sourceUid));
		if (source == null) return new Viewable("error", "source not found");

		// get references
		List<ISourceReference> entities = service.findBySource(source.getId());

		// create model map
		Map<String, Object> model = new HashMap<>();
		model.put("uid", sourceUid);
		model.put("source", source);
		model.put("entities", entities);
		model.put("targetId", "#references-by-ref-" + sourceUid);

		if (errors != null) {
			try {
				JSONObject errorData = new JSONObject(errors);
				Map<String, String> errorMessages = new HashMap<>();
				Iterator it = errorData.keys();
				while (it.hasNext()) {
					String key = (String) it.next();
					errorMessages.put(key, errorData.getString(key));
				}
				model.put("errors", errorMessages);
			} catch (Exception e) {
				//TODO: log
			}
		}

		return new Viewable("source_reference/by_source", model);
	}
}
