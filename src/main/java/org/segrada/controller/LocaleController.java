package org.segrada.controller;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

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
@Path("/locale")
public class LocaleController {
	@GET
	@Path("/{locale}")
	@Produces(MediaType.TEXT_HTML)
	public String index(@PathParam("locale") String locale, @Context HttpServletRequest request) {
		HttpSession session = request.getSession();

		// get language from session
		Object lObject = session.getAttribute("language");
		String language = lObject==null?null:(String) lObject;

		// update only if correctly set
		if (locale != null && !locale.equals(language)) {
			session.setAttribute("language", locale);

			return locale;
		}

		return "";
	}
}
