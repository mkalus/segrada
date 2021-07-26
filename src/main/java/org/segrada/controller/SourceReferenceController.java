package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.SourceReference;
import org.segrada.model.prototype.ISource;
import org.segrada.model.prototype.ISourceReference;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.service.ColorService;
import org.segrada.service.SourceReferenceService;
import org.segrada.service.SourceService;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.base.SegradaService;
import org.segrada.service.util.PaginationInfo;
import org.segrada.session.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URLEncoder;
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
 * Controller for source references
 */
@Path("/source_reference")
@RequestScoped
public class SourceReferenceController extends AbstractColoredController<ISourceReference> {
	private static final Logger logger = LoggerFactory.getLogger(SourceReferenceController.class);

	@Inject
	private SourceReferenceService service;

	@Inject
	private SourceService sourceService;

	@Inject
	private Map<String, AbstractRepositoryService> annotatedServices;

	@Inject
	protected ColorService colorService;

	@Inject
	private Identity identity;

	@Override
	protected String getBasePath() {
		return "/source_reference/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("SOURCE_REFERENCE")
	public String index() {
		return "Not implemented.";
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"SOURCE_REFERENCE_ADD", "SOURCE_REFERENCE_EDIT", "SOURCE_REFERENCE_EDIT_MINE"})
	public Viewable edit(
			@PathParam("uid") String uid,
			@QueryParam("backUrl") String backUrl
	) {
		ISourceReference entity = service.findById(service.convertUidToId(uid));

		if (entity == null)
			return new Viewable("error", "Could not find entity in database.");

		Map<String, Object> model = new HashMap<>();

		model.put("isNewEntity", entity.getId()==null|| entity.getId().isEmpty());
		model.put("entity", entity);
		model.put("backUrl", backUrl);

		enrichModelForEditingAndSaving(model);

		return new Viewable(getBasePath() + "form", model);
	}

