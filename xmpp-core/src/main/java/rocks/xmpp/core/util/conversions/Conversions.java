/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Markus KARG
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package rocks.xmpp.core.util.conversions;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Helper class providing common data type conversions.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 */
public final class Conversions {
	private Conversions() {
		// Static utility class
	}

	/**
	 * Converts {@code bitmap} into {@link Image}.
	 *
	 * @param bitmap
	 *            The bitmap to convert. Must not be {@code null}.
	 * @return Instance of {@link Image} created from {@code bitmap}. Never
	 *         {@code null}.
	 */
	public static final Image asAwtImage(final byte[] bitmap) throws ConversionException {
		try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(requireNonNull(bitmap))) {
			return ofNullable(ImageIO.read(inputStream)).orElseThrow(ConversionException::new);
		} catch (final IOException e) {
			throw new ConversionException(e);
		}
	}

	/**
	 * Converts {@code image} into {@code byte[]}.
	 *
	 * @param awtImage
	 *            The image to convert. Must not be {@code null}.
	 * @return PNG bitmap created from {@code image}. Never {@code null}.
	 */
	public static final byte[] asPNG(final Image awtImage) throws ConversionException {
		try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			if (!ImageIO.write(asBufferedImage(requireNonNull(awtImage)), "png", outputStream))
				throw new ConversionException();
			return outputStream.toByteArray();
		} catch (final IOException e) {
			throw new ConversionException(e);
		}
	}

	/**
	 * Guarantees that {@code awtImage} is a {@link BufferedImage}.
	 *
	 * @param awtImage
	 *            The object to approve. Must not be {@code null}.
	 * @return Either {@code awtImage} if it is a {@code BufferedImage}, or a
	 *         {@code BufferedImage} created from {@code awtImage}. Never
	 *         {@code null}.
	 */
	private static final BufferedImage asBufferedImage(final Image awtImage) {
		return requireNonNull(awtImage) instanceof BufferedImage ? (BufferedImage) awtImage : toBufferedImage(awtImage);
	}

	/**
	 * Creates new {@link BufferedImage} instance from {@code awtImage}.
	 *
	 * @param awtImage
	 *            The object to turn into a {@code BufferedImage}. Must not be
	 *            {@code null}.
	 * @return A new instance of {@code BufferedImage} with the same bitmap as
	 *         {@code Image}. Never {@code null}.
	 */
	private static final BufferedImage toBufferedImage(final Image awtImage) {
		return render(requireNonNull(awtImage), new BufferedImage(awtImage.getWidth(null), awtImage.getHeight(null), TYPE_INT_ARGB));
	}

	/**
	 * Renders source on target.
	 *
	 * @param source
	 *            Gets rendered on target. Must not be {@code null}.
	 * @param target
	 *            Gets source rendered on itself. Must not be {@code null}.
	 * @return {@code target} for in-line use. Never {@code null}.
	 */
	private static final BufferedImage render(final Image source, final BufferedImage target) {
		requireNonNull(source);
		requireNonNull(target);

		final Graphics2D graphics = target.createGraphics();
		try {
			graphics.drawImage(source, 0, 0, null);
		} finally {
			graphics.dispose();
		}
		return target;
	}
}