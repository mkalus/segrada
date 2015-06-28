package org.segrada.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.segrada.config.ServiceModule;
import org.segrada.config.TemplateModule;
import org.segrada.controller.MainController;

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
						//serve("/*").with(GuiceContainer.class);
						Map<String, String> initParams = new TreeMap<String, String>();

						initParams.put("com.sun.jersey.config.property.WebPageContentRegex", "/.*\\.(jpg|ico|png|gif|html|id|txt|css|js|xml)");

						filter("/*").through(GuiceContainer.class, initParams);
						filter("/*").through(orientDBFilter);
					}
				}
		);

		// manually inject this injector
		orientDBFilter.setInjector(injector);

		return injector;
	}
}
