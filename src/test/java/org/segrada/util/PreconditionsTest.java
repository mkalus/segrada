package org.segrada.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PreconditionsTest {

	@Test
	public void testCheckNotNull() throws Exception {
		String test = "Hallo";

		assertEquals(test, Preconditions.checkNotNull(test, "test"));
		// no exception should be thrown
	}

	@Test(expected = NullPointerException.class)
	public void testCheckNotNullFail() throws Exception {
		assertNull(Preconditions.checkNotNull(null, "test"));
		// exception should be thrown
	}
}