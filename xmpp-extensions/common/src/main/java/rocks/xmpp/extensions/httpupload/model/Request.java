/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.extensions.httpupload.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Requests a new upload slot.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0363.html#request">4. Requesting a Slot</a>
 */
@XmlRootElement(name = "request")
public final class Request {

    /**
     * urn:xmpp:http:upload:0
     */
    public static final String NAMESPACE = "urn:xmpp:http:upload:0";

    @XmlAttribute
    private final String filename;

    @XmlAttribute
    private final long size;

    @XmlAttribute(name = "content-type")
    private final String contentType;

    private Request() {
        this.filename = null;
        this.size = 0;
        this.contentType = null;
    }

    /**
     * Constructs a request.
     *
     * @param filename    The filename.
     * @param size        The file size.
     * @param contentType The content type.
     */
    public Request(final String filename, final long size, final String contentType) {
        this.filename = Objects.requireNonNull(filename);
        this.size = Objects.requireNonNull(size);
        this.contentType = contentType;
    }

    /**
     * Constructs a request from a path.
     *
     * @param path The path.
     * @throws IOException If file size or content type could not be read.
     */
    public Request(final Path path) throws IOException {
        this.filename = path.getFileName().toString();
        this.size = Files.size(path);
        this.contentType = Files.probeContentType(path);
    }

    /**
     * Gets the file name.
     *
     * @return The file name.
     */
    public final String getFilename() {
        return filename;
    }

    /**
     * Gets the file size.
     *
     * @return The file size.
     */
    public final long getSize() {
        return size;
    }

    /**
     * Gets the file's content type (maybe null.
     *
     * @return The content type or null.
     */
    public final String getContentType() {
        return contentType;
    }
}
