package org.segrada.util;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright 2015-2019 Maximilian Kalus [segrada@auxnet.de]
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
	 * instance of LanguageDetector
	 */
	private static LanguageDetector languageDetector;

	/**
	 * instance of TextObjectFactory
	 */
	private static TextObjectFactory textObjectFactory;

	static {
		try {
			//load all languages:
			List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

			//build language detector:
			languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
					.withProfiles(languageProfiles)
					.build();

			//create a text object factory
			textObjectFactory = CommonTextObjectFactories.forDetectingShortCleanText();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading language in TextExtractor (Silent fallback to English)", e);
		}
	}

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
			// remove 65533 artifact at beginning
			if (plain.length() > 0 && plain.charAt(0) == 65533) {
				plain = plain.replace((char) 65533, (char) 10).trim();
			}
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
	 * @return identified language (or enpty if no language could be detected)
	 */
	public String identifyLanguage(String text) {
		TextObject textObject = textObjectFactory.forText(text);
		Optional<LdLocale> lang = languageDetector.detect(textObject);

		// no language present?
		if (lang.isPresent())
			return lang.get().getLanguage();

		return ""; // fallback to none
	}
}
