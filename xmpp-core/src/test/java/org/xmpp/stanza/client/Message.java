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

package org.xmpp.stanza.client;

import org.xmpp.Jid;
import org.xmpp.stanza.AbstractMessage;
import org.xmpp.stanza.StanzaError;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the {@code <message/>} element.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message">5.  Exchanging Messages</a></cite></p>
 * <p>Once a client has authenticated with a server and bound a resource to an XML stream as described in [XMPP-CORE], an XMPP server will route XML stanzas to and from that client. One kind of stanza that can be exchanged is {@code <message/>} (if, that is, messaging functionality is enabled on the server). Exchanging messages is a basic use of XMPP and occurs when a user generates a message stanza that is addressed to another entity. As defined under Section 8, the sender's server is responsible for delivering the message to the intended recipient (if the recipient is on the same local server) or for routing the message to the recipient's server (if the recipient is on a remote server). Thus a message stanza is used to "push" information to another entity.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "message")
public final class Message extends AbstractMessage {

    Message() {
    }

    /**
     * Constructs an empty message.
     *
     * @param to The recipient.
     */
    public Message(Jid to) {
        super(to);
    }

    /**
     * Constructs a message with a type.
     *
     * @param to   The recipient.
     * @param type The message type.
     */
    public Message(Jid to, Type type) {
        super(to, type);
    }

    /**
     * Constructs a message with body and type.
     *
     * @param to   The recipient.
     * @param body The message body.
     * @param type The message type.
     */
    public Message(Jid to, Type type, String body) {
        super(to, type, body);
    }

    @Override
    public Message createError(StanzaError error) {
        Message message = new Message(getFrom(), Type.ERROR);
        createError(message, error);
        return message;
    }
}
