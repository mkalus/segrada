package org.segrada.util;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

public class TextExtractorTest {

	@Test
	public void testParseToString() throws Exception {
		// create instance
		TextExtractor textExtractor = new TextExtractor();

		// try to load odt document
		InputStream odtTestIs = TextExtractorTest.class.getResourceAsStream("/documents/test.odt");

		// parse text
		String text = textExtractor.parseToString(odtTestIs);

		assertNotNull(text);
		assertTrue(text.startsWith("Test document"));

		// try to load pdf document
		InputStream pdfTestIs = TextExtractorTest.class.getResourceAsStream("/documents/test.pdf");

		// parse text
		text = textExtractor.parseToString(pdfTestIs);

		assertNotNull(text);
		assertTrue(text.startsWith("Test document"));

		// load non text document
		InputStream jpgTestIs = TextExtractorTest.class.getResourceAsStream("/img/test_frieda.jpg");

		// parse text
		text = textExtractor.parseToString(jpgTestIs);

		// should be empty
		assertNull(text);
	}

	@Test
	public void testIdentifyLanguage() throws Exception {
		// create instance
		TextExtractor textExtractor = new TextExtractor();

		String textEn = "This is an English text. Tika should be able to detect its language.";

		String lang = textExtractor.identifyLanguage(textEn);
		assertEquals("en", lang);

		String textDe = "Dies ist ein deutscher Text. Tika sollte in der Lage sein, die Sprache zu erkennen.";

		lang = textExtractor.identifyLanguage(textDe);
		assertEquals("de", lang);
	}
}