/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.core;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;

import java.util.concurrent.CompletionStage;

/**
 * A generic interface for different kinds of XMPP sessions.
 * <ul>
 * <li>A client-to-server session</li>
 * <li>An external component session (<a href="https://xmpp.org/extensions/xep-0114.html">XEP-0114: Jabber Component Protocol</a>).</li>
 * <li>A client-to-client session (<a href="https://xmpp.org/extensions/xep-0174.html">XEP-0174: Serverless Messaging</a>)</li>
 * <li>A server-to-server session</li>
 * </ul>
 *
 * @author Christian Schudt
 */
public interface Session extends AutoCloseable {

    /**
     * Gets the local XMPP address.
     * This method may return null, e.g. for client-to-server sessions before resource binding.
     *
     * @return The local XMPP address or null.
     */
    Jid getLocalXmppAddress();

    /**
     * Gets the remote XMPP address.
     * This method may return null, if the session is not fully negotiated.
     *
     * @return The remote XMPP address or null.
     */
    Jid getRemoteXmppAddress();

    /**
     * Sends an element to the peer entity.
     *
     * @param streamElement The element.
     * @return The completion stage, which is complete when the element has been sent.
     */
    CompletionStage<Void> send(StreamElement streamElement);

    /**
     * Asynchronously closes the session.
     * <p>
     * Closing usually involves a round-trip with the peer on the XMPP layer first by sending a closing stream element,
     * then waiting on the response and then closing the underlying transport layer.
     *
     * @return The completion stage, which is complete, when the session is closed.
     * @see #close()
     * @see Connection#closeAsync()
     */
    CompletionStage<Void> closeAsync();

    /**
     * Asynchronously closes the session with a stream error.
     *
     * @param streamError The stream error, which is sent before closing the stream.
     * @return The completion stage, which is complete, when the session is closed.
     * @see #closeAsync()
     * @see Connection#closeAsync(StreamError)
     */
    CompletionStage<Void> closeAsync(StreamError streamError);

    @Override
    void close() throws Exception;
}
