package org.segrada.util;

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
 * Fuzzy Date Renderer to print out fuzzy dates nicely
 */
public final class FuzzyDateRenderer {
	private FuzzyDateRenderer() throws InstantiationException {
		throw new InstantiationException("The class is not created for instantiation");
	}

	/**
	 * basic date rendering
	 * @param julianDate JD value of date
	 * @param dateString date string e.g. 2.1505
	 * @param calendar calendar to use, e.g. G or J
	 * @param fuzzyFlags char[] array of fuzzy flags to render with
	 * @return
	 */
	public static String render(long julianDate, String dateString, String calendar, char[] fuzzyFlags, String before, String after) {
		// no further work wirg empty values
		if (dateString == null || dateString.isEmpty()) return "";

		if (fuzzyFlags != null)
			for (char flag : fuzzyFlags)
				switch (flag) {
					case '-': dateString = before + ' ' + dateString; break; // add before prefix
					case '+': dateString = after + ' ' + dateString; break; // add after prefix
					case 'c': dateString = '~' + dateString; break; // add ca. prefix
					case '?': dateString += '?'; break; // add ? suffix
					default: break;
				}

		if (calendar != null && !calendar.isEmpty() && !calendar.equals("G")) {
			// default Julian calendar before 15.10.1582 - do not show explicitly
			if (!(julianDate < 2299161 && calendar.equals("J")))
				dateString += "<sup><small class=\"text-muted\">" + calendar + "</small></sup>";
		}

		return dateString;
	}

	/**
	 * Render from/to dates in a nice way
	 * @param fromJulianDate JD value of date (from)
	 * @param fromDateString date string e.g. 2.1505 (from)
	 * @param fromCalendar calendar to use, e.g. G or J (from)
	 * @param fromFuzzyFlags char[] array of fuzzy flags to render with (from)
	 * @param toJulianDate JD value of date (to)
	 * @param toDateString date string e.g. 2.1505 (to)
	 * @param toCalendar calendar to use, e.g. G or J (to)
	 * @param toFuzzyFlags char[] array of fuzzy flags to render with (to)
	 * @return
	 */
	public static String renderFromTo(int fromJulianDate, String fromDateString, String fromCalendar, char[] fromFuzzyFlags,
	                                  int toJulianDate, String toDateString, String toCalendar, char[] toFuzzyFlags, String before, String after) {
		// avoid NPEs
		if (fromDateString == null) fromDateString = "";
		if (toDateString == null) toDateString = "";

		// render only one date, if same
		if (fromDateString.equals(toDateString)) return render(fromJulianDate, fromDateString, fromCalendar, fromFuzzyFlags, before, after);

		// render two dates
		String from = render(fromJulianDate, fromDateString, fromCalendar, fromFuzzyFlags, before, after);
		String to = render(toJulianDate, toDateString, toCalendar, toFuzzyFlags, before, after);

		return from + " &ndash; " + to;
	}

	/**
	 * render fuzzy date or print --- if empty
	 * @param julianDate JD value of date
	 * @param dateString date string e.g. 2.1505
	 * @param calendar calendar to use, e.g. G or J
	 * @param fuzzyFlags char[] array of fuzzy flags to render with
	 * @return
	 */
	public static String renderOrEmpty(int julianDate, String dateString, String calendar, char[] fuzzyFlags, String before, String after) {
		String fuzzyDate = render(julianDate, dateString, calendar, fuzzyFlags, before, after);
		return fuzzyDate.isEmpty()?"---":fuzzyDate;
	}
}
