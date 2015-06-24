package org.segrada.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.JulianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import javax.annotation.Nullable;
import java.util.Locale;
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
 * Helper class to parse strings into dates
 */
public class FlexibleDateParser {
	private static final Logger logger = Logger.getLogger(FlexibleDateParser.class.getName());

	/**
	 * parsers
	 */
	protected static final DateTimeParser[] parsers = {
			DateTimeFormat.forPattern("y").getParser(),
			// month/year
			DateTimeFormat.forPattern("MM.y").getParser(),
			DateTimeFormat.forPattern("MM/y").getParser(),
			DateTimeFormat.forPattern("MM-y").getParser(),
			DateTimeFormat.forPattern("y-MM").getParser(),
			// month/day/year
			DateTimeFormat.forPattern("d.MM.y").getParser(),
			DateTimeFormat.forPattern("MM/d/y").getParser(),
			DateTimeFormat.forPattern("d-MM-y").getParser(),
			DateTimeFormat.forPattern("y-MM-d").getParser(),
	};

	/**
	 * DateTimeFormatter
	 */
	protected static DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

	/**
	 * Parse input to joda datetime
	 * @param input string
	 * @param type calendar type, e.g. "G" or "J"
	 * @return joda datetime
	 */
	public DateTime parseInput(@Nullable String input, String type) {
		if (input == null || "".equals(input)) return null;

		try {
			return inputFormatter.withChronology(getChronologyFromType(type))
					.withLocale(Locale.getDefault())
					.withZoneUTC()
					.parseDateTime(input);
		} catch(Exception e) {
			logger.warning("Could not parse to DateTime: " + input + " (type = " + type + ")");
		}
		return null;
	}

	/**
	 * Parse input to Julian Day number
	 * @param input string
	 * @param type calendar type, e.g. "G" or "J"
	 * @param high true to get last instance of parsed time, first otherwise
	 * @return julian day number
	 */
	public Long inputToJd(@Nullable String input, String type, boolean high) {
		// sanity check
		if (input == null || "".equals(input))
			return high?Long.MAX_VALUE:0L;

		try {
			DateTime date = inputFormatter.withChronology(getChronologyFromType(type))
					.withLocale(Locale.getDefault())
					.withZoneUTC()
					.parseDateTime(input);

			// get last time instance of the input
			if (high) {
				// guess input pattern by counting character occurences
				int count = Math.max(
						StringUtils.countMatches(input, "."),
						Math.max(
								StringUtils.countMatches(input, "/"),
								StringUtils.countMatches(input, "-")
						)
				);

				if (count == 0) // year only
					date = date.withMonthOfYear(12).withDayOfMonth(31);
				else if (count == 1) { // year/month
					date = date.withDayOfMonth(date.dayOfMonth().getMaximumValue());
				} // day/month/year as is
			}

			return DateTimeUtils.toJulianDayNumber(date.getMillis());
		} catch(Exception e) {
			logger.warning("Could not parse to DateTime: " + input + " (type = " + type + ")");
		}
		return null;
	}

	/**
	 * get chronology for type
	 * @param type e.g. "G" or "J"
	 * @return chronology for type
	 */
	protected Chronology getChronologyFromType(String type) {
		// fallback to default
		if (type == null) type = "G";

		if (type.equals("J")) return JulianChronology.getInstance();

		// default is gregorian/julian chronology
		return GJChronology.getInstance();
	}
}
