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
import org.xmpp.extension.data.DataForm;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Schudt
 */
public final class RoomConfigurationForm {

    //    private String name;
    //
    //    private String description;
    //
    //    private Set<MucFeature> features;
    //
    //    private List<Jid> contacts;
    //
    //    private String language;
    //
    //    private String ldapGroup;
    //
    //    private URL logs;
    //
    //    private int currentNumberOfOccupants;
    //
    //    private boolean changeSubjectAllowed;
    //
    //    private String subject;
    //
    //    private Connection connection;
    private DataForm dataForm;

    public RoomConfigurationForm(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    /**
     * Gets the maximum number of history messages returned by the room.
     *
     * @return Gets the maximum number of history messages returned by the room.
     */
    public int getMaxHistoryMessages() {
        DataForm.Field field = dataForm.findField("muc#maxhistoryfetch");
        if (field != null && !field.getValues().isEmpty()) {
            try {
                return Integer.parseInt(field.getValues().get(0));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * @return
     */
    public Set<Role> getAllowedRolesToSendPrivateMessages() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_allowpm");
        Set<Role> roles = EnumSet.of(Role.NONE);
        if (field != null && !field.getValues().isEmpty()) {
            String value = field.getValues().get(0);
            switch (value) {
                case "anyone":
                    roles = EnumSet.of(Role.MODERATOR, Role.PARTICIPANT);
                    break;
                case "participants":
                    roles = EnumSet.of(Role.PARTICIPANT);
                    break;
                case "moderators":
                    roles = EnumSet.of(Role.MODERATOR);
                    break;
                default:
                    roles = EnumSet.of(Role.NONE);
                    break;
            }
        }
        return roles;
    }

    /**
     * Whether to allow occupants to invite others.
     *
     * @return
     */
    public boolean isInviteAllowed() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_allowinvites");
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Whether to allow occupants to change subject.
     *
     * @return
     */
    public boolean isChangeSubjectAllowed() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_changesubject");
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Whether to enable public logging of room conversations.
     *
     * @return
     */
    public boolean isPublicLoggingEnabled() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_enablelogging");
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Gets the natural language for room discussions.
     *
     * @return The language.
     */
    public String getLanguage() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_lang");
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Gets the XMPP URI of associated publish-subscribe node.
     *
     * @return The language.
     */
    public URI getPubSubNode() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_pubsub");
        if (field != null && !field.getValues().isEmpty()) {
            try {
                return URI.create(field.getValues().get(0));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets the maximum number of room occupants.
     *
     * @return The maximum number of room occupants.
     */
    public int getMaxOccupants() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_maxusers");
        if (field != null && !field.getValues().isEmpty()) {
            try {
                return Integer.parseInt(field.getValues().get(0));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Whether the room is members-only.
     *
     * @return
     */
    public boolean isMembersOnly() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_membersonly");
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Whether the room is moderated.
     *
     * @return
     */
    public boolean isModerated() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_moderatedroom");
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Whether the room is moderated.
     *
     * @return
     */
    public boolean isPasswordProtected() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_passwordprotectedroom");
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Whether the room is persistent.
     *
     * @return
     */
    public boolean isPersistent() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_persistentroom");
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    public List<Role> getRolesForWhichPresenceIsBroadcast() {
        // TODO
        // muc#roomconfig_publicroom
        return null;
    }

    /**
     * Whether the room is public.
     *
     * @return
     */
    public boolean isPublic() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_publicroom");
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Gets the list of administrators.
     *
     * @return
     */
    public List<Jid> getAdministrators() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_roomadmins");
        List<Jid> admins = new ArrayList<>();
        if (field != null) {
            for (String value : field.getValues()) {
                admins.add(Jid.valueOf(value, true));
            }
        }
        return admins;
    }

    /**
     * Get a short description.
     *
     * @return The description.
     */
    public String getDescription() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_roomdesc");
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Get the natural-language room name.
     *
     * @return The description.
     */
    public String getName() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_roomname");
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Gets the owners.
     *
     * @return
     */
    public List<Jid> getOwners() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_roomowners");
        List<Jid> owners = new ArrayList<>();
        if (field != null) {
            for (String value : field.getValues()) {
                owners.add(Jid.valueOf(value, true));
            }
        }
        return owners;
    }

    /**
     * Get the room password.
     *
     * @return The description.
     */
    public String getPassword() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_roomsecret");
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Get the room password.
     *
     * @return The description.
     */
    public List<Affiliation> getAffiliationsWhichMayDiscoverRealJids() {
        DataForm.Field field = dataForm.findField("muc#roomconfig_whois");
        // TODO
        return null;
    }

    //
    //    /**
    //     * Gets the contact addresses (normally, room owner or owners).
    //     *
    //     * @return The contact addresses.
    //     */
    //    public List<Jid> getContacts() {
    //        // muc#roominfo_contactjid
    //        return contacts;
    //    }
    //
    //    public void setContacts(List<Jid> contacts) {
    //        this.contacts = contacts;
    //    }
    //
    //    /**
    //     * Gets an associated LDAP group that defines
    //     * room membership; this should be an LDAP
    //     * Distinguished Name according to an
    //     * implementation-specific or
    //     * deployment-specific definition of a
    //     * group.
    //     *
    //     * @return The LDAP group.
    //     */
    //    public String getLdapGroup() {
    //        // muc#roominfo_ldapgroup
    //        return ldapGroup;
    //    }
    //
    //    public void setLdapGroup(String ldapGroup) {
    //        this.ldapGroup = ldapGroup;
    //    }
    //
    //    /**
    //     * Gets an URL for archived discussion logs
    //     *
    //     * @return The URL.
    //     */
    //    public URL getLogs() {
    //        // muc#roominfo_logs
    //        return logs;
    //    }
    //
    //    public void setLogs(URL logs) {
    //        this.logs = logs;
    //    }
    //
    //    public int getCurrentNumberOfOccupants() {
    //        // muc#roominfo_occupants
    //        return currentNumberOfOccupants;
    //    }
    //
    //    public void setCurrentNumberOfOccupants(int currentNumberOfOccupants) {
    //        this.currentNumberOfOccupants = currentNumberOfOccupants;
    //    }
    //
    //    /**
    //     * Indicates, whether the room subject can be modified by participants.
    //     *
    //     * @return Whether the room subject can be modified by participants.
    //     */
    //    public boolean isChangeSubjectAllowed() {
    //        // muc#roominfo_subjectmod
    //        return changeSubjectAllowed;
    //    }
    //
    //    public void setChangeSubjectAllowed(boolean changeSubjectAllowed) {
    //        this.changeSubjectAllowed = changeSubjectAllowed;
    //    }
    //
    //    public void setMaxHistory(int maxHistory) {
    //
    //    }
    //
    //    public String getSubject() {
    //        return subject;
    //    }
    //
    //    public void setSubject(String subject) {
    //        this.subject = subject;
    //    }
}
