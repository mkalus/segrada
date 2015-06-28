package org.segrada.rendering.thymeleaf;

import org.segrada.rendering.markup.MarkupFilter;
import org.segrada.rendering.markup.MarkupFilterFactory;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

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
 * Helper class to provide some formatting options to Thymeleaf
 */
public class SegradaFormatter {
	/**
	 * nl2br for Thymeleaf - used in th:utext
	 * @param text to be formatted
	 * @return formatted text
	 */
	public String nl2br(String text) {
		return escapeHtml(text)
				.replace("\r\n", "\n")
				.replace("\r", "\n")
				.replace("\n", "<br/>\n");
	}

	/**
	 * to be used in th:utext: Format text with a certain markup
	 * @param text to be formatted
	 * @param markup to be applied on text
	 * @return formatted and escaped text
	 */
	public String markup(String text, String markup) throws IllegalArgumentException {
		// do not format empty texts
		if (text == null) return "";

		// produce filter
		MarkupFilter markupFilter = MarkupFilterFactory.produce(markup);
		return markupFilter.toHTML(text);
	}
}
