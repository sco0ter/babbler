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

package org.xmpp.extension.register;

import org.xmpp.extension.data.DataForm;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class Registration {

    private String registered;

    @XmlElement
    private String instructions;

    @XmlElement
    private String username;

    @XmlElement
    private String nick;

    @XmlElement
    private String password;

    @XmlElement
    private String name;

    @XmlElement
    private String first;

    @XmlElement
    private String last;

    @XmlElement
    private String email;

    @XmlElement
    private String address;

    @XmlElement
    private String city;

    @XmlElement
    private String state;

    @XmlElement
    private String zip;

    @XmlElement
    private String phone;

    @XmlElement
    private String url;

    @XmlElement
    private String date;

    @XmlElement
    private String misc;

    @XmlElement
    private String text;

    @XmlElement
    private String key;

    @XmlElement
    private String remove;

    @XmlElement
    private DataForm dataForm;

    public Registration() {

    }

    public Registration(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
