package org.segrada.rendering.thymeleaf;

import org.segrada.rendering.thymeleaf.processor.*;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright 2016 Maximilian Kalus [segrada@auxnet.de]
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
 * Custom segrada dialect elements using "th:" prefix
 */
public class SegradaDialect extends AbstractProcessorDialect {
	/**
	 * dialect name
	 */
	private static final String DIALECT_NAME = "Segrada Dialect";

	/**
	 * Constructor
	 */
	public SegradaDialect() {
		super(DIALECT_NAME, "sg", StandardDialect.PROCESSOR_PRECEDENCE);
	}

	@Override
	public Set<IProcessor> getProcessors(final String dialectPrefix) {
		final Set<IProcessor> processors = new HashSet<>();
		processors.add(new Nl2BrProcessor(dialectPrefix));
		processors.add(new MarkupProcessor(dialectPrefix));
		processors.add(new NumberFormatProcessor(dialectPrefix));
		processors.add(new DateTimeFormatProcessor(dialectPrefix));
		processors.add(new StripWhitespaceProcessor(dialectPrefix));
		return processors;
	}
}
