package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.segrada.controller.base.AbstractColoredController;
import org.segrada.model.prototype.IComment;
import org.segrada.service.CommentService;
import org.segrada.service.base.SegradaService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Copyright 2015-2019 Maximilian Kalus [segrada@auxnet.de]
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
 * Controller for comments
 */
@Path("/comment")
@RequestScoped
public class CommentController extends AbstractColoredController<IComment> {
	@Inject
	private CommentService service;

	@Override
	protected String getBasePath() {
		return "/comment/";
	}

	@Override
	public SegradaService getService() {
		return service;
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String index() {
		return "Not implemented yet.";
	}
}
