package org.segrada.rendering.markup;

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
 * Markup filter base - filters will get text and parse it to HTML or plain text (for search engine)
 */
public abstract class MarkupFilter {
	/**
	 * return canonical name of filter, i.e. class name lower cased without MarkupFilter as name
	 * @return name; TestMarkupFilter will return "test"
	 */
	public String getName() {
		String className = this.getClass().getSimpleName();
		return className.substring(0, className.length() - 12).toLowerCase();
	}

	/**
	 * Convert markup to HTML
	 * @param markupText input text
	 * @return html text
	 */
	abstract public String toHTML(String markupText);

	/**
	 * Convert markup to plain text
	 * @param markupText input text
	 * @return plain text (stripped)
	 */
	abstract public String toPlain(String markupText);
}