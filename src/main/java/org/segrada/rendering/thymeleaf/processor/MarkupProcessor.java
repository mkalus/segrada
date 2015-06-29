package org.segrada.rendering.thymeleaf.processor;

import org.segrada.rendering.markup.MarkupFilter;
import org.segrada.rendering.markup.MarkupFilterFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.thymeleaf.processor.element.AbstractMarkupSubstitutionElementProcessor;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

import java.util.ArrayList;
import java.util.List;

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
 * Helper class to provide markup functionality to thymeleaf
 *
 * Usage: <th:markup markup="'html'" text="'text'" />
 */
public class MarkupProcessor extends AbstractMarkupSubstitutionElementProcessor {
	public MarkupProcessor() {
		super("markup");
	}

	public int getPrecedence() {
		return 1000;
	}

	@Override
	protected List<Node> getMarkupSubstitutes(final Arguments arguments, final Element element) {
		final Configuration configuration = arguments.getConfiguration();

		// Obtain the Thymeleaf Standard Expression parser
		final IStandardExpressionParser parser =
				StandardExpressions.getExpressionParser(configuration);

		// Obtain the attribute value
		final String attributeValue = element.getAttributeValue("text");

		// Parse the attribute value as a Thymeleaf Standard Expression
		final IStandardExpression expression =
				parser.parseExpression(configuration, arguments, attributeValue);

		// Execute the expression just parsed
		final String content =
				(String) expression.execute(configuration, arguments);



		// get markup processor value
		final String markupAttributeValue = element.getAttributeValue("markup");

		// Parse the attribute value as a Thymeleaf Standard Expression
		final IStandardExpression markupExpression =
				parser.parseExpression(configuration, arguments, markupAttributeValue);

		// Execute the expression just parsed
		final String markup =
				(String) markupExpression.execute(configuration, arguments);



		// create div to contain markup text
		final Element container = new Element("div");
		container.setAttribute("class", "sg-markup");

		// render
		try {
			final Text text = new Text(markup(content, markup));
			container.addChild(text);
		} catch (Exception e) {
			//TODO log
		}

		final List<Node> nodes = new ArrayList<>();
		nodes.add(container); return nodes;
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
