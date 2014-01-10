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

package org.xmpp.extension.servicediscovery.info;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents an identity of an XMPP entity.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0030.html#info">3. Discovering Information About a Jabber Entity</a></cite></p>
 * <p>In disco, an entity's identity is broken down into its category (server, client, gateway, directory, etc.) and its particular type within that category (IM server, phone vs. handheld client, MSN gateway vs. AIM gateway, user directory vs. chatroom directory, etc.). This information helps requesting entities to determine the group or "bucket" of services into which the entity is most appropriately placed (e.g., perhaps the entity is shown in a GUI with an appropriate icon). An entity MAY have multiple identities. When multiple identity elements are provided, the name attributes for each identity element SHOULD have the same value.</p>
 * </blockquote>
 *
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

    /**
     * Private default constructor for unmarshalling.
     */
    private Identity() {
    }

    /**
     * Creates an identity with a category and type.
     *
     * @param category The category.
     * @param type     The type.
     */
    public Identity(String category, String type) {
        this.category = category;
        this.type = type;
    }

    /**
     * Creates an identity with a category, type and name.
     *
     * @param category The category.
     * @param type     The type.
     * @param name     The name.
     */
    public Identity(String category, String type, String name) {
        this.category = category;
        this.type = type;
        this.name = name;
    }

    /**
     * Creates an identity with a category, type, name and language.
     *
     * @param category The category.
     * @param type     The type.
     * @param name     The name.
     * @param language The language.
     */
    public Identity(String category, String type, String name, String language) {
        this.category = category;
        this.type = type;
        this.name = name;
        this.language = language;
    }

    /**
     * Gets the category, e.g. server, client, gateway, directory, etc.
     *
     * @return The category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Gets the type within the {@linkplain #getCategory() category}, e.g. IM server, phone vs. handheld client, MSN gateway vs. AIM gateway, user directory vs. chatroom directory, etc.
     *
     * @return The type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the identity's name.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * The optional language to localize the {@linkplain #getName() name}.
     *
     * @return The language.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * An identity is considered equal, if category, type and language are equal, because there cannot be two identities with the same category, type and language, but with different names.
     *
     * @param o The other object.
     * @return True, if category, type and language are equal.
     */
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

    /**
     * Implements a natural ordering of an identity, as suggested and required by <a href="http://xmpp.org/extensions/xep-0115.html">XEP-0115: Entity Capabilities</a>.
     *
     * @param o The other identity.
     * @return The result of the comparison.
     */
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

