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

package rocks.xmpp.extensions.bytestreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An abstract byte stream session, which either represents a <a href="http://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band Bytestreams</a> or <a href="http://xmpp.org/extensions/xep-0065.html">XEP-0065: SOCKS5 Bytestreams</a> session.
 * <p>
 * Both kind of sessions have a session id and are bidirectional, that's why this class has an {@linkplain #getInputStream() input} and {@linkplain #getOutputStream() output stream}.
 *
 * @author Christian Schudt
 * @see rocks.xmpp.extensions.bytestreams.ibb.InBandByteStreamManager#initiateSession(rocks.xmpp.core.Jid, String, int)
 * @see rocks.xmpp.extensions.bytestreams.s5b.Socks5ByteStreamManager#initiateSession(rocks.xmpp.core.Jid, String)
 * @see ByteStreamEvent#accept()
 */
public abstract class ByteStreamSession implements AutoCloseable {

    private final String sessionId;

    public ByteStreamSession(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the output stream.
     *
     * @return The output stream.
     * @throws java.io.IOException If an I/O error occurs when creating the output stream or the session is closed.
     */
    public abstract OutputStream getOutputStream() throws IOException;

    /**
     * Gets the input stream.
     *
     * @return The input stream.
     * @throws java.io.IOException If an I/O error occurs when creating the input stream or the session is closed.
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Gets the session id for this byte stream session.
     *
     * @return The session id.
     */
    public final String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the timeout (in milliseconds) for read operations on the {@linkplain #getInputStream() input stream}.
     * The default timeout is 0, i.e. infinite. Read operations will therefore block forever, unless the session is closed.
     *
     * @return The read timeout.
     * @throws java.io.IOException If an I/O error occurred.
     * @see #setReadTimeout(int)
     */
    public abstract int getReadTimeout() throws IOException;

    /**
     * Sets the timeout (in milliseconds).
     *
     * @param readTimeout The read timeout.
     * @throws java.io.IOException If an I/O error occurred.
     * @see #getReadTimeout()
     */
    public abstract void setReadTimeout(int readTimeout) throws IOException;
}
