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

public class RelationTypeTest {
	private static Validator validator;

	@BeforeAll
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void defaultValues() throws Exception {
		final RelationType relationType = new RelationType();

		assertEquals("", relationType.getFromTitle());
		assertEquals("", relationType.getToTitle());
		assertEquals("", relationType.getDescription());
		assertNotNull(relationType.getDescriptionMarkup());
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new RelationType());
	}

	@Test
	public void testValidRelationType() throws Exception {
		final RelationType relationType = new RelationType();
		relationType.setFromTitle("from");
		relationType.setToTitle("to");
		relationType.setDescription("Description");
		relationType.setDescriptionMarkup("default");
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validate(relationType);
		assertTrue(constraintViolations.size() == 0, "Relation Type not valid");
	}

	@Test
	public void testFromTitleEmpty() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "fromTitle", null);
		assertTrue(constraintViolations.size() == 1, "fromTitle empty");
	}

	@Test
	public void testFromTitleTooShort() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "fromTitle", "");
		assertTrue(constraintViolations.size() == 1, "fromTitle too short");
	}

	@Test
	public void testToTitleEmpty() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "toTitle", null);
		assertTrue(constraintViolations.size() == 1, "toTitle empty");
	}

	@Test
	public void testToTitleTooShort() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "toTitle", "");
		assertTrue(constraintViolations.size() == 1, "toTitle too short");
	}

	@Test
	public void testDescriptionEmpty() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "description", null);
		assertTrue(constraintViolations.size() == 1, "Description empty");
	}

	/*@Test
	public void testDescriptionTooShort() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "description", "");
		assertTrue("Description too short", constraintViolations.size() == 1);
	}*/

	@Test
	public void testDescriptionMarkupEmpty() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "descriptionMarkup", null);
		assertTrue(constraintViolations.size() == 1, "Description markup empty");
	}
}
