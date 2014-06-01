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

package org.xmpp.extension.privacy;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of a privacy list.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0016.html#protocol">2. Protocol</a></cite></p>
 * <p>Most instant messaging systems have found it necessary to implement some method for users to block communications from particular other users (this is also required by sections 5.1.5, 5.1.15, 5.3.2, and 5.4.10 of RFC 2779 [3]. In XMPP this is done by managing one's privacy lists using the 'jabber:iq:privacy' namespace.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
public final class PrivacyList {
    @XmlElement(name = "item")
    private final List<PrivacyRule> items = new ArrayList<>();

    @XmlAttribute(name = "name")
    private String name;

    private PrivacyList() {
    }

    /**
     * Creates a privacy list.
     *
     * @param name The privacy list's name.
     */
    public PrivacyList(String name) {
        this.name = name;
    }

    /**
     * Gets the privacy rules.
     *
     * @return The privacy rules.
     */
    public List<PrivacyRule> getPrivacyRules() {
        return items;
    }

    /**
     * Gets the name of the privacy list.
     *
     * @return The name.
     * @see #setName(String)
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the privacy list.
     *
     * @param name The name.
     * @see #getName()
     */
    public void setName(String name) {
        this.name = name;
    }
}