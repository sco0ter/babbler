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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a slot from the upload service.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "slot")
public final class Slot {

    @XmlElement
    private final Put put;

    @XmlElement
    private final Get get;

    private Slot() {
        this.put = null;
        this.get = null;
    }

    /**
     * Constructs a slot for up- and download.
     *
     * @param uploadUrl   The upload URL.
     * @param downloadUrl The download URL.
     */
    public Slot(URL uploadUrl, URL downloadUrl) {
        this(uploadUrl, Collections.emptyMap(), downloadUrl);
    }

    /**
     * Constructs a slot for up- and download with upload headers.
     *
     * @param uploadUrl   The upload URL.
     * @param headers     The upload headers.
     * @param downloadUrl The download URL.
     */
    public Slot(URL uploadUrl, Map<String, String> headers, URL downloadUrl) {
        this.put = new Put(uploadUrl, headers.entrySet().stream().map(entry -> new Put.Header(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
        this.get = new Get(downloadUrl);
    }

    /**
     * Gets the upload URL.
     *
     * @return The upload URL.
     */
    public final URL getUploadUrl() {
        return put != null ? put.url : null;
    }

    /**
     * Gets the upload HTTP headers.
     *
     * @return The upload HTTP headers.
     */
    public final Map<String, String> getUploadHeaders() {
        return put != null ? Collections.unmodifiableMap(put.headers.stream().collect(Collectors.toMap(header -> header.name, header -> header.value, (first, second) -> second))) : Collections.emptyMap();
    }

    /**
     * Gets the download URL (GET).
     *
     * @return The download URL.
     */
    public final URL getDownloadUrl() {
        return get != null ? get.url : null;
    }

    @Override
    public final String toString() {
        return put.toString() + ", " + get;
    }

    private static final class Put {

        @XmlAttribute
        private final URL url;

        @XmlElement(name = "header")
        private final List<Header> headers = new ArrayList<>();

        private Put() {
            this.url = null;
        }

        private Put(final URL url, final Collection<Header> headers) {
            this.url = url;
            if (headers != null) {
                this.headers.addAll(headers);
            }
        }

        @Override
        public final String toString() {
            return "PUT: " + (url != null ? url.toString() : "") + ", " + headers;
        }

        private static final class Header {

            @XmlAttribute
            private final String name;

            @XmlValue
            private final String value;

            private Header() {
                this.name = null;
                this.value = null;
            }

            private Header(final String name, final String value) {
                if (!name.equals("Authorization")
                        && !name.equals("Cookie")
                        && !name.equals("Expires")) {
                    throw new IllegalArgumentException("Header '" + name + "' not allowed. Only 'Authorization', 'Cookie' or 'Expires' headers are allowed.");
                }
                this.name = name;
                this.value = value;
            }

            @Override
            public final String toString() {
                return name + ": " + value;
            }
        }
    }

    private static final class Get {

        @XmlAttribute
        private final URL url;

        private Get() {
            this.url = null;
        }

        private Get(URL url) {
            this.url = url;
        }

        @Override
        public final String toString() {
            return "GET: " + (url != null ? url.toString() : "");
        }
    }
}
