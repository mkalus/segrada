package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.File;
import org.segrada.model.prototype.IFile;
import org.segrada.model.prototype.IPictogram;
import org.segrada.service.FileService;
import org.segrada.service.PictogramService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
 * Controller for files
 */
@Path("/file")
@RequestScoped
public class FileController extends AbstractColoredController<IFile> {
	@Inject
	private FileService service;

	@Inject
	private PictogramService pictogramService;

	@Override
	protected String getBasePath() {
		return "/file/";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index(@QueryParam("page") int page, @QueryParam("entriesPerPage") int entriesPerPage) {
		// TODO: do filters
		return handlePaginatedIndex(service, page, entriesPerPage, null);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
	}

	@GET
	@Path("/file/{uid}")
	public Response download(@PathParam("uid") String uid) {
		try {
			IFile entity = service.findById(service.convertUidToId(uid));
			final InputStream in = (entity == null)?
					getClass().getResourceAsStream("/img/no_image.png"):
					service.getBinaryDataAsStream(entity);
			String mime = (entity == null)?
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

			return Response.ok(output, mime).build();
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
		} else if (entity.getId() == null || entity.getId().isEmpty())
			errors.put("uploadedFile", "error.UploadEmpty");

		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("isNewEntity", entity.getId()==null|| entity.getId().isEmpty());

		// no validation errors: save entity
		if (errors.isEmpty()) {
			if (service.save(entity)) {
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
