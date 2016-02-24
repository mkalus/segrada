package org.segrada.rendering.thymeleaf.util;

import java.util.HashMap;
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
 * Table sorting helper/preprocessor for thymeleaf
 */
public class TableSortPreprocessor {
	/**
	 * method to create sort link map
	 * @param baseUrl base url
	 * @param field to sort by
	 * @param defaultField default sort field
	 * @param defaultSort default sort direction
	 * @param filters filters map
	 * @return string map containing two keys: "url" and "icon"
	 */
	public static Map<String, String> createSortLink(String baseUrl, String field, String defaultField, String defaultSort, Map<String, Object> filters) {
		// default values
		String url = createSortUrl(baseUrl, field, "asc"); // default sort
		String icon = "";

		// filter active?
		if (filters.get("sort") != null) {
			String sortedField = (String) filters.get("sort");
			String sortDirection = filters.get("dir")==null?null:(String)filters.get("dir");

			// is sorted field our field?
			if (sortedField.equals(field)) {
				if (sortDirection != null && !sortDirection.isEmpty()) {
					// define new sort directions
					if (sortDirection.equalsIgnoreCase("asc")) {
						icon = getIcon(true);
						url = createSortUrl(baseUrl, field, "desc");
					} else if (sortDirection.equalsIgnoreCase("desc")) {
						icon = getIcon(false);
						if (!field.equals(defaultField)) // only if not default field
							url = createSortUrl(baseUrl, field, "none");
					}
				}
			} else if (sortDirection != null && sortDirection.equals("none") && field.equals(defaultField)) {
				// no filter set, but field equals default field: create
				if (defaultSort == null || defaultSort.isEmpty() || !defaultSort.equalsIgnoreCase("asc")) {
					// default is desc
					icon = getIcon(false);
					//url = createSortUrl(baseUrl, field, "asc"); // not changed
				} else {
					// default is asc
					icon = getIcon(true);
					url = createSortUrl(baseUrl, field, "desc");
				}
			}
		} else if (field.equals(defaultField)) {
			// no filter set, but field equals default field: create
			if (defaultSort == null || defaultSort.isEmpty() || !defaultSort.equalsIgnoreCase("asc")) {
				// default is desc
				icon = getIcon(false);
				//url = createSortUrl(baseUrl, field, "asc"); // not changed
			} else {
				// default is asc
				icon = getIcon(true);
				url = createSortUrl(baseUrl, field, "desc");
			}
		}

		// create map to return
		Map<String, String> sortHelper = new HashMap<>();

		sortHelper.put("url", url);
		sortHelper.put("icon", icon);

		return sortHelper;
	}

	/**
	 * Get correct caret icon
	 * @param asc true asc icon is needed, false otherwise
	 * @return icon code
	 */
	private static String getIcon(boolean asc) {
		return asc?"<i class=\"fa fa-caret-down\"></i>":"<i class=\"fa fa-caret-up\"></i>";
	}

	/**
	 * Get url
	 * @param url base url
	 * @param field field to sort by
	 * @param order new (!) sort order (asc/desc/none)
	 * @return created url
	 */
	private static String createSortUrl(String url, String field, String order) {
		return url.concat(url.contains("?")?"&":"?").concat("sort=").concat(field).concat("&dir=").concat(order);
	}
}
