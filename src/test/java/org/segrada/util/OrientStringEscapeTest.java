package org.segrada.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OrientStringEscapeTest {

	@Test
	public void testEscapeOrientSql() throws Exception {
		assertNull(OrientStringEscape.escapeOrientSql(null));
		assertEquals("", OrientStringEscape.escapeOrientSql(""));
		assertEquals("test", OrientStringEscape.escapeOrientSql("test"));
		assertEquals("\"test\"", OrientStringEscape.escapeOrientSql("\"test\""));
		assertEquals("\\'test\\'", OrientStringEscape.escapeOrientSql("'test'"));
	}
}
