package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.Tag;
import org.segrada.model.prototype.ISource;
import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaTaggable;
import org.segrada.service.TagService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.LinkedList;
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
 * Controller for tags
 */
@Path("/tag")
@RequestScoped
public class TagController extends AbstractBaseController<ITag> {
	@Inject
	private TagService service;

	@Override
	protected String getBasePath() {
		return "/tag/";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			@QueryParam("search") String search,
			@QueryParam("tags") List<String> tags
	) {
		// filters:
		Map<String, Object> filters = new HashMap<>();
		if (reset > 0) filters.put("reset", true);
		if (search != null) filters.put("search", search);
		if (tags != null) {
			if (tags.size() == 0) filters.put("tags", null);
			else {
				String[] tagArray = new String[tags.size()];
				tags.toArray(tagArray);
				filters.put("tags", tagArray);
			}
		}

		// handle pagination
		return handlePaginatedIndex(service, page, entriesPerPage, filters);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable show(
			@PathParam("uid") String uid,
			@QueryParam("name") String name
	) {
		// create model map
		Map<String, Object> model = new HashMap<>();

		// get tag
		ITag tag;
		if (name != null && uid.equals("name")) tag = service.findByTitle(name);
		else tag = service.findById(service.convertUidToId(uid));
		model.put("entity", tag);

		List<String> childTags = new LinkedList<>();
		for (SegradaTaggable taggable : service.findByTag(tag.getId(), false, new String[]{"Tag"})) {
			childTags.add(((ITag) taggable).getTitle());
		}
		model.put("childTags", childTags);

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
	public Response update(Tag entity) {
		return handleUpdate(entity, service);
	}

	@Override
	protected void validateExtra(Map<String, String> errors, ITag entity) {
		super.validateExtra(errors, entity);

		// check duplicate ref
		if (entity != null && entity.getTitle() != null && !entity.getTitle().isEmpty()) {
			ITag toCheck = service.findByTitle(entity.getTitle());

			// not the same entity
			if (toCheck != null && !toCheck.getId().equals(entity.getId())) {
				errors.put("title", "error.double");
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
		for (ITag tag : service.findBySearchTerm(term, 10, true)) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", tag.getId());
				jsonObject.put("title", tag.getTitle());

				jsonArray.put(jsonObject);
			} catch (JSONException e) {
				//IGNORE
			}
		}

		return jsonArray.toString();
	}
}
