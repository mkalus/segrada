package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.segrada.search.SearchEngine;
import org.segrada.service.base.AbstractFullTextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

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
 * Admin controller
 */
@Path("/admin")
@RequestScoped
public class AdminController {
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Inject
	private SearchEngine searchEngine;

	/**
	 * map to all full text services
	 */
	@Inject
	private Map<String, AbstractFullTextService> fullTextServiceMap;

	/**
	 * resource bundle
	 */
	private ResourceBundle messages;

	@GET
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("admin")
	public String index() {
		return "Not implemented.";
	}

	@GET
	@Path("/reindex")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("admin")
	public String reindex(@Context ServletContext context) {
		clearCache(context); // delete caches

		// clear index
		searchEngine.clearAllIndexes();

		// how many entities do we have in total?
		long total = 0;
		long done = 0;
		for (Map.Entry<String, AbstractFullTextService> entry : fullTextServiceMap.entrySet()) {
			total += (float) entry.getValue().count();
		}
		if (logger.isDebugEnabled())
			logger.debug("Reindexing count: " + total);

		// work all services and update index
		for (Map.Entry<String, AbstractFullTextService> entry : fullTextServiceMap.entrySet()) {
			// reindex this batch
			entry.getValue().reindexAll();

			// all done
			done += (float) entry.getValue().count();

			if (logger.isDebugEnabled())
				logger.debug("Reindexed " + entry + ": " + done + "/" + total);
		}

		initI18N(context);
		try {
			return messages.getString("ReindexingFinished");
		} catch (Exception e) {
			return "Finished with error(s).";
		}
	}

	@GET
	@Path("/clear_cache")
	@Produces(MediaType.TEXT_HTML)
	@RolesAllowed("admin")
	public String clearCache(@Context ServletContext context) {
		// delete caches
		Ehcache cache = CacheManager.getInstance().getEhcache("SimplePageCachingFilter");
		if (cache != null) {
			cache.removeAll(); // flush whole cache
		}

		initI18N(context);
		try {
			return messages.getString("CacheCleared");
		} catch (Exception e) {
			return "Cache has been cleared.";
		}
	}

	/**
	 * Initialize I18N - not elegant but works for this controller
	 */
	private void initI18N(ServletContext context) {
		if (messages == null) {
			Locale locale = Locale.getDefault();

			try {
				// try to get language in correct locale
				URL url = context.getResource("/WEB-INF/i18n/messages_" + locale.getLanguage() + ".properties");
				// fallback to English
				if (url == null)
					url = context.getResource("/WEB-INF/i18n/messages.properties");

				// load resources
				messages = new PropertyResourceBundle(url.openStream());
			} catch (IOException e) {
				logger.error("Could not load resource file for admin controller for language " + locale.getLanguage(), e);
			}
		}
	}
}
