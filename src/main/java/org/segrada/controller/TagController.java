package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.Tag;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.ITag;
import org.segrada.model.prototype.SegradaTaggable;
import org.segrada.rendering.json.JSONConverter;
import org.segrada.service.NodeService;
import org.segrada.service.TagService;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.base.SegradaService;
import org.segrada.service.repository.prototype.CRUDRepository;
import org.segrada.util.Sluggify;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * Controller for tags
 */
@Path("/tag")
@RequestScoped
public class TagController extends AbstractBaseController<ITag> {
	@Inject
	private TagService service;

	@Inject
	private JSONConverter jsonConverter;

	@Override
	protected String getBasePath() {
		return "/tag/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("TAG")
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

		// handle pagination
		return handlePaginatedIndex(service, page, entriesPerPage, filters);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("TAG")
	public Viewable show(
			@PathParam("uid") String uid
	) {
		return reallyShow(service.findById(service.convertUidToId(uid)));
	}

	@GET
	@Path("/by_title/{title}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("TAG")
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

		List<String> childTags = new ArrayList<>();
		for (SegradaTaggable taggable : service.findByTag(tag.getId(), false, new String[]{"Tag"})) {
			childTags.add(((ITag) taggable).getTitle());
		}
		model.put("childTags", childTags);

		return new Viewable(getBasePath() + "show", model);
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("TAG_ADD")
	public Viewable add() {
		return handleForm(service.createNewInstance());
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"TAG_EDIT_MINE", "TAG_EDIT"})
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed({"TAG_ADD", "TAG_EDIT_MINE", "TAG_EDIT"})
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
		List<String> childTags = new ArrayList<>();
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
	//TODO: ACL
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
	@RolesAllowed({"TAG_DELETE_MINE", "TAG_DELETE"})
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@PermitAll
	public String search(@QueryParam("s") String term) {
		// json array to hold hits
		JSONArray jsonArray = new JSONArray();

		// search term finding
		for (ITag tag : service.findBySearchTerm(term, 25, true)) {
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

	@POST
	@Path("/graph/{uid}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@RolesAllowed("GRAPH")
	public String postGraph(@PathParam("uid") String uid, @QueryParam("expand") boolean expand, String jsonData) {
		return graph(uid, expand, jsonData);
	}

	@GET
	@Path("/graph/{uid}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@RolesAllowed("GRAPH")
	public String getGraph(@PathParam("uid") String uid, @QueryParam("expand") boolean expand, @QueryParam("data") String jsonData) {
		return graph(uid, expand, jsonData);
	}

	/**
	 * Handle graph creation
	 * @param uid of node
	 * @param expand expand data to include all connected nodes?
	 * @param jsonData optional json data (can be null)
	 * @return json string to/add remove data
	 */
	protected String graph(String uid, boolean expand, String jsonData) {
		try {
			// get tag
			ITag tag = service.findById(service.convertUidToId(uid));
			if (tag == null)
				throw new Exception("Tag " + uid + " not found.");

			// get posted json data
			JSONObject data;
			if (jsonData != null && !jsonData.isEmpty()) data = new JSONObject(jsonData);
			else data = null;

			// create node list
			JSONArray nodes = new JSONArray(1);
			nodes.put(jsonConverter.convertTagToJSON(tag)); // add node

			// edges to add
			JSONArray edges = new JSONArray();
			if (expand) {
				// get children
				for (SegradaTaggable taggable : service.findByTag(tag.getId(), false, new String[]{"Node"})) {
					nodes.put(jsonConverter.convertNodeToJSON((INode) taggable)); // add node

					// tags have only one direction
					edges.put(jsonConverter.createTagEntityConnection(tag.getId(), taggable.getId()));
				}
			}
			//TODO: create a service method that does this query on a db level
			// add edges between nodes that are on the canvas already
			else if (data != null) {
				JSONArray nodeIds = data.getJSONArray("nodes");
				if (nodeIds != null && nodeIds.length() > 0) {
					for (int i = 0; i < nodeIds.length(); i++) {
						String nodeId = nodeIds.getString(i);
						// connected? -> add edge
						if (service.isTagConnectedTo(tag.getId(), nodeId))
							edges.put(jsonConverter.createTagEntityConnection(tag.getId(), nodeId));
						else if (service.isTagConnectedTo(nodeId, tag.getId())) // vice versa?
							edges.put(jsonConverter.createTagEntityConnection(nodeId, tag.getId()));
						//TODO: make this work on other elements, too, i.e. when a node or relation is added, check for tag links
					}
				}
			}

			// create response object
			JSONObject response = new JSONObject();

			response.put("nodes", nodes);
			response.put("edges", edges);
			response.put("highlightNode", tag.getId());

			return response.toString();
		} catch (Exception e) {
			return "{\"error\": \"" + JSONObject.quote(e.getMessage()) + "\"}";
		}
	}
}
