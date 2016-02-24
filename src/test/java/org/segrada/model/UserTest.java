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

public class UserTest {
	private static Validator validator;

	@BeforeClass
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new User());
	}

	@Test
	public void testValidUser() throws Exception {
		final UserGroup userGroup = new UserGroup();

		final User user = new User();
		user.setLogin("login");
		user.setName("John Doe");
		user.setPassword("supersecretpassword");
		user.setGroup(userGroup);
		Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
		assertTrue("User not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testLoginEmpty() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "login", null);
		assertTrue("Login empty", constraintViolations.size() == 1);
	}

	@Test
	public void testLoginTooShort() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "login", "abc");
		assertTrue("Login too short", constraintViolations.size() == 1);
	}

	@Test
	public void testLoginTooLong() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "login", "THISISAVERYLONGLOGINTOOLONG");
		assertTrue("Login too long", constraintViolations.size() == 1);
	}

	/*@Test
	public void testLoginInvalidCharacters() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "login", "x\t\r\n");
		assertTrue("Login not valid", constraintViolations.size() == 1);
	}*/

	@Test
	public void testPasswordEmpty() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "password", null);
		assertTrue("Password empty", constraintViolations.size() == 1);
	}

	@Test
	public void testPasswordTooShort() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "password", "abcd");
		assertTrue("Password too short", constraintViolations.size() == 1);
	}

	@Test
	public void testPasswordTooLong() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "password", "THISISAVERYLONGPASSWORDJUSTTOOLONGTHISISAVERYLONGPASSWORDJUSTTOOLONGTHISISAVERYLONGPASSWORDJUSTTOOLONGTHISISAVERYLONGPASSWORDJUSTTOOLONGTHISISAVERYLONGPASSWORDJUSTTOOLONGTHISISAVERYLONGPASSWORDJUSTTOOLONGTHISISAVERYLONGPASSWORDJUSTTOOLONGTHISISAVERYLONGPASSWORDJUSTTOOLONGTHISISAVERYLONGPASSWORDJUSTTOOLONG");
		assertTrue("Password too long", constraintViolations.size() == 1);
	}

	@Test
	public void testNameEmpty() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "name", null);
		assertTrue("Name empty", constraintViolations.size() == 1);
	}

	@Test
	public void testNameTooShort() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "name", "a");
		assertTrue("Name too short", constraintViolations.size() == 1);
	}

	@Test
	public void testNameTooLong() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "name", "This name is too long. This name is too long. This name is too long. This name is too long.");
		assertTrue("Name too long", constraintViolations.size() == 1);
	}

	@Test
	public void testEmptyGroup() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "group", null);
		assertTrue("Group empty", constraintViolations.size() == 1);
	}

	@Test
	public void testGetTitle() throws Exception {
		final User user = new User();
		user.setLogin("login");
		user.setName("John Doe");
		user.setPassword("supersecretpassword");

		assertEquals("John Doe", user.getTitle());
	}

	@Test
	public void testPasswordsMatch() throws Exception {
		final User user = new User();

		// nothing set
		assertTrue(user.passwordsMatch());

		user.setPassword("pass1");
		assertFalse(user.passwordsMatch());

		user.setConfirmPassword("pass1");
		assertTrue(user.passwordsMatch());

		user.setPassword(null);
		assertFalse(user.passwordsMatch());
	}
}