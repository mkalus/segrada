package org.segrada.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.*;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class UserGroupTest {
	private static Validator validator;

	@BeforeClass
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void defaultValues() throws Exception {
		final UserGroup userGroup = new UserGroup();

		assertEquals("", userGroup.getTitle());
		assertEquals("", userGroup.getDescription());
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new UserGroup());
	}

	@Test
	public void testValidUserGroup() throws Exception {
		final UserGroup userGroup = new UserGroup();
		userGroup.setTitle("test");
		userGroup.setDescription("description");
		userGroup.setSpecial("ADMIN");
		Set<ConstraintViolation<UserGroup>> constraintViolations = validator.validate(userGroup);
		assertTrue("UserGroup not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<UserGroup>> constraintViolations = validator.validateValue(UserGroup.class, "title", null);
		assertTrue("Title empty", constraintViolations.size() == 1);
	}

	@Test
	public void testTitleTooShort() throws Exception {
		Set<ConstraintViolation<UserGroup>> constraintViolations = validator.validateValue(UserGroup.class, "title", "");
		assertTrue("Title too short", constraintViolations.size() == 1);
	}


	@Test
	public void testSetRole() throws Exception {
		final UserGroup userGroup = new UserGroup();

		// should be 0 at the beginning
		assertEquals(0, userGroup.getRole("Test"));

		userGroup.setRole("Test", 1);
		assertEquals(1, userGroup.getRole("Test"));

		userGroup.setRole("Test2", -1);
		assertEquals(-1, userGroup.getRole("Test2"));

		userGroup.setRole("Test2", 0);
		assertEquals(0, userGroup.getRole("Test2"));
		assertEquals(1, userGroup.getRole("Test"));
	}

	@Test
	public void testSetRole1() throws Exception {
		final UserGroup userGroup = new UserGroup();

		// should be 0 at the beginning
		assertEquals(0, userGroup.getRole("Test"));

		userGroup.setRole("Test");
		assertTrue(userGroup.hasRole("Test"));
		assertEquals(1, userGroup.getRole("Test"));
	}

	@Test
	public void testUnsetRole() throws Exception {
		final UserGroup userGroup = new UserGroup();

		// should be 0 at the beginning
		assertEquals(0, userGroup.getRole("Test"));

		assertFalse(userGroup.hasRole("Test"));

		userGroup.unsetRole("Test");
		assertFalse(userGroup.hasRole("Test"));

		userGroup.setRole("Test");
		assertTrue(userGroup.hasRole("Test"));

		userGroup.unsetRole("Test");
		assertFalse(userGroup.hasRole("Test"));
	}

	@Test
	public void testHasRole() throws Exception {
		final UserGroup userGroup = new UserGroup();

		// should be 0 at the beginning
		assertEquals(0, userGroup.getRole("Test"));

		assertFalse(userGroup.hasRole("Test"));

		userGroup.setRole("Test");
		assertTrue(userGroup.hasRole("Test"));
	}

	@Test
	public void testGetRoles() throws Exception {
		final UserGroup userGroup = new UserGroup();

		// should be 0 at the beginning
		assertEquals(0, userGroup.getRoles().size());

		userGroup.setRole("Test", 1);

		assertEquals(1, userGroup.getRoles().size());

		userGroup.setRole("Test", -1);

		assertEquals(1, userGroup.getRoles().size());

		userGroup.setRole("Test", 0);

		// deleted
		assertEquals(0, userGroup.getRoles().size());

		userGroup.setRole("Test", 1);
		userGroup.setRole("Test2", -1);

		assertEquals(2, userGroup.getRoles().size());
		assertTrue(userGroup.getRoles().containsKey("Test"));
		assertTrue(userGroup.getRoles().containsKey("Test2"));

		assertEquals(new Integer(1), userGroup.getRoles().get("Test"));
		assertEquals(new Integer(-1), userGroup.getRoles().get("Test2"));
	}

	/*@Test
	public void testGetRole() throws Exception {
		// covered above
	}*/
}