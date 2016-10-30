package org.segrada.rendering.thymeleaf.processor;

import org.segrada.util.NumberFormatter;
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
 * Helper class to process numbers in a nice way
 *
 * Usage: <sg:formatnumber number="'1234567'" format="'si'" />
 *
 * format must be si or binary
 */
public class NumberFormatProcessor extends AbstractSegradaTagProcessor {
	/**
	 * Tag name
	 */
	private static final String TAG_NAME = "formatnumber";

	/**
	 * Precedence of processor
	 */
	private static final int PRECEDENCE = 1000;

	private NumberFormatter numberFormatter;

	/**
	 * Constructor
	 * @param dialectPrefix dialect prefix, e.g. th or segrada
	 */
	public NumberFormatProcessor(final String dialectPrefix) {
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
		final Long number = parseTagValue(parser, context, tag, "number");
		final String format = parseTagValue(parser, context, tag, "format");

		// interpret values
		boolean si = "si".equals(format);

		// if number ok
		if (number != null) {
			// lazily create instance
			if (numberFormatter == null) numberFormatter = new NumberFormatter();

			// parse number
			structureHandler.replaceWith(numberFormatter.humanReadableByteCount(number, si), false);
		} else {
			// do not show element at all
			structureHandler.removeElement();
		}
	}
}
