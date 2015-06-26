package org.segrada.rendering.markup;

import org.junit.Test;
import org.segrada.rendering.markup.HtmlMarkupFilter;

import static org.junit.Assert.assertEquals;

public class HtmlMarkupFilterTest {
	@Test
	public void testToHTML() throws Exception {
		HtmlMarkupFilter filter = new HtmlMarkupFilter();

		String test1 = "This is a text string";
		assertEquals("Test 1 failed", test1, filter.toHTML(test1));

		String test2 = "<h1>This is a text string</h1>";
		assertEquals("Test 1 failed", test2, filter.toHTML(test2));

		//trivial...
	}

	@Test
	public void testToPlain() throws Exception {
		String test = "<h1>Hello World</h1>\n" +
				"<p>This is a text</p><p>yes</p>" +
				"It is not well formed </p>" +
				"still it should be converted <img data-text=\"correctly\" src=\"hello.jpg\" />\n" +
				"ÄÖÜ@µ\n" +
				"&Auml;&auml;&copy;&nbsp;&hellip;&lt;&#062;<br />" +
				"<b>Bye!</b>";

		HtmlMarkupFilter filter = new HtmlMarkupFilter();
		assertEquals("Hello World This is a text yesIt is not well formed still it should be converted ÄÖÜ@µ Ää© …<> Bye!", filter.toPlain(test));
	}
}