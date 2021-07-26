package org.segrada.rendering.thymeleaf.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
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
 * Helper for Thymeleaf to check for browser openable mime types
 */
public final class MimeTypeIsRenderable {
	private MimeTypeIsRenderable() throws InstantiationException{
		throw new InstantiationException("The class is not created for instantiation");
	}

	private static final Set<String> validMimeTypes;

	// valid types
	static {
		validMimeTypes = new HashSet<>();
		validMimeTypes.add("application/xhtml+xml");
		validMimeTypes.add("application/xml");
		validMimeTypes.add("image/gif");
		validMimeTypes.add("image/jpeg");
		validMimeTypes.add("image/png");
		validMimeTypes.add("image/svg+xml");
		validMimeTypes.add("image/x-icon");
		validMimeTypes.add("text/html");
		validMimeTypes.add("text/plain");
		validMimeTypes.add("text/xml");
		validMimeTypes.add("video/mpeg");
	}

	/**
	 * Check mime type if it is renderable
	 * @param mimeType to check
	 * @return true if rendable by browser
	 */
	public static boolean isRenderable(String mimeType) {
		// fallback
		if (mimeType == null || mimeType.isEmpty()) return false;

		if (validMimeTypes.contains(mimeType))
			return true;

		return false;
	}
}
