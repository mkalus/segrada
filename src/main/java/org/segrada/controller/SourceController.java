package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.Source;
import org.segrada.model.prototype.IFile;
import org.segrada.model.prototype.ISource;
import org.segrada.model.prototype.ITag;
import org.segrada.service.SourceService;
import org.segrada.service.TagService;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
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
 * Controller for sources
 */
@Path("/source")
@RequestScoped
public class SourceController extends AbstractColoredController<ISource> {
	@Inject
	private SourceService service;

	@Inject
	private TagService tagService;

	@Override
	protected String getBasePath() {
		return "/source/";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			@QueryParam("search") String search,
			@QueryParam("short_ref") String shortRef,
			@QueryParam("minEntry") String minEntry,
			@QueryParam("maxEntry") String maxEntry,
			@QueryParam("tags") List<String> tags,
			@QueryParam("sort") String sortBy, // shortRef, shortTitleasc
			@QueryParam("dir") String sortOrder // asc, desc, none
	) {
		return getPaginatedView(page, entriesPerPage, reset, search, shortRef, minEntry, maxEntry, tags, sortBy, sortOrder, null, null, null, null);
	}

	@GET
	@Path("/by_tag/{tagUid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable byTag(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			@QueryParam("search") String search,
			@QueryParam("short_ref") String shortRef,
			@QueryParam("minEntry") String minEntry,
			@QueryParam("maxEntry") String maxEntry,
			@PathParam("tagUid") String tagUid,
			@QueryParam("withSubTags") String withSubTags,
			@QueryParam("sort") String sortBy, // titleasc, minJD, maxJD
			@QueryParam("dir") String sortOrder // asc, desc, none
	) {
		// get tag
		ITag tag = tagService.findById(tagService.convertUidToId(tagUid));
		if (tag == null) return new Viewable("error", "Tag not found");

		// predefine filters
		Map<String, Object> filters = new HashMap<>();
		filters.put("key", "Source/by_tag/" + tagUid + "Service"); // has to be named like this in order to make cache work properly
		if (withSubTags != null) filters.put("withSubTags", withSubTags.equals("1"));

		// tags to contain
		List<String> tags = new ArrayList<>(1);
		tags.add(tag.getTitle());

		// create model
		Map<String, Object> model = new HashMap<>();
		model.put("tag", tag);
		model.put("targetId", "#refs-by-tag-" + tag.getUid() + "-source");
		model.put("baseUrl", getBasePath() + "by_tag/" + tag.getUid());

		// reset keep
		String[] resetKeep = new String[]{"tags"};

		return getPaginatedView(page, entriesPerPage, reset, search, shortRef, minEntry, maxEntry, tags, sortBy, sortOrder, resetKeep, "by_tag", model, filters);
	}

	/**
	 * create paginated view
	 */
	protected Viewable getPaginatedView(
			int page,
			int entriesPerPage,
			int reset,
			String search,
			String shortRef,
			String minEntry,
			String maxEntry,
			List<String> tags,
			String sortBy, // titleasc, minJD, maxJD
			String sortOrder, // asc, desc, none
			@Nullable String[] resetKeep,
			@Nullable String viewName,
			@Nullable Map<String, Object> model,
			@Nullable Map<String, Object> filters
	) {
		// filters:
		if (filters == null) filters = new HashMap<>();
		if (reset > 0) filters.put("reset", true);
		if (search != null) filters.put("search", search);
		if (shortRef != null) filters.put("shortRef", shortRef);
		if (minEntry != null) filters.put("minEntry", minEntry);
		if (maxEntry != null) filters.put("maxEntry", maxEntry);
		if (tags != null) {
			if (tags.isEmpty() && search != null) filters.put("tags", null);
			else if (!tags.isEmpty()) {
				String[] tagArray = new String[tags.size()];
				tags.toArray(tagArray);
				filters.put("tags", tagArray);
			}
		}
		// sorting
		if (sortBy != null && sortOrder != null) {
			filters.put("sort", sortBy);
			filters.put("dir", sortOrder);
		}
		// keep reset
		if (resetKeep != null)
			filters.put("resetKeep", resetKeep);

		// handle pagination
		return handlePaginatedIndex(service, page, entriesPerPage, filters, viewName, model);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable show(@PathParam("uid") String uid) {
		// create model map
		Map<String, Object> model = new HashMap<>();

		// get source
		ISource source = service.findById(service.convertUidToId(uid));
		model.put("entity", source);

		// check whether we have a pdf connected to the source: this will be shown on the first page
		IFile pdfFile = null;
		List<IFile> files = source.getFiles();
		if (files != null && !files.isEmpty())
			for (IFile file : files)
				if (file.getMimeType().equals("application/pdf")) {
					pdfFile = file;
					break;
				}
		model.put("pdfFile", pdfFile);

		return new Viewable(getBasePath() + "show", model);
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	public Viewable add() {
		return handleForm(service.createNewInstance());
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response update(Source entity) {
		return handleUpdate(entity, service);
	}

	@Override
	protected void validateExtra(Map<String, String> errors, ISource entity) {
		super.validateExtra(errors, entity);

		// check duplicate ref
		if (entity != null && entity.getShortRef() != null && !entity.getShortRef().isEmpty()) {
			ISource toCheck = service.findByRef(entity.getShortRef());

			// not the same entity
			if (toCheck != null && !toCheck.getId().equals(entity.getId())) {
				errors.put("shortRef", "error.double");
			}
		}
	}

	@GET
	@Path("/delete/{uid}/{empty}")
	@Produces(MediaType.TEXT_HTML)
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public String search(@QueryParam("s") String term) {
		// json array to hold hits
		JSONArray jsonArray = new JSONArray();

		// search term finding
		for (ISource node : service.findBySearchTerm(term, 30, true)) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", node.getId());
				jsonObject.put("uid", node.getUid());
				jsonObject.put("title", node.getTitle());

				jsonArray.put(jsonObject);
			} catch (JSONException e) {
				//IGNORE
			}
		}

		return jsonArray.toString();
	}
}
