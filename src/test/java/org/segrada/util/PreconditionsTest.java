package org.segrada.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PreconditionsTest {

	@Test
	public void testCheckNotNull() throws Exception {
		String test = "Hallo";

		assertEquals(test, Preconditions.checkNotNull(test, "test"));
		// no exception should be thrown
	}

	@Test
	public void testCheckNotNullFail() throws Exception {
		assertThrows(NullPointerException.class, () -> Preconditions.checkNotNull(null, "test"));
		// exception should be thrown
	}
}
