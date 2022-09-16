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

public class ColorTest {
	private static Validator validator;

	@BeforeAll
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new Color());
	}

	@Test
	public void testValidColor() throws Exception {
		final Color color = new Color();
		color.setTitle("Example title");
		color.setColor(123456);
		Set<ConstraintViolation<Color>> constraintViolations = validator.validate(color);
		assertTrue(constraintViolations.size() == 0, "Color not valid");
	}

	@Test
	public void testTitleEmpty() throws Exception {
		Set<ConstraintViolation<Color>> constraintViolations = validator.validateValue(Color.class, "title", null);
		assertTrue(constraintViolations.size() == 1, "Title empty");
	}

	@Test
	public void testTitleTooShort() throws Exception {
		Set<ConstraintViolation<Color>> constraintViolations = validator.validateValue(Color.class, "title", "");
		assertTrue(constraintViolations.size() == 1, "Title too short");
	}

	@Test
	public void testColorEmpty() throws Exception {
		Set<ConstraintViolation<Color>> constraintViolations = validator.validateValue(Color.class, "color", null);
		assertTrue(constraintViolations.size() == 1, "Color empty");

		constraintViolations = validator.validateValue(Color.class, "color", 0);
		assertTrue(constraintViolations.size() == 0, "Color can be 0!");
	}

	@Test
	public void testGetColorCode() throws Exception {
		final Color color = new Color();

		color.setColor(null);
		assertEquals("", color.getColorCode());

		color.setColor(0);
		assertEquals("#000000", color.getColorCode());

		color.setColor(0x112233);
		assertEquals("#112233", color.getColorCode());

		color.setColor(0xabcdef);
		assertEquals("#ABCDEF", color.getColorCode());

		color.setColor(0xffffff);
		assertEquals("#FFFFFF", color.getColorCode());
	}

	@Test
	public void testSetColorCode() throws Exception {
		final Color color = new Color();

		color.setColorCode(null);
		assertNull(color.getColor());

		color.setColorCode("0");
		assertEquals(new Integer(0), color.getColor());

		color.setColorCode("000000");
		assertEquals(new Integer(0), color.getColor());

		color.setColorCode("#000000");
		assertEquals(new Integer(0), color.getColor());

		color.setColorCode("#ffffff");
		assertEquals(new Integer(0xffffff), color.getColor());

		color.setColorCode("#FFFFFF");
		assertEquals(new Integer(0xffffff), color.getColor());

		color.setColorCode("112233");
		assertEquals(new Integer(0x112233), color.getColor());

		color.setColorCode("#112233");
		assertEquals(new Integer(0x112233), color.getColor());

		color.setColorCode("abcdef");
		assertEquals(new Integer(0xabcdef), color.getColor());

		color.setColorCode("#ABCDEF");
		assertEquals(new Integer(0xabcdef), color.getColor());

		// should fail silently
		color.setColorCode("XX");
		assertNull(color.getColor());

		color.setColorCode("#XX");
		assertNull(color.getColor());
	}

	@Test
	public void testGetR() throws Exception {
		final Color color = new Color();

		color.setColorCode("#ffffff");
		assertEquals(255, color.getR());

		color.setColorCode("#00ffff");
		assertEquals(0, color.getR());

		color.setColorCode("#33ffff");
		assertEquals(51, color.getR());
	}

	@Test
	public void testGetG() throws Exception {
		final Color color = new Color();

		color.setColorCode("#ffffff");
		assertEquals(255, color.getG());

		color.setColorCode("#ff00ff");
		assertEquals(0, color.getG());

		color.setColorCode("#ff33ff");
		assertEquals(51, color.getG());
	}

	@Test
	public void testGetB() throws Exception {
		final Color color = new Color();

		color.setColorCode("#ffffff");
		assertEquals(255, color.getB());

		color.setColorCode("#ffff00");
		assertEquals(0, color.getB());

		color.setColorCode("#ffff33");
		assertEquals(51, color.getB());
	}
}
