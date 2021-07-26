package org.segrada.controller;

import com.google.inject.Singleton;
import com.sun.jersey.api.view.Viewable;
import org.parboiled.common.StringUtils;
import org.pegdown.FastEncoder;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
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
 * Controller for nodes
 */
@Path("/page")
@Singleton
public class PageController {
	@GET
	@Produces(MediaType.TEXT_HTML)
	@PermitAll
	public Viewable index(@Context HttpServletRequest request) {
		return getPage("index", request);
	}

	@GET
	@Path("/img/{image}")
	@PermitAll
	public Response getImage(@PathParam("image") String image, @Context HttpServletRequest request) {
		HttpSession session = request.getSession();

		// get language from session
		Object lObject = session.getAttribute("language");
		String language = lObject==null?Locale.getDefault().getLanguage():(String) lObject;

		try {
			InputStream ins = getClass().getResourceAsStream("/documentation/" + language + "/" + image + ".png");
			// fallback to English language
			if (ins == null) ins = getClass().getResourceAsStream("/documentation/en/" + image + ".png");
			// make final
			final InputStream in = ins;

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
	@Path("/{page}")
	@Produces(MediaType.TEXT_HTML)
	@PermitAll
	public Viewable show(@PathParam("page") String page, @Context HttpServletRequest request) {
		return getPage(page, request);
	}

	/**
	 * get page from resource
	 * @param page id
	 * @return rendered view
	 */
	private Viewable getPage(String page, HttpServletRequest request) {
		HttpSession session = request.getSession();

		// get language from session
		Object lObject = session.getAttribute("language");
		String language = lObject==null?Locale.getDefault().getLanguage():(String) lObject;

		// create model map
		Map<String, Object> model = new HashMap<>();
		// add rendered page content
		model.put("page", renderPageContent(page, language, request.getContextPath()));
		model.put("pageId", page);
		model.put("language", language);

		return new Viewable("page", model);
	}

	/**
	 * render markdown content
	 * @param page id
	 * @param language language
	 * @param contextPath contextPath
	 * @return rendered markdown or error
	 */
	private String renderPageContent(String page, String language, String contextPath) {
		String resourceName = "/documentation/" + language + "/" + page + ".md";

		// read schema from resource file
		InputStream is = this.getClass().getResourceAsStream(resourceName);
		if (is == null) {
			// try fallback to english language
			resourceName = "/documentation/en/" + page + ".md";
			is = this.getClass().getResourceAsStream(resourceName);
			if (is == null) // still not found
				return "NOT AVAILABLE";
		}

		// if no resouce exists do not run updater
		try {
			if (is.available() == 0) return "NOT AVAILABLE";
		} catch (IOException e) {
			return "IOEXCEPTION: " + e.getMessage();
		}

		StringBuilder sb = new StringBuilder();
		// read lines
		try {
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			while((line = in.readLine()) != null) {
				sb.append(line).append('\n');
			}
			in.close();
		} catch (IOException e) {
			return "IOEXCEPTION: " + e.getMessage();
		}

		// create markdown renderer
		PegDownProcessor markdownParser = new PegDownProcessor();

		// render markdown to HTML
		return markdownParser.markdownToHtml(sb.toString(), new PageLinkRenderer(contextPath));
	}

	/**
	 * link renderer for page context
	 */
	private class PageLinkRenderer extends LinkRenderer {
		private final String contextPath;

		public PageLinkRenderer(String contextPath) {
			this.contextPath = contextPath;
		}

		@Override
		public Rendering render(ExpLinkNode node, String text) {
			// do not change links starting with http
			if (node.url.startsWith("http")) // add external class
				return super.render(node, text).withAttribute("class", "sg-link-external");

			// add context path to url
			String url = contextPath + "/page/" + node.url;
			// cut away .md suffix if needed
			if (url.endsWith(".md")) url = url.substring(0, url.length() - 3);

			LinkRenderer.Rendering rendering = new LinkRenderer.Rendering(url, text);
			rendering = rendering.withAttribute("class", "sg-data-add");
			return StringUtils.isEmpty(node.title)?rendering:rendering.withAttribute("title", FastEncoder.encode(node.title));
		}

		@Override
		public Rendering render(ExpImageNode node, String text) {
			// do not change links starting with http
			if (node.url.startsWith("http")) return super.render(node, text);

			// add context path to url
			String url = contextPath + "/page/img/" + node.url;
			// cut away .png suffix if needed //TODO: make this work nicely
			if (url.endsWith(".png")) url = url.substring(0, url.length() - 4);

			LinkRenderer.Rendering rendering = new LinkRenderer.Rendering(url, text);
			return StringUtils.isEmpty(node.title)?rendering:rendering.withAttribute("title", FastEncoder.encode(node.title));
		}
	}
}
