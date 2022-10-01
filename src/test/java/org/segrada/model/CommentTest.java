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

public class CommentTest {
	private static Validator validator;

	@BeforeAll
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new Comment());
	}

	@Test
	public void testValidComment() throws Exception {
		final Comment comment = new Comment();
		comment.setText("Example text");
		comment.setMarkup("DUMMY");
		Set<ConstraintViolation<Comment>> constraintViolations = validator.validate(comment);
		assertTrue(constraintViolations.size() == 0, "Comment not valid");
	}

	@Test
	public void testTextEmpty() throws Exception {
		Set<ConstraintViolation<Comment>> constraintViolations = validator.validateValue(Comment.class, "text", null);
		assertTrue(constraintViolations.size() == 1, "Text empty");
	}

	@Test
	public void testTextTooShort() throws Exception {
		Set<ConstraintViolation<Comment>> constraintViolations = validator.validateValue(Comment.class, "text", "");
		assertTrue(constraintViolations.size() == 1, "Text too short");
	}

	@Test
	public void testmarkupEmpty() throws Exception {
		Set<ConstraintViolation<Comment>> constraintViolations = validator.validateValue(Comment.class, "markup", null);
		assertTrue(constraintViolations.size() == 1, "Markup empty");
	}
}
