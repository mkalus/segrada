package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.segrada.service.base.AbstractFullTextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
 * Admin controller
 */
@Path("/admin")
@RequestScoped
public class AdminController {
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	/**
	 * map to all full text services
	 */
	@Inject
	private Map<String, AbstractFullTextService> fullTextServiceMap;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String index() {
		return "Not implemented.";
	}

	@GET
	@Path("/reindex")
	@Produces(MediaType.TEXT_HTML)
	public String reindex() {
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

		return "Fertig.";
	}
}
