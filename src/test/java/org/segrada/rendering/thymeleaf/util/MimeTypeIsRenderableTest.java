package org.segrada.rendering.thymeleaf.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class MimeTypeIsRenderableTest {
	@Test
	public void isRenderable() throws Exception {
		assertFalse(MimeTypeIsRenderable.isRenderable(null));
		assertFalse(MimeTypeIsRenderable.isRenderable(""));
		assertFalse(MimeTypeIsRenderable.isRenderable("image"));
		assertFalse(MimeTypeIsRenderable.isRenderable("application/pdf")); // not directly openable (opened in viewer)
		assertTrue(MimeTypeIsRenderable.isRenderable("application/xhtml+xml"));
		assertTrue(MimeTypeIsRenderable.isRenderable("application/xml"));
		assertTrue(MimeTypeIsRenderable.isRenderable("image/png"));
		assertTrue(MimeTypeIsRenderable.isRenderable("text/plain"));
		assertTrue(MimeTypeIsRenderable.isRenderable("image/gif"));
		assertTrue(MimeTypeIsRenderable.isRenderable("image/jpeg"));
		assertTrue(MimeTypeIsRenderable.isRenderable("image/x-icon"));
		assertTrue(MimeTypeIsRenderable.isRenderable("text/html"));
		assertTrue(MimeTypeIsRenderable.isRenderable("text/xml"));
		assertTrue(MimeTypeIsRenderable.isRenderable("video/mpeg"));
	}

}