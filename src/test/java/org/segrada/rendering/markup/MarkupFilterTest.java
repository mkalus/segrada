package org.segrada.rendering.markup;

import org.junit.Test;
import org.segrada.rendering.markup.MarkupFilter;

import static org.junit.Assert.assertEquals;

public class MarkupFilterTest {

	@Test
	public void testGetName() throws Exception {
		MarkupFilter markupFilter = new MockMarkupFilter();
		assertEquals("mock", markupFilter.getName());
	}

	/**
	 * Mock class
	 */
	private class MockMarkupFilter extends MarkupFilter {
		@Override
		public String toHTML(String markupText) {
			return null;
		}

		@Override
		public String toPlain(String markupText) {
			return null;
		}
	}
}