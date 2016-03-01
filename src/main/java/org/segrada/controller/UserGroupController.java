package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.auth.DenyAdminGroupEdit;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.UserGroup;
import org.segrada.model.prototype.IUserGroup;
import org.segrada.service.UserGroupService;
import org.segrada.service.base.SegradaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
 * Controller for users
 */
@Path("/user_group")
@RequestScoped
public class UserGroupController extends AbstractBaseController<IUserGroup> {
	private static final Logger logger = LoggerFactory.getLogger(UserGroupController.class);

	/**
	 * keeps list of privileges to show in form
	 */
	public static final Map<String, String[]> PRIVILEGES = new LinkedHashMap<String, String[]>() {{
		put("Nodes", new String[]{ "NODE", "NODE_ADD", "NODE_EDIT_MINE", "NODE_EDIT", "NODE_DELETE_MINE", "NODE_DELETE" });
		put("Sources", new String[]{ "SOURCE", "SOURCE_ADD", "SOURCE_EDIT_MINE", "SOURCE_EDIT", "SOURCE_DELETE_MINE", "SOURCE_DELETE" });
		put("Files", new String[]{ "FILE", "FILE_ADD", "FILE_EDIT_MINE", "FILE_EDIT", "FILE_DELETE_MINE", "FILE_DELETE" });
		put("Relations", new String[]{ "RELATION", "RELATION_ADD", "RELATION_EDIT_MINE", "RELATION_EDIT", "RELATION_DELETE_MINE", "RELATION_DELETE" });
		put("Relation_Types", new String[]{ "RELATION_TYPE", "RELATION_TYPE_ADD", "RELATION_TYPE_EDIT_MINE", "RELATION_TYPE_EDIT", "RELATION_TYPE_DELETE_MINE", "RELATION_TYPE_DELETE" });
		put("SourceReferences", new String[]{ "SOURCE_REFERENCE", "SOURCE_REFERENCE_ADD", "SOURCE_REFERENCE_EDIT_MINE", "SOURCE_REFERENCE_EDIT", "SOURCE_REFERENCE_DELETE_MINE", "SOURCE_REFERENCE_DELETE" });
		put("Locations", new String[]{ "LOCATION", "LOCATION_ADD", "LOCATION_EDIT_MINE", "LOCATION_EDIT", "LOCATION_DELETE_MINE", "LOCATION_DELETE" });
		put("Periods", new String[]{ "PERIOD", "PERIOD_ADD", "PERIOD_EDIT_MINE", "PERIOD_EDIT", "PERIOD_DELETE_MINE", "PERIOD_DELETE" });
		put("Pictograms", new String[]{ "PICTOGRAM", "PICTOGRAM_ADD", "PICTOGRAM_EDIT_MINE", "PICTOGRAM_EDIT", "PICTOGRAM_DELETE_MINE", "PICTOGRAM_DELETE" });
		put("Tags", new String[]{ "TAG", "TAG_ADD", "TAG_EDIT_MINE", "TAG_EDIT", "TAG_DELETE_MINE", "TAG_DELETE" });
		put("Colors", new String[]{ "COLOR", "COLOR_ADD", "COLOR_EDIT_MINE", "COLOR_EDIT", "COLOR_DELETE_MINE", "COLOR_DELETE" });
		put("Data", new String[]{ "SEARCH", "GRAPH" });
		put("Administration", new String[]{ "ADMIN", "CLEAR_CACHE" });
		put("Users", new String[]{ "USER", "GROUP", "MY_PROFILE" });
	}};

	/**
	 * keeps list of allowed privileges
	 */
	public static final Set<String> PRIVILEGES_ALLOWED = new HashSet<>(Arrays.asList(
			"NODE", "NODE_ADD", "NODE_EDIT_MINE", "NODE_EDIT", "NODE_DELETE_MINE", "NODE_DELETE",
			"SOURCE", "SOURCE_ADD", "SOURCE_EDIT_MINE", "SOURCE_EDIT", "SOURCE_DELETE_MINE", "SOURCE_DELETE",
			"FILE", "FILE_ADD", "FILE_EDIT_MINE", "FILE_EDIT", "FILE_DELETE_MINE", "FILE_DELETE",
			"RELATION", "RELATION_ADD", "RELATION_EDIT_MINE", "RELATION_EDIT", "RELATION_DELETE_MINE", "RELATION_DELETE",
			"RELATION_TYPE", "RELATION_TYPE_ADD", "RELATION_TYPE_EDIT_MINE", "RELATION_TYPE_EDIT", "RELATION_TYPE_DELETE_MINE", "RELATION_TYPE_DELETE",
			"SOURCE_REFERENCE", "SOURCE_REFERENCE_ADD", "SOURCE_REFERENCE_EDIT_MINE", "SOURCE_REFERENCE_EDIT", "SOURCE_REFERENCE_DELETE_MINE", "SOURCE_REFERENCE_DELETE",
			"LOCATION", "LOCATION_ADD", "LOCATION_EDIT_MINE", "LOCATION_EDIT", "LOCATION_DELETE_MINE", "LOCATION_DELETE",
			"PERIOD", "PERIOD_ADD", "PERIOD_EDIT_MINE", "PERIOD_EDIT", "PERIOD_DELETE_MINE", "PERIOD_DELETE",
			"PICTOGRAM", "PICTOGRAM_ADD", "PICTOGRAM_EDIT_MINE", "PICTOGRAM_EDIT", "PICTOGRAM_DELETE_MINE", "PICTOGRAM_DELETE",
			"TAG", "TAG_ADD", "TAG_EDIT_MINE", "TAG_EDIT", "TAG_DELETE_MINE", "TAG_DELETE",
			"COLOR", "COLOR_ADD", "COLOR_EDIT_MINE", "COLOR_EDIT", "COLOR_DELETE_MINE", "COLOR_DELETE",
			"SEARCH", "GRAPH",
			"ADMIN", "CLEAR_CACHE",
			"USER", "GROUP", "MY_PROFILE"
	));

	/**
	 * translation helper for privileges
	 */
	public static final Map<String, String> PRIVILEGES_TRANSLATION = new HashMap<String, String>() {{
		final String[] prepopulate = new String[]{
				"NODE", "ACCESS", "NODE_ADD", "ADD", "NODE_EDIT_MINE", "EDIT_MINE", "NODE_EDIT", "EDIT", "NODE_DELETE_MINE", "DELETE_MINE", "NODE_DELETE", "DELETE",
				"SOURCE", "ACCESS", "SOURCE_ADD", "ADD", "SOURCE_EDIT_MINE", "EDIT_MINE", "SOURCE_EDIT", "EDIT", "SOURCE_DELETE_MINE", "DELETE_MINE", "SOURCE_DELETE", "DELETE",
				"FILE", "ACCESS", "FILE_ADD", "ADD", "FILE_EDIT_MINE", "EDIT_MINE", "FILE_EDIT", "EDIT", "FILE_DELETE_MINE", "DELETE_MINE", "FILE_DELETE", "DELETE",
				"RELATION", "ACCESS", "RELATION_ADD", "ADD", "RELATION_EDIT_MINE", "EDIT_MINE", "RELATION_EDIT", "EDIT", "RELATION_DELETE_MINE", "DELETE_MINE", "RELATION_DELETE", "DELETE",
				"RELATION_TYPE", "ACCESS", "RELATION_TYPE_ADD", "ADD", "RELATION_TYPE_EDIT_MINE", "EDIT_MINE", "RELATION_TYPE_EDIT", "EDIT", "RELATION_TYPE_DELETE_MINE", "DELETE_MINE", "RELATION_TYPE_DELETE", "DELETE",
				"SOURCE_REFERENCE", "ACCESS", "SOURCE_REFERENCE_ADD", "ADD", "SOURCE_REFERENCE_EDIT_MINE", "EDIT_MINE", "SOURCE_REFERENCE_EDIT", "EDIT", "SOURCE_REFERENCE_DELETE_MINE", "DELETE_MINE", "SOURCE_REFERENCE_DELETE", "DELETE",
				"LOCATION", "ACCESS", "LOCATION_ADD", "ADD", "LOCATION_EDIT_MINE", "EDIT_MINE", "LOCATION_EDIT", "EDIT", "LOCATION_DELETE_MINE", "DELETE_MINE", "LOCATION_DELETE", "DELETE",
				"PERIOD", "ACCESS", "PERIOD_ADD", "ADD", "PERIOD_EDIT_MINE", "EDIT_MINE", "PERIOD_EDIT", "EDIT", "PERIOD_DELETE_MINE", "DELETE_MINE", "PERIOD_DELETE", "DELETE",
				"PICTOGRAM", "ACCESS", "PICTOGRAM_ADD", "ADD", "PICTOGRAM_EDIT_MINE", "EDIT_MINE", "PICTOGRAM_EDIT", "EDIT", "PICTOGRAM_DELETE_MINE", "DELETE_MINE", "PICTOGRAM_DELETE", "DELETE",
				"TAG", "ACCESS", "TAG_ADD", "ADD", "TAG_EDIT_MINE", "EDIT_MINE", "TAG_EDIT", "EDIT", "TAG_DELETE_MINE", "DELETE_MINE", "TAG_DELETE", "DELETE",
				"COLOR", "ACCESS", "COLOR_ADD", "ADD", "COLOR_EDIT_MINE", "EDIT_MINE", "COLOR_EDIT", "EDIT", "COLOR_DELETE_MINE", "DELETE_MINE", "COLOR_DELETE", "DELETE",
				"SEARCH", "SEARCH", "GRAPH", "GRAPH",
				"ADMIN", "ADMIN", "CLEAR_CACHE", "CLEAR_CACHE",
				"USER", "USER_ACCESS", "GROUP", "GROUP_ACCESS", "MY_PROFILE", "MY_PROFILE"
		};

		for (int i = 0; i < prepopulate.length; i+=2)
			put(prepopulate[i], prepopulate[i+1]);
	}};

