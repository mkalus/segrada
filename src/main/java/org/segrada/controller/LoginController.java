package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.model.prototype.IUser;
import org.segrada.service.UserService;
import org.segrada.session.ApplicationSettings;
import org.segrada.session.Identity;
import org.segrada.util.PasswordEncoder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
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
 * Base controller (root)
 */
@Path("/login")
@RequestScoped
public class LoginController {
	@Inject
	private ApplicationSettings applicationSettings;

	@Inject
	private PasswordEncoder passwordEncoder;

	@Inject
	private UserService service;

	@Inject
	private Identity identity;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response login() {
		// test application if login is allowed
		String requireLogin = applicationSettings.getSetting("requireLogin");
		if (requireLogin == null || requireLogin.isEmpty() || !requireLogin.equalsIgnoreCase("true")) {
			return Response.ok(new Viewable("error", "Login not allowed")).build();
		}

		// already logged in?
		if (identity.getName() != null)
			try {
				return Response.seeOther(new URI("/")).build();
			} catch (URISyntaxException e) {
				return Response.ok(new Viewable("error", e.getMessage())).build();
			}

		// is there a login named admin?
		IUser user = service.findByLogin("admin");
		boolean defaultAdminActive = user != null && passwordEncoder.matches("password", user.getPassword());

		// create model map
		Map<String, Object> model = new HashMap<>();
		model.put("defaultAdminActive", defaultAdminActive);

		return Response.ok(new Viewable("login", model)).build();
	}

	@POST
	@Produces(MediaType.TEXT_HTML)
	public Response workLogin(
			@FormParam("login") String login,
			@FormParam("password") String password,
			@FormParam("rememberMe") String rememberMe
	) {
		// test application if login is allowed
		String requireLogin = applicationSettings.getSetting("requireLogin");
		if (requireLogin == null || requireLogin.isEmpty() || !requireLogin.equalsIgnoreCase("true")) {
			return Response.ok(new Viewable("error", "Login not allowed")).build();
		}

		// already logged in?
		if (identity.getName() != null)
			try {
				return Response.seeOther(new URI("/")).build();
			} catch (URISyntaxException e) {
				return Response.ok(new Viewable("error", e.getMessage())).build();
			}

		String error = null;

		if (login == null || login.isEmpty() || password == null || password.isEmpty())
			error = "Login.err.empty";

		// check access
		if (error == null) {
			// find by login
			IUser user = service.findByLogin(login);

			// user ok and password matches?
			if (user != null && passwordEncoder.matches(password, user.getPassword())) {
				// update last login of user
				user.setLastLogin(System.currentTimeMillis());
				// save
				service.save(user);

				// save identity in session
				identity.setUser(user);

				//redirect to index
				try {
					return Response.seeOther(new URI("/")).build();
				} catch (URISyntaxException e) {
					return Response.ok(new Viewable("error", e.getMessage())).build();
				}
			}

			// sleep 2 secs to avoid brute force - not quite secure because we would have to slow down by ip/user
			// and not by request
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// Do nothing
			}

			error = "Login.err.loginError";
		}

		// rememberMe?
		boolean rememberMeValue = rememberMe != null && rememberMe.equals("1");

		// create model map
		Map<String, Object> model = new HashMap<>();
		model.put("login", login);
		model.put("rememberMe", rememberMeValue);
		model.put("error", error);

		return Response.ok(new Viewable("login", model)).build();
	}

	@GET
	@Path("/logout")
	@Produces(MediaType.TEXT_HTML)
	public Response logout() {
		// test application if login is allowed
		String requireLogin = applicationSettings.getSetting("requireLogin");
		if (requireLogin == null || requireLogin.isEmpty() || !requireLogin.equalsIgnoreCase("true")) {
			return Response.ok(new Viewable("error", "Login not allowed")).build();
		}

		// reset identity
		identity.logout();

		//redirect to index
		try {
			return Response.seeOther(new URI("/login")).build();
		} catch (URISyntaxException e) {
			return Response.ok(new Viewable("error", e.getMessage())).build();
		}
	}
}
