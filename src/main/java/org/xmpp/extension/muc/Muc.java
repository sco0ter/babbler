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

package org.xmpp.extension.muc;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <x/>} element, which allows to enter a room.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "x")
public final class Muc {

    @XmlElement(name = "password")
    private String password;

    @XmlElement(name = "history")
    private History history;

    /**
     * Creates an empty element.
     */
    public Muc() {
    }

    /**
     * Creates an element with a password for the room.
     *
     * @param password The password.
     */
    public Muc(String password) {
        this.password = password;
    }

    /**
     * Creates an element with a history element, indicating the user wishes to retrieve history.
     *
     * @param history The history.
     */
    public Muc(History history) {
        this.history = history;
    }

    /**
     * Creates an element with a history element and a password for the room.
     *
     * @param history  The history.
     * @param password The password.
     */
    public Muc(History history, String password) {
        this.history = history;
        this.password = password;
    }
}
