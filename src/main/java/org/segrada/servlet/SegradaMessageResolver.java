package org.segrada.servlet;

import org.segrada.rendering.thymeleaf.SegradaMessageResolutionUtils;
import org.thymeleaf.Arguments;
import org.thymeleaf.messageresolver.MessageResolution;
import org.thymeleaf.messageresolver.StandardMessageResolver;

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
 * Message resolver overwriting settings of Thymeleaf
 */
public class SegradaMessageResolver extends StandardMessageResolver {
	@Override
	public MessageResolution resolveMessage(Arguments arguments, String key, Object[] messageParameters) {
		// This method can be overriden

		checkInitialized();

		// custom message resolution
		final String message =
				SegradaMessageResolutionUtils.resolveMessageForTemplate(
						arguments, key, messageParameters, unsafeGetDefaultMessages());

		if (message == null) {
			return null;
		}

		return new MessageResolution(message);
	}
}
