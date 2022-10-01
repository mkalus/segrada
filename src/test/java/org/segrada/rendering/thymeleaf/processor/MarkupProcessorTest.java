package org.segrada.rendering.thymeleaf.processor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MarkupProcessorTest {
	@Test
	public void testMarkup() throws Exception {
		MarkupProcessor processor = new MarkupProcessor("sg");

		// no NPEs!
		assertEquals("", processor.markup(null, "default"));
		assertEquals("", processor.markup(null, ""));
		assertEquals("", processor.markup(null, null));

		// basic test
		assertEquals("Test", processor.markup("Test", "default"));
		assertEquals("Test<br/>\nTest", processor.markup("Test\nTest", "default"));
		assertEquals("&copy;", processor.markup("(c)", "default"));

		// this should prove that we are indeed calling some markup
	}

	@Test
	public void testCorruptMarkup() throws Exception {
		MarkupProcessor processor = new MarkupProcessor("th");

		assertEquals("", processor.markup("", "xxxyyy"));
		assertEquals("Test", processor.markup("Test", "xxxyyy"));
	}
}
