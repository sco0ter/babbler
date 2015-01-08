/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Christian Schudt
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

package rocks.xmpp.extensions.compress;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface to describe a compression method.
 *
 * @author Christian Schudt
 */
public interface CompressionMethod {

    /**
     * Gets the compression method name, e.g. "zlib", "lzw", "exi", ...
     *
     * @return The compression method name.
     */
    String getName();

    /**
     * Decompresses an input stream.
     *
     * @param inputStream The compressed input stream.
     * @return The decompressed input stream.
     */
    InputStream decompress(InputStream inputStream);

    /**
     * Gets the compressed output stream.
     *
     * @param outputStream The uncompressed output stream.
     * @return The compressed output stream.
     */
    OutputStream compress(OutputStream outputStream);
}
