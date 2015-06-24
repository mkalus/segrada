package org.segrada.util;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FlexibleDateParserTest {
	@Test
	public void testGetChronologyFromType() throws Exception {
		FlexibleDateParser parser = new FlexibleDateParser();

		assertEquals(org.joda.time.chrono.GJChronology.class, parser.getChronologyFromType(null).getClass());
		assertEquals(org.joda.time.chrono.GJChronology.class, parser.getChronologyFromType("").getClass());
		assertEquals(org.joda.time.chrono.GJChronology.class, parser.getChronologyFromType("X").getClass());
		assertEquals(org.joda.time.chrono.GJChronology.class, parser.getChronologyFromType("G").getClass());
		assertEquals(org.joda.time.chrono.JulianChronology.class, parser.getChronologyFromType("J").getClass());
	}

	@Test
	public void testParseInput() throws Exception {
		FlexibleDateParser parser = new FlexibleDateParser();

		// empty
		DateTime dt = parser.parseInput(null, "G");
		assertNull(dt);
		dt = parser.parseInput("", "G");
		assertNull(dt);

		// simple year
		dt = parser.parseInput("1585", "G");
		assertEquals(1585, dt.getYear());
		dt = parser.parseInput("85", "G");
		assertEquals(85, dt.getYear());
		dt = parser.parseInput("-85", "G");
		assertEquals(-85, dt.getYear());

		// month/year
		dt = parser.parseInput("05/1722", "G");
		assertEquals(1722, dt.getYear());
		assertEquals(5, dt.getMonthOfYear());
		// month/year
		dt = parser.parseInput("5/1066", "G");
		assertEquals(1066, dt.getYear());
		assertEquals(5, dt.getMonthOfYear());
		// month/year
		dt = parser.parseInput("01.1822", "G");
		assertEquals(1822, dt.getYear());
		assertEquals(1, dt.getMonthOfYear());
		// month/year
		dt = parser.parseInput("02-1823", "G");
		assertEquals(1823, dt.getYear());
		assertEquals(2, dt.getMonthOfYear());
		// month/year
		dt = parser.parseInput("1824-03", "G");
		assertEquals(1824, dt.getYear());
		assertEquals(3, dt.getMonthOfYear());

		// day/month/year
		dt = parser.parseInput("28.4.1289", "G");
		assertEquals(1289, dt.getYear());
		assertEquals(4, dt.getMonthOfYear());
		assertEquals(28, dt.getDayOfMonth());
		// day/month/year
		dt = parser.parseInput("30-1-1769", "G");
		assertEquals(1769, dt.getYear());
		assertEquals(1, dt.getMonthOfYear());
		assertEquals(30, dt.getDayOfMonth());
		// day/month/year
		dt = parser.parseInput("1882-5-19", "G");
		assertEquals(1882, dt.getYear());
		assertEquals(5, dt.getMonthOfYear());
		assertEquals(19, dt.getDayOfMonth());
		// day/month/year => English!
		dt = parser.parseInput("12/6/1599", "G");
		assertEquals(1599, dt.getYear());
		assertEquals(12, dt.getMonthOfYear());
		assertEquals(6, dt.getDayOfMonth());
	}

	@Test
	public void testInputToJd() throws Exception {
		FlexibleDateParser parser = new FlexibleDateParser();

		Long jd;
		// test empty
		assertEquals(new Long(Long.MIN_VALUE), parser.inputToJd(null, "G", false));
		assertEquals(new Long(Long.MIN_VALUE), parser.inputToJd("", "G", false));
		assertEquals(new Long(Long.MAX_VALUE), parser.inputToJd(null, "G", true));
		assertEquals(new Long(Long.MAX_VALUE), parser.inputToJd("", "G", true));

		// test start jd entries
		assertEquals(new Long(2299970), parser.inputToJd("1585", "G", false));
		assertEquals(new Long(2299980), parser.inputToJd("1585", "J", false)); // julian
		assertEquals(new Long(2299970), parser.inputToJd("1.1585", "G", false));
		assertEquals(new Long(2299970), parser.inputToJd("1.1.1585", "G", false));
		assertEquals(new Long(2299971), parser.inputToJd("2.1.1585", "G", false));
		assertEquals(new Long(2300001), parser.inputToJd("2.1585", "G", false));
		assertEquals(new Long(2300001), parser.inputToJd("1.2.1585", "G", false));
		assertEquals(new Long(2300002), parser.inputToJd("2.2.1585", "G", false));

		// test end js entries
		assertEquals(new Long(2300334), parser.inputToJd("1585", "G", true));
		assertEquals(new Long(2300344), parser.inputToJd("1585", "J", true)); // julian
		assertEquals(new Long(2300000), parser.inputToJd("1.1585", "G", true));
		assertEquals(new Long(2299970), parser.inputToJd("1.1.1585", "G", true));
		assertEquals(new Long(2299971), parser.inputToJd("2.1.1585", "G", true));
		assertEquals(new Long(2300028), parser.inputToJd("2.1585", "G", true));
	}
}