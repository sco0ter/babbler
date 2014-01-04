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

package org.xmpp.extension.servicediscovery;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christian Schudt
 */

@XmlRootElement(name = "identity")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Identity implements Comparable<Identity> {

    @XmlAttribute(name = "category")
    private String category;

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "type")
    private String type;

    @XmlAttribute(name = "lang", namespace = XMLConstants.XML_NS_URI)
    private String language;

    private Identity() {

    }

    public Identity(String category, String type) {
        this.category = category;
        this.type = type;
    }

    public Identity(String category, String type, String name) {
        this.category = category;
        this.type = type;
        this.name = name;
    }

    public Identity(String category, String type, String name, String language) {
        this.category = category;
        this.type = type;
        this.name = name;
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Identity)) {
            return false;
        }
        Identity other = (Identity) o;

        return (category == null ? other.category == null : category.equals(other.category))
                && (type == null ? other.type == null : type.equals(other.type))
                && (language == null ? other.language == null : language.equals(other.language));

    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ((category == null) ? 0 : category.hashCode());
        result = 31 * result + ((type == null) ? 0 : type.hashCode());
        result = 31 * result + ((language == null) ? 0 : language.hashCode());
        return result;
    }

    @Override
    public int compareTo(Identity o) {
        int result;
        if (o == null) {
            result = 1;
        } else {
            if (getCategory() == null && o.getCategory() == null) {
                result = 0;
            } else if (getCategory() == null) {
                result = -1;
            } else if (o.getCategory() == null) {
                result = 1;
            } else {
                result = getCategory().compareTo(o.getCategory());
            }

            if (result == 0) {
                if (getType() == null && o.getType() == null) {
                    result = 0;
                } else if (getType() == null) {
                    result = -1;
                } else if (o.getType() == null) {
                    result = 1;
                } else {
                    result = getType().compareTo(o.getType());
                }
            }

            if (result == 0) {
                if (getLanguage() == null && o.getLanguage() == null) {
                    result = 0;
                } else if (getLanguage() == null) {
                    result = -1;
                } else if (o.getLanguage() == null) {
                    result = 1;
                } else {
                    result = getLanguage().compareTo(o.getLanguage());
                }
            }

            if (result == 0) {
                if (getName() == null && o.getName() == null) {
                    result = 0;
                } else if (getName() == null) {
                    result = -1;
                } else if (o.getName() == null) {
                    result = 1;
                } else {
                    result = getName().compareTo(o.getName());
                }
            }
        }
        return result;
    }
}

