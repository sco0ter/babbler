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

package rocks.xmpp.extensions.compress.model.feature;

import rocks.xmpp.core.stream.model.StreamFeature;
import rocks.xmpp.extensions.compress.model.CompressionMethod;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the {@code <compression/>} element in the {@code http://jabber.org/features/compress} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0138.html">XEP-0138: Stream Compression</a>
 * @see <a href="http://xmpp.org/extensions/xep-0138.html#schemas-stream">XML Schema</a>
 */
@XmlRootElement(name = "compression")
public final class CompressionFeature extends StreamFeature {

    @XmlElement(name = "method")
    private final List<CompressionMethod> methods = new ArrayList<>();

    /**
     * Gets the available compression methods.
     *
     * @return The compression methods.
     */
    public List<CompressionMethod> getMethods() {
        return methods;
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
