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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An error condition which indicates that the requested file is too large.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0363.html#errors">5. Error Conditions</a>
 */
@XmlRootElement(name = "file-too-large")
public final class FileTooLarge {

    @XmlElement(name = "max-file-size")
    private final Long maxFileSize;

    public FileTooLarge() {
        this(null);
    }

    private FileTooLarge(final Long maxFileSize) {
        if (maxFileSize != null && maxFileSize < 0) {
            throw new IllegalArgumentException("maxFileSize must be positive");
        }
        this.maxFileSize = maxFileSize;
    }

    /**
     * The max file size or null, if it's unspecified.
     *
     * @return The max file size.
     */
    public final Long getMaxFileSize() {
        return maxFileSize;
    }

    @Override
    public final String toString() {
        return "File too large, maximum file size: " + (maxFileSize != null ? (maxFileSize + " bytes") : "unknown");
    }
}
