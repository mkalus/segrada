package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.model.User;
import org.segrada.model.prototype.IUser;
import org.segrada.service.UserGroupService;
import org.segrada.service.UserService;
import org.segrada.service.repository.RememberMeRepository;
import org.segrada.session.ApplicationSettings;
import org.segrada.session.Identity;
import org.segrada.util.PasswordEncoder;

import javax.annotation.security.PermitAll;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
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
	public final static String REMEMBER_ME_COOKIE_NAME = "remember-me-token";

	@Inject
	private ApplicationSettings applicationSettings;

	@Inject
	private PasswordEncoder passwordEncoder;

	@Inject
	private UserService service;

	@Inject
	private UserGroupService userGroupService;

	@Inject
	private Identity identity;

	@Inject
	private RememberMeRepository rememberMeRepository;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response login(@QueryParam("logout") String logout,
	                      @Context HttpServletRequest servletRequest,
	                      @Context HttpServletResponse servletResponse) {
		if (!requireLoginSet()) return loginNotAllowed();

		// remove remember me cookie, if needed
		if (logout != null && logout.equals("1")) {
			for (Cookie c : servletRequest.getCookies()) {
				if (c.getName().equals(REMEMBER_ME_COOKIE_NAME)) {
					// remove cookie
					c.setMaxAge(0);
					c.setValue(null);
					servletResponse.addCookie(c);

					break;
				}
			}
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
		// define whether anonymous login is allowed
		String anonymousLoginSetting = applicationSettings.getSetting("allowAnonymous");
		model.put("anonymousLogin", anonymousLoginSetting != null && !anonymousLoginSetting.isEmpty() && anonymousLoginSetting.equalsIgnoreCase("true"));

		return Response.ok(new Viewable("login", model)).build();
	}

	@POST
	@Produces(MediaType.TEXT_HTML)
	public Response workLogin(
			@FormParam("login") String login,
			@FormParam("password") String password,
			@FormParam("rememberMe") String rememberMe,
			@Context HttpServletResponse servletResponse
		) {
		if (!requireLoginSet()) return loginNotAllowed();

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

			// user ok and active and password matches?
			if (user != null && user.getActive() && passwordEncoder.matches(password, user.getPassword())) {
				// update last login of user
				user.setLastLogin(System.currentTimeMillis());
				// save
				service.save(user);

				// save identity in session
				identity.setUser(user);

				// remember me?
				if (rememberMe != null && rememberMe.equals("1")) {
					// create new token
					String token = rememberMeRepository.createTokenForCookie(user.getId());
					if (token != null && !token.isEmpty()) {
						Cookie c = new Cookie(REMEMBER_ME_COOKIE_NAME, token);
						c.setMaxAge(30 * 25 * 60 * 60); // 30 days
						servletResponse.addCookie(c);
					}
				}

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
	@Path("/anonymous")
	public Response anonymousLogin() {
		if (!requireLoginSet()) return loginNotAllowed();

		// anonymous login allowed?
		String anonymousLoginSetting = applicationSettings.getSetting("allowAnonymous");
		if (anonymousLoginSetting == null || anonymousLoginSetting.isEmpty() || !anonymousLoginSetting.equalsIgnoreCase("true"))
			return loginNotAllowed();

		// simulate user
		IUser user = new User();
		user.setId(""); // must be "" in order to work properly!!
		user.setLogin("anonymous");
		user.setName("Anonymus"); // prefer the latin name
		user.setPassword("");
		user.setActive(true);
		// set anonymous user group
		user.setGroup(userGroupService.findSpecial("anonymous"));

		// save identity in session
		identity.setUser(user);

		//redirect to index
		try {
			return Response.seeOther(new URI("/")).build();
		} catch (URISyntaxException e) {
			return Response.ok(new Viewable("error", e.getMessage())).build();
		}
	}

	@GET
	@Path("/logout")
	@Produces(MediaType.TEXT_HTML)
	@PermitAll
	public Response logout(@Context HttpServletRequest servletRequest, @Context HttpServletResponse servletResponse) {
		if (!requireLoginSet()) return loginNotAllowed();

		// reset identity
		identity.logout();

		// remove remember me cookie, if needed
		for (Cookie c : servletRequest.getCookies()) {
			if (c.getName().equals(REMEMBER_ME_COOKIE_NAME)) {
				// remove from service
				rememberMeRepository.removeToken(c.getValue());

				// remove cookie
				c.setMaxAge(0);
				c.setValue(null);
				servletResponse.addCookie(c);

				break;
			}
		}

		//redirect to index
		try {
			return Response.seeOther(new URI("/login?logout=1")).build();
		} catch (URISyntaxException e) {
			return Response.ok(new Viewable("error", e.getMessage())).build();
		}
	}

	/**
	 * Check whether requireLogin has been set in settings
	 * @return true if requireLogin has been set
	 */
	private boolean requireLoginSet() {
		// test application if login is allowed
		String requireLogin = applicationSettings.getSetting("requireLogin");
		return requireLogin != null && !requireLogin.isEmpty() && requireLogin.equalsIgnoreCase("true");
	}

	/**
	 * error response
	 * @return error response
	 */
	private Response loginNotAllowed() {
		return Response.ok(new Viewable("error", "Login not allowed")).build();
	}
}
