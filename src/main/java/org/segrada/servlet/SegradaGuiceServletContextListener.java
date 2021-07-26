package org.segrada.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.segrada.auth.CheckAuthentication;
import org.segrada.config.AuthenticationModule;
import org.segrada.config.ServiceModule;
import org.segrada.config.TemplateModule;
import org.segrada.controller.*;
import org.segrada.rendering.markup.DefaultMarkupFilter;

import javax.servlet.annotation.WebListener;
import java.util.Map;
import java.util.TreeMap;

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
 * Bootstrap Guice
 */
@WebListener
public class SegradaGuiceServletContextListener extends GuiceServletContextListener {
	@Override
	protected Injector getInjector() {
		Injector injector = Guice.createInjector(
				new AuthenticationModule(),
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
						bind(UserGroupController.class);
						bind(SavedQueryController.class);
						bind(SearchController.class);
						bind(AdminController.class);
						bind(LocaleController.class);
						bind(PageController.class);
						bind(LoginController.class);

						// CSRF filter
						bind(CSRFFilter.class).asEagerSingleton();
						filter("/*").through(CSRFFilter.class);

						// cache filter
						bind(SegradaSimplePageCachingFilter.class).asEagerSingleton();
						filter("/*").through(SegradaSimplePageCachingFilter.class);

						String filterPattern = "/.*\\.(jpg|ico|png|gif|html|txt|css|js|xml|otf|svg|ttf|woff|woff2|eot)";

						bind(OrientDBFilter.class).asEagerSingleton();
						Map<String, String> initParams = new TreeMap<>();
						initParams.put("excludePatterns", filterPattern + "$");
						filter("/*").through(OrientDBFilter.class, initParams);

						initParams = new TreeMap<>();
						initParams.put("com.sun.jersey.config.property.WebPageContentRegex", filterPattern);

						// guice container filter
						filter("/*").through(GuiceContainer.class, initParams);
					}
				}
		);

		// manually inject this injector
		OrientDBFilter.setInjector(injector);
		SegradaSimplePageCachingFilter.setInjector(injector);
		DefaultMarkupFilter.setInjector(injector);
		CheckAuthentication.setInjector(injector);

		return injector;
	}
}
