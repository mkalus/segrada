package org.segrada.rendering.markup;

import org.junit.Test;
import org.segrada.rendering.markup.DefaultMarkupFilter;

import static org.junit.Assert.assertEquals;

public class DefaultMarkupFilterTest {
	@Test
	public void testToHTML() throws Exception {
		DefaultMarkupFilter filter = new DefaultMarkupFilter();

		String test1 = "This is a text string";
		assertEquals("Test 1 failed", "This is a text string", filter.toHTML(test1));

		String test2 = "This is a text string\nNext line";
		assertEquals("Test 2 failed", "This is a text string<br/>\nNext line", filter.toHTML(test2));

		String test3 = "This is a text string\rNext line";
		assertEquals("Test 3 failed", "This is a text string<br/>\nNext line", filter.toHTML(test3));

		String test4 = "This is a text string\r\nNext line";
		assertEquals("Test 4 failed", "This is a text string<br/>\nNext line", filter.toHTML(test4));

		String test5 = "<br>ÄÖÜ@µ";
		assertEquals("Test 5 failed", "&lt;br&gt;&Auml;&Ouml;&Uuml;@&micro;", filter.toHTML(test5));
	}

	@Test
	public void testToHTMLDecorations() throws Exception {
		DefaultMarkupFilter filter = new DefaultMarkupFilter();

		String test1 = "Normaltext *bold* _emphasised_ ==underline== *not\nbold* _not\nemphasised_ ==no\nunderline== *not\n*yesbold*";
		assertEquals("Decoration test failed", "Normaltext <strong>bold</strong> <em>emphasised</em> <span style=\"text-decoration:underline\">underline</span> *not<br/>\n" +
				"bold* _not<br/>\n" +
				"emphasised_ ==no<br/>\n" +
				"underline== *not<br/>\n" +
				"<strong>yesbold</strong>", filter.toHTML(test1));
	}

	@Test
	public void testToHTMLBibliographicAnnotations() throws Exception {
		DefaultMarkupFilter filter = new DefaultMarkupFilter();

		String test1 = "[[haebler:rott]] Blahblah [13f:]";
		assertEquals("Bibliographic annotations test failed", "[[haebler:rott]] Blahblah <span class=\"sg-label sg-info\">13f:</span>", filter.toHTML(test1));
	}

	@Test
	public void testToHTMLEntities() throws Exception {
		DefaultMarkupFilter filter = new DefaultMarkupFilter();

		String test1 = "1 - 2--3. (c)(C)(R)<=><-> <=<- =>->";
		assertEquals("Entity test failed", "1 &ndash; 2&mdash;3. &copy;&copy;&reg;&hArr;&harr; &lArr;&larr; &rArr;&rarr;", filter.toHTML(test1));
	}

	@Test
	public void testToPlain() throws Exception {
		String test = "This is a text string\n" +
				"[[haebler:rott]] Blahblah [13f:]\n" +
				"*bold* _emphasised_ ==underline== *not\n" +
				"bold* _not\n" +
				"emphasised_ ==no\n" +
				"underline== *not\n" +
				"*yesbold*\n" +
				"ÄÖÜ@µ";

		DefaultMarkupFilter filter = new DefaultMarkupFilter();
		assertEquals("This is a text string Blahblah bold emphasised underline *not bold* _not emphasised_ ==no underline== *not yesbold ÄÖÜ@µ", filter.toPlain(test));
	}
}