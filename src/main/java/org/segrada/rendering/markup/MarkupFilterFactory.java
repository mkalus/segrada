package org.segrada.rendering.markup;

import java.util.logging.Logger;

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
 * Factory to produce markup filters
 */
public class MarkupFilterFactory {
	private static Logger logger = Logger.getLogger(MarkupFilterFactory.class.getName());

	private MarkupFilterFactory() throws InstantiationException{
		throw new InstantiationException("The class is not created for instantiation");
	}

	/**
	 * poduce a markup filter instance or throw exception
	 * @param type of markup filter, e.g. "default", null will mean default
	 * @return MarkupFilter instance
	 * @throws IllegalArgumentException if no fitting filter can be found
	 */
	public static MarkupFilter produce(String type) throws IllegalArgumentException {
		if (type == null || "".equals(type)) type = "default";

		// create class name => uppercase first letter and add MarkupFilter
		String className = "org.segrada.rendering.markup." +
				Character.toUpperCase(type.charAt(0)) + type.substring(1)
				+ "MarkupFilter";

		// try to find fitting class
		try {
			Class myClass = Class.forName(className);
			return (MarkupFilter) myClass.newInstance();
		} catch (Exception e) {
			if (!type.equals("default")) {
				logger.warning("Could not produce MarkupFilter of type " + type + ". Falling back to default.");
				return produce("default"); // try to fall back to default handler
			} //... unless it is default already
			throw new IllegalArgumentException("Could not find MarkupFilter class " + className);
		}
	}

	/**
	 * produce default instance
	 * @return MarkupFilter instance
	 * @throws IllegalArgumentException if no fitting filter can be found
	 */
	public static MarkupFilter produce() throws IllegalArgumentException {
		return produce(null);
	}
}
