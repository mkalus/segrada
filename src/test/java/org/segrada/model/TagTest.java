package org.segrada.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class TagTest {
	private static Validator validator;

	@BeforeClass
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testValidTag() throws Exception {
		final Tag tag = new Tag();
		tag.setTitle("Example title");
		Set<ConstraintViolation<Tag>> constraintViolations = validator.validate(tag);
		assertTrue("Tag not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<Tag>> constraintViolations = validator.validateValue(Tag.class, "title", null);
		assertTrue("Title empty", constraintViolations.size() == 1);
	}

	@Test
	public void testTitleTooShort() throws Exception {
		Set<ConstraintViolation<Tag>> constraintViolations = validator.validateValue(Tag.class, "title", "");
		assertTrue("Title too short", constraintViolations.size() == 1);
	}

}