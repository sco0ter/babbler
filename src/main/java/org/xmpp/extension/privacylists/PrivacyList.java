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

package org.xmpp.extension.privacylists;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Schudt
 */
public final class PrivacyList {
    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "item")
    private List<PrivacyRule> items = new ArrayList<>();

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