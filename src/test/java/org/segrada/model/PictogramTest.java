package org.segrada.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class PictogramTest {
	private static byte[] pictureData={0xa,0x2,0xf};

	private static Validator validator;

	@BeforeClass
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testValidPictogram() throws Exception {
		final Pictogram pictogram = new Pictogram();
		pictogram.setTitle("Example Titel");
		pictogram.setData(pictureData);
		Set<ConstraintViolation<Pictogram>> constraintViolations = validator.validate(pictogram);
		assertTrue("Pictogram not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<Pictogram>> constraintViolations = validator.validateValue(Pictogram.class, "title", null);
		assertTrue("Title empty", constraintViolations.size() == 1);
	}

	@Test
	public void testTitleTooShort() throws Exception {
		Set<ConstraintViolation<Pictogram>> constraintViolations = validator.validateValue(Pictogram.class, "title", "T");
		assertTrue("Title too short", constraintViolations.size() == 1);
	}

	@Test
	public void testDataEmpty() throws Exception {
		Set<ConstraintViolation<Pictogram>> constraintViolations = validator.validateValue(Pictogram.class, "data", null);
		assertTrue("Title empty", constraintViolations.size() == 1);
	}
}