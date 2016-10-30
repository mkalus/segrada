package org.segrada.rendering.thymeleaf.processor;

import org.segrada.rendering.markup.MarkupFilter;
import org.segrada.rendering.markup.MarkupFilterFactory;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Copyright 2016 Maximilian Kalus [segrada@auxnet.de]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Helper class to provide markup functionality to thymeleaf
 *
 * Usage: <sg:markup markup="'html'" text="'text'" />
 */
public class MarkupProcessor extends AbstractSegradaTagProcessor {
	/**
	 * Tag name
	 */
	private static final String TAG_NAME = "markup";

	/**
	 * Precedence of processor
	 */
	private static final int PRECEDENCE = 1000;

	/**
	 * Constructor
	 * @param dialectPrefix dialect prefix, e.g. th or segrada
	 */
	public MarkupProcessor(final String dialectPrefix) {
		super(
			TemplateMode.HTML, // This processor will apply only to HTML mode
			dialectPrefix,     // Prefix to be applied to name for matching
			TAG_NAME,          // Tag name: match specifically this tag
			true,              // Apply dialect prefix to tag name
			null,              // No attribute name: will match by tag name
			false,             // No prefix to be applied to attribute name
			PRECEDENCE); // Precedence (inside dialect's own precedence)
	}

	@Override
	protected void doProcess(
			final ITemplateContext context, final IProcessableElementTag tag,
			final IElementTagStructureHandler structureHandler) {
		final IStandardExpressionParser parser = getParser(context);

		// Get attribute values
		final String markup = parseTagValue(parser, context, tag, "markup");
		final String text = parseTagValue(parser, context, tag, "text");

		// replace tag completely with char sequence
		structureHandler.replaceWith("<div class=\"sg-markup\">" + markup(text, markup) + "</div>", false);
	}

	/**
	 * to be used in th:utext: Format text with a certain markup
	 * @param text to be formatted
	 * @param markup to be applied on text
	 * @return formatted and escaped text
	 */
	protected String markup(String text, String markup) throws IllegalArgumentException {
		// do not format empty texts
		if (text == null) return "";

		// produce filter
		MarkupFilter markupFilter = MarkupFilterFactory.produce(markup);
		return markupFilter.toHTML(text);
	}
}
