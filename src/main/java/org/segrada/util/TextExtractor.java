package org.segrada.util;

import org.apache.tika.Tika;
import org.apache.tika.language.LanguageIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * Extract text from document using Tika
 */
public class TextExtractor {
	private static final Logger logger = Logger.getLogger(TextExtractor.class.getName());

	/**
	 * extract text from input document
	 * @param source input stream document
	 * @return plain text extracted from document
	 */
	public String parseToString(InputStream source) {
		Tika tika = new Tika();
		try {
			// parse to string
			String plain = tika.parseToString(source);

			// some formatting and checking
			if (plain == null) return null;
			plain = plain.trim();
			if (plain.equals("")) return null;
			return plain;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not parse input stream " + source + ": " + e.getMessage());
		} finally {
			try {
				source.close();
			} catch (IOException e) {
				logger.fine("Could not close input stream " + source + ": " + e.getMessage());
			}
		}

		// return null on error
		return null;
	}

	/**
	 * identify language of a text
	 * @param text inserted
	 * @return identified language
	 */
	public String identifyLanguage(String text) {
		LanguageIdentifier identifier = new LanguageIdentifier(text);
		return identifier.getLanguage();
	}
}
