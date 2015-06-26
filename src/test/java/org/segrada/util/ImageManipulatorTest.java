package org.segrada.util;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class ImageManipulatorTest {
	/**
	 * hold the image files resources
	 */
	private static byte[] icon_png;
	private static byte[] icon_gif;
	private static byte[] landscape_png;
	private static byte[] landscape_jpg;
	private static byte[] portrait_png;
	private static byte[] portrait_jpg;

	private static final String mimePNG = "image/png";
	private static final String mimeJPG = "image/jpeg";
	private static final String mimeGIF = "image/gif";

	@BeforeClass
	public static void setUpClass() throws Exception {
		// load bytes
		icon_png = resourceToBytes("/img/test_icon.png");
		icon_gif = resourceToBytes("/img/test_icon.gif");
		landscape_png = resourceToBytes("/img/test_landscape.png");
		landscape_jpg = resourceToBytes("/img/test_landscape.jpg");
		portrait_png = resourceToBytes("/img/test_portrait.png");
		portrait_jpg = resourceToBytes("/img/test_portrait.jpg");
	}

	@Test
	public void testLoading() throws Exception {
		ImageManipulator manipulator;

		// test loading types => should not throw errors
		manipulator = new ImageManipulator(icon_png, mimePNG);
		assertEquals(mimePNG, manipulator.getContentType());

		manipulator = new ImageManipulator(icon_gif, mimeGIF);
		assertEquals(mimeGIF, manipulator.getContentType());

		manipulator = new ImageManipulator(landscape_png, mimePNG);
		assertEquals(mimePNG, manipulator.getContentType());

		manipulator = new ImageManipulator(landscape_jpg, mimeJPG);
		assertEquals(mimeJPG, manipulator.getContentType());

		manipulator = new ImageManipulator(portrait_png, mimePNG);
		assertEquals(mimePNG, manipulator.getContentType());

		manipulator = new ImageManipulator(portrait_jpg, mimeJPG);
		assertEquals(mimeJPG, manipulator.getContentType());
	}

	@Test
	public void testCropImageToSquare() throws Exception {
		ImageManipulator manipulator;
		BufferedImage image;

		// test icon
		manipulator = new ImageManipulator(icon_png, mimePNG);
		manipulator.cropImageToSquare();
		// get image
		image = ImageIO.read(new ByteArrayInputStream(manipulator.getImageBytes()));
		// should not be changed
		assertEquals(24, image.getHeight());
		assertEquals(24, image.getWidth());

		// test landscape
		manipulator = new ImageManipulator(landscape_png, mimePNG);
		manipulator.cropImageToSquare();
		// get image
		image = ImageIO.read(new ByteArrayInputStream(manipulator.getImageBytes()));
		// should not be changed
		assertEquals(254, image.getHeight());
		assertEquals(254, image.getWidth());

		// test portrait
		manipulator = new ImageManipulator(portrait_png, mimePNG);
		manipulator.cropImageToSquare();
		// get image
		image = ImageIO.read(new ByteArrayInputStream(manipulator.getImageBytes()));
		// should not be changed
		assertEquals(191, image.getHeight());
		assertEquals(191, image.getWidth());
	}

	@Test
	public void testCreateThumbnailExact() throws Exception {
		ImageManipulator manipulator;
		BufferedImage image;

		// test icon
		manipulator = new ImageManipulator(icon_png, mimePNG);
		manipulator.createThumbnail(24, true);
		// get image
		image = ImageIO.read(new ByteArrayInputStream(manipulator.getImageBytes()));
		// should not be changed
		assertEquals(24, image.getHeight());
		assertEquals(24, image.getWidth());

		// test landscape
		manipulator = new ImageManipulator(landscape_png, mimePNG);
		manipulator.createThumbnail(24, true);
		// get image
		image = ImageIO.read(new ByteArrayInputStream(manipulator.getImageBytes()));
		// should not be changed
		assertEquals(24, image.getHeight());
		assertEquals(24, image.getWidth());

		// test portrait
		manipulator = new ImageManipulator(portrait_png, mimePNG);
		manipulator.createThumbnail(24, true);
		// get image
		image = ImageIO.read(new ByteArrayInputStream(manipulator.getImageBytes()));
		// should not be changed
		assertEquals(24, image.getHeight());
		assertEquals(24, image.getWidth());
	}

	@Test
	public void testCreateThumbnailNotExact() throws Exception {
		ImageManipulator manipulator;
		BufferedImage image;

		// test icon
		manipulator = new ImageManipulator(icon_png, mimePNG);
		manipulator.createThumbnail(24, false);
		// get image
		image = ImageIO.read(new ByteArrayInputStream(manipulator.getImageBytes()));
		// should not be changed
		assertEquals(24, image.getHeight());
		assertEquals(24, image.getWidth());

		// test landscape
		manipulator = new ImageManipulator(landscape_png, mimePNG);
		manipulator.createThumbnail(24, false);
		// get image
		image = ImageIO.read(new ByteArrayInputStream(manipulator.getImageBytes()));
		// should not be changed
		assertEquals(17, image.getHeight());
		assertEquals(24, image.getWidth());

		// test portrait
		manipulator = new ImageManipulator(portrait_png, mimePNG);
		manipulator.createThumbnail(24, false);
		// get image
		image = ImageIO.read(new ByteArrayInputStream(manipulator.getImageBytes()));
		// should not be changed
		assertEquals(24, image.getHeight());
		assertEquals(13, image.getWidth());
	}

	/**
	 * helper to load resource
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public static byte[] resourceToBytes(String resource) throws IOException {
		InputStream is = ImageManipulatorTest.class.getResourceAsStream(resource);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		int c = 0;
		while ((c = is.read()) != -1) {
			bos.write((char) c);
		}

		return bos.toByteArray();
	}
}