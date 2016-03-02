package org.segrada.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class SourceTest {
	private static Validator validator;

	@BeforeClass
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
	public void testValidColor() throws Exception {
		final Source source = new Source();
		source.setShortTitle("Example title");
		source.setShortRef("ex");
		Set<ConstraintViolation<Source>> constraintViolations = validator.validate(source);
		assertTrue("Source not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<Source>> constraintViolations = validator.validateValue(Source.class, "shortTitle", null);
		assertTrue("Short Title empty", constraintViolations.size() == 1);
	}

	@Test
	public void testTitleTooShort() throws Exception {
		Set<ConstraintViolation<Source>> constraintViolations = validator.validateValue(Source.class, "shortTitle", "");
		assertTrue("Short Title too short", constraintViolations.size() == 1);
	}

	@Test
	public void testShortRefEmpty() throws Exception {
		Set<ConstraintViolation<Source>> constraintViolations = validator.validateValue(Source.class, "shortRef", null);
		assertTrue("Short Ref empty", constraintViolations.size() == 1);
	}

	@Test
	public void testShortRefTooShort() throws Exception {
		Set<ConstraintViolation<Source>> constraintViolations = validator.validateValue(Source.class, "shortRef", "");
		assertTrue("Short Ref too short", constraintViolations.size() == 1);
	}
}