	@GET
	@Path("/delete/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"SOURCE_REFERENCE_DELETE", "SOURCE_REFERENCE_DELETE_MINE"})
	public Response delete(@PathParam("uid") String uid, @QueryParam("backUrl") String backUrl) {
		if (!service.delete(service.findById(service.convertUidToId(uid)))) {
			return Response.ok(new Viewable("error", "DELETE failed.")).build();
		}
		clearCache(); // delete caches

		return Response.seeOther(URI.create(backUrl)).build();
	}

	@POST
	@Path("/update")
	@RolesAllowed({"SOURCE_REFERENCE_ADD", "SOURCE_REFERENCE_EDIT", "SOURCE_REFERENCE_EDIT_MINE"})
	public Response update(
			@FormParam("id") String id,
			@FormParam("referenceId") String referenceId,
			@FormParam("referenceModel") String referenceModel,
			@FormParam("sourceId") String sourceId,
			@FormParam("referenceText") String referenceText,
			@FormParam("roleOfNode") String roleOfNode,
			@FormParam("color") String color,
			@FormParam("backUrl") String backUrl
	) {
		String error = null;

		// create or load?
		ISourceReference sourceReference;
		if (id != null && !id.isEmpty()) sourceReference = service.findById(id);
		else sourceReference = new SourceReference();

		// sanity checks
		if (sourceReference == null) error = "error.entityNotFound";
		if (referenceId == null || referenceId.isEmpty() || referenceModel == null || referenceModel.isEmpty()) error = "error.referenceEmpty";
		if (sourceId == null || sourceId.isEmpty()) error = "error.sourceEmpty";

		// no errors, yet: load reference and source
		if (error == null) {
			ISource source = sourceReference.getSource();
			if (source == null || !source.getId().equals(sourceId)) {
				// load source
				source = sourceService.findById(sourceId);
				if (source == null) error = "error.sourceEmpty";
				sourceReference.setSource(source);
			}
			SegradaAnnotatedEntity referenceEntity = sourceReference.getReference();
			if (referenceEntity == null || !referenceEntity.getId().equals(referenceId)) {
				// load reference
				referenceModel = referenceModel.substring(0,1).toUpperCase() + referenceModel.substring(1);
				AbstractRepositoryService referenceService = annotatedServices.get(referenceModel);
				if (referenceService == null) error = "error.referenceEmpty";
				else referenceEntity = (SegradaAnnotatedEntity) referenceService.findById(referenceId);
				if (referenceEntity == null) error = "error.referenceEmpty";
				sourceReference.setReference(referenceEntity);
			}
			// update text
			if (referenceText != null && referenceText.isEmpty()) referenceText = null;
			sourceReference.setReferenceText(referenceText);
			// update roleOfNode
			if (roleOfNode != null && roleOfNode.isEmpty()) roleOfNode = null;
			sourceReference.setRoleOfNode(roleOfNode);
			sourceReference.setColorCode(color);

			clearCache(); // delete caches
		}

		// no errors: save
		if (error == null && !service.save(sourceReference)) error = "error.whileSaving";

		if (error != null)
			try {
				return Response.seeOther(URI.create(backUrl + "?error=" + URLEncoder.encode(error, "UTF-8"))).build();
			} catch (Exception e) {
				logger.warn("Could not encode error.", e);
			}

		return Response.seeOther(URI.create(backUrl)).build();
	}

	/**
	 * show files by reference
	 * @param referenceUid uid of reference
	 * @param referenceModel reference model, e.g. "node"
	 * @param error error message string
	 * @return view
	 */
	@GET
	@Path("/by_reference/{model}/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("SOURCE_REFERENCE")
	public Viewable byReference(
			@PathParam("uid") String referenceUid,
			@PathParam("model") String referenceModel,
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("error") String error,
			@QueryParam("sort") String sortBy, // titleasc, roleOfNode
			@QueryParam("dir") String sortOrder // asc, desc, none
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

		// not admin and not both node and relation access
		String limitToClass = getAccessLimit();

		// get references
		final String key = "Source_ref/by_ref/" + referenceModel + "/" + referenceUid; // shortened a bit
		Map<String, Object> filters = this.createSortingFilter(sortBy, sortOrder, key);

		// entries per page?
		entriesPerPage = this.createEntriesPerPage(entriesPerPage, key);

		PaginationInfo<ISourceReference> paginationInfo = service.findByReference(service.convertUidToId(referenceUid), page, entriesPerPage, limitToClass, filters);

		// create model map
		Map<String, Object> model = new HashMap<>();
		model.put("uid", referenceUid);
		model.put("model", referenceModel);
		model.put("referenceEntity", referenceEntity);
		model.put("paginationInfo", paginationInfo);
		model.put("targetId", "#sources-by-ref-" + referenceUid);
		model.put("error", error);
		model.put("colors", colorService.findAll());
		model.put("filters", filters);

		Map<String, Object> saved = new HashMap<>();
		if (filters.containsKey("sort") && filters.containsKey("dir")) {
			saved.put("sort", filters.get("sort"));
			saved.put("dir", filters.get("dir"));
		}
		saved.put("entriesPerPage", paginationInfo.getEntriesPerPage());
		// save to session
		session.setAttribute(key, saved);

		return new Viewable("source_reference/by_reference", model);
	}

	/**
	 * show references by source
	 * @param sourceUid uid of source
	 * @param error error message string
	 * @return view
	 */
	@GET
	@Path("/by_source/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"SOURCE_REFERENCE", "SOURCE"})
	public Viewable bySource(
			@PathParam("uid") String sourceUid,
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("error") String error,
			@QueryParam("sort") String sortBy, // titleasc, roleOfNode
			@QueryParam("dir") String sortOrder // asc, desc, none
	) {
		// get source by uid
		ISource source = sourceService.findById(sourceService.convertUidToId(sourceUid));
		if (source == null) return new Viewable("error", "source not found");

		// not admin and not both node and relation access
		String limitToClass = getAccessLimit();

		// get references
		final String key = "Source_reference/by_source/" + sourceUid + "Service";
		Map<String, Object> filters = this.createSortingFilter(sortBy, sortOrder, key);

		// entries per page?
		entriesPerPage = this.createEntriesPerPage(entriesPerPage, key);

		PaginationInfo<ISourceReference> paginationInfo = service.findBySource(source.getId(), page, entriesPerPage, limitToClass, filters);

		// create model map
		Map<String, Object> model = new HashMap<>();
		model.put("uid", sourceUid);
		model.put("source", source);
		model.put("paginationInfo", paginationInfo);
		model.put("targetId", "#references-by-ref-" + sourceUid);
		model.put("error", error);
		model.put("filters", filters);

		Map<String, Object> saved = new HashMap<>();
		if (filters.containsKey("sort") && filters.containsKey("dir")) {
			saved.put("sort", filters.get("sort"));
			saved.put("dir", filters.get("dir"));
		}
		saved.put("entriesPerPage", paginationInfo.getEntriesPerPage());
		// save to session
		session.setAttribute(key, saved);

		return new Viewable("source_reference/by_source", model);
	}

	/**
	 * Create sorting filter for source references
	 * @param sortBy sort by string: titleasc, roleOfNode
	 * @param sortOrder sort order string asc/desc/none
	 * @param key cache key
	 * @return created filter (can be an empty has map)
	 */
	private Map<String, Object> createSortingFilter(String sortBy, String sortOrder, String key) {
		Map<String, Object> filters = new HashMap<>();

		if (sortBy != null && sortOrder != null) {
			filters.put("sort", sortBy);
			filters.put("dir", sortOrder);
		} else {
			// get filter entries from session
			Object o = session.getAttribute(key);
			if (o != null && o instanceof Map) { // we have a session object saved - now copy filters
				Map<String, Object> sessionFilter = (Map<String, Object>) o;
				if (sessionFilter.containsKey("sort") && sessionFilter.containsKey("dir")) {
					filters.put("sort", sessionFilter.get("sort").toString());
					filters.put("dir", sessionFilter.get("dir").toString());
				}
			}
		}

		return filters;
	}

	/**
	 * get entries per page, possibly saved in session
	 * @param entriesPerPage
	 * @param key
	 * @return
	 */
	private int createEntriesPerPage(int entriesPerPage, String key) {
		// set by param
		if (entriesPerPage > 0) {
			return  entriesPerPage;
		}

		// get filter entries from session
		Object o = session.getAttribute(key);
		if (o != null && o instanceof Map) { // we have a session object saved - now copy filters
			Map<String, Object> sessionFilter = (Map<String, Object>) o;
			Object f = sessionFilter.get("entriesPerPage");
			if (f instanceof Integer) {
				return (int) f;
			}
		}

		return entriesPerPage;
	}

	/**
	 * helper that returns access limits, if needed
	 * @return null or class access is limited to
	 */
	private String getAccessLimit() {
		// either admin or all required access rights
		if (identity.hasAccess("ADMIN") || (identity.hasAccess("NODE") && identity.hasAccess("RELATION"))) return null;
		return identity.hasAccess("NODE")?"Node":"Relation";
	}
}
