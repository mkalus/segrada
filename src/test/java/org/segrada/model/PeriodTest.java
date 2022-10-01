package org.segrada.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class PeriodTest {
	private static Validator validator;

	@BeforeAll
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new Period());
	}

	@Test
	public void testValidPeriod() throws Exception {
		final Period period = new Period();
		period.setParentId("#99:99");
		period.setParentModel("Mock");
		period.setFromEntry("21.05.2000");
		period.setToEntry("22.05.2009");
		Set<ConstraintViolation<Period>> constraintViolations = validator.validate(period);
		assertTrue(constraintViolations.size() == 0, "Period not valid");

		assertEquals("period", period.getType());
		assertEquals(new Long(2451686L), period.getFromJD());
		assertEquals(new Long(2454974L), period.getToJD());
	}

	@Test
	public void testFromAfterTo() throws Exception {
		// from is later than to
		final Period period = new Period();
		period.setParentId("#99:99");
		period.setParentModel("Mock");
		period.setFromEntry("21.05.2009");
		period.setToEntry("22.05.2000");
		Set<ConstraintViolation<Period>> constraintViolations = validator.validate(period);
		assertTrue(constraintViolations.size() > 0, "Invalid Period is valid");
	}

	@Test
	public void testNothingSet() throws Exception {
		// from is later than to
		final Period period = new Period();
		period.setParentId("#99:99");
		period.setParentModel("Mock");
		period.setFromEntry("");
		period.setToEntry("");
		Set<ConstraintViolation<Period>> constraintViolations = validator.validate(period);
		assertTrue(constraintViolations.size() > 0, "Invalid Period is valid");

		period.setFromEntry(null);
		period.setToEntry(null);
		constraintViolations = validator.validate(period);
		assertTrue(constraintViolations.size() > 0, "Invalid Period is valid");
	}

	@Test
	public void testSettingValues() throws Exception {
		final Period period = new Period();
		period.setFromEntry("21.05.2000");
		period.setToEntry("22.05.2009");

		assertTrue(period.getFromJD() != null);
		assertTrue(period.getFromJD() > 0);

		assertTrue(period.getToJD() != null);
		assertTrue(period.getToJD() < Long.MAX_VALUE);

		// reset values later on
		period.setFromEntry(null);
		period.setToEntry(null);

		assertTrue(period.getFromJD() != null);
		assertTrue(period.getFromJD() == Long.MIN_VALUE);

		assertTrue(period.getToJD() != null);
		assertTrue(period.getToJD() == Long.MAX_VALUE);
	}

	@Test
	public void testType() throws Exception {
		Period period = new Period();
		period.setFromEntry("21.05.2000");
		period.setToEntry("22.05.2009");
		assertEquals("period", period.getType());

		period = new Period();
		period.setFromEntry("21.05.2000");
		period.setToEntry("21.05.2000");
		assertEquals("moment", period.getType());

		period = new Period();
		period.setFromEntry("05.2000");
		period.setToEntry("5.2000");
		assertEquals("period", period.getType());

		period = new Period();
		period.setFromEntry("2000");
		period.setToEntry("2000");
		assertEquals("period", period.getType());

		period = new Period();
		period.setFromEntry(null);
		period.setToEntry("2000");
		assertEquals("period", period.getType());

		period = new Period();
		period.setFromEntry("2099");
		period.setToEntry(null);
		assertEquals("period", period.getType());

		period = new Period();
		period.setFromEntry(null);
		period.setToEntry(null);
		assertEquals("period", period.getType());
	}

	@Test
	public void testParentEmpty() throws Exception {
		Set<ConstraintViolation<Period>> constraintViolations = validator.validateValue(Period.class, "parentId", null);
		assertTrue(constraintViolations.size() == 1, "Parent Id empty");

		constraintViolations = validator.validateValue(Period.class, "parentModel", null);
		assertTrue(constraintViolations.size() == 1, "Parent model empty");
	}

	@Test
	public void testFromEmpty() throws Exception {
		Set<ConstraintViolation<Period>> constraintViolations = validator.validateValue(Period.class, "fromJD", null);
		assertTrue(constraintViolations.size() == 1, "from empty");
	}

	@Test
	public void testToEmpty() throws Exception {
		Set<ConstraintViolation<Period>> constraintViolations = validator.validateValue(Period.class, "toJD", null);
		assertTrue(constraintViolations.size() == 1, "to empty");
	}

	@Test
	public void testFromEntryCalendarEmpty() throws Exception {
		Set<ConstraintViolation<Period>> constraintViolations = validator.validateValue(Period.class, "fromEntryCalendar", null);
		assertTrue(constraintViolations.size() == 1, "fromEntryCalendar empty");
	}

	@Test
	public void testToEntryCalendarEmpty() throws Exception {
		Set<ConstraintViolation<Period>> constraintViolations = validator.validateValue(Period.class, "toEntryCalendar", null);
		assertTrue(constraintViolations.size() == 1, "toEntryCalendar empty");
	}

	@Test
	public void testFromEntryCalendarValidSettings() throws Exception {
		Set<ConstraintViolation<Period>> constraintViolations = validator.validateValue(Period.class, "fromEntryCalendar", "G");
		assertTrue(constraintViolations.size() == 0, "fromEntryCalendar settings");

		constraintViolations = validator.validateValue(Period.class, "fromEntryCalendar", "J");
		assertTrue(constraintViolations.size() == 0, "fromEntryCalendar settings");

		constraintViolations = validator.validateValue(Period.class, "fromEntryCalendar", "");
		assertTrue(constraintViolations.size() == 1, "fromEntryCalendar settings");
	}

	@Test
	public void testToEntryCalendarValidSettings() throws Exception {
		Set<ConstraintViolation<Period>> constraintViolations = validator.validateValue(Period.class, "toEntryCalendar", "G");
		assertTrue(constraintViolations.size() == 0, "toEntryCalendar settings");

		constraintViolations = validator.validateValue(Period.class, "toEntryCalendar", "J");
		assertTrue(constraintViolations.size() == 0, "toEntryCalendar settings");

		constraintViolations = validator.validateValue(Period.class, "toEntryCalendar", "");
		assertTrue(constraintViolations.size() == 1, "toEntryCalendar settings");
	}

	@Test
	public void testAddFuzzyFromFlag() throws Exception {
		Period period = new Period();

		assertFalse(period.hasFuzzyFromFlag('c'));
		assertFalse(period.hasFuzzyFromFlag('x'));

		period.addFuzzyFromFlag('x');
		period.addFuzzyFromFlag('c');

		assertTrue(period.hasFuzzyFromFlag('c'));
		assertFalse(period.hasFuzzyFromFlag('x'));
	}

	@Test
	public void testDeleteFuzzyFromFlag() throws Exception {
		Period period = new Period();

		assertFalse(period.hasFuzzyFromFlag('c'));
		period.addFuzzyFromFlag('c');

		assertTrue(period.hasFuzzyFromFlag('c'));

		period.deleteFuzzyFromFlag('c');
		assertFalse(period.hasFuzzyFromFlag('c'));
	}

	/*Essentially tested in other methods @Test
	public void testHasFuzzyFromFlag() throws Exception {
	}*/

	@Test
	public void testGetFuzzyFromFlag() throws Exception {
		Period period = new Period();

		assertArrayEquals(new char[]{}, period.getFuzzyFromFlags());
		period.addFuzzyFromFlag('c');

		assertArrayEquals(new char[]{'c'}, period.getFuzzyFromFlags());
	}

	@Test
	public void testAddFuzzyToFlag() throws Exception {
		Period period = new Period();

		assertFalse(period.hasFuzzyToFlag('c'));
		assertFalse(period.hasFuzzyToFlag('x'));
		assertFalse(period.hasFuzzyToFlag('+'));
		assertFalse(period.hasFuzzyToFlag('-'));
		assertFalse(period.hasFuzzyToFlag('?'));

		period.addFuzzyToFlag('x');
		period.addFuzzyToFlag('c');
		period.addFuzzyToFlag('?');
		period.addFuzzyToFlag('+');
		period.addFuzzyToFlag('-');

		assertTrue(period.hasFuzzyToFlag('c'));
		assertTrue(period.hasFuzzyToFlag('?'));
		assertFalse(period.hasFuzzyToFlag('x'));
		assertTrue(period.hasFuzzyToFlag('+'));
		assertTrue(period.hasFuzzyToFlag('-'));
	}

	@Test
	public void testDeleteFuzzyToFlag() throws Exception {
		Period period = new Period();

		assertFalse(period.hasFuzzyToFlag('c'));
		period.addFuzzyToFlag('c');

		assertTrue(period.hasFuzzyToFlag('c'));

		period.deleteFuzzyToFlag('c');
		assertFalse(period.hasFuzzyToFlag('c'));
	}

	/*Essentially tested in other methods @Test
	public void testHasFuzzyToFlag() throws Exception {
	}*/

	@Test
	public void testGetFuzzyToFlag() throws Exception {
		Period period = new Period();

		assertArrayEquals(new char[]{}, period.getFuzzyToFlags());
		period.addFuzzyToFlag('c');

		assertArrayEquals(new char[]{'c'}, period.getFuzzyToFlags());
	}
}
