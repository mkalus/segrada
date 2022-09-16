package org.segrada.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class PictogramTest {
	private static byte[] pictureData={0xa,0x2,0xf};

	private static Validator validator;

	@BeforeAll
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void defaultValues() throws Exception {
		final Pictogram pictogram = new Pictogram();

		assertEquals("", pictogram.getTitle());
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new Pictogram());
	}

	@Test
	public void testValidPictogram() throws Exception {
		final Pictogram pictogram = new Pictogram();
		pictogram.setTitle("Example Titel");
		pictogram.setData(pictureData);
		Set<ConstraintViolation<Pictogram>> constraintViolations = validator.validate(pictogram);
		assertTrue(constraintViolations.size() == 0, "Pictogram not valid");
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<Pictogram>> constraintViolations = validator.validateValue(Pictogram.class, "title", null);
		assertTrue(constraintViolations.size() == 1, "Title empty");
	}

	@Test
	public void testTitleTooShort() throws Exception {
		Set<ConstraintViolation<Pictogram>> constraintViolations = validator.validateValue(Pictogram.class, "title", "T");
		assertTrue(constraintViolations.size() == 1, "Title too short");
	}
}
