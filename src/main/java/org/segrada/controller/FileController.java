package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.auth.CheckSecondParameterForModelName;
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.File;
import org.segrada.model.prototype.*;
import org.segrada.service.FileService;
import org.segrada.service.PictogramService;
import org.segrada.service.TagService;
import org.segrada.service.base.AbstractRepositoryService;
import org.segrada.service.base.SegradaService;
import org.segrada.session.CSRFTokenManager;
import org.segrada.util.Sluggify;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

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
 * Controller for files
 */
@Path("/file")
@RequestScoped
public class FileController extends AbstractColoredController<IFile> {
	@Inject
	private FileService service;

	@Inject
	private PictogramService pictogramService;

	@Inject
	private TagService tagService;

	@Inject
	private Map<String, AbstractRepositoryService> annotatedServices;

	@Override
	protected String getBasePath() {
		return "/file/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("FILE")
	public Viewable index(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			@QueryParam("search") String search,
			@QueryParam("minEntry") String minEntry,
			@QueryParam("maxEntry") String maxEntry,
			@QueryParam("tags") List<String> tags,
			@QueryParam("sort") String sortBy, // titleasc
			@QueryParam("dir") String sortOrder // asc, desc, none
	) {
		return getPaginatedView(page, entriesPerPage, reset, search, minEntry, maxEntry, tags, sortBy, sortOrder, null, null, null, null);
	}

	@GET
	@Path("/by_tag/{tagUid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"FILE", "TAG"})
	public Viewable byTag(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			@QueryParam("search") String search,
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
		filters.put("key", "File/by_tag/" + tagUid + "Service"); // has to be named like this in order to make cache work properly
		if (withSubTags != null) filters.put("withSubTags", withSubTags.equals("1"));

		// tags to contain
		List<String> tags = new ArrayList<>(1);
		tags.add(tag.getTitle());

		// create model
		Map<String, Object> model = new HashMap<>();
		model.put("tag", tag);
		model.put("targetId", "#refs-by-tag-" + tag.getUid() + "-file");
		model.put("baseUrl", getBasePath() + "by_tag/" + tag.getUid());

		// reset keep
		String[] resetKeep = new String[]{"tags"};

		return getPaginatedView(page, entriesPerPage, reset, search, minEntry, maxEntry, tags, sortBy, sortOrder, resetKeep, "by_tag", model, filters);
	}

	/**
	 * create paginated view
	 */
	protected Viewable getPaginatedView(
			int page,
			int entriesPerPage,
			int reset,
			String search,
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

	/**
	 * show entities by files reference
	 * @param fileUid uid of file
	 * @param referenceModel contain hits by this
	 * @param errors json encoded errors
	 * @return view
	 */
	@GET
	@Path("/by_file/{uid}/{model}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"FILE"})
	@CheckSecondParameterForModelName
	public Viewable byFile(
			@PathParam("uid") String fileUid,
			@PathParam("model") String referenceModel,
			@QueryParam("errors") String errors // json object of errors
	) {
		IFile file = service.findById(service.convertUidToId(fileUid));

		// null, if empty
		if (referenceModel != null && referenceModel.isEmpty()) referenceModel = null;
		String modelName = referenceModel.substring(0, 1).toUpperCase() + referenceModel.substring(1);

		List<SegradaEntity> entities = service.findByFile(file.getId(), modelName);

		// create model map
		Map<String, Object> model = new HashMap<>();
		model.put("uid", fileUid);
		model.put("model", referenceModel);
		model.put("modelName", modelName);
		model.put("file", file);
		model.put("entities", entities);
		model.put("targetId", "#refs-by-file-" + fileUid + "-" + referenceModel);

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

		return new Viewable("file/by_file", model);
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
	@RolesAllowed("FILE")
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
		List<IFile> entities = service.findByReference(service.convertUidToId(referenceUid), referenceEntity.getModelName().equals("File"));

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

		return new Viewable("file/by_reference", model);
	}

	/**
	 * add a reference to an entity
	 * @param referenceUid uid of reference
	 * @param referenceModel reference model, e.g. "node"
	 * @param sourceId source id to be added
	 * @return response
	 */
	@GET
	@Path("/add_reference/{model}/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("FILE_REFERENCE_ADD")
	public Response addReference(@PathParam("uid") String referenceUid, @PathParam("model") String referenceModel, @QueryParam("source") String sourceId) {
		// create error map
		Map<String, String> errors = new HashMap<>();

		// find source
		IFile source = service.findById(sourceId);
		if (source == null) errors.put("source", "error.notNull");

		// find reference model
		SegradaAnnotatedEntity referenceEntity;
		referenceModel = referenceModel.substring(0,1).toUpperCase() + referenceModel.substring(1);
		AbstractRepositoryService referenceService = annotatedServices.get(referenceModel);
		if (referenceService == null) {
			errors.put("model", "error.notNull");
			referenceEntity = null;
		} else {
			referenceEntity = (SegradaAnnotatedEntity) referenceService.findById(service.convertUidToId(referenceUid));
		}

		// do connect
		if (referenceEntity != null && source != null)
			service.connectFileToEntity(source, referenceEntity);

		clearCache(); // delete caches

		String add = "";
		if (!errors.isEmpty()) {
			try {
				JSONObject jsonObject = new JSONObject();
				for (Map.Entry<String, String> errorEntry : errors.entrySet())
					jsonObject.put(errorEntry.getKey(), errorEntry.getValue());

				add = "?errors=" + URLEncoder.encode(jsonObject.toString(), "UTF-8");
			} catch (Exception e) {
				//TODO: log
			}
		}

		try {
			return Response.seeOther(new URI(getBasePath() + "by_reference/" + referenceModel + "/" + referenceUid + add)).build();
		} catch (URISyntaxException e) {
			return Response.ok(new Viewable("error", e.getMessage())).build();
		}
	}

	/**
	 * remove file reference from entity
	 * @param referenceUid uid of reference
	 * @param referenceModel reference model, e.g. "node"
	 * @param sourceUid source uid to be removed
	 * @return response
	 */
	@GET
	@Path("/remove_reference/{model}/{uid}/{source}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("FILE_REFERENCE_DELETE")
	public Response removeReference(@PathParam("uid") String referenceUid, @PathParam("model") String referenceModel, @PathParam("source") String sourceUid) {
		// find source
		IFile source = service.findById(service.convertUidToId(sourceUid));

		// find reference model
		SegradaAnnotatedEntity referenceEntity;
		referenceModel = referenceModel.substring(0,1).toUpperCase() + referenceModel.substring(1);
		AbstractRepositoryService referenceService = annotatedServices.get(referenceModel);
		if (referenceService == null) {
			referenceEntity = null;
		} else {
			referenceEntity = (SegradaAnnotatedEntity) referenceService.findById(service.convertUidToId(referenceUid));
		}

		// do removal
		if (referenceEntity != null && source != null) {
			clearCache(); // delete caches

			service.removeFileFromEntity(source, referenceEntity);

			// also delete other direction, if file
			if (referenceEntity.getModelName().equals("File"))
				service.removeFileFromEntity((IFile) referenceEntity, source);
		}

		try {
			return Response.seeOther(new URI(getBasePath() + "by_reference/" + referenceModel + "/" + referenceUid)).build();
		} catch (URISyntaxException e) {
			return Response.ok(new Viewable("error", e.getMessage())).build();
		}
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("FILE")
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@PermitAll //TODO: ACL
	public String search(@QueryParam("s") String term, @QueryParam("tags") String tags) {
		// json array to hold hits
		JSONArray jsonArray = new JSONArray();

		// explode tags
		String[] tagIds;
		if (tags != null && !tags.isEmpty()) tagIds = tags.split(",");
		else tagIds = null;

		// search term finding
		for (IFile node : service.search(term)) {
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

	@GET
	@Path("/download/{uid}")
	@PermitAll //TODO: ACL for full view?
	public Response download(@PathParam("uid") String uid) {
		return getImage(uid, false, true);
	}

	@GET
	@Path("/get/{uid}")
	@PermitAll //TODO: ACL for full view?
	public Response stream(@PathParam("uid") String uid) {
		return getImage(uid, false, false);
	}

	@GET
	@Path("/thumbnail/{uid}")
	@PermitAll
	public Response getThumbnail(@PathParam("uid") String uid) {
		return getImage(uid, true, false);
	}

	/**
	 * actual worker function for download and getThumbnail
	 * @param uid of image
	 * @param thumbnail get thumbnail image?
	 * @param forceDownload force download?
	 * @return reponse containing image or error
	 */
	private Response getImage(String uid, boolean thumbnail, boolean forceDownload) {
		try {
			IFile entity = service.findById(service.convertUidToId(uid));
			final InputStream in = (entity == null)?
					getClass().getResourceAsStream("/img/no_image.png"):
					service.getBinaryDataAsStream(thumbnail?entity.getThumbFileIdentifier():entity.getFileIdentifier());
			String mime = (thumbnail || entity == null)?
					"image/png":entity.getMimeType();

			// set streaming output
			StreamingOutput output = outputStream -> {
				byte[] buffer = new byte[4096];
				int bytesRead = -1;

				// write bytes read from the input stream into the output stream
				while ((bytesRead = in.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				in.close();
				outputStream.close();
			};

			// build response
			Response.ResponseBuilder responseBuilder = Response.ok(output, mime);
			if (entity.getFileSize() != null)
				responseBuilder.header("Content-Length", String.valueOf(entity.getFileSize()));

			// if download is forced, add attachment header
			if (forceDownload) {
				// create file name
				String filename = entity==null?"no_image.png":(thumbnail?"thumb_" + entity.getFilename():entity.getFilename());
				responseBuilder.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			}

			return responseBuilder.build();
		} catch (Exception e) {
			return Response.ok(new Viewable("error", e.getMessage())).build();
		}
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("FILE_ADD")
	public Viewable add() {
		return handleForm(service.createNewInstance());
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"FILE_EDIT", "FILE_EDIT_MINE"})
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@RolesAllowed({"FILE_ADD", "FILE_EDIT", "FILE_EDIT_MINE"})
	public Response update(@FormDataParam("id") final String id,
	                       @FormDataParam("title") final String title,
	                       @FormDataParam("description") final String description,
	                       @FormDataParam("descriptionMarkup") final String descriptionMarkup,
	                       @FormDataParam("copyright") final String copyright,
	                       @FormDataParam("location") final String location,
	                       @FormDataParam("indexFullText") final String indexFullText,
	                       @FormDataParam("containFile") final String containFile,
	                       @FormDataParam("color") final String color,
	                       @FormDataParam("pictogram") final String pictogramId,
	                       @FormDataParam("tags") final List<FormDataBodyPart> tagParts,
	                       @FormDataParam("uploadedFile") final byte[] uploadedFile,
	                       @FormDataParam("uploadedFile") final FormDataContentDisposition uploadedFileDetail,
	                       @FormDataParam("uploadedFile") final FormDataBodyPart uploadedFilePart,
	                       @FormDataParam("_csrf") final String csrf, // _csrf checked locally
	                       @Context HttpServletRequest request) {
		// check csrf
		String sessionToken = CSRFTokenManager.getTokenForSession(request.getSession());

		if (csrf == null || !csrf.equals(sessionToken)) {
			return Response.serverError().build();
		}

		// new or existing entity?
		File entity;
		if (id == null || id.isEmpty()) entity = new File();
		else entity = (File) service.findById(id);

		// get pictogram from id
		IPictogram pictogram = null;
		if (pictogramId != null) pictogram = pictogramService.findById(pictogramId);

		// get tags
		List<String> tagList;
		if (tagParts != null) {
			tagList = new ArrayList<>(tagParts.size());
			for (FormDataBodyPart part : tagParts)
				tagList.add(part.getValueAs(String.class));
		} else tagList = new ArrayList<>(0);
		String[] tags = new String[tagList.size()];
		tagList.toArray(tags);

		// add data
		entity.setTitle(Sluggify.normalize(title));
		entity.setDescription(Sluggify.normalize(description));
		entity.setDescriptionMarkup(descriptionMarkup);
		entity.setCopyright(Sluggify.normalize(copyright));
		entity.setLocation(Sluggify.normalize(location));
		entity.setIndexFullText(indexFullText != null && indexFullText.equals("1"));
		entity.setContainFile(containFile != null && containFile.equals("1"));
		entity.setColorCode(color);
		entity.setPictogram(pictogram);
		entity.setTags(tags);

		// prefill entity before validation in order to not have certain errors
		if (entity.getMimeType() == null)
			entity.setMimeType(uploadedFilePart.getMediaType().toString());
		if (entity.getFilename() == null)
			entity.setFilename(uploadedFileDetail.getFileName());

		// validate entity
		Map<String, String> errors = validate(entity);

		// handle file upload
		if (uploadedFile != null && uploadedFile.length > 0) {
			entity.setData(uploadedFile);
			entity.setMimeType(uploadedFilePart.getMediaType().toString());
			entity.setFilename(uploadedFileDetail.getFileName());
			entity.setFileSize(new Long(uploadedFile.length));

			// fix mime type when uploading pdfs with Firefox
			if (entity.getMimeType().equals("application/x-download") && entity.getFilename().endsWith(".pdf"))
				entity.setMimeType("application/pdf");
		} else if (entity.getId() == null || entity.getId().isEmpty())
			errors.put("uploadedFile", "error.UploadEmpty");

		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("isNewEntity", entity.getId()==null|| entity.getId().isEmpty());

		// no validation errors: save entity
		if (errors.isEmpty()) {
			if (service.save(entity)) {
				clearCache(); // delete caches

				// remember tags and colors
				rememberLastTags(tags, entity.getModelName());
				rememberLastColor(color, entity.getModelName());
				rememberLastPictogram(entity.getPictogram(), entity.getModelName());

				//OK - redirect to show
				try {
					return Response.seeOther(new URI(getBasePath() + "show/" + entity.getUid())).build();
				} catch (URISyntaxException e) {
					return Response.ok(new Viewable("error", e.getMessage())).build();
				}
			} else return Response.ok(new Viewable("error", "SAVE failed.")).build();
		}

		// fill model map
		model.put("entity", entity);
		model.put("errors", errors);

		enrichModelForEditingAndSaving(model);

		// return viewable
		return Response.ok(new Viewable(getBasePath() + "form", model)).build();
	}

	@GET
	@Path("/delete/{uid}/{empty}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"FILE_DELETE", "FILE_DELETE_MINE"})
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}
}
