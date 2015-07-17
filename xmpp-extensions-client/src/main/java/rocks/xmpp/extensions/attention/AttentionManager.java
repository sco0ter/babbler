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

package rocks.xmpp.extensions.attention;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.extensions.attention.model.Attention;

/**
 * This manager allows to capture another user's attention.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0224.html">XEP-0224: Attention</a></cite></p>
 * <p>This feature is known as 'nudge' or 'buzz' in some non-XMPP IM protocols.</p>
 * </blockquote>
 * <p>If you want to listen for inbound attention requests, listen for inbound messages and check if they have the {@link rocks.xmpp.extensions.attention.model.Attention} extension.
 * </p>
 * <h3>Sample</h3>
 * <pre>
 * {@code
 * xmppSession.addInboundMessageListener(e -> {
 *     if (e.getMessage().getExtension(Attention.class) != null) {
 *         // Handle attention request.
 *     }
 * });
 * }
 * </pre>
 * <p>If you use attentions, enable this manager class, in order to register this extension in service discovery:</p>
 * <pre>
 * <code>
 * xmppSession.getManager(AttentionManager.class).setEnabled(true);
 * </code>
 * </pre>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
public final class AttentionManager extends Manager {

    private AttentionManager(XmppSession xmppSession) {
        super(xmppSession);
    }

    /**
     * Captures the attention of another user.
     *
     * @param jid The user
     */
    public final void captureAttention(Jid jid) {
        Message message = new Message(jid, Message.Type.HEADLINE);
        message.addExtension(Attention.INSTANCE);
        xmppSession.send(message);
    }
}
