package org.segrada.rendering.markup;

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
 * Default text markup filter - mostly plain text with some link elements
 */

import com.google.inject.Injector;
import org.segrada.model.prototype.ISource;
import org.segrada.service.SourceService;
import org.segrada.session.Identity;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class DefaultMarkupFilter extends MarkupFilter {
	/**
	 * reference to injector
	 */
	public static Injector injector;
	
	/**
	 * reference to source service
	 */
	private final SourceService sourceService;

	/**
	 * cache for source reference links
	 * TODO: use different cache, because otherwise this might fill up memory eventually, rather unlikely, but there is no deletion of old keys here
	 */
	private static final Map<String, String> sourceReferenceCache = new HashMap<>();
	
	/**
	 * replacement entities
	 */
	protected static final String[] entities = {
			" - ", " &ndash; ",
			"--", "&mdash;",
			"(c)", "&copy;",
			"(C)", "&copy;",
			"(R)", "&reg;",
			"&lt;=&gt;", "&hArr;",
			"&lt;-&gt;", "&harr;",
			"&lt;=", "&lArr;",
			"&lt;-", "&larr;",
			"=&gt;", "&rArr;",
			"-&gt;", "&rarr;",
	};

	private static final Pattern bibRefPattern = Pattern.compile("\\[\\[([^]]+)\\]\\]");

	/**
	 * @return pattern to find bibliographic references
	 */
	public static Pattern getBibRefPattern() {
		return bibRefPattern;
	}

	public DefaultMarkupFilter() {
		if (injector != null)
			sourceService = injector.getInstance(SourceService.class);
		else sourceService = null;
	}

	public static void setInjector(Injector injector) {
		DefaultMarkupFilter.injector = injector;
	}

	@Override
	public String toHTML(String markupText) {
		// empty text?
		if (markupText == null || markupText.equals("")) return "";

		// escape html and replace new lines
		String htmlText = escapeHtml4(markupText)
				.replace("\r\n", "\n")
				.replace("\r", "\n")
				.replace("\n", "<br/>\n");

		// decorations replacement
		htmlText = decorateText(htmlText);

		// replace entities
		htmlText = replaceEntities(htmlText);

		// replace bibliographic annotations
		htmlText = annotateBibliographies(htmlText);

		return htmlText;
	}

	/**
	 * replace decorations with HTML
	 *
	 * these are
	 *  *BOLD*
	 *  _EMPHASISE_
	 *  ==UNDERLINE==
	 *
	 * @param text input text
	 * @return output text
	 */
	protected String decorateText(String text) {
		// bold
		text = text.replaceAll("\\*([^\\*\\n]*)\\*", "<strong>$1</strong>");
		// emphasise
		text = text.replaceAll("_([^_\\n]*)_", "<em>$1</em>");
		// underline
		text = text.replaceAll("==([^\\n]*)==", "<span style=\"text-decoration:underline\">$1</span>");

		return text;
	}

	/**
	 * replace entities in HTML escaped text
	 * @param text input text
	 * @return output text
	 */
	protected String replaceEntities(String text) {
		for (int i = 0; i < entities.length; i+=2) {
			text= text.replace(entities[i], entities[i+1]);
		}

		return text;
	}

	/**
	 * replace text parts with bibliographic annotations
	 *
	 * there are two:
	 *
	 * [[haebler:rott]] => bibliographic reference -> replace with link to bib, but this will be done in the view
	 *   so we do not have to replace this at all here...
	 * [13:] => means page 13 in cited text => just decorated
	 *
	 * @param text input text
	 * @return output text
	 */
	protected String annotateBibliographies(String text) {
		// check identity, if injector has been set
		Identity identity = injector!=null?injector.getInstance(Identity.class):null;

		if (sourceService != null && identity != null) { // skipped in tests and if source not allowed
			// bibliographic references
			Matcher matcher = bibRefPattern.matcher(text);
			StringBuffer sb = new StringBuffer(text.length());
			while (matcher.find()) {
				String match = matcher.group(1);

				// try to get cached entry
				String replacement = sourceReferenceCache.get(match);
				if (replacement == null) {
					// find corresponding source
					ISource source = sourceService.findByRef(match);
					if (source == null) replacement = "[[" + match + "]]"; // fallback
					else {
						replacement = "<a href=\"source/show/" + source.getUid() + "\" class=\"sg-data-add\">" + source.getShortTitle() + "</a>";
					}

					// write to cache
					sourceReferenceCache.put(match, replacement);
				}

				matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
			}
			matcher.appendTail(sb);
			text = sb.toString();
		}

		// page reference
		text = text.replaceAll("\\[([0-9f]+:)\\]", "<span class=\"sg-label sg-info\">$1</span>");

		return text;
	}

	@Override
	public String toPlain(String markupText) {
		// sane default
		if (markupText == null || markupText.equals("")) return "";

		// remove bibliographic entries
		String plainText = markupText.replaceAll("\\[\\[[\\w:]+\\]\\]", "").replaceAll("\\[[0-9f]+:\\]", "");

		// remove decorations
		plainText = plainText.replaceAll("\\*([^\\*\\n]*)\\*", "$1");
		plainText = plainText.replaceAll("_([^_\\n]*)_", "$1");
		plainText = plainText.replaceAll("==([^\\n]*)==", "$1");

		// contract whitespace
		plainText = plainText
				.replaceAll("(\\u00a0|\\u202f|\\u2007|\\ufeff)", " ") // replace no-break spaces
				.replaceAll("\\s+", " ");

		return plainText; // is plain already
	}
}
