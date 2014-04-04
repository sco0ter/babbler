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

package org.xmpp.extension.muc;

import org.xmpp.Jid;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.extension.muc.user.Status;
import org.xmpp.stanza.Presence;

/**
 * The main actor in a multi-user chat environment is the occupant, who can be said to be located "in" a multi-user chat room and to participate in the discussions held in that room.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#user">7. Occupant Use Cases</a>
 */
public final class Occupant {

    private final Affiliation affiliation;

    private final Role role;

    private final Jid jid;

    private final String nick;

    private final boolean me;

    Occupant(Presence presence) {
        this.nick = presence.getFrom().getResource();
        MucUser mucUser = presence.getExtension(MucUser.class);
        if (mucUser != null && mucUser.getItem() != null) {
            this.affiliation = mucUser.getItem().getAffiliation();
            this.role = mucUser.getItem().getRole();
            this.jid = mucUser.getItem().getJid();
            this.me = mucUser.getStatusCodes().contains(new Status(110));
        } else {
            this.affiliation = null;
            this.role = null;
            this.jid = null;
            this.me = false;
        }
    }

    /**
     * Gets the affiliation of the occupant.
     *
     * @return The affiliation.
     */
    public Affiliation getAffiliation() {
        return affiliation;
    }

    /**
     * Gets the role of the occupant.
     *
     * @return The role.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Gets the JID of the occupant. Note that it can be null, if the room is (semi-)anonymous.
     *
     * @return The JID or null for (semi-)anonymous rooms.
     */
    public Jid getJid() {
        return jid;
    }

    /**
     * Gets the nickname.
     *
     * @return The nickname.
     */
    public String getNick() {
        return nick;
    }
}
