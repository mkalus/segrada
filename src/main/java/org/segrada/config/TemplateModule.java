package org.segrada.config;

import com.google.inject.AbstractModule;
import org.segrada.servlet.SegradaMessageBodyReader;
import org.segrada.servlet.ThymeleafViewProcessor;

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
 * Bind template engine
 */
public class TemplateModule extends AbstractModule {
	@Override
	protected void configure() {
		// bind thymeleaf processor as eager singleton
		bind(ThymeleafViewProcessor.class).asEagerSingleton();

		// automatically convert forms to segrada entities
		bind(SegradaMessageBodyReader.class).asEagerSingleton();
	}
}
