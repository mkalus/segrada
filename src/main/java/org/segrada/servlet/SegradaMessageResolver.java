package org.segrada.servlet;

import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.templateresource.ITemplateResource;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.IOException;
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
 * Message resolver overwriting settings of Thymeleaf
 */
public class SegradaMessageResolver extends StandardMessageResolver {
	private static final String TEMPLATE_CACHE_PREFIX = "{template_msg}";

	@Context
	private final ServletContext servletContext;

	/**
	 * keeps cached translation resources
	 */
	private static Map<String, Map<String, String>> cachedResources = new HashMap<>();

	/**
	 * Constructor
	 * @param servletContext
	 */
	public SegradaMessageResolver(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	protected Map<String, String> resolveMessagesForTemplate(String template, ITemplateResource templateResource, Locale locale) {
		if (!cachedResources.containsKey(locale.getLanguage())) {
			Set<String> resources = servletContext.getResourcePaths("/WEB-INF/i18n/");

			String resourceName;
			if (resources.contains("/WEB-INF/i18n/messages_" + locale.getLanguage() + ".properties")) {
				resourceName = "messages_" + locale.getLanguage() + ".properties";
			} else resourceName = "messages.properties";

			Properties properties = new Properties();
			try {
				properties.load(servletContext.getResourceAsStream("/WEB-INF/i18n/" + resourceName));
			} catch (IOException e) {
				e.printStackTrace();
			}

			Map<String, String> messages = new HashMap<>();
			for (String key : properties.stringPropertyNames()) {
				messages.put(key, properties.getProperty(key));
			}

			// cache messages
			cachedResources.put(locale.getLanguage(), messages);

			return messages;
		}

		return cachedResources.get(locale.getLanguage());
	}
}
