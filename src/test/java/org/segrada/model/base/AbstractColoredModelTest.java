package org.segrada.model.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractColoredModelTest {
	@Test
	public void testGetColorCode() throws Exception {
		final MockAbstractColoredModel entity = new MockAbstractColoredModel();

		// null
		assertEquals("", entity.getColorCode());

		entity.setColor(0);
		assertEquals("#000000", entity.getColorCode());

		entity.setColor(0x112233);
		assertEquals("#112233", entity.getColorCode());

		entity.setColor(0xabcdef);
		assertEquals("#ABCDEF", entity.getColorCode());

		entity.setColor(0xffffff);
		assertEquals("#FFFFFF", entity.getColorCode());
	}

	/**
	 * method to test
	 */
	private class MockAbstractColoredModel extends AbstractColoredModel {
		@Override
		public void setId(String id) {

		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public String getTitle() {
			return "DUMMY";
		}
	}
}
