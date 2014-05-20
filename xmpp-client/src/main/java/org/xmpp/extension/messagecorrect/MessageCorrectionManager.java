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

package org.xmpp.extension.messagecorrect;

import org.xmpp.XmppSession;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.AbstractMessage;
import org.xmpp.stanza.client.Message;

/**
 * This manager allows to correct last sent messages as specified by <a href="http://xmpp.org/extensions/xep-0308.html">XEP-0308: Last Message Correction</a>.
 * <p>
 * By default this manager is disabled. If you support last message correction, explicitly enable it and check incoming messages for the {@link Replace} extension.
 * </p>
 * <h3>Advertising support for message correction</h3>
 * <pre><code>
 * connection.getExtensionManager(MessageCorrectionManager.class).setEnabled(true);
 * </code></pre>
 * <h3>Checking if a message should be corrected</h3>
 * <pre><code>
 * Replace replace = message.getExtension(Replace.class);
 * if (replace != null) {
 *     String oldMessageId = replace.getId();
 *     // Replace old message with message...
 * }
 * </code></pre>
 *
 * @author Christian Schudt
 */
public final class MessageCorrectionManager extends ExtensionManager {

    private MessageCorrectionManager(XmppSession xmppSession) {
        super(xmppSession, Replace.NAMESPACE);
    }

    /**
     * Corrects a message with a new message by sending the new message and appending the 'replace' extension.
     *
     * @param id      The message id of the old message, which shall be corrected.
     * @param message The new message, which will replace the old message.
     */
    public void correctMessage(String id, Message message) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }

        message.getExtensions().add(new Replace(id));
        xmppSession.send(message);
    }
}
