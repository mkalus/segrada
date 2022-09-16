package org.segrada.rendering.markup;

import org.junit.jupiter.api.Test;
import org.segrada.rendering.markup.DefaultMarkupFilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultMarkupFilterTest {
	@Test
	public void testToHTML() throws Exception {
		DefaultMarkupFilter filter = new DefaultMarkupFilter();

		String test1 = "This is a text string";
		assertEquals("This is a text string", filter.toHTML(test1), "Test 1 failed");

		String test2 = "This is a text string\nNext line";
		assertEquals("This is a text string<br/>\nNext line", filter.toHTML(test2), "Test 2 failed");

		String test3 = "This is a text string\rNext line";
		assertEquals("This is a text string<br/>\nNext line", filter.toHTML(test3), "Test 3 failed");

		String test4 = "This is a text string\r\nNext line";
		assertEquals("This is a text string<br/>\nNext line", filter.toHTML(test4), "Test 4 failed");

		String test5 = "<br>ÄÖÜ@µ";
		assertEquals("&lt;br&gt;&Auml;&Ouml;&Uuml;@&micro;", filter.toHTML(test5), "Test 5 failed");
	}

	@Test
	public void testToHTMLDecorations() throws Exception {
		DefaultMarkupFilter filter = new DefaultMarkupFilter();

		String test1 = "Normaltext *bold* _emphasised_ ==underline== *not\nbold* _not\nemphasised_ ==no\nunderline== *not\n*yesbold*";
		assertEquals("Normaltext <strong>bold</strong> <em>emphasised</em> <span style=\"text-decoration:underline\">underline</span> *not<br/>\n" +
				"bold* _not<br/>\n" +
				"emphasised_ ==no<br/>\n" +
				"underline== *not<br/>\n" +
				"<strong>yesbold</strong>", filter.toHTML(test1), "Decoration test failed");
	}

	@Test
	public void testToHTMLBibliographicAnnotations() throws Exception {
		DefaultMarkupFilter filter = new DefaultMarkupFilter();

		String test1 = "[[haebler:rott]] Blahblah [13f:]";
		assertEquals("[[haebler:rott]] Blahblah <span class=\"sg-label sg-info\">13f:</span>", filter.toHTML(test1), "Bibliographic annotations test failed");
	}

	@Test
	public void testToHTMLEntities() throws Exception {
		DefaultMarkupFilter filter = new DefaultMarkupFilter();

		String test1 = "1 - 2--3. (c)(C)(R)<=><-> <=<- =>->";
		assertEquals("1 &ndash; 2&mdash;3. &copy;&copy;&reg;&hArr;&harr; &lArr;&larr; &rArr;&rarr;", filter.toHTML(test1), "Entity test failed");
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

	@Test
	public void testBibRefPattern() throws Exception {
		Pattern bibRefPattern = DefaultMarkupFilter.getBibRefPattern();

		Matcher matcher = bibRefPattern.matcher("[[haebler:rott]] Blahblah [13f:]");
		assertEquals("_R_ Blahblah [13f:]", matcher.replaceFirst("_R_"));

		matcher = bibRefPattern.matcher("Blahblah [13f:] [[haebler:rott]] Blahblah [13f:]");
		assertEquals("Blahblah [13f:] _R_ Blahblah [13f:]", matcher.replaceFirst("_R_"));

		matcher = bibRefPattern.matcher("[[haebler:rott]] Blahblah [13f:] [[test:test1]]");
		assertEquals("_R_ Blahblah [13f:] _R_", matcher.replaceAll("_R_"));

		matcher = bibRefPattern.matcher("from source [[article:Procurement Nadir: India Howitzer Competitions]]");
		assertEquals("from source _R_", matcher.replaceFirst("_R_"));
	}
}
