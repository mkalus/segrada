package org.segrada.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertTrue;
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
		final User user = new User();
		user.setLogin("login");
		user.setName("John Doe");
		user.setPassword("supersecretpassword");
		user.setRole("USER");
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

	@Test
	public void testLoginInvalidCharacters() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "login", "x\t\r\n");
		assertTrue("Login not valid", constraintViolations.size() == 1);
	}

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
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "password", "THISISAVERYLONGPASSWORDJUSTTOOLONG");
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
	public void testEmptyRole() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "role", null);
		assertTrue("Role empty", constraintViolations.size() == 1);
	}

	@Test
	public void testWrongRole() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = validator.validateValue(User.class, "role", "COOLUSER");
		assertTrue("Role not USER or ADMIN", constraintViolations.size() == 1);
	}
}