package org.segrada.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class LocationTest {
	private static Validator validator;

	@BeforeAll
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
		assertTrue(constraintViolations.size() == 0, "Location not valid");
	}

	@Test
	public void testParentEmpty() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "parentId", null);
		assertTrue(constraintViolations.size() == 1, "Parent Id empty");

		constraintViolations = validator.validateValue(Location.class, "parentModel", null);
		assertTrue(constraintViolations.size() == 1, "Parent model empty");
	}

	@Test
	public void testLatitudeEmpty() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "latitude", null);
		assertTrue(constraintViolations.size() == 1, "Latitude empty");
	}

	@Test
	public void testLatitudeTooLow() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "latitude", -500);
		assertTrue(constraintViolations.size() == 1, "Latitude too low");
	}

	@Test
	public void testLatitudeTooHigh() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "latitude", 500);
		assertTrue(constraintViolations.size() == 1, "Latitude too high");
	}

	@Test
	public void testLongitudeEmpty() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "longitude", null);
		assertTrue(constraintViolations.size() == 1, "Longitude empty");
	}

	@Test
	public void testLongitudeTooLow() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "longitude", -500);
		assertTrue(constraintViolations.size() == 1, "Longitude too low");
	}

	@Test
	public void testLongitudeTooHigh() throws Exception {
		Set<ConstraintViolation<Location>> constraintViolations = validator.validateValue(Location.class, "longitude", 500);
		assertTrue(constraintViolations.size() == 1, "Longitude too high");
	}
}
