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
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "metadata")
public final class AvatarMetadata {

    private List<Info> infoList = new ArrayList<>();

    @XmlAnyElement(lax = true)
    private Object pointer;

    private AvatarMetadata() {
    }

    public AvatarMetadata(List<Info> infoList) {
        this.infoList = infoList;
    }

    public static final class Info {

        @XmlAttribute(name = "bytes")
        private Integer bytes;

        @XmlAttribute(name = "height")
        private Short height;

        @XmlAttribute(name = "id")
        private String id;

        @XmlAttribute(name = "type")
        private String type;

        @XmlAttribute(name = "url")
        private URL url;

        @XmlAttribute(name = "width")
        private Short width;

        public Info() {

        }

        public Info(int bytes, String id, String type) {
            this.bytes = bytes;
            this.id = id;
            this.type = type;
        }

        private Info(int bytes, String id, String type, short width, short height, URL url) {
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
         * @see #setBytes(Integer)
         */
        public Integer getBytes() {
            return bytes;
        }

        /**
         * Sets the size of the image data in bytes.
         *
         * @param bytes The bytes.
         * @see #getBytes()
         */
        public void setBytes(Integer bytes) {
            this.bytes = bytes;
        }

        /**
         * Gets the height of the image in pixels.
         *
         * @return The height.
         * @see #setHeight(Short)
         */
        public Short getHeight() {
            return height;
        }

        /**
         * Sets The height of the image in pixels.
         *
         * @param height The height.
         * @see #getHeight()
         */
        public void setHeight(Short height) {
            this.height = height;
        }

        /**
         * Gets a hash of the image data for the specified content-type, where the hash is produced in accordance with the SHA-1 algorithm as specified in RFC 3174 [11] (with binary output).
         *
         * @return The id.
         * @see #setId(String)
         */
        public String getId() {
            return id;
        }

        /**
         * Sets a hash of the image data for the specified content-type, where the hash is produced in accordance with the SHA-1 algorithm as specified in RFC 3174 [11] (with binary output).
         *
         * @param id The id.
         * @see #getId()
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Gets the IANA-registered content type of the image data.
         *
         * @return The type.
         * @see #setType(String)
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the IANA-registered content type of the image data.
         *
         * @param type The type.
         * @see #getType()
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Gets the http: or https: URL at which the image data file is hosted; this attribute MUST NOT be included unless the image data file can be retrieved via HTTP.
         *
         * @return The URL.
         * @see #setUrl(java.net.URL)
         */
        public URL getUrl() {
            return url;
        }

        /**
         * Sets the http: or https: URL at which the image data file is hosted; this attribute MUST NOT be included unless the image data file can be retrieved via HTTP.
         *
         * @param url The URL.
         * @see #getUrl()
         */
        public void setUrl(URL url) {
            this.url = url;
        }

        /**
         * Gets the width of the image in pixels.
         *
         * @return The width.
         * @see #setWidth(Short)
         */
        public Short getWidth() {
            return width;
        }

        /**
         * Sets the width of the image in pixels.
         *
         * @param width The width.
         * @see #getWidth()
         */
        public void setWidth(Short width) {
            this.width = width;
        }
    }
}
