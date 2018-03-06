/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.session.server;

import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.errors.Condition;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.stream.Stream;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class MessageRouter {

    @Inject
    private UserManager userManager;

    @Inject
    private SessionManager sessionManager;

    public boolean process(Message message) {
        // 10.3.1.  Message
        // If the server receives a message stanza with no 'to' attribute,
        // it MUST treat the message as if the 'to' address were the bare JID <localpart@domainpart> of the sending entity.
        if (message.getTo() == null) {
            message.setTo(message.getFrom().asBareJid());
        }

        // For a message stanza, the server MUST either (a) silently ignore the stanza or (b) return a <service-unavailable/> stanza error (Section 8.3.3.19) to the sender.
        if (!userManager.userExists(message.getTo().getLocal())) {
            // silently ignore the stanza
            return true;
        }

        // 8.5.2.1.1.  Message
        // https://xmpp.org/rfcs/rfc6121.html#rules-localpart-barejid-resource-message
        if (message.isNormal() || message.getType() == Message.Type.CHAT) {
            Stream<Session> userSessions = sessionManager.getUserSessions(message.getTo().asBareJid());
            userSessions.forEach(session -> session.send(message));
        } else if (message.getType() == Message.Type.GROUPCHAT) {
            // For a message stanza of type "groupchat", the server MUST NOT deliver the stanza to any
            // of the available resources but instead MUST return a stanza error to the sender, which SHOULD be <service-unavailable/>.
            Session session = sessionManager.getSession(message.getFrom());
            // TODO from attribute?
            session.send(message.createError(Condition.SERVICE_UNAVAILABLE));
        } else if (message.getType() == Message.Type.HEADLINE) {
            Stream<Session> userSessions = sessionManager.getUserSessions(message.getTo().asBareJid());
            userSessions.forEach(session -> session.send(message));
        }

        return false;
    }
}
