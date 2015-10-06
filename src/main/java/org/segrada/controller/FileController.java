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
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.File;
import org.segrada.model.prototype.IFile;
import org.segrada.model.prototype.IPictogram;
import org.segrada.model.prototype.SegradaAnnotatedEntity;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.service.FileService;
import org.segrada.service.PictogramService;
import org.segrada.service.base.AbstractRepositoryService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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
	private Map<String, AbstractRepositoryService> annotatedServices;

	@Override
	protected String getBasePath() {
		return "/file/";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index(
			@QueryParam("page") int page,
			@QueryParam("entriesPerPage") int entriesPerPage,
			@QueryParam("reset") int reset,
			@QueryParam("search") String search,
			@QueryParam("tags") List<String> tags,
			@QueryParam("sort") String sortBy, // titleasc
			@QueryParam("dir") String sortOrder // asc, desc, none
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
		// sorting
		if (sortBy != null && sortOrder != null) {
			filters.put("sort", sortBy);
			filters.put("dir", sortOrder);
		}

		return handlePaginatedIndex(service, page, entriesPerPage, filters);
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
		if (errors.size() > 0) {
			try {
				JSONObject jsonObject = new JSONObject();
				for (String key : errors.keySet())
					jsonObject.put(key, errors.get(key));

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
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public String search(@QueryParam("s") String term, @QueryParam("tags") String tags) {
		// json array to hold hits
		JSONArray jsonArray = new JSONArray();

		// explode tags
		String[] tagIds;
		if (tags != null && tags.length() > 0) tagIds = tags.split(",");
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
	@Path("/file/{uid}")
	public Response download(@PathParam("uid") String uid) {
		return getImage(uid, false);
	}

	@GET
	@Path("/thumbnail/{uid}")
	public Response getThumbnail(@PathParam("uid") String uid) {
		return getImage(uid, true);
	}

	/**
	 * actual worker function for download and getThumbnail
	 * @param uid of image
	 * @param thumbnail get thumbnail image?
	 * @return reponse containing image or error
	 */
	private Response getImage(String uid, boolean thumbnail) {
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

			// create file name
			String filename = entity==null?"no_image.png":(thumbnail?"thumb_" + entity.getFilename():entity.getFilename());

			return Response.ok(output, mime).header("Content-Disposition", "attachment; filename=\"" + filename + "\"").build();
		} catch (Exception e) {
			return Response.ok(new Viewable("error", e.getMessage())).build();
		}
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
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
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
	                       @FormDataParam("uploadedFile") final FormDataBodyPart uploadedFilePart) {
		// new or existing entity?
		File entity;
		if (id == null || id.isEmpty()) entity = new File();
		else entity = (File) service.findById(id);

		// get pictogram from id
		IPictogram pictogram = null;
		if (pictogramId != null) pictogram = pictogramService.findById(pictogramId);

		// get tags
		List<String> tagList = new LinkedList<>();
		if (tagParts != null)
			for (FormDataBodyPart part : tagParts)
					tagList.add(part.getValueAs(String.class));
		String tags[] = new String[tagList.size()];
		tagList.toArray(tags);

		// add data
		entity.setTitle(title);
		entity.setDescription(description);
		entity.setDescriptionMarkup(descriptionMarkup);
		entity.setCopyright(copyright);
		entity.setLocation(location);
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
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}
}
