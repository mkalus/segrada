package org.segrada.util;

/**
 * Created by mkalus on 22.05.15.
 */
public interface PasswordEncoder {
	/**
	 * encode password
	 * @param password plaintext pasword
	 * @return encoded password
	 */
	String encode(CharSequence password);

	/**
	 * match passwords to check if one is valid
	 * @param charSequence password entered to be checked
	 * @param storedPassword stored password to check against
	 * @return true if passwords matched, false otherwise
	 */
	boolean matches(CharSequence charSequence, String storedPassword);
}
