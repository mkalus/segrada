package org.segrada.util;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PBKDF2WithHmacSHA1PasswordEncoderTest {
	/**
	 * sample password
	 */
	private static final String password = "Password-Test";

	/**
	 * encoder instance
	 */
	private static PasswordEncoder encoder;

	@BeforeClass
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
		assertTrue("Password is not composed of three parts", parts.length == 3);
		assertTrue("First part of password smaller than 5 letters", parts[0].length() >= 5); // should be at least 10000 today
		assertTrue("First part of password not a number", parts[0].matches("^[1-9][0-9]*$"));
		assertTrue("Second part of password smaller than 16 bytes", parts[1].length() == 32);
		assertTrue("Second part of password not a hex number", parts[1].matches("^[0-9a-f]+$"));
		assertTrue("Third part of password smaller than 64 bytes", parts[2].length() == 128);
		assertTrue("Third part of password not a hex number", parts[2].matches("^[0-9a-f]+$"));
	}

	@Test
	public void testMatches() throws Exception {
		// different sample passwords
		assertTrue("Password 1000 did not match", encoder.matches(password, "1000:ecebaf7f4ca80b35ad01d9cc9a23d712:e28d2699de28279030c0c2ddb90bf7e32428ccf455123a823ee9e04187ca73e427b12241315048599a56a2d471b85768ecc5250a29b55fb1831b413730d383cb"));
		assertTrue("Password 10000 did not match", encoder.matches(password, "10000:4a708704ba083f99033cf726f63b88c7:7605720d577d1567659594673596bdb70dadd8286662c4a1bf77e56f304021cb7d91459366ff053d8007f3cfc8a1fdf78d2f633ab14d9f8d9a4ed35546eefa99"));
		assertTrue("Password 100000 did not match", encoder.matches(password, "100000:de7b326aa219cf55a6868d9ad87df65c:dc2f56de369744eb67ecc33ebb0541223af8d8dfd900e3d3d2150b44213bfe05f704e79c3e314a0e4445af9cd80ae8f09fc80f81e684f5ff99deaf9b72e33bbb"));
	}
}