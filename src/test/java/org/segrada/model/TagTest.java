package org.segrada.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class TagTest {
	private static Validator validator;

	@BeforeAll
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void defaultValues() throws Exception {
		final Tag tag = new Tag();

		assertNull(tag.getTitle());
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new Tag());
	}

	@Test
	public void testValidTag() throws Exception {
		final Tag tag = new Tag();
		tag.setTitle("Example title");
		Set<ConstraintViolation<Tag>> constraintViolations = validator.validate(tag);
		assertTrue(constraintViolations.size() == 0, "Tag not valid");
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<Tag>> constraintViolations = validator.validateValue(Tag.class, "title", null);
		assertTrue(constraintViolations.size() == 1, "Title empty");
	}

	@Test
	public void testTitleTooShort() throws Exception {
		Set<ConstraintViolation<Tag>> constraintViolations = validator.validateValue(Tag.class, "title", "");
		assertTrue(constraintViolations.size() == 1, "Title too short");
	}

}
