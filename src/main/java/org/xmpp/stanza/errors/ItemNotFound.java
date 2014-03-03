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

package org.xmpp.stanza.errors;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <item-not-found/>} stanza error.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-item-not-found">8.3.3.7.  item-not-found</a></cite></p>
 * <p>The addressed JID or item requested cannot be found; the associated error type SHOULD be "cancel".</p>
 * </blockquote>
 */
@XmlRootElement(name = "item-not-found")
public final class ItemNotFound extends Condition {

    @Override
    public String toString() {
        return "item-not-found";
    }
}
