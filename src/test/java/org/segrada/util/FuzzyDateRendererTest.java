package org.segrada.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FuzzyDateRendererTest {

	@Test
	public void testRender() throws Exception {
		assertEquals("", FuzzyDateRenderer.render(0, "", "G", new char[0], "", ""));
		assertEquals("", FuzzyDateRenderer.render(0, null, "G", new char[0], "", ""));

		assertEquals("1505", FuzzyDateRenderer.render(0, "1505", "G", new char[0], "", ""));
		assertEquals("1505", FuzzyDateRenderer.render(0, "1505", "J", new char[0], "", ""));

		assertEquals("~1505", FuzzyDateRenderer.render(0, "1505", "G", new char[]{'c'}, "", ""));
		assertEquals("~1505", FuzzyDateRenderer.render(0, "1505", "J", new char[]{'c'}, "", ""));

		assertEquals("~1505?", FuzzyDateRenderer.render(0, "1505", "G", new char[]{'c', '?'}, "", ""));
		assertEquals("1505?", FuzzyDateRenderer.render(0, "1505", "J", new char[]{'?'}, "", ""));

		assertEquals("1585<sup><small class=\"text-muted\">J</small></sup>", FuzzyDateRenderer.render(2300257, "1585", "J", new char[0], "", ""));
		assertEquals("1585?<sup><small class=\"text-muted\">J</small></sup>", FuzzyDateRenderer.render(2300257, "1585", "J", new char[]{'?'}, "", ""));
		assertEquals("~1585<sup><small class=\"text-muted\">J</small></sup>", FuzzyDateRenderer.render(2300257, "1585", "J", new char[]{'c'}, "", ""));
	}

	@Test
	public void testRenderOrEmpty() throws Exception {
		assertEquals("---", FuzzyDateRenderer.renderOrEmpty(0, "", "G", new char[0], "", ""));
		assertEquals("---", FuzzyDateRenderer.renderOrEmpty(0, null, "G", new char[0], "", ""));

		assertEquals("1505", FuzzyDateRenderer.render(0, "1505", "G", new char[0], "", ""));
		assertEquals("1505", FuzzyDateRenderer.render(0, "1505", "J", new char[0], "", ""));

		assertEquals("~1505", FuzzyDateRenderer.render(0, "1505", "G", new char[]{'c'}, "", ""));
		assertEquals("~1505", FuzzyDateRenderer.render(0, "1505", "J", new char[]{'c'}, "", ""));

		assertEquals("~1505?", FuzzyDateRenderer.render(0, "1505", "G", new char[]{'c', '?'}, "", ""));
		assertEquals("1505?", FuzzyDateRenderer.render(0, "1505", "J", new char[]{'?'}, "", ""));
	}

	@Test
	public void testRenderFromTo() throws Exception {
		assertEquals("", FuzzyDateRenderer.renderFromTo(0, "", "G", new char[0], 0, "", "G", new char[0], "", ""));
		assertEquals("", FuzzyDateRenderer.renderFromTo(0, null, "G", new char[0], 0, null, "G", new char[0], "", ""));
		assertEquals("", FuzzyDateRenderer.renderFromTo(0, null, "G", new char[0], 0, "", "G", new char[0], "", ""));
		assertEquals("", FuzzyDateRenderer.renderFromTo(0, "", "G", new char[0], 0, null, "G", new char[0], "", ""));

		assertEquals("1505 &ndash; ", FuzzyDateRenderer.renderFromTo(0, "1505", "G", new char[0], 0, null, "G", new char[0], "", ""));
		assertEquals("1505 &ndash; 1506", FuzzyDateRenderer.renderFromTo(0, "1505", "G", new char[0], 0, "1506", "G", new char[0], "", ""));
		assertEquals(" &ndash; 1506", FuzzyDateRenderer.renderFromTo(0, "", "G", new char[0], 0, "1506", "G", new char[0], "", ""));
	}
}
