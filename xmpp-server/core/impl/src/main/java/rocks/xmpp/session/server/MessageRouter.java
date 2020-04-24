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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.errors.Condition;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;
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

        if (message.getTo().getLocal() != null) {
            // 8.5.  Local User

            if (message.getTo().getResource() == null) {
                // 8.5.2.  localpart@domainpart
                // 8.5.2.1.1.  Message
                // https://xmpp.org/rfcs/rfc6121.html#rules-localpart-barejid-resource-message
                if (message.isNormal() || message.getType() == Message.Type.CHAT) {
                    Iterator<? extends Session> userSessions = getNonNegativeResources(message.getTo());
                    if (userSessions.hasNext()) {
                        // 8.5.2.1.  Available or Connected Resources
                        // 8.5.2.1.1.  Message
                        deliverToMostAvailableResources(userSessions, message);
                    } else {
                        // 8.5.2.2.  No Available or Connected Resources
                        // 8.5.2.2.1.  Message
                        storeOfflineOrReturnError(message);
                    }
                } else if (message.getType() == Message.Type.GROUPCHAT) {
                    // For a message stanza of type "groupchat", the server MUST NOT deliver the stanza to any
                    // of the available resources but instead MUST return a stanza error to the sender, which SHOULD be <service-unavailable/>.
                    Session session = sessionManager.getSession(message.getFrom());
                    session.send(message.createError(Condition.SERVICE_UNAVAILABLE));
                } else if (message.getType() == Message.Type.HEADLINE) {
                    // For a message stanza of type "headline":
                    // If there is more than one resource with a non-negative presence priority then the server MUST deliver the message to all of the non-negative resources.
                    Iterator<? extends Session> userSessions = getNonNegativeResources(message.getTo());
                    userSessions.forEachRemaining(session -> session.send(message));
                }
            } else {
                // 8.5.3.  localpart@domainpart/resourcepart
                Session session = sessionManager.getSession(message.getTo());
                if (session != null) {
                    // 8.5.3.1.  Resource Matches
                    // For a message stanza, the server MUST deliver the stanza to the resource.
                    session.send(message);
                } else {
                    // // 8.5.3.2.1.  Message
                    if (message.getType() == Message.Type.CHAT) {
                        // 8.5.3.2.  No Resource Matches
                        Iterator<? extends Session> userSessions = getNonNegativeResources(message.getTo().asBareJid());
                        if (userSessions.hasNext()) {
                            deliverToMostAvailableResources(userSessions, message);
                        } else {
                            storeOfflineOrReturnError(message);
                        }
                    } else if (message.getType() != Message.Type.ERROR) {
                        // For a message stanza of type "normal", "groupchat", or "headline", the server MUST either
                        // (a) silently ignore the stanza or
                        // (b) return an error stanza to the sender, which SHOULD be <service-unavailable/>.
                        ignoreOrReturnError(message);
                    }
                    // else: For a message stanza of type "error", the server MUST silently ignore the stanza.
                }
            }
        }
        return false;
    }

    void ignoreOrReturnError(Message message) {
        if (message.getType() == Message.Type.GROUPCHAT) {
            returnError(message);
        }
    }

    private void returnError(Message message) {
        Session session = sessionManager.getSession(message.getFrom());
        if (session != null) {
            session.send(message.createError(Condition.SERVICE_UNAVAILABLE));
        }
    }

    void deliverToMostAvailableResources(Iterator<? extends Session> nonNegativeResources, Message message) {
        nonNegativeResources.forEachRemaining(availableResource -> availableResource.send(message));
    }

    void storeOfflineOrReturnError(Message message) {
        if (userManager.userExists(message.getTo().getLocal())) {

        } else {
            ignoreOrReturnError(message);
        }
    }

    private Iterator<? extends Session> getNonNegativeResources(Jid bareJid) {
        Stream<Session> userSessions = sessionManager.getUserSessions(bareJid);
        return userSessions.map(session -> (InboundClientSession) session)
                .filter(inboundClientSession -> {
                    Presence presence = inboundClientSession.getPresence();
                    return presence != null && presence.getPriority() >= 0;
                }).iterator();
    }
}
