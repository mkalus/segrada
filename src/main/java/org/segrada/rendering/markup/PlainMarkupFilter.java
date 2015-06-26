package org.segrada.rendering.markup;

/**
 * Copyright 2015 Maximilian Kalus [segrada@auxnet.de]
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
 * Markup filter for plain texts without changing anything
 */
public class PlainMarkupFilter extends MarkupFilter {
	@Override
	public String toHTML(String markupText) {
		return markupText;
	}

	@Override
	public String toPlain(String markupText) {
		return markupText;
	}
}
