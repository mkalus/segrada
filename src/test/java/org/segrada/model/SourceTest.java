package org.segrada.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class SourceTest {
	private static Validator validator;

	@BeforeAll
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void defaultValues() throws Exception {
		final Source source = new Source();

		assertEquals("", source.getShortTitle());
		assertEquals("", source.getLongTitle());
		assertEquals("", source.getShortRef());
		assertEquals("", source.getUrl());
		assertEquals("", source.getProductCode());
		assertEquals("", source.getAuthor());
		assertEquals("", source.getCitation());
		assertEquals("", source.getCopyright());
		assertEquals("", source.getDescription());
		assertNotNull(source.getDescriptionMarkup());
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new Source());
	}

	@Test
	public void testValidSource() throws Exception {
		final Source source = new Source();
		source.setShortTitle("Example title");
		source.setShortRef("ex");
		Set<ConstraintViolation<Source>> constraintViolations = validator.validate(source);
		assertTrue(constraintViolations.size() == 0, "Source not valid");
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<Source>> constraintViolations = validator.validateValue(Source.class, "shortTitle", null);
		assertTrue(constraintViolations.size() == 1, "Short Title empty");
	}

	@Test
	public void testTitleTooShort() throws Exception {
		Set<ConstraintViolation<Source>> constraintViolations = validator.validateValue(Source.class, "shortTitle", "");
		assertTrue(constraintViolations.size() == 1, "Short Title too short");
	}

	@Test
	public void testShortRefEmpty() throws Exception {
		Set<ConstraintViolation<Source>> constraintViolations = validator.validateValue(Source.class, "shortRef", null);
		assertTrue(constraintViolations.size() == 1, "Short Ref empty");
	}

	@Test
	public void testShortRefTooShort() throws Exception {
		Set<ConstraintViolation<Source>> constraintViolations = validator.validateValue(Source.class, "shortRef", "");
		assertTrue(constraintViolations.size() == 1, "Short Ref too short");
	}
}
