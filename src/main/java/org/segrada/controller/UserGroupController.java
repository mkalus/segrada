package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.UserGroup;
import org.segrada.model.prototype.IUserGroup;
import org.segrada.service.UserGroupService;
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
	public static final SortedMap<String, String[]> PRIVILEGES = new TreeMap<String, String[]>() {{
		put("ADMIN", new String[]{ "ADMIN", "CLEAR_CACHE" });
		put("COLOR", new String[]{ "COLOR", "COLOR_ADD", "COLOR_EDIT_MINE", "COLOR_EDIT", "COLOR_DELETE_MINE", "COLOR_DELETE" });
	}};

	/**
	 * keeps list of allowed privileges
	 */
	public static final Set<String> PRIVILEGES_ALLOWED = new HashSet<>(Arrays.asList(
			"ADMIN", "CLEAR_CACHE",
			"COLOR", "COLOR_ADD", "COLOR_EDIT_MINE", "COLOR_EDIT", "COLOR_DELETE_MINE", "COLOR_DELETE"
	));

	@Inject
	private UserGroupService service;

	@Override
	protected String getBasePath() {
		return "/user_group/";
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
	public Viewable edit(@PathParam("uid") String uid) {
		return handleForm(service.findById(service.convertUidToId(uid)));
	}

	@Override
	protected void enrichModelForEditingAndSaving(Map<String, Object> model) {
		model.put("privileges", PRIVILEGES);
	}

	@POST
	@Path("/update")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@RolesAllowed("ADMIN")
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
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}
}
