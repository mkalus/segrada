package org.segrada.util;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Copyright 2015 Maximilian Kalus [segrada@auxnet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Manipulate images - helper class
 */
public class ImageManipulator {
	/**
	 * keep current mime format
	 */
	private String mime;

	/**
	 * buffer of image
	 */
	private BufferedImage image;

	/**
	 * getter
	 * @return
	 */
	public String getContentType() {
		return mime;
	}

	/**
	 * getter for image bytes
	 * @return
	 */
	public byte[] getImageBytes() {
		try {
			// flush image to png byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			baos.flush();

			// change content type
			mime = "image/png";

			return baos.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}

	public ImageManipulator(byte[] imageBytes, String mime) {
		this.mime = mime;
		this.image = getBufferedImageFromBytes(imageBytes);
	}

	/**
	 * Read image from bytes
	 * @param imageBytes
	 * @return
	 */
	private BufferedImage getBufferedImageFromBytes(byte[] imageBytes) {
		try {
			// try to read image using ImageIO
			return ImageIO.read(new ByteArrayInputStream(imageBytes));
		} catch (Throwable e) {
			// try to load image trough awt - this is a workaround for Java 8 on OpenJDK on Linux
			ImageIcon icon = new ImageIcon(imageBytes);
			image = new BufferedImage(
					icon.getIconWidth(),
					icon.getIconHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics g = image.createGraphics();
			// paint the Icon to the BufferedImage.
			icon.paintIcon(null, g, 0, 0);
			g.dispose();

			return image;
		}
	}

	/**
	 * crops image to square
	 */
	public void cropImageToSquare() {
		int width = image.getWidth();
		int height = image.getHeight();

		// possibly crop
		if (width > height)
			image = Scalr.crop(image, (width - height) / 2, 0, height, height);
		else if (width < height)
			image = Scalr.crop(image, 0, (height - width) / 2, width, width);
	}

	/**
	 * create image thumbnail
	 * @param width
	 * @param exact fix exactly width and height (might stretch image)
	 */
	public void createThumbnail(int width, boolean exact) {
		if (exact) // resize image to fit exactly
			image = Scalr.resize(image, Scalr.Mode.FIT_EXACT, width);
		else image = Scalr.resize(image, width);
	}
}
