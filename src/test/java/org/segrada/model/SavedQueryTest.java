package org.segrada.model;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class SavedQueryTest {
	private static Validator validator;

	@BeforeAll
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new SavedQuery());
	}

	@Test
	public void testValidNode() throws Exception {
		final SavedQuery savedQuery = new SavedQuery();
		savedQuery.setType("TestType");
		savedQuery.setTitle("Example title");
		savedQuery.setDescription("Description");
		savedQuery.setData("Data");
		Set<ConstraintViolation<SavedQuery>> constraintViolations = validator.validate(savedQuery);
		assertTrue(constraintViolations.size() == 0, "Node not valid");
	}

	@Test
	public void testTypeEmpty() throws Exception {
		Set<ConstraintViolation<SavedQuery>> constraintViolations = validator.validateValue(SavedQuery.class, "type", null);
		assertTrue(constraintViolations.size() == 1, "Type empty");
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<SavedQuery>> constraintViolations = validator.validateValue(SavedQuery.class, "title", null);
		assertTrue(constraintViolations.size() == 1, "Title empty");
	}

	@Test
	public void testDescriptionEmpty() throws Exception {
		Set<ConstraintViolation<SavedQuery>> constraintViolations = validator.validateValue(SavedQuery.class, "description", null);
		assertTrue(constraintViolations.size() == 1, "Description empty");
	}

	@Test
	public void testDataEmpty() throws Exception {
		Set<ConstraintViolation<SavedQuery>> constraintViolations = validator.validateValue(SavedQuery.class, "data", null);
		assertTrue(constraintViolations.size() == 1, "Data empty");
	}

	@Test
	public void getJSONData() throws Exception {
		final SavedQuery savedQuery = new SavedQuery();

		// null should not fail
		savedQuery.setData(null);
		JSONObject o = savedQuery.getJSONData();
		assertNotNull(o);
		assertEquals(0, o.length());

		// empty string should not fail
		savedQuery.setData("");
		o = savedQuery.getJSONData();
		assertNotNull(o);
		assertEquals(0, o.length());

		// invalid JSON should not fail
		savedQuery.setData("[hello");
		o = savedQuery.getJSONData();
		assertNotNull(o);
		assertEquals(0, o.length());

		// valid json test
		savedQuery.setData("{\"test\": \"test\"}");
		o = savedQuery.getJSONData();
		assertNotNull(o);
		assertEquals(1, o.length());
		assertEquals("test", o.get("test"));
	}
}
