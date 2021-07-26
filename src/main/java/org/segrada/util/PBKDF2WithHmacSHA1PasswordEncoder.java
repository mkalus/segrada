package org.segrada.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Password service implementing PBKDF2 - inspired by
 * http://howtodoinjava.com/2013/07/22/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
 */
public class PBKDF2WithHmacSHA1PasswordEncoder implements PasswordEncoder {
	/**
	 * number of iterations
	 */
	private static final int ITERATIONS = 10000;

	/**
	 * create a salt string
	 *
	 * @return 16 byte salt
	 * @throws NoSuchAlgorithmException
	 */
	private static byte[] getSalt() throws NoSuchAlgorithmException {
		SecureRandom sr = new SecureRandom();
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}

	@Override
	public String encode(CharSequence password) {
		try {
			char[] chars = password.toString().toCharArray();
			byte[] salt = getSalt();

			PBEKeySpec spec = new PBEKeySpec(chars, salt, ITERATIONS, 64 * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = skf.generateSecret(spec).getEncoded();
			return ITERATIONS + ":" + toHex(salt) + ":" + toHex(hash);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean matches(CharSequence charSequence, String storedPassword) {
		try {
			String[] parts = storedPassword.split(":");
			int iterations = Integer.parseInt(parts[0]);
			byte[] salt = fromHex(parts[1]);
			byte[] hash = fromHex(parts[2]);

			PBEKeySpec spec = new PBEKeySpec(charSequence.toString().toCharArray(), salt, iterations, hash.length * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] testHash = skf.generateSecret(spec).getEncoded();

			int diff = hash.length ^ testHash.length;
			for (int i = 0; i < hash.length && i < testHash.length; i++) {
				diff |= hash[i] ^ testHash[i];
			}
			return diff == 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Convert bytes to hex
	 *
	 * @param array byte array
	 * @return hex representation
	 * @throws NoSuchAlgorithmException
	 */
	private static String toHex(byte[] array) throws NoSuchAlgorithmException {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		} else {
			return hex;
		}
	}

	/**
	 * Convert hex to byte array
	 *
	 * @param hex number
	 * @return byte array
	 * @throws NoSuchAlgorithmException
	 */
	private static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}
}
