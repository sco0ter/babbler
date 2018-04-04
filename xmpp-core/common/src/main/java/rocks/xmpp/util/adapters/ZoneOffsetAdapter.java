/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.util.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.ZoneOffset;

/**
 * Converts a {@link java.time.ZoneOffset} to a string representation according to <a href="https://xmpp.org/extensions/xep-0082.html">XEP-0082: XMPP Date and Time Profiles</a> and vice versa.
 */
public final class ZoneOffsetAdapter extends XmlAdapter<String, ZoneOffset> {

    @Override
    public final ZoneOffset unmarshal(String v) {
        return v != null ? ZoneOffset.of(v) : null;
    }

    @Override
    public final String marshal(ZoneOffset v) {
        return v != null ? v.toString() : null;
    }
}