package org.segrada.rendering.markup;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;

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
 * HTML/Richtext markup filter
 */
public class HtmlMarkupFilter extends DefaultMarkupFilter {
	@Override
	public String toHTML(String markupText) {
		// empty text?
		if (markupText == null || markupText.equals("")) return "";

		// replace entities
		String htmlText = replaceEntities(markupText);

		// replace bibliographic annotations
		htmlText = annotateBibliographies(htmlText);

		return htmlText;
	}

	@Override
	public String toPlain(String markupText) {
		// sane default
		if (markupText == null || markupText.equals("")) return "";

		// first clean to have valid html
		String cleaned = Jsoup.clean(markupText, Whitelist.basic());
		// then strip all html out
		cleaned = Jsoup.clean(cleaned, Whitelist.none());

		// unescape all entities
		cleaned = Parser.unescapeEntities(cleaned, false);

		// clean further
		return super.toPlain(cleaned);
	}
}
