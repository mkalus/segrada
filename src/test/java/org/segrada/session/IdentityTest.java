package org.segrada.session;

import org.junit.Test;
import org.segrada.model.User;
import org.segrada.model.prototype.IUser;

import static org.junit.Assert.*;

public class IdentityTest {

	@Test
	public void testIsAuthenticated() throws Exception {
		Identity identity = new Identity();
		IUser user = new User();
		user.setId("#99:99");

		assertFalse(identity.isAuthenticated());

		identity.setUser(user);

		assertTrue(identity.isAuthenticated());
	}

	@Test
	public void testLogout() throws Exception {
		Identity identity = new Identity();
		IUser user = new User();
		user.setId("#99:99");

		assertNull(identity.getId());
		assertFalse(identity.isAuthenticated());

		identity.setUser(user);

		assertNotNull(identity.getId());
		assertTrue(identity.isAuthenticated());

		identity.logout();

		assertNull(identity.getId());
		assertFalse(identity.isAuthenticated());
	}

	@Test
	public void testGetId() throws Exception {
		Identity identity = new Identity();
		IUser user = new User();
		user.setId("#99:99");
		user.setName("Testini");
		user.setRole("TEST");

		assertNull(identity.getId());

		identity.setUser(user);

		assertNotNull(identity.getId());

		// logout
		identity.logout();

		// should return null again
		assertNull(identity.getId());
	}

	@Test
	public void testGetName() throws Exception {
		Identity identity = new Identity();
		IUser user = new User();
		user.setId("#99:99");
		user.setName("Testini");
		user.setRole("TEST");

		assertNull(identity.getName());

		identity.setUser(user);

		assertNotNull(identity.getName());

		// logout
		identity.logout();

		// should return null again
		assertNull(identity.getName());
	}

	@Test
	public void testGetRole() throws Exception {
		Identity identity = new Identity();
		IUser user = new User();
		user.setId("#99:99");
		user.setName("Testini");
		user.setRole("TEST");

		assertNull(identity.getRole());

		identity.setUser(user);

		assertNotNull(identity.getRole());

		// logout
		identity.logout();

		// should return null again
		assertNull(identity.getRole());
	}
}