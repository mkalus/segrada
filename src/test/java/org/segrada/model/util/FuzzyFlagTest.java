package org.segrada.model.util;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FuzzyFlagTest {

	@Test
	public void testGetFuzzyValue() throws Exception {
		assertEquals('c', FuzzyFlag.FUZZY_CA.getFuzzyValue());
		assertEquals('?', FuzzyFlag.FUZZY_UNKNOWN.getFuzzyValue());
	}

	@Test
	public void testTranslateCharToFuzzyFlag() throws Exception {
		assertEquals(FuzzyFlag.FUZZY_CA, FuzzyFlag.translateCharToFuzzyFlag('c'));
		assertEquals(FuzzyFlag.FUZZY_UNKNOWN, FuzzyFlag.translateCharToFuzzyFlag('?'));

		assertNull(FuzzyFlag.translateCharToFuzzyFlag('a'));
	}

	@Test
	public void testAddFuzzyFlag() throws Exception {
		// add invalid value to null container
		assertNull(FuzzyFlag.addFuzzyFlag('a', null));

		// add valid value to null container
		Set<FuzzyFlag> testContainer = FuzzyFlag.addFuzzyFlag('c', null);
		assertNotNull(testContainer);
		assertTrue(testContainer.size() == 1);
		assertTrue(testContainer.contains(FuzzyFlag.FUZZY_CA));

		// add another value
		testContainer = FuzzyFlag.addFuzzyFlag('?', testContainer);
		assertNotNull(testContainer);
		assertTrue(testContainer.size() == 2);
		assertTrue(testContainer.contains(FuzzyFlag.FUZZY_CA));
		assertTrue(testContainer.contains(FuzzyFlag.FUZZY_UNKNOWN));

		// add invalid value
		testContainer = FuzzyFlag.addFuzzyFlag('a', testContainer);
		assertNotNull(testContainer);
		assertTrue(testContainer.size() == 2);
		assertTrue(testContainer.contains(FuzzyFlag.FUZZY_CA));
		assertTrue(testContainer.contains(FuzzyFlag.FUZZY_UNKNOWN));
	}

	@Test
	public void testDeleteFuzzyFlag() throws Exception {
		// remove invalid and valid value from null container
		assertNull(FuzzyFlag.deleteFuzzyFlag('a', null));
		assertNull(FuzzyFlag.deleteFuzzyFlag('c', null));

		Set<FuzzyFlag> testContainer = EnumSet.of(FuzzyFlag.FUZZY_CA, FuzzyFlag.FUZZY_UNKNOWN);

		// remove invalid from container
		testContainer = FuzzyFlag.deleteFuzzyFlag('a', testContainer);
		assertNotNull(testContainer);
		assertTrue(testContainer.size() == 2);
		assertTrue(testContainer.contains(FuzzyFlag.FUZZY_CA));
		assertTrue(testContainer.contains(FuzzyFlag.FUZZY_UNKNOWN));

		// remove valid from container
		testContainer = FuzzyFlag.deleteFuzzyFlag('c', testContainer);
		assertNotNull(testContainer);
		assertTrue(testContainer.size() == 1);
		assertTrue(testContainer.contains(FuzzyFlag.FUZZY_UNKNOWN));

		// again
		testContainer = FuzzyFlag.deleteFuzzyFlag('c', testContainer);
		assertNotNull(testContainer);
		assertTrue(testContainer.size() == 1);
		assertTrue(testContainer.contains(FuzzyFlag.FUZZY_UNKNOWN));

		// remove last element
		testContainer = FuzzyFlag.deleteFuzzyFlag('?', testContainer);
		assertNull(testContainer);
	}

	@Test
	public void testHasFuzzyFlag() throws Exception {
		// test null container
		assertFalse(FuzzyFlag.hasFuzzyFlag('a', null));
		assertFalse(FuzzyFlag.hasFuzzyFlag('c', null));

		EnumSet<FuzzyFlag> testContainer = EnumSet.of(FuzzyFlag.FUZZY_CA);

		assertFalse(FuzzyFlag.hasFuzzyFlag('a', testContainer));
		assertTrue(FuzzyFlag.hasFuzzyFlag('c', testContainer));
		assertFalse(FuzzyFlag.hasFuzzyFlag('?', testContainer));
	}

	@Test
	public void testGetFuzzyFlags() throws Exception {
		assertArrayEquals(new char[]{}, FuzzyFlag.getFuzzyFlags(null));
		assertArrayEquals(new char[]{}, FuzzyFlag.getFuzzyFlags(EnumSet.noneOf(FuzzyFlag.class)));
		assertArrayEquals(new char[]{'c'}, FuzzyFlag.getFuzzyFlags(EnumSet.of(FuzzyFlag.FUZZY_CA)));
		assertArrayEquals(new char[]{'c', '?'}, FuzzyFlag.getFuzzyFlags(EnumSet.of(FuzzyFlag.FUZZY_CA, FuzzyFlag.FUZZY_UNKNOWN))); //TODO: does this work on every machine?
	}
}
