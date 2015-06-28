package org.segrada.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.view.Viewable;
import org.segrada.service.ColorService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by mkalus on 27.06.15.
 */
@Path("/")
@Singleton
public class MainController {
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable sayHello() {
		return new Viewable("home");
	}
}
