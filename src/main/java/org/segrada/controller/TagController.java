package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.Tag;
import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaTaggable;
import org.segrada.service.TagService;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.repository.orientdb.exception.CircularConnectionException;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.util.Sluggify;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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
			@QueryParam("tags") List<String> tags,
			@QueryParam("sort") String sortBy, // title
			@QueryParam("dir") String sortOrder // asc, desc, none
	) {
		// filters:
		Map<String, Object> filters = new HashMap<>();
		if (reset > 0) filters.put("reset", true);
		if (search != null) filters.put("search", search);
		if (tags != null) {
			if (tags.size() == 0 && search != null) filters.put("tags", null);
			else if (tags.size() > 0) {
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

		// handle pagination
		return handlePaginatedIndex(service, page, entriesPerPage, filters);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable show(
			@PathParam("uid") String uid
	) {
		return reallyShow(service.findById(service.convertUidToId(uid)));
	}

	@GET
	@Path("/by_title/{title}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable showByTitle(
			@PathParam("title") String title
	) {
		return reallyShow(service.findByTitle(title, true));
	}

	/**
	 * show action called by methods above
	 * @param tag to show
	 * @return viewable
	 */
	private Viewable reallyShow(ITag tag) {
		// create model map
		Map<String, Object> model = new HashMap<>();

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
	protected <E extends CRUDRepository<ITag>> Response displayUpdateError(ITag entity, AbstractRepositoryService<ITag, E> service) {
		// custom update error

		// create model map
		Map<String, Object> model = new HashMap<>();
		// create errors
		Map<String, String> errors = new HashMap<>();
		errors.put("tags", "error.circularTags");

		// fill model map
		model.put("entity", entity);
		model.put("errors", errors);

		enrichModelForEditingAndSaving(model);

		// return viewable
		return Response.ok(new Viewable(getBasePath() + "form", model)).build();
	}

	@Override
	protected void validateExtra(Map<String, String> errors, ITag entity) {
		super.validateExtra(errors, entity);

		// check duplicate ref
		if (entity != null && entity.getTitle() != null && !entity.getTitle().isEmpty()) {
			ITag toCheck = service.findByTitle(entity.getTitle(), false);
			// also check slug title
			if (toCheck == null)
				toCheck = service.findByTitle(Sluggify.sluggify(entity.getTitle()), true);

			// not the same entity
			if (toCheck != null && !toCheck.getId().equals(entity.getId())) {
				errors.put("title", "error.double");
			}
		}
	}

	@Override
	protected void enrichModelForEditingAndSaving(Map<String, Object> model) {
		super.enrichModelForEditingAndSaving(model);

		ITag entity = (ITag) model.get("entity");

		// get child tags
		List<String> childTags = new LinkedList<>();
		for (SegradaTaggable taggable : service.findByTag(entity.getId(), false, new String[]{"Tag"})) {
			childTags.add(((ITag) taggable).getTitle());
		}

		// tags to list
		String[] tags = new String[childTags.size()];
		tags = childTags.toArray(tags);

		// enrich model with child tags
		entity.setChildTags(tags);
	}

	/**
	 * remove tag from entity
	 * @param referenceUid uid of reference
	 * @param referenceModel reference model, e.g. "node"
	 * @param tagUid source uid to be removed
	 * @return response
	 */
	@GET
	@Path("/remove_tag/{model}/{uid}/{source}")
	@Produces(MediaType.TEXT_HTML)
	public Response removeTag(@PathParam("uid") String referenceUid, @PathParam("model") String referenceModel, @PathParam("source") String tagUid) {
		ITag tag = service.findById(service.convertUidToId(tagUid));

		// null, if empty
		if (referenceModel != null && referenceModel.isEmpty()) referenceModel = null;

		// do removal
		if (tagUid != null && referenceModel != null && referenceUid != null) {
			clearCache(); // delete caches

			service.removeTag(tag.getId(), service.convertUidToId(referenceUid));
		}

		try {
			return Response.seeOther(new URI(getBasePath() + "by_tag/" + tagUid + "/" + referenceModel)).build();
		} catch (URISyntaxException e) {
			return Response.ok(new Viewable("error", e.getMessage())).build();
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
