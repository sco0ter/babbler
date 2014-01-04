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

package org.xmpp.extension.lastactivity;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.servicediscovery.Feature;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.stanza.IQ;

import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public final class LastActivityManager extends ExtensionManager {

    static {
        ServiceDiscoveryManager.INSTANCE.addFeature(new Feature("jabber:iq:last"));
    }

    private LastActivityStrategy lastActivityStrategy;

    public LastActivityManager(final Connection connection) {
        super(connection);
        /*connection.addStanzaInterceptor(new StanzaInterceptor() {
            @Override
            public void intercept(Stanza stanza) {
                if (stanza instanceof Presence) {
                    Presence presence = (Presence) stanza;
                    // If an available presence is sent.
                    if (presence.getType() == null && presence.getExtension(LastActivity.class) == null) {
                        //presence.getExtensions().add(new LastActivity(lastActivityStrategy.getLastActivity()));
                    }
                }
            }
        });

        connection.addStanzaListener(new StanzaListener() {
            @Override
            public void handle(Stanza stanza) {
                if (stanza instanceof IQ) {
                    IQ iq = (IQ) stanza;
                    if (iq.getType() == IQ.Type.GET && iq.getExtension() instanceof LastActivity) {
                        IQ result = iq.createResult();
                        result.setExtension(new LastActivity(lastActivityStrategy.getLastActivity()));
                        connection.send(result);
                    }
                }
            }
        });  */
    }

    /**
     * Gets the last activity of the specified user.
     * <blockquote>
     * <p>The information contained in an IQ reply for this namespace is inherently ambiguous. Specifically, for a bare JID {@code <localpart@domain.tld>} the information is the time since the JID was last connected to its server; for a full JID {@code <localpart@domain.tld/resource>} the information is the time since the resource was last active in the context of an existing session; and for a bare domain the information is the uptime for the server or component. An application MUST take these differences into account when presenting the information to a human user (if any).</p>
     * </blockquote>
     *
     * @param jid The JID for which the last activity is requested.
     * @return The last activity of the requested JID or null if the feature is not implemented or a time out has occurred.
     */
    public LastActivity getLastActivity(Jid jid) {
        IQ request = new IQ(IQ.Type.GET, new LastActivity());
        request.setTo(jid);
        IQ result;
        try {
            result = connection.query(request);
        } catch (TimeoutException e) {
            return null;
        }
        if (result.getError() != null) {
            return null;
        }
        return result.getExtension(LastActivity.class);
    }

    public LastActivityStrategy getLastActivityStrategy() {
        return this.lastActivityStrategy;
    }

    public void setLastActivityStrategy(LastActivityStrategy lastActivityStrategy) {
        this.lastActivityStrategy = lastActivityStrategy;
    }
}
