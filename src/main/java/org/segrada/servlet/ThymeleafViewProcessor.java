package org.segrada.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;

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
		templateEngine.process(viewable.getTemplateName(), context, responseInvoker
				.get().getWriter());
	}
}
