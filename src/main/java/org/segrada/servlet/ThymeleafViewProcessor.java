package org.segrada.servlet;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
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
 * Inject thymeleaf templating into views
 */
@Provider
public class ThymeleafViewProcessor implements ViewProcessor<String> {
	@Context
	ServletContext servletContext;

	@Context
	ThreadLocal<HttpServletRequest> requestInvoker;

	@Context
	ThreadLocal<HttpServletResponse> responseInvoker;

	TemplateEngine templateEngine;

	public ThymeleafViewProcessor() {
		TemplateResolver templateResolver = new ServletContextTemplateResolver();
		templateResolver.setPrefix("/WEB-INF/templates/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode("HTML5");
		templateResolver.setCacheTTLMs(3600000L);

		templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
		templateEngine.setMessageResolver(new SegradaMessageResolver());
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
		Map<String, Object> variables = new HashMap<>();
		variables.put("it", viewable.getModel());
		context.setVariables(variables);

		// set fixed variables?
		//context.setVariable("numberFormatter", new NumberFormatter());
		//context.setVariable("format", new SegradaFormatter());
		//<p th:text="${@format.nl2br('Hallo Welt!')}"></p>

		templateEngine.process(viewable.getTemplateName(), context, responseInvoker
				.get().getWriter());
	}
}
