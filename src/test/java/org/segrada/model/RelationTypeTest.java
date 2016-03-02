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

public class RelationTypeTest {
	private static Validator validator;

	@BeforeClass
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
		assertTrue("Relation Type not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testFromTitleEmpty() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "fromTitle", null);
		assertTrue("fromTitle empty", constraintViolations.size() == 1);
	}

	@Test
	public void testFromTitleTooShort() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "fromTitle", "");
		assertTrue("fromTitle too short", constraintViolations.size() == 1);
	}

	@Test
	public void testToTitleEmpty() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "toTitle", null);
		assertTrue("toTitle empty", constraintViolations.size() == 1);
	}

	@Test
	public void testToTitleTooShort() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "toTitle", "");
		assertTrue("toTitle too short", constraintViolations.size() == 1);
	}

	@Test
	public void testDescriptionEmpty() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "description", null);
		assertTrue("Description empty", constraintViolations.size() == 1);
	}

	/*@Test
	public void testDescriptionTooShort() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "description", "");
		assertTrue("Description too short", constraintViolations.size() == 1);
	}*/

	@Test
	public void testDescriptionMarkupEmpty() throws Exception {
		Set<ConstraintViolation<RelationType>> constraintViolations = validator.validateValue(RelationType.class, "descriptionMarkup", null);
		assertTrue("Description markup empty", constraintViolations.size() == 1);
	}
}