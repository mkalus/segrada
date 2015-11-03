package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.controller.base.AbstractBaseController;
import org.segrada.model.User;
import org.segrada.model.prototype.IUser;
import org.segrada.service.UserService;
import org.segrada.util.PasswordEncoder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
@Path("/user")
@RequestScoped
public class UserController extends AbstractBaseController<IUser> {
	@Inject
	private UserService service;

	@Inject
	private PasswordEncoder passwordEncoder;

	@Override
	protected String getBasePath() {
		return "/user/";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable index() {
		return handleShowAll(service);
	}

	@GET
	@Path("/show/{uid}")
	@Produces(MediaType.TEXT_HTML)
	public Viewable show(@PathParam("uid") String uid) {
		return handleShow(uid, service);
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
	public Response update(User entity) {
		return handleUpdate(entity, service);
	}

	@GET
	@Path("/delete/{uid}/{empty}")
	@Produces(MediaType.TEXT_HTML)
	public Response delete(@PathParam("uid") String uid, @PathParam("empty") String empty) {
		return handleDelete(empty, service.findById(service.convertUidToId(uid)), service);
	}

	@Override
	protected void validateExtra(Map<String, String> errors, IUser entity) {
		// check login
		if (!entity.getLogin().isEmpty()) {
			entity.setLogin(entity.getLogin().toLowerCase());
			Pattern p = Pattern.compile("^[^a-z0-9_]+$");
			if (p.matcher(entity.getLogin()).matches())
				errors.put("login", "error.login.pattern");
		}

		// check if we have a new or updated entity
		if (entity.getId() == null || entity.getId().isEmpty()) {
			// new model: password has to be set and match!

			// cast to user
			User user = (User) entity;

			// now check passwords
			if (user.getPassword().isEmpty() || user.getConfirmPassword().isEmpty()) {
				// empty: add error for second field
				errors.put("confirmPassword", "error.notEmpty");
			} else if (!user.getPassword().equals(user.getConfirmPassword())) {
				// passwords do not match

			} else {
				// set new password
				entity.setPassword(passwordEncoder.encode(entity.getPassword()));
			}

			// validate if there is a double entry of user names
			if (!entity.getLogin().isEmpty() && service.findByLogin(entity.getLogin()) != null) {
				errors.put("login", "error.double");
			}
		} else {
			// updated model: no need to enter password, except if password has been set explicitly

			// get original entry
			IUser originalEntity = service.findById(entity.getId());
			if (originalEntity == null) throw new NotAuthorizedException(entity); // should not happen, but might

			// cast to user
			User user = (User) entity;

			// now check passwords
			if (user.getPassword().isEmpty() && user.getConfirmPassword().isEmpty()) {
				// no changed passwords, just copy
				entity.setPassword(originalEntity.getPassword());

				// remove validation error, if needed
				errors.remove("password");
			} else if (!user.getPassword().equals(user.getConfirmPassword())) {
				// passwords do not match
				errors.put("confirmPassword", "error.passwordsNoMatch");
			} else {
				// set new password
				entity.setPassword(passwordEncoder.encode(entity.getPassword()));
			}

			// validate if there is a double entry of user names - only if user name has changed
			if (!entity.getLogin().isEmpty() && !entity.getLogin().equals(originalEntity.getLogin()) && service.findByLogin(entity.getLogin()) != null) {
				errors.put("login", "error.double");
			}
		}
	}
}
