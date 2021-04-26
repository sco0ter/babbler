/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

package rocks.xmpp.extensions.httpbind;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import rocks.xmpp.core.net.Connection;
import rocks.xmpp.core.net.client.TransportConnector;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.model.SessionOpen;

/**
 * A BOSH transport connector which uses {@link java.net.HttpURLConnection}.
 *
 * <p>This is the default connector for BOSH based XMPP connections if none is defined.</p>
 *
 * <h3>Sample Usage</h3>
 *
 * <pre>{@code
 * BoshConnectionConfiguration boshConfiguration = BoshConnectionConfiguration.builder()
 *     .hostname("localhost")
 *     .port(443)
 *     .path("/http-bind")
 *     .sslContext(sslContext)
 *     .channelEncryption(ChannelEncryption.DIRECT)
 *     .connector(new HttpUrlConnector())
 *     .build();
 * }</pre>
 *
 * @see BoshConnectionConfiguration.Builder#connector(TransportConnector)
 */
public final class HttpUrlConnector implements TransportConnector<BoshConnectionConfiguration> {

    @Override
    public final CompletableFuture<Connection> connect(final XmppSession xmppSession,
                                                       final BoshConnectionConfiguration configuration,
                                                       final SessionOpen sessionOpen) {
        try {
            HttpUrlBoshConnection boshConnection =
                    new HttpUrlBoshConnection(BoshConnection.getUrl(xmppSession, configuration), xmppSession,
                            configuration);
            return boshConnection.open(sessionOpen).thenApply(aVoid -> (Connection) boshConnection)
                    .toCompletableFuture();
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
