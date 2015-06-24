package org.segrada.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class NodeTest {
	private static Validator validator;

	@BeforeClass
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testValidNode() throws Exception {
		final Node node = new Node();
		node.setTitle("Example title");
		node.setAlternativeTitles("");
		node.setDescription("Description");
		Set<ConstraintViolation<Node>> constraintViolations = validator.validate(node);
		assertTrue("Node not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "title", null);
		assertTrue("Title empty", constraintViolations.size() == 1);
	}

	@Test
	public void testTitleTooShort() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "title", "");
		assertTrue("Title too short", constraintViolations.size() == 1);
	}

	@Test
	public void testAlternativeTitlesEmpty() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "alternativeTitles", null);
		assertTrue("Alternative titles empty", constraintViolations.size() == 1);
	}

	@Test
	public void testDescriptionEmpty() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "description", null);
		assertTrue("Description empty", constraintViolations.size() == 1);
	}

	/*@Test can be "" for now
	public void testDescriptionTooShort() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "description", "");
		assertTrue("Description too short", constraintViolations.size() == 1);
	}*/

	@Test
	public void testDescriptionMarkupEmpty() throws Exception {
		Set<ConstraintViolation<Node>> constraintViolations = validator.validateValue(Node.class, "descriptionMarkup", null);
		assertTrue("Description markup empty", constraintViolations.size() == 1);
	}
}