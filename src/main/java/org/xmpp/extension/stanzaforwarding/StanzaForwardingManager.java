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

package org.xmpp.extension.stanzaforwarding;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.delayeddelivery.DelayedDelivery;
import org.xmpp.stanza.Message;

import java.util.Date;

/**
 * This manager allows to forward stanzas to other XMPP entities.
 * <p>
 * Enabling this manager simply indicates support for the stanza forwarding extension in a service discovery response and has no further effect.
 * </p>
 * <p>By default, stanza forwarding support is not enabled.</p>
 * In order to check for a forwarded stanza, simply check for the {@link Forwarded} extension:
 * <pre>
 * <code>
 * Forwarded forwarded = message.getExtension(Forwarded.class);
 * if (forwarded != null) {
 *     Message forwardedMessage = forwarded.getMessage();
 * }
 * </code>
 * </pre>
 *
 * @author Christian Schudt
 */
public final class StanzaForwardingManager extends ExtensionManager {

    /**
     * Creates the stanza forwarding manager.
     *
     * @param connection The underlying connection.
     */
    private StanzaForwardingManager(final Connection connection) {
        super(connection, "urn:xmpp:forward:0");
        setEnabled(false);
    }

    /**
     * Forwards a message to another XMPP entity.
     *
     * @param message The original message.
     * @param to      The receiver.
     */
    public void forwardMessage(Message message, Jid to) {
        Message outerMessage = new Message(to, message.getType());
        // Include a empty body to make sure it will be stored in offline case.
        outerMessage.setBody(" ");
        outerMessage.getExtensions().add(new Forwarded(new DelayedDelivery(new Date()), message));
        connection.send(outerMessage);
    }
}
