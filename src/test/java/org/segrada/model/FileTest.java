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

public class FileTest {
	private static byte[] fileData={0xa,0x2,0xf};

	private static Validator validator;

	@BeforeAll
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new File());
	}

	@Test
	public void defaultValues() throws Exception {
		final File file = new File();

		assertEquals("", file.getTitle());
		assertNull(file.getFilename());
		assertEquals("", file.getDescription());
		assertNotNull(file.getDescriptionMarkup());
		assertEquals("", file.getCopyright());
		assertNull(file.getMimeType());
		assertEquals("", file.getLocation());
		assertNull(file.getFullText());
		assertEquals(new Long(0L), file.getFileSize());
	}

	@Test
	public void testValidFile() throws Exception {
		final File file = new File();
		file.setTitle("Example title");
		file.setFilename("filename");
		file.setMimeType("text/plain");
		file.setData(fileData);
		file.setDescriptionMarkup("default");
		Set<ConstraintViolation<File>> constraintViolations = validator.validate(file);
		assertTrue(constraintViolations.size() == 0, "File not valid");
	}

	@Test
	public void testFilenameEmpty() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "filename", null);
		assertTrue(constraintViolations.size() == 1, "Filename empty");
	}

	@Test
	public void testFilenameTooShort() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "filename", "");
		assertTrue(constraintViolations.size() == 1, "Filename too short");
	}

	@Test
	public void testMimeTypeEmpty() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "mimeType", null);
		assertTrue(constraintViolations.size() == 1, "MimeType empty");
	}

	@Test
	public void testMimeTypeTooShort() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "mimeType", "");
		assertTrue(constraintViolations.size() == 1, "MimeType too short");
	}

	@Test
	public void testIndexFullTextEmpty() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "indexFullText", null);
		assertTrue(constraintViolations.size() == 1, "indexFullText empty");
	}

	@Test
	public void testContainFileEmpty() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "containFile", null);
		assertTrue(constraintViolations.size() == 1, "containFile empty");
	}

	@Test
	public void testDescriptionMarkupEmpty() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "descriptionMarkup", null);
		assertTrue(constraintViolations.size() == 1, "Description markup empty");
	}

	@Test
	public void testGetMimeType() throws Exception {
		final File file = new File();

		file.setFilename("filename");
		assertEquals("", file.getFileType());

		file.setFilename("filename.pdf");
		assertEquals("pdf", file.getFileType());

		file.setFilename("filename.PDF");
		assertEquals("pdf", file.getFileType());

		file.setFilename("filename.");
		assertEquals("", file.getFileType());

		file.setFilename("filename.png");
		assertEquals("image", file.getFileType());
	}
}
