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

package org.xmpp.extension.ping;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

/**
 * This class implements the application-level ping mechanism as specified in <a href="http://xmpp.org/extensions/xep-0199.html">XEP-0199: XMPP Ping</a>.
 * <p>
 * For <a href="http://xmpp.org/extensions/xep-0199.html#s2c">Server-To-Client Pings</a> it automatically responds with a result (pong), in enabled.
 * </p>
 * <p>
 * It also allows to ping the server (<a href="http://xmpp.org/extensions/xep-0199.html#c2s">Client-To-Server Pings</a>) or to ping other XMPP entities (<a href="http://xmpp.org/extensions/xep-0199.html#e2e">Client-to-Client Pings</a>).
 * </p>
 *
 * @author Christian Schudt
 */
public final class PingManager extends ExtensionManager {

    private static final String FEATURE = "urn:xmpp:ping";

    /**
     * Creates the ping manager.
     *
     * @param connection The underlying connection.
     */
    private PingManager(final Connection connection) {
        super(connection);
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming()) {
                    IQ iq = e.getIQ();
                    if (iq.getType() == IQ.Type.GET && iq.getExtension(Ping.class) != null) {
                        if (isEnabled()) {
                            connection.send(iq.createResult());
                        } else {
                            sendServiceUnavailable(iq);
                        }
                    }
                }
            }
        });
        setEnabled(true);
    }

    /**
     * Pings the given XMPP entity.
     *
     * @param jid The JID to ping.
     * @return True, if the entity responded with a result; or false if it does not support the ping protocol.
     * @throws TimeoutException If the ping timed out, i.e. no response has been received in time.
     */
    public boolean ping(Jid jid) throws TimeoutException {
        try {
            IQ result = connection.query(new IQ(jid, IQ.Type.GET, new Ping()));
            return result.getError() == null;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Pings the connected server.
     *
     * @return True, if the server responded with a result; or false if it does not support the ping protocol.
     * @throws TimeoutException If the ping timed out, i.e. no response has been received in time.
     */
    public boolean pingServer() throws TimeoutException {
        return ping(null);
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return Arrays.asList(FEATURE);
    }
}
