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

public class NodeTest {
	private static Validator validator;

	@BeforeAll
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void defaultValues() throws Exception {
		final Node node = new Node();

		assertEquals("", node.getTitle());
		assertEquals("", node.getAlternativeTitles());
		assertEquals("", node.getDescription());
		assertNotNull(node.getDescriptionMarkup());
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new Node());
	}

	@Test
	public void testValidNode() throws Exception {
		final Node node = new Node();
		node.setTitle("Example title");
		node.setAlternativeTitles("");
		node.setDescription("Description");
		Set<ConstraintViolation<Node>> constraintViolations = validator.validate(node);
		assertTrue(constraintViolations.size() == 0, "Node not valid");
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "title", null);
		assertTrue(constraintViolations.size() == 1, "Title empty");
	}

	@Test
	public void testTitleTooShort() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "title", "");
		assertTrue(constraintViolations.size() == 1, "Title too short");
	}

	@Test
	public void testAlternativeTitlesEmpty() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "alternativeTitles", null);
		assertTrue(constraintViolations.size() == 1, "Alternative titles empty");
	}

	@Test
	public void testDescriptionEmpty() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "description", null);
		assertTrue(constraintViolations.size() == 1, "Description empty");
	}

	/*@Test can be "" for now
	public void testDescriptionTooShort() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "description", "");
		assertTrue("Description too short", constraintViolations.size() == 1);
	}*/

	@Test
	public void testDescriptionMarkupEmpty() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "descriptionMarkup", null);
		assertTrue(constraintViolations.size() == 1, "Description markup empty");
	}
}
