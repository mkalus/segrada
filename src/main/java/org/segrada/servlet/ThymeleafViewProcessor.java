package org.segrada.servlet;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;
import org.segrada.rendering.thymeleaf.SegradaDialect;
import org.segrada.session.CSRFTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.fragment.ElementAndAttributeNameFragmentSpec;
import org.thymeleaf.fragment.IFragmentSpec;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
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

	/**
	 * Constructor
	 */
	public ThymeleafViewProcessor() {
		TemplateResolver templateResolver = new ServletContextTemplateResolver();
		templateResolver.setPrefix("/WEB-INF/templates/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode("HTML5");
		templateResolver.setCacheTTLMs(3600000L);

		templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
		templateEngine.setMessageResolver(new SegradaMessageResolver());
		templateEngine.addDialect(new SegradaDialect());
	}

	@Override
	public String resolve(final String path) {
		return path;
	}

	@Override
	public void writeTo(final String resolvedPath, final Viewable viewable,
	                    final OutputStream out) throws IOException {
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
		context.setVariable("_csrf", CSRFTokenManager.getTokenForSession(context.getHttpSession()));
		// get identity from session and save it as variable for easy access in frontend
		Enumeration<String> elements = context.getHttpSession().getAttributeNames();
		while (elements.hasMoreElements()) {
			String key = elements.nextElement();
			if (key.contains("org.segrada.session.Identity")) {
				context.setVariable("identity", context.getHttpSession().getAttribute(key));
				break;
			}
		}

		// set fixed variables?
		//context.setVariable("numberFormatter", new NumberFormatter());
		//context.setVariable("format", new SegradaFormatter());
		//<p th:text="${@format.nl2br('Hallo Welt!')}"></p>

		// resolve template name by ::
		String templateName;
		IFragmentSpec fragmentSpec = null;
		String[] subtemplate = viewable.getTemplateName().split("::");

		if (subtemplate.length != 2) templateName = viewable.getTemplateName();
		else {
			templateName = subtemplate[0].trim();
			fragmentSpec = new ElementAndAttributeNameFragmentSpec(null, "class", subtemplate[1].trim(), true);
		}

		// resolve AJAX calls
		// only render content fragment in AJAX calls
		if (fragmentSpec == null && "XMLHttpRequest".equals(context.getHttpServletRequest().getHeader("X-Requested-With")) && !templateName.startsWith("redirect:")) {
			fragmentSpec = new ElementAndAttributeNameFragmentSpec(null, "class", "container", true);
		}

		///TODO: handle redirects?

		// update locale
		updateLocale(context);

		// set charset type by hand
		context.getHttpServletResponse().setContentType("text/html; charset=utf-8");

		templateEngine.process(templateName, context, fragmentSpec, responseInvoker
				.get().getWriter());
	}

	/**
	 * set locale according to session, if needed
	 * @param context web context
	 */
	private static void updateLocale(WebContext context) {
		if (context != null && context.getHttpSession() != null) {
			// get locale saved in session
			Object lObject = context.getHttpSession().getAttribute("language");
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