	@Inject
	private UserGroupService service;

	@Override
	protected String getBasePath() {
		return "/user_group/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("GROUP")
	public Viewable index() {
		return handleShowAll(service);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("GROUP")
	public Viewable show(@PathParam("uid") String uid) {
		// create model map
		Map<String, Object> model = new HashMap<>();

		model.put("entity", service.findById(service.convertUidToId(uid)));
		model.put("privileges", PRIVILEGES);
		model.put("privilegesTranslation", PRIVILEGES_TRANSLATION);

		return new Viewable(getBasePath() + "show", model);
	}

	@GET
	@Path("/add")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("ADMIN")
	public Viewable add() {
		return handleForm(service.createNewInstance());
	}

	@GET
	@Path("/edit/{uid}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("ADMIN")
	@DenyAdminGroupEdit
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@Override
	protected void enrichModelForEditingAndSaving(Map<String, Object> model) {
		model.put("privileges", PRIVILEGES);
		model.put("privilegesTranslation", PRIVILEGES_TRANSLATION);
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed("ADMIN")
	@DenyAdminGroupEdit
	public Response update(UserGroup entity, @Context HttpServletRequest servletRequest) {
		// get all parameters in order to map array-parameters
		final Map<String, String[]> params = servletRequest.getParameterMap();
		for (Map.Entry<String, String[]> param : params.entrySet()) {
			// try to find entries starting with privilege. -> these are my privileges defined in the form
			if (param.getKey().startsWith("privilege.")) {
				final String key = param.getKey().substring(10); // extract key
				if (PRIVILEGES_ALLOWED.contains(key)) { // check if key is actually allowed in this context
					// get values
					boolean value = false;
					for (String v : param.getValue())
						if (v != null && (v.equals("1") || v.equalsIgnoreCase("t") || v.equalsIgnoreCase("true"))) {
							// value is true - everything ok, otherwise there will only be a value containing "0"
							value = true;
							break;
						}
					// set or unset role depending on value
					if (value) entity.setRole(key);
					else entity.unsetRole(key);
				} else logger.error("Privileges not allowed to be set: " + key);
			}
		}
		return handleUpdate(entity, service);
	}

	@GET
	@Path("/delete/{uid}/{empty}")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("ADMIN")
	@DenyAdminGroupEdit
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}
}
