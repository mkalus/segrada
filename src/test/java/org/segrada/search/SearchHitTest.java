package org.segrada.search;

import org.junit.Test;

import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class SearchHitTest {
	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new SearchHit());
	}
}