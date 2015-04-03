/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.time.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.TimeZone;

/**
 * Converts a {@link java.util.TimeZone} to a string representation according to <a href="http://xmpp.org/extensions/xep-0082.html">XEP-0082: XMPP Date and Time Profiles</a> and vice versa.
 */
@Deprecated
public final class TimeZoneAdapter extends XmlAdapter<String, TimeZone> {

    @Override
    public final TimeZone unmarshal(String v) throws Exception {
        return TimeZone.getTimeZone("GMT" + v);
    }

    @Override
    public final String marshal(TimeZone v) throws Exception {
        int seconds = Math.abs(v.getRawOffset()) / 1000;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        return (v.getRawOffset() < 0 ? "-" : "+") + String.format("%02d:%02d", hours, minutes);
    }
}