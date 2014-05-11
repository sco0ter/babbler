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
import org.xmpp.stanza.client.Presence;

/**
 * The main actor in a multi-user chat environment is the occupant, who can be said to be located "in" a multi-user chat room and to participate in the discussions held in that room.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#user">7. Occupant Use Cases</a>
 */
public final class Occupant implements Comparable<Occupant> {

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
            this.me = mucUser.getStatusCodes().contains(Status.self());
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

    /**
     * Compares this occupant with another occupant.
     * Occupants are compared first by their affiliation, then by their role, then by their nickname.
     * <p>
     * Affiliations and roles are ranked by their privileges, so that occupants with the most privileges are ranked higher.
     * </p>
     * <p>
     * That means, in a sorted list of occupants, the owners are listed first, followed by the admins, followed by the mere members.
     * Within each affiliation group, the moderators are listed first, followed by the participants and visitors. Each group is then sorted by its occupants' nicknames.
     * </p>
     *
     * @param o The other occupant.
     * @return The comparison result.
     */
    @Override
    public int compareTo(Occupant o) {
        if (this == o) {
            return 0;
        }

        if (o != null) {
            int result;
            // First compare affiliations.
            if (affiliation != null) {
                if (o.affiliation != null) {
                    result = affiliation.compareTo(o.affiliation);
                } else {
                    result = -1;
                }
            } else {
                if (o.affiliation != null) {
                    result = 1;
                } else {
                    result = 0;
                }
            }
            // If the affiliations are equal, compare roles.
            if (result == 0) {
                if (role != null) {
                    if (o.role != null) {
                        result = role.compareTo(o.role);
                    } else {
                        // If this role is not null, but the other is null, move this up (-1).
                        result = -1;
                    }
                } else {
                    // If this role is null, but the other is not, move this down (1).
                    if (o.role != null) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                }
            }
            // If the roles are equal, compare nick names.
            if (result == 0) {
                if (nick != null) {
                    if (o.nick != null) {
                        result = nick.compareTo(o.nick);
                    } else {
                        // If this nick is not null, but the other is null, move this up (-1).
                        result = -1;
                    }
                } else {
                    // If this nick is null, but the other is not, move this down (1).
                    if (o.nick != null) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                }
            }
            return result;
        } else {
            return -1;
        }
    }
}
