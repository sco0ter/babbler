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

package org.xmpp;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Christian Schudt
 */
public final class XmppUtils {

    private XmppUtils() {
    }

    public static XMLStreamWriter createXmppStreamWriter(XMLStreamWriter xmlStreamWriter, boolean clientNamespace) throws XMLStreamException {
        return new PrefixFreeCanonicalizationWriter(xmlStreamWriter, clientNamespace ? "jabber:client" : "jabber:server");
    }

    public static InputStream createBranchedInputStream(InputStream inputStream, OutputStream outputStream) {
        return new BranchedInputStream(inputStream, outputStream);
    }

    public static OutputStream createBranchedOutputStream(OutputStream out, OutputStream branch) {
        return new BranchedOutputStream(out, branch);
    }
}
