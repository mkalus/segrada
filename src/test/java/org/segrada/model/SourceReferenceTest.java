package org.segrada.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class SourceReferenceTest {
	private static Validator validator;

	@BeforeClass
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void defaultValues() throws Exception {
		final SourceReference sourceReference = new SourceReference();

		assertEquals("", sourceReference.getReferenceText());
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new SourceReference());
	}

	@Test
	public void testValidSourceReference() throws Exception {
		final SourceReference sourceReference = new SourceReference();
		sourceReference.setSource(new Source());
		sourceReference.setReference(new Node());
		Set<ConstraintViolation<SourceReference>> constraintViolations = validator.validate(sourceReference);
		assertTrue("Source reference not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testSourceEmpty() throws Exception {
		Set<ConstraintViolation<SourceReference>> constraintViolations = validator.validateValue(SourceReference.class, "source", null);
		assertTrue("Source empty", constraintViolations.size() == 1);
	}

	@Test
	public void testReferenceEmpty() throws Exception {
		Set<ConstraintViolation<SourceReference>> constraintViolations = validator.validateValue(SourceReference.class, "reference", null);
		assertTrue("Reference empty", constraintViolations.size() == 1);
	}
}