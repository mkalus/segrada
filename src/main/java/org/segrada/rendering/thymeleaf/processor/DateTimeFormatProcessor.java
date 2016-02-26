package org.segrada.rendering.thymeleaf.processor;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.segrada.util.NumberFormatter;
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
 * Helper class to process date time millis to readable date
 *
 * Usage: <th:datetimeformat millis="'1234567'" format="'HH'" />
 *
 */
public class DateTimeFormatProcessor extends AbstractMarkupSubstitutionElementProcessor {
	private NumberFormatter numberFormatter;

	public DateTimeFormatProcessor() {
		super("datetimeformat");
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
		final String attributeValue = element.getAttributeValue("millis");

		// Parse the attribute value as a Thymeleaf Standard Expression
		final IStandardExpression expression =
				parser.parseExpression(configuration, arguments, attributeValue);

		// Execute the expression just parsed
		final Long number =
				(Long) expression.execute(configuration, arguments);



		// get markup processor value
		final String formatAttributeValue = element.getAttributeValue("format");

		// Parse the attribute value as a Thymeleaf Standard Expression
		final IStandardExpression formatExpression =
				parser.parseExpression(configuration, arguments, formatAttributeValue);

		// Execute the expression just parsed
		final String format =
				(String) formatExpression.execute(configuration, arguments);

		String text;
		if (number == null || format == null || format.isEmpty()) {
			text = "";
		} else {
			DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
			text = formatter.print(number);
		}

		final List<Node> nodes = new ArrayList<>();

		// render
		try {
			final Text textElement = new Text(text);
			nodes.add(textElement);
		} catch (Exception e) {
			//TODO log
		}

		return nodes;
	}
}
