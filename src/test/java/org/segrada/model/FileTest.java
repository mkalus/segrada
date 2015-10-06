package org.segrada.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.segrada.test.PropertyAsserter.assertBasicGetterSetterBehavior;

public class FileTest {
	private static byte[] fileData={0xa,0x2,0xf};

	private static Validator validator;

	@BeforeClass
	public static void setUp() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testProperties() {
		assertBasicGetterSetterBehavior(new File());
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
		assertTrue("File not valid", constraintViolations.size() == 0);
	}

	@Test
	public void testFilenameEmpty() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "filename", null);
		assertTrue("Filename empty", constraintViolations.size() == 1);
	}

	@Test
	public void testFilenameTooShort() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "filename", "");
		assertTrue("Filename too short", constraintViolations.size() == 1);
	}

	@Test
	public void testMimeTypeEmpty() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "mimeType", null);
		assertTrue("MimeType empty", constraintViolations.size() == 1);
	}

	@Test
	public void testMimeTypeTooShort() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "mimeType", "");
		assertTrue("MimeType too short", constraintViolations.size() == 1);
	}

	@Test
	public void testIndexFullTextEmpty() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "indexFullText", null);
		assertTrue("indexFullText empty", constraintViolations.size() == 1);
	}

	@Test
	public void testContainFileEmpty() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "containFile", null);
		assertTrue("containFile empty", constraintViolations.size() == 1);
	}

	@Test
	public void testDescriptionMarkupEmpty() throws Exception {
		Set<ConstraintViolation<File>> constraintViolations = validator.validateValue(File.class, "descriptionMarkup", null);
		assertTrue("Description markup empty", constraintViolations.size() == 1);
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