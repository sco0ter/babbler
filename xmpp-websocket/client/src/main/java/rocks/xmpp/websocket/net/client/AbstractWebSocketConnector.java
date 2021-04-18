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

package rocks.xmpp.websocket.net.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.client.TransportConnector;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.dns.DnsResolver;
import rocks.xmpp.dns.TxtRecord;

abstract class AbstractWebSocketConnector implements TransportConnector<WebSocketConnectionConfiguration> {

    protected AbstractWebSocketConnector() {
    }

    protected URI getUri(final XmppSession xmppSession, final WebSocketConnectionConfiguration configuration)
            throws URISyntaxException {
        String protocol = configuration.getChannelEncryption() == ChannelEncryption.DIRECT ? "wss" : "ws";
        // If no port has been configured, use the default ports.
        int targetPort =
                configuration.getPort() > 0 ? configuration.getPort()
                        : (configuration.getChannelEncryption() == ChannelEncryption.DIRECT ? 5281 : 5280);
        // If a hostname has been configured, use it to connect.
        if (configuration.getHostname() != null) {
            return new URI(protocol, null, configuration.getHostname(), targetPort, configuration.getPath(), null,
                    null);
        } else if (xmppSession.getDomain() != null) {
            // If a URL has not been set, try to find the URL by the domain
            // via a DNS-TXT lookup as described in XEP-0156.
            String resolvedUrl = findWebSocketEndpoint(xmppSession.getDomain().toString(),
                    xmppSession.getConfiguration().getNameServer(), configuration.getConnectTimeout());
            if (resolvedUrl != null) {
                return new URI(resolvedUrl);
            } else {
                // Fallback mechanism:
                // If the URL could not be resolved, use the domain name and port 5280 as default.
                return new URI(protocol, null, xmppSession.getDomain().toString(), targetPort, configuration.getPath(),
                        null,
                        null);
            }
        } else {
            throw new IllegalStateException("Neither an URL nor a domain given for a WebSocket connection.");
        }
    }

    private static String findWebSocketEndpoint(String xmppServiceDomain, String nameServer, long timeout) {

        try {
            List<TxtRecord> txtRecords = DnsResolver.resolveTXT(xmppServiceDomain, nameServer, timeout);
            for (TxtRecord txtRecord : txtRecords) {
                Map<String, String> attributes = txtRecord.asAttributes();
                String url = attributes.get("_xmpp-client-websocket");
                if (url != null) {
                    return url;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
