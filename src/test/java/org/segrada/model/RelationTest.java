package org.segrada.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.segrada.model.prototype.INode;
import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.IRelationType;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class RelationTest {
	private static Validator validator;

	@BeforeAll
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
		assertTrue(constraintViolations.size() == 0, "Relation not valid");
	}

	@Test
	public void testRelationTypeEmpty() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "relationType", null);
		assertTrue(constraintViolations.size() == 1, "RelationType empty");
	}

	@Test
	public void testFromEntityEmpty() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "fromEntity", null);
		assertTrue(constraintViolations.size() == 1, "FromEntity empty");
	}

	@Test
	public void testToEntityEmpty() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "toEntity", null);
		assertTrue(constraintViolations.size() == 1, "ToEntity empty");
	}

	@Test
	public void testDescriptionEmpty() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "description", null);
		assertTrue(constraintViolations.size() == 1, "Description empty");
	}

	/*@Test can be "" for now
	public void testDescriptionTooShort() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "description", "");
		assertTrue("Description too short", constraintViolations.size() == 1);
	}*/

	@Test
	public void testDescriptionMarkupEmpty() throws Exception {
		Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "descriptionMarkup", null);
		assertTrue(constraintViolations.size() == 1, "Description markup empty");
	}

	@Test
	public void testGetReversedTitle() throws Exception {
		INode from = new Node();
		from.setTitle("from");

		INode to = new Node();
		to.setTitle("to");

		IRelationType relationType = new RelationType();
		relationType.setFromTitle("xfromx");
		relationType.setToTitle("xtox");

		IRelation relation = new Relation();
		relation.setFromEntity(from);
		relation.setToEntity(to);
		relation.setRelationType(relationType);

		assertEquals("to⇒xtox⇒from", relation.getReversedTitle());
	}
}
