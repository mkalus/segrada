package org.segrada.rendering.thymeleaf.processor;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

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
 * Helper class to provide nl2br functionality to thymeleaf
 *
 * Usage: sg:nl2br="'Text'"
 */
public class Nl2BrProcessor extends AbstractElementTagProcessor {
	/**
	 * Tag name
	 */
	private static final String ATTR_NAME = "nl2br";

	/**
	 * Precedence of processor
	 */
	private static final int PRECEDENCE = 1200;

	/**
	 * Constructor
	 * @param dialectPrefix dialect prefix, e.g. th or segrada
	 */
	public Nl2BrProcessor(final String dialectPrefix) {
		super(
				TemplateMode.HTML, // This processor will apply only to HTML mode
				dialectPrefix,     // Prefix to be applied to name for matching
				null,              // No tag name: match any tag name
				false,             // No prefix to be applied to tag name
				ATTR_NAME,         // Name of the attribute that will be matched
				true,              // Apply dialect prefix to attribute name
				PRECEDENCE); // Precedence (inside dialect's own precedence)
	}

	@Override
	protected void doProcess(
			final ITemplateContext context, final IProcessableElementTag tag,
			final IElementTagStructureHandler structureHandler) {
		final IStandardExpressionParser parser = getParser(context);

		String content = parseTagValue(parser, context, tag);

		// If no content is to be applied, just convert to an empty message
		if (content == null) content = "";
		else content = nl2br(content);

		// replace whole tag completely with char sequence
		structureHandler.replaceWith(content, false);
	}

	/**
	 * helper method to parse a tag value through the IStandardExpressionParser
	 * @param parser standard expression parser
	 * @param context Template context
	 * @param tag tag to be parsed
	 * @return parsed tag value
	 */
	private String parseTagValue(final IStandardExpressionParser parser, final ITemplateContext context, final IProcessableElementTag tag) {
		// Parse the attribute value as a Thymeleaf Standard Expression
		return (String) parser.parseExpression(context, tag.getAttributeValue(this.getDialectPrefix(), ATTR_NAME)).execute(context);
	}

	/**
	 * obtain standard expression parser
	 * @param context Template context
	 * @return standard expression parser
	 */
	private IStandardExpressionParser getParser(final ITemplateContext context) {
		final IEngineConfiguration configuration = context.getConfiguration();

		return StandardExpressions.getExpressionParser(configuration);
	}

	/**
	 * actual worker
	 *
	 * @param text to be worked on
	 * @return worked text
	 */
	protected String nl2br(String text) {
		return escapeHtml(text)
				.replace("\r\n", "\n")
				.replace("\r", "\n")
				.replace("\n", "<br/>\n");
	}
}
