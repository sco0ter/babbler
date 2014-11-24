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

package rocks.xmpp.extensions.muc;

import rocks.xmpp.core.Jid;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.muc.model.MucFeature;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This class wraps the service discovery information result, which usually consists of identities, features and extended forms
 * into one class, so that a developer doesn't have to deal with the complex structure of the service discovery result.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-roominfo">6.4 Querying for Room Information</a>
 */
public final class RoomInfo {

    private final Identity identity;

    private final Set<MucFeature> features;

    private final rocks.xmpp.extensions.muc.model.RoomInfo roomInfo;

    public RoomInfo(Identity identity, Set<MucFeature> features, rocks.xmpp.extensions.muc.model.RoomInfo roomInfo) {
        this.identity = identity;
        this.features = features;
        this.roomInfo = roomInfo;
    }

    /**
     * Gets the features of this room.
     *
     * @return The features.
     */
    public Set<MucFeature> getFeatures() {
        return features;
    }

    /**
     * Gets the name of the room.
     *
     * @return The room name.
     */
    public String getName() {
        return identity != null ? identity.getName() : null;
    }

    /**
     * Gets the maximum number of history messages returned by the room.
     *
     * @return The maximum number of history messages returned by the room.
     */
    public int getMaxHistoryMessages() {
        if (roomInfo != null) {
            return roomInfo.getMaxHistoryMessages();
        }
        return 0;
    }

    /**
     * Gets the contact addresses (normally, room owner or owners).
     *
     * @return The contact addresses.
     */
    public List<Jid> getContacts() {
        if (roomInfo != null) {
            return roomInfo.getContacts();
        }
        return Collections.emptyList();
    }

    /**
     * Gets a short description.
     *
     * @return The description.
     */
    public String getDescription() {
        if (roomInfo != null) {
            return roomInfo.getDescription();
        }
        return null;
    }

    /**
     * Gets the natural language for room discussions.
     *
     * @return The language.
     */
    public String getLanguage() {
        if (roomInfo != null) {
            return roomInfo.getLanguage();
        }
        return null;
    }

    /**
     * Gets an associated LDAP group that defines
     * room membership; this should be an LDAP
     * Distinguished Name according to an
     * implementation-specific or
     * deployment-specific definition of a
     * group.
     *
     * @return The LDAP group.
     */
    public String getLdapGroup() {
        if (roomInfo != null) {
            return roomInfo.getLdapGroup();
        }
        return null;
    }

    /**
     * Gets an URL for archived discussion logs.
     *
     * @return The URL.
     */
    public URL getLogs() {
        if (roomInfo != null) {
            return roomInfo.getLogs();
        }
        return null;
    }

    /**
     * Gets the current number of occupants in the room.
     *
     * @return The number of occupants.
     */
    public int getCurrentNumberOfOccupants() {
        if (roomInfo != null) {
            return roomInfo.getCurrentNumberOfOccupants();
        }
        return 0;
    }

    /**
     * Gets the current discussion topic.
     *
     * @return The topic.
     */
    public String getSubject() {
        if (roomInfo != null) {
            return roomInfo.getSubject();
        }
        return null;
    }

    /**
     * Indicates, whether the room subject can be modified by participants.
     *
     * @return Whether the room subject can be modified by participants.
     */
    public boolean isChangeSubjectAllowed() {
        return roomInfo != null && roomInfo.isChangeSubjectAllowed();
    }
}
