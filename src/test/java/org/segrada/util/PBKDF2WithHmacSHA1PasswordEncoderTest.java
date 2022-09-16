package org.segrada.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PBKDF2WithHmacSHA1PasswordEncoderTest {
	/**
	 * sample password
	 */
	private static final String password = "Password-Test";

	/**
	 * encoder instance
	 */
	private static PasswordEncoder encoder;

	@BeforeAll
	public static void setup() {
		encoder = new PBKDF2WithHmacSHA1PasswordEncoder();
	}

    /*@Test
    public void testTimeTaken() throws Exception {
        long start = System.currentTimeMillis();
        encoder.encode("Password-Test");
        long end = System.currentTimeMillis();

        // should take at least 50 ms on a fast machine
        assertTrue("Time taken was only " + (end - start) + "ms", end - start >= 50);
    }*/

	@Test
	public void testEncode() throws Exception {
		String encoded = encoder.encode(password);
		String parts[] = encoded.split(":");

		// make sure that password follows certain patterns
		assertTrue(parts.length == 3, "Password is not composed of three parts");
		assertTrue(parts[0].length() >= 5, "First part of password smaller than 5 letters"); // should be at least 10000 today
		assertTrue(parts[0].matches("^[1-9][0-9]*$"), "First part of password not a number");
		assertTrue(parts[1].length() == 32, "Second part of password smaller than 16 bytes");
		assertTrue(parts[1].matches("^[0-9a-f]+$"), "Second part of password not a hex number");
		assertTrue(parts[2].length() == 128, "Third part of password smaller than 64 bytes");
		assertTrue(parts[2].matches("^[0-9a-f]+$"), "Third part of password not a hex number");
	}

	@Test
	public void testMatches() throws Exception {
		// different sample passwords
		assertTrue(encoder.matches(password, "1000:ecebaf7f4ca80b35ad01d9cc9a23d712:e28d2699de28279030c0c2ddb90bf7e32428ccf455123a823ee9e04187ca73e427b12241315048599a56a2d471b85768ecc5250a29b55fb1831b413730d383cb"), "Password 1000 did not match");
		assertTrue(encoder.matches(password, "10000:4a708704ba083f99033cf726f63b88c7:7605720d577d1567659594673596bdb70dadd8286662c4a1bf77e56f304021cb7d91459366ff053d8007f3cfc8a1fdf78d2f633ab14d9f8d9a4ed35546eefa99"), "Password 10000 did not match");
		assertTrue(encoder.matches(password, "100000:de7b326aa219cf55a6868d9ad87df65c:dc2f56de369744eb67ecc33ebb0541223af8d8dfd900e3d3d2150b44213bfe05f704e79c3e314a0e4445af9cd80ae8f09fc80f81e684f5ff99deaf9b72e33bbb"), "Password 100000 did not match");
	}
}
