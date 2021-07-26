package org.segrada.servlet;

import com.google.inject.Inject;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;
import net.sf.ehcache.CacheManager;
import org.segrada.rendering.thymeleaf.SegradaDialect;
import org.segrada.session.CSRFTokenManager;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

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
 * Inject thymeleaf templating into views
 */
@Provider
public class ThymeleafViewProcessor implements ViewProcessor<String> {
	//private static final Logger logger = LoggerFactory.getLogger(ThymeleafViewProcessor.class);

	/**
	 * keep valid locales
	 */
	private static final Set<String> validLocales;
	static {
		validLocales = new HashSet<>();
		validLocales.add("de");
		validLocales.add("en");
	}

	@Context
	private ServletContext servletContext;

	@Context
	private HttpServletRequest servletRequest;

	@Context
	private ThreadLocal<HttpServletRequest> requestInvoker;

	@Context
	private ThreadLocal<HttpServletResponse> responseInvoker;

	private TemplateEngine templateEngine;

	@Override
	public String resolve(final String path) {
		return path;
	}

	@Override
	public void writeTo(final String resolvedPath, final Viewable viewable,
	                    final OutputStream out) throws IOException {
		// create template engine on first write
		if (templateEngine == null) {
			ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
			templateResolver.setPrefix("/WEB-INF/templates/");
			templateResolver.setSuffix(".html");
			templateResolver.setTemplateMode("HTML");
			templateResolver.setCacheTTLMs(3600000L);

			templateEngine = new TemplateEngine();
			templateEngine.setTemplateResolver(templateResolver);
			templateEngine.setMessageResolver(new SegradaMessageResolver(servletContext));
			templateEngine.addDialect(new SegradaDialect());
		}

		// Commit the status and headers to the HttpServletResponse
		out.flush();

		WebContext context = new WebContext(requestInvoker.get(),
				responseInvoker.get(), servletContext, requestInvoker.get().getLocale());
		if (viewable.getModel() != null) {
			// map model
			if (viewable.getModel() instanceof Map)
				context.setVariables((Map) viewable.getModel());
			else {
				// generic model data
				Map<String, Object> variables = new HashMap<>();
				variables.put("data", viewable.getModel());
				context.setVariables(variables);
			}
		}

		// Define _csrf token
		context.setVariable("_csrf", CSRFTokenManager.getTokenForSession(context.getSession()));
		// get identity from session and save it as variable for easy access in frontend
		Enumeration<String> elements = context.getSession().getAttributeNames();
		while (elements.hasMoreElements()) {
			String key = elements.nextElement();
			if (key.contains("org.segrada.session.Identity")) {
				context.setVariable("identity", context.getSession().getAttribute(key));
				break;
			}
		}

		// set fixed variables?
		//context.setVariable("numberFormatter", new NumberFormatter());
		//context.setVariable("format", new SegradaFormatter());
		//<p th:text="${@format.nl2br('Hallo Welt!')}"></p>

		// resolve template name by ::
		String templateName;
		Set<String> templateSelectors = null;
		String[] subtemplate = viewable.getTemplateName().split("::");

		if (subtemplate.length != 2) templateName = viewable.getTemplateName();
		else {
			templateName = subtemplate[0].trim();
			templateSelectors = new HashSet<>();
			templateSelectors.add("." + subtemplate[1].trim() + "/div");
		}

		// resolve AJAX calls
		// only render content fragment in AJAX calls
		if (templateSelectors == null && "XMLHttpRequest".equals(context.getRequest().getHeader("X-Requested-With")) && !templateName.startsWith("redirect:")) {
			templateSelectors = new HashSet<>();
			templateSelectors.add(".container/div");
		}

		///TODO: handle redirects?

		// update locale
		updateLocale(context);

		// set charset type by hand
		context.getResponse().setContentType("text/html; charset=utf-8");

		templateEngine.process(templateName, templateSelectors, context, responseInvoker.get().getWriter());
	}

	/**
	 * set locale according to session, if needed
	 * @param context web context
	 */
	private static void updateLocale(WebContext context) {
		if (context != null && context.getSession() != null) {
			// get locale saved in session
			Object lObject = context.getSession().getAttribute("language");
			String language = lObject==null?null:(String) lObject;

			if (language != null && !language.isEmpty()) {
				Locale newLocale = new Locale(language);

				context.setLocale(newLocale);
			}

			// valid locale? If not, fall back to English
			if (!validLocales.contains(context.getLocale().getLanguage()))
				context.setLocale(new Locale("en"));
		}
	}
}
