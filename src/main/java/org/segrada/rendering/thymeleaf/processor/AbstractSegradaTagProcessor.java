package org.segrada.rendering.thymeleaf.processor;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
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
 * Abstract base class for segrada tag processors
 */
abstract public class AbstractSegradaTagProcessor extends AbstractElementTagProcessor {
	public AbstractSegradaTagProcessor(TemplateMode templateMode, String dialectPrefix, String elementName, boolean prefixElementName, String attributeName, boolean prefixAttributeName, int precedence) {
		super(templateMode, dialectPrefix, elementName, prefixElementName, attributeName, prefixAttributeName, precedence);
	}

	/**
	 * helper method to parse a tag value through the IStandardExpressionParser
	 * @param parser standard expression parser
	 * @param context Template context
	 * @param tag tag to be parsed
	 * @param tagName tag attribute name
	 * @return parsed tag value
	 */
	<T> T parseTagValue(final IStandardExpressionParser parser, final ITemplateContext context, final IProcessableElementTag tag, final String tagName) {
		// Parse the attribute value as a Thymeleaf Standard Expression
		try {
			return (T) parser.parseExpression(context, tag.getAttributeValue(tagName)).execute(context);
		} catch (ClassCastException e) {
			return null;
		}
	}

	/**
	 * obtain standard expression parser
	 * @param context Template context
	 * @return standard expression parser
	 */
	IStandardExpressionParser getParser(final ITemplateContext context) {
		final IEngineConfiguration configuration = context.getConfiguration();

		return StandardExpressions.getExpressionParser(configuration);
	}
}
