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

package org.xmpp.extension.reach;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * The implementation of the {@code <address/>} element in the {@code urn:xmpp:reach:0} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0152.html">XEP-0152: Reachability Addresses</a>
 * @see <a href="http://xmpp.org/extensions/xep-0152.html#schema">XML Schema</a>
 */
public final class Address {

    @XmlAttribute
    private URI uri;

    @XmlElement(name = "desc")
    private List<Description> descriptions;

    private Address() {
    }

    public Address(URI uri) {
        this.uri = uri;
    }

    public Address(URI uri, Description... descriptions) {
        this.uri = uri;
        this.descriptions = Arrays.asList(descriptions);
    }

    public URI getUri() {
        return uri;
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Address)) {
            return false;
        }
        Address other = (Address) o;

        return (descriptions == null ? other.descriptions == null : descriptions.equals(other.descriptions))
                && (uri == null ? other.uri == null : uri.equals(other.uri));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ((descriptions == null) ? 0 : descriptions.hashCode());
        result = 31 * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    public static final class Description {

        @XmlValue
        private String value;

        @XmlAttribute(name = "lang", namespace = XMLConstants.XML_NS_URI)
        private String language;

        private Description() {
        }

        /**
         * @param value    The actual description.
         * @param language The language of the description.
         */
        public Description(String value, String language) {
            this.value = value;
            this.language = language;
        }

        /**
         * Gets the description.
         *
         * @return The description.
         */
        public String getValue() {
            return value;
        }

        /**
         * Gets the language.
         *
         * @return The language.
         */
        public String getLanguage() {
            return language;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Description)) {
                return false;
            }
            Description other = (Description) o;

            return (value == null ? other.value == null : value.equals(other.value))
                    && (language == null ? other.language == null : language.equals(other.language));
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + ((value == null) ? 0 : value.hashCode());
            result = 31 * result + ((language == null) ? 0 : language.hashCode());
            return result;
        }
    }
}
