package org.segrada.rendering.thymeleaf;

import org.junit.Test;

import static org.junit.Assert.*;

public class SegradaFormatterTest {
	@Test
	public void testNl2br() throws Exception {
		SegradaFormatter formatter = new SegradaFormatter();

		// simple test cases
		assertEquals("Hello<br/>\nWorld!", formatter.nl2br("Hello\nWorld!"));
		assertEquals("Hello<br/>\nWorld!", formatter.nl2br("Hello\r\nWorld!"));
		assertEquals("Hello<br/>\nWorld!", formatter.nl2br("Hello\rWorld!"));
		assertEquals("Hello<br/>\nWorld<br/>\n!", formatter.nl2br("Hello\nWorld\n!"));
	}

	@Test
	public void testMarkup() throws Exception {
		SegradaFormatter formatter = new SegradaFormatter();

		// no NPEs!
		assertEquals("", formatter.markup(null, "default"));
		assertEquals("", formatter.markup(null, ""));
		assertEquals("", formatter.markup(null, null));

		// basic test
		assertEquals("Test", formatter.markup("Test", "default"));
		assertEquals("Test<br/>\nTest", formatter.markup("Test\nTest", "default"));
		assertEquals("&copy;", formatter.markup("(c)", "default"));

		// this should prove that we are indeed calling some markup
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCorruptMarkup1() throws Exception {
		SegradaFormatter formatter = new SegradaFormatter();

		assertEquals("", formatter.markup("", "xxxyyy"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCorruptMarkup2() throws Exception {
		SegradaFormatter formatter = new SegradaFormatter();

		assertEquals("", formatter.markup("Test", "xxxyyy"));
	}
}