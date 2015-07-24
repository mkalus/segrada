package org.segrada.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.glassfish.jersey.client.filter.CsrfProtectionFilter;
import org.segrada.config.ServiceModule;
import org.segrada.config.TemplateModule;
import org.segrada.controller.*;
import org.segrada.rendering.markup.DefaultMarkupFilter;

import javax.servlet.annotation.WebListener;
import java.util.Map;
import java.util.TreeMap;

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
 * Bootstrap Guice
 */
@WebListener
public class SegradaGuiceServletContextListener extends GuiceServletContextListener {
	@Override
	protected Injector getInjector() {
		OrientDBFilter orientDBFilter = new OrientDBFilter();

		Injector injector = Guice.createInjector(
				new TemplateModule(),
				new ServiceModule(),
				new ServletModule() {
					@Override
					protected void configureServlets() {
						bind(MainController.class);
						bind(ColorController.class);
						bind(CommentController.class);
						bind(FileController.class);
						bind(LocationController.class);
						bind(NodeController.class);
						bind(PeriodController.class);
						bind(PictogramController.class);
						bind(RelationController.class);
						bind(RelationTypeController.class);
						bind(SourceController.class);
						bind(SourceReferenceController.class);
						bind(TagController.class);
						bind(UserController.class);
						bind(SearchController.class);
						bind(AdminController.class);
						bind(LocaleController.class);

						Map<String, String> initParams = new TreeMap<String, String>();
						initParams.put("com.sun.jersey.config.property.WebPageContentRegex", "/.*\\.(jpg|ico|png|gif|html|txt|css|js|xml|otf|svg|ttf|woff|woff2|eot)");
						//TODO: implement client side of this, in order to make it work
						//initParams.put("com.sun.jersey.spi.container.ContainerRequestFilters", "com.sun.jersey.api.container.filter.CsrfProtectionFilter");

						filter("/*").through(GuiceContainer.class, initParams);
						filter("/*").through(orientDBFilter);
					}
				}
		);

		// manually inject this injector
		orientDBFilter.setInjector(injector);
		DefaultMarkupFilter.setInjector(injector);

		return injector;
	}
}
