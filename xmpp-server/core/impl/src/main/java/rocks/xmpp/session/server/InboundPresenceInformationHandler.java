/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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
import rocks.xmpp.core.stanza.model.Presence;

import javax.inject.Inject;

/*
 * Handles inbound presence information.
 *
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-initial-inbound">4.2.3.  Server Processing of Inbound Initial Presence</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-probe-inbound">4.3.2.  Server Processing of Inbound Presence Probe</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-broadcast-inbound">4.4.3.  Server Processing of Subsequent Inbound Presence</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-unavailable-inbound">4.5.3.  Server Processing of Inbound Unavailable Presence</a>
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-directed-inbound">4.6.4.  Server Processing of Inbound Directed Presence</a>
 */
public class InboundPresenceInformationHandler {

    @Inject
    private SessionManager sessionManager;

    public void process(final Presence presence) {
        if (presence.getTo() != null) {
            // No 'to' attribute not applicable for inbound presences.
            // Actually all presences which are processed here should have a 'to' attribute.
            if (presence.getTo().isBareJid()) {
                // RFC 6121 § 8.5.2.1.2.  Presence
                // For a presence stanza with no type or of type "unavailable", the server MUST deliver it to all available resources.
                if (presence.isAvailable()) {
                    sessionManager.getUserSessions(presence.getTo()).forEach(session -> session.send(presence));
                } else if (presence.getType() == Presence.Type.UNAVAILABLE) {
                    sessionManager.getUserSessions(presence.getTo()).forEach(session -> session.send(presence));
                    // TODO If the contact's server does not broadcast subsequent presence notifications to users who are offline (as described under Section 4.4.2), it MUST also update its internal representation of which entities are online by noting that the user is unavailable.
                }
            } else if (presence.isAvailable() || presence.getType() == Presence.Type.UNAVAILABLE) {
                // RFC 6121 8.5.3.  localpart@domainpart/resourcepart
                Session session = sessionManager.getSession(presence.getTo());
                if (session != null) {
                    // RFC 6121 § 8.5.3.1.  Resource Matches
                    // For a presence stanza with no 'type' attribute or a 'type' attribute of "unavailable", the server MUST deliver the stanza to the resource.
                    session.send(presence);
                }
                // else:
                // RFC 6121 § 8.5.3.2.2.  Presence
                // For a presence stanza with no 'type' attribute or a 'type' attribute of "unavailable", the server MUST silently ignore the stanza.
            }
        }
    }
}
