package org.segrada.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.*;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class RelationTest {
	private static Validator validator;

	@BeforeClass
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void defaultValues() throws Exception {
		final Relation relation = new Relation();

		assertEquals("", relation.getDescription());
		assertNotNull(relation.getDescriptionMarkup());
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new Relation());
	}

	@Test
	public void testValidRelation() throws Exception {
		final RelationType relationType = new RelationType();
		final Node fromNode = new Node();
		final Node toNode = new Node();

		final Relation relation = new Relation();
		relation.setRelationType(relationType);
		relation.setFromEntity(fromNode);
		relation.setToEntity(toNode);
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validate(relation);
		assertTrue("Relation not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testRelationTypeEmpty() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "relationType", null);
		assertTrue("RelationType empty", constraintViolations.size() == 1);
	}

	@Test
	public void testFromEntityEmpty() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "fromEntity", null);
		assertTrue("FromEntity empty", constraintViolations.size() == 1);
	}

	@Test
	public void testToEntityEmpty() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "toEntity", null);
		assertTrue("ToEntity empty", constraintViolations.size() == 1);
	}

	@Test
	public void testDescriptionEmpty() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "description", null);
		assertTrue("Description empty", constraintViolations.size() == 1);
	}

	/*@Test can be "" for now
	public void testDescriptionTooShort() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "description", "");
		assertTrue("Description too short", constraintViolations.size() == 1);
	}*/

	@Test
	public void testDescriptionMarkupEmpty() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "descriptionMarkup", null);
		assertTrue("Description markup empty", constraintViolations.size() == 1);
	}
}