/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
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

package org.xmpp.extension.avatar;

/**
 * Represents an avatar image.
 *
 * @author Christian Schudt
 */
public final class Avatar {
    private final String type;

    private final byte[] imageData;

    /**
     * @param type      The image type.
     * @param imageData The image data.
     */
    public Avatar(String type, byte[] imageData) {
        this.type = type;
        this.imageData = imageData;
    }

    /**
     * Gets the image type, e.g. "image/jpeg"
     *
     * @return The type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the binary image data.
     *
     * @return The image data.
     */
    public byte[] getImageData() {
        return imageData;
    }
}
