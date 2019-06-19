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
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.Pictogram;
import org.segrada.model.prototype.IPictogram;
import org.segrada.service.PictogramService;
import org.segrada.service.base.SegradaService;
import org.segrada.session.CSRFTokenManager;
import org.segrada.util.Sluggify;

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
import java.util.Arrays;
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
 * Controller for pictograms
 */
@Path("/pictogram")
@RequestScoped
public class PictogramController extends AbstractBaseController<IPictogram> {
	@Inject
	private PictogramService service;

	@Override
	protected String getBasePath() {
		return "/pictogram/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("PICTOGRAM")
	public Viewable index() {
		return handleShowAll(service);
	}

	@GET
	@Path("/show/{uid}")
	@RolesAllowed("PICTOGRAM")
	@Produces(MediaType.TEXT_HTML)
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
	}

	@GET
	@Path("/file/{uid}")
	@PermitAll
	public Response download(@PathParam("uid") String uid) {
		try {
			IPictogram entity = service.findById(service.convertUidToId(uid));
			final InputStream in = (entity == null)?
					getClass().getResourceAsStream("/img/no_image.png"):
					service.getBinaryDataAsStream(entity);

			// set streaming output
			StreamingOutput output = outputStream -> {
				try {
					byte[] buffer = new byte[4096];
					int bytesRead = -1;

					// write bytes read from the input stream into the output stream
					while ((bytesRead = in.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}

					in.close();
					outputStream.close();
				} catch (Exception e) {
					//TODO log
				}
			};

			return Response.ok(output, "image/png").build();
		} catch (Exception e) {
			return Response.ok(new Viewable("error", e.getMessage())).build();
		}
	}

	@GET
	@Path("/by_ref")
	@PermitAll
	public Response downloadRaw(@QueryParam("ref") String iconFileIdentifier) {
		try {
			final InputStream in = service.getBinaryDataAsStream(iconFileIdentifier);

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

			return Response.ok(output, "image/png").build();
		} catch (Exception e) {
			return Response.ok(new Viewable("error", e.getMessage())).build();
		}
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@PermitAll
	public String search(@QueryParam("s") String term) {
		// json array to hold hits
		JSONArray jsonArray = new JSONArray();

		// search term finding
		for (IPictogram pictogram : service.findBySearchTerm(term, 36, true)) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", pictogram.getId());
				jsonObject.put("uid", pictogram.getUid());
				jsonObject.put("title", pictogram.getTitle());

				jsonArray.put(jsonObject);
			} catch (JSONException e) {
				//IGNORE
			}
		}

		return jsonArray.toString();
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("PICTOGRAM_ADD")
	public Viewable add() {
		return handleForm(service.createNewInstance());
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed({"PICTOGRAM_EDIT_MINE", "PICTOGRAM_EDIT"})
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@RolesAllowed({"PICTOGRAM_ADD", "PICTOGRAM_EDIT_MINE", "PICTOGRAM_EDIT"})
	public Response update(@FormDataParam("id") final String id,
	                       @FormDataParam("title") final String title,
	                       @FormDataParam("uploadedImage") final byte[] uploadedImage,
	                       @FormDataParam("uploadedImage") final FormDataContentDisposition uploadedImageDetail,
	                       @FormDataParam("uploadedImage") final FormDataBodyPart uploadedImagePart,
	                       @FormDataParam("_csrf") final String csrf, // _csrf checked locally
	                       @Context HttpServletRequest request) {
		// check csrf
		String sessionToken = CSRFTokenManager.getTokenForSession(request.getSession());

		if (csrf == null || !csrf.equals(sessionToken)) {
			return Response.serverError().build();
		}

		// new or existing entity?
		Pictogram entity;
		if (id == null || id.isEmpty()) entity = new Pictogram();
		else entity = (Pictogram) service.findById(id);

		// add data
		entity.setTitle(Sluggify.normalize(title));

		// validate entity
		Map<String, String> errors = validate(entity);

		// handle file upload
		if (uploadedImage != null && uploadedImage.length > 0) {
			if (Arrays.binarySearch(Pictogram.ALLOWED_IMAGE_TYPES, uploadedImagePart.getMediaType().toString()) < 0) {
				errors.put("uploadedImage", "error.UploadWrongFileTypeOnlyImages");
			} else {
				entity.setData(uploadedImage);
				entity.setMimeType(uploadedImagePart.getMediaType().toString());
				entity.setFileName(uploadedImageDetail.getFileName());
			}
		} else if (entity.getId() == null || entity.getId().isEmpty())
			errors.put("uploadedImage", "error.UploadEmpty");

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
	@RolesAllowed({"PICTOGRAM_DELETE_MINE", "PICTOGRAM_DELETE"})
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}
}
