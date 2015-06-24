package org.segrada.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class CommentTest {
	private static Validator validator;

	@BeforeClass
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testValidComment() throws Exception {
		final Comment comment = new Comment();
		comment.setText("Example text");
		comment.setMarkup("DUMMY");
		Set<ConstraintViolation<Comment>> constraintViolations = validator.validate(comment);
		assertTrue("Comment not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testTextEmpty() throws Exception {
		Set<ConstraintViolation<Comment>> constraintViolations = validator.validateValue(Comment.class, "text", null);
		assertTrue("Text empty", constraintViolations.size() == 1);
	}

	@Test
	public void testTextTooShort() throws Exception {
		Set<ConstraintViolation<Comment>> constraintViolations = validator.validateValue(Comment.class, "text", "");
		assertTrue("Text too short", constraintViolations.size() == 1);
	}

	@Test
	public void testmarkupEmpty() throws Exception {
		Set<ConstraintViolation<Comment>> constraintViolations = validator.validateValue(Comment.class, "markup", null);
		assertTrue("Markup empty", constraintViolations.size() == 1);
	}
}