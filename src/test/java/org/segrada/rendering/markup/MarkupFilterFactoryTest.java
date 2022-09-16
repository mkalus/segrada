package org.segrada.rendering.markup;

import org.junit.jupiter.api.Test;
import org.segrada.rendering.markup.DefaultMarkupFilter;
import org.segrada.rendering.markup.MarkupFilter;
import org.segrada.rendering.markup.MarkupFilterFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkupFilterFactoryTest {

	@Test
	public void testProduce() throws Exception {
		MarkupFilter markupFilter = MarkupFilterFactory.produce("default");
		assertNotNull(markupFilter);
	}

	@Test
	public void testProduceUnknownType() throws Exception {
		MarkupFilter markupFilter = MarkupFilterFactory.produce("unknown");
		assertTrue(markupFilter instanceof DefaultMarkupFilter);
	}

	@Test
	public void testProduceNoArguments() throws Exception {
		MarkupFilter markupFilter = MarkupFilterFactory.produce();
		assertTrue(markupFilter instanceof DefaultMarkupFilter);
	}
}
