package org.segrada.rendering.thymeleaf.processor;

import org.junit.Test;

import static org.junit.Assert.*;

public class Nl2BrProcessorTest {

	@Test
	public void textNl2br() throws Exception {
		Nl2BrProcessor processor = new Nl2BrProcessor();

		// simple test cases
		assertEquals("Hello<br/>\nWorld!", processor.nl2br("Hello\nWorld!"));
		assertEquals("Hello<br/>\nWorld!", processor.nl2br("Hello\r\nWorld!"));
		assertEquals("Hello<br/>\nWorld!", processor.nl2br("Hello\rWorld!"));
		assertEquals("Hello<br/>\nWorld<br/>\n!", processor.nl2br("Hello\nWorld\n!"));
	}
}