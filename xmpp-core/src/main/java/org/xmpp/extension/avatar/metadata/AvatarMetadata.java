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

package org.xmpp.extension.avatar.metadata;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The implementation of the {@code <metadata/>} element in the {@code urn:xmpp:avatar:metadata} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a>
 * @see <a href="http://xmpp.org/extensions/xep-0084.html#schema-metadata">XML Schema</a>
 */
@XmlRootElement(name = "metadata")
public final class AvatarMetadata {

    public static final String NAMESPACE = "urn:xmpp:avatar:metadata";

    @XmlElement(name = "info")
    private List<Info> infoList = new ArrayList<>();

    @XmlAnyElement(lax = true)
    private Object pointer;

    private AvatarMetadata() {
    }

    public AvatarMetadata(Info... info) {
        this.infoList = Arrays.asList(info);
    }

    /**
     * Gets the info list.
     *
     * @return The info list.
     */
    public List<Info> getInfoList() {
        return infoList;
    }

    /**
     * The implementation of the {@code <info/>} element in the {@code urn:xmpp:avatar:metadata} namespace.
     */
    public static final class Info {

        @XmlAttribute(name = "bytes")
        private Integer bytes;

        @XmlAttribute(name = "id")
        private String id;

        @XmlAttribute(name = "type")
        private String type;

        @XmlAttribute(name = "url")
        private URL url;

        @XmlAttribute(name = "width")
        private Integer width;

        @XmlAttribute(name = "height")
        private Integer height;

        public Info() {
        }

        public Info(int bytes, String id, String type) {
            this.bytes = bytes;
            this.id = id;
            this.type = type;
        }

        public Info(int bytes, String id, String type, Integer width, Integer height, URL url) {
            this.bytes = bytes;
            this.id = id;
            this.type = type;
            this.width = width;
            this.height = height;
            this.url = url;
        }

        /**
         * Gets the size of the image data in bytes.
         *
         * @return The bytes.
         */
        public Integer getBytes() {
            return bytes;
        }

        /**
         * Gets a hash of the image data for the specified content-type, where the hash is produced in accordance with the SHA-1 algorithm as specified in RFC 3174 [11] (with binary output).
         *
         * @return The id.
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the IANA-registered content type of the image data.
         *
         * @return The type.
         */
        public String getType() {
            return type;
        }

        /**
         * Gets the http: or https: URL at which the image data file is hosted; this attribute MUST NOT be included unless the image data file can be retrieved via HTTP.
         *
         * @return The URL.
         */
        public URL getUrl() {
            return url;
        }

        /**
         * Gets the width of the image in pixels.
         *
         * @return The width.
         */
        public Integer getWidth() {
            return width;
        }

        /**
         * Gets the height of the image in pixels.
         *
         * @return The height.
         */
        public Integer getHeight() {
            return height;
        }
    }
}
