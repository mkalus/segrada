package org.segrada.rendering.thymeleaf.processor;

import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractTextChildModifierAttrProcessor;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * Copyright 2015 Maximilian Kalus [segrada@auxnet.de]
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
 * Usage: th:nl2br="'Text'"
 */
public class Nl2BrProcessor extends AbstractTextChildModifierAttrProcessor {
	public Nl2BrProcessor() {
		super("nl2br");
	}

	@Override
	protected String getText(Arguments arguments, Element element, String attributeName) {
		final Configuration configuration = arguments.getConfiguration();

		// Obtain the attribute value
		final String attributeValue = element.getAttributeValue(attributeName);

		// Obtain the Thymeleaf Standard Expression parser
		final IStandardExpressionParser parser =
				StandardExpressions.getExpressionParser(configuration);

		// Parse the attribute value as a Thymeleaf Standard Expression
		final IStandardExpression expression =
				parser.parseExpression(configuration, arguments, attributeValue);

		// Execute the expression just parsed
		final String content =
				(String) expression.execute(configuration, arguments);

		// If no content is to be applied, just return an empty message
		if (content == null) {
			return "";
		}

		return nl2br(content);
	}

	/**
	 * actual worker
	 *
	 * @param text to be worked on
	 * @return worked text
	 */
	public String nl2br(String text) {
		return escapeHtml(text)
				.replace("\r\n", "\n")
				.replace("\r", "\n")
				.replace("\n", "<br/>\n");
	}

	@Override
	public int getPrecedence() {
		return 12000;
	}
}
