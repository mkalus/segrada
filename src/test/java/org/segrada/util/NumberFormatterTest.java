package org.segrada.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberFormatterTest {
	private NumberFormatter numberFormatter;

	@BeforeEach
	public void setUp() {
		numberFormatter = new NumberFormatter();
	}

	@Test
	public void testFileSizeSI() throws Exception {
		// to make separator reliable
		Locale.setDefault(Locale.US);

		assertEquals("0 B", numberFormatter.fileSizeSI(0));
		assertEquals("27 B", numberFormatter.fileSizeSI(27));
		assertEquals("999 B", numberFormatter.fileSizeSI(999));
		assertEquals("1.0 kB", numberFormatter.fileSizeSI(1000));
		assertEquals("1.0 kB", numberFormatter.fileSizeSI(1023));
		assertEquals("1.0 kB", numberFormatter.fileSizeSI(1024));
		assertEquals("1.7 kB", numberFormatter.fileSizeSI(1728));
		assertEquals("110.6 kB", numberFormatter.fileSizeSI(110592));
		assertEquals("7.1 MB", numberFormatter.fileSizeSI(7077888));
		assertEquals("453.0 MB", numberFormatter.fileSizeSI(452984832));
		assertEquals("29.0 GB", numberFormatter.fileSizeSI(28991029248L));
		assertEquals("1.9 TB", numberFormatter.fileSizeSI(1855425871872L));
		assertEquals("9.2 EB", numberFormatter.fileSizeSI(Long.MAX_VALUE));
	}

	@Test
	public void testFileSizeBinary() throws Exception {
		// to make separator reliable
		Locale.setDefault(Locale.US);

		assertEquals("0 B", numberFormatter.fileSizeBinary(0));
		assertEquals("27 B", numberFormatter.fileSizeBinary(27));
		assertEquals("999 B", numberFormatter.fileSizeBinary(999));
		assertEquals("1000 B", numberFormatter.fileSizeBinary(1000));
		assertEquals("1023 B", numberFormatter.fileSizeBinary(1023));
		assertEquals("1.0 KiB", numberFormatter.fileSizeBinary(1024));
		assertEquals("1.7 KiB", numberFormatter.fileSizeBinary(1728));
		assertEquals("108.0 KiB", numberFormatter.fileSizeBinary(110592));
		assertEquals("6.8 MiB", numberFormatter.fileSizeBinary(7077888));
		assertEquals("432.0 MiB", numberFormatter.fileSizeBinary(452984832));
		assertEquals("27.0 GiB", numberFormatter.fileSizeBinary(28991029248L));
		assertEquals("1.7 TiB", numberFormatter.fileSizeBinary(1855425871872L));
		assertEquals("8.0 EiB", numberFormatter.fileSizeBinary(Long.MAX_VALUE));
	}
}
