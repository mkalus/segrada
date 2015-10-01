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

public class LocationTest {
	private static Validator validator;

	@BeforeClass
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new Location());
	}

	@Test
	public void testValidLocation() throws Exception {
		final Location location = new Location();
		location.setParentId("#99:99");
		location.setParentModel("Mock");
		location.setLatitude(23.0);
		location.setLongitude(21.0);
		location.setComment(null);
		Set<ConstraintViolation<Location>> constraintViolations = validator.validate(location);
		assertTrue("Location not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testParentEmpty() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "parentId", null);
		assertTrue("Parent Id empty", constraintViolations.size() == 1);

		constraintViolations = validator.validateValue(Location.class, "parentModel", null);
		assertTrue("Parent model empty", constraintViolations.size() == 1);
	}

	@Test
	public void testLatitudeEmpty() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "latitude", null);
		assertTrue("Latitude empty", constraintViolations.size() == 1);
	}

	@Test
	public void testLatitudeTooLow() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "latitude", -500);
		assertTrue("Latitude too low", constraintViolations.size() == 1);
	}

	@Test
	public void testLatitudeTooHigh() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "latitude", 500);
		assertTrue("Latitude too high", constraintViolations.size() == 1);
	}

	@Test
	public void testLongitudeEmpty() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "longitude", null);
		assertTrue("Longitude empty", constraintViolations.size() == 1);
	}

	@Test
	public void testLongitudeTooLow() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "longitude", -500);
		assertTrue("Longitude too low", constraintViolations.size() == 1);
	}

	@Test
	public void testLongitudeTooHigh() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "longitude", 500);
		assertTrue("Longitude too high", constraintViolations.size() == 1);
	}
}