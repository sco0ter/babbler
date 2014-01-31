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

package org.xmpp.extension.attention;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.Message;

import java.util.Arrays;
import java.util.Collection;

/**
 * This manager allows to capture another user's attention.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0224.html">XEP-0224: Attention</a></cite></p>
 * <p>This feature is known as 'nudge' or 'buzz' in some non-XMPP IM protocols.</p>
 * </blockquote>
 * <p>If you want to listen for incoming attention requests, listen for incoming messages and check if they have the {@link Attention} extension.
 * </p>
 * <h3>Sample</h3>
 * <pre>
 * <code>
 * connection.addMessageListener(new MessageListener() {
 *     {@literal @}Override
 *     public void handle(MessageEvent e) {
 *         if (e.isIncoming() &amp;&amp; e.getMessage().getExtension(Attention.class) != null) {
 *             // Handle attention request.
 *         }
 *     }
 * });
 * </code>
 * </pre>
 * <p>If you use attentions, enable this manager class, in order to register this extension in service discovery:</p>
 * <pre>
 * <code>
 * connection.getExtensionManager(AttentionManager.class).setEnabled(true);
 * </code>
 * </pre>
 *
 * @author Christian Schudt
 */
public final class AttentionManager extends ExtensionManager {

    private static final String FEATURE = "urn:xmpp:attention:0";

    private AttentionManager(Connection connection) {
        super(connection);
    }

    /**
     * Captures the attention of another user.
     *
     * @param jid The user
     */
    public void captureAttention(Jid jid) {
        Message message = new Message(jid, Message.Type.HEADLINE);
        message.getExtensions().add(new Attention());
        connection.send(message);
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return Arrays.asList(FEATURE);
    }
}
