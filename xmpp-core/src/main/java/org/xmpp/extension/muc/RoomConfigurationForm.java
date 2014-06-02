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
import java.util.*;

/**
 * @author Christian Schudt
 */
public final class RoomConfigurationForm {

    private static final String MAX_HISTORY_FETCH = "muc#maxhistoryfetch";

    private static final String ALLOW_PM = "muc#roomconfig_allowpm";

    private static final String ALLOW_INVITES = "muc#roomconfig_allowinvites";

    private static final String CHANGE_SUBJECT = "muc#roomconfig_changesubject";

    private static final String ENABLE_LOGGING = "muc#roomconfig_enablelogging";

    private static final String GET_MEMBER_LIST = "muc#roomconfig_getmemberlist";

    private static final String LANGUAGE = "muc#roomconfig_lang";

    private static final String PUBSUB = "muc#roomconfig_pubsub";

    private static final String MAX_USERS = "muc#roomconfig_maxusers";

    private static final String MEMBERS_ONLY = "muc#roomconfig_membersonly";

    private static final String MODERATED_ROOM = "muc#roomconfig_moderatedroom";

    private static final String PASSWORD_PROTECTED = "muc#roomconfig_passwordprotectedroom";

    private static final String PERSISTENT_ROOM = "muc#roomconfig_persistentroom";

    private static final String PRESENCE_BROADCAST = "muc#roomconfig_presencebroadcast";

    private static final String PUBLIC_ROOM = "muc#roomconfig_publicroom";

    private static final String ROOM_ADMINS = "muc#roomconfig_roomadmins";

    private static final String ROOM_DESC = "muc#roomconfig_roomdesc";

    private static final String ROOM_NAME = "muc#roomconfig_roomname";

    private static final String ROOM_OWNERS = "muc#roomconfig_roomowners";

    private static final String ROOM_SECRET = "muc#roomconfig_roomsecret";

    private static final String WHOIS = "muc#roomconfig_whois";

    private final DataForm dataForm;

    public RoomConfigurationForm(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    /**
     * Gets the maximum number of history messages returned by the room.
     *
     * @return The maximum number of history messages returned by the room.
     */
    public int getMaxHistoryMessages() {
        return getAsInteger(MAX_HISTORY_FETCH);
    }

    /**
     * Sets the maximum number of history messages returned by the room.
     *
     * @param maxHistoryMessages The maximum number of history messages returned by the room.
     */
    public void setMaxHistoryMessages(int maxHistoryMessages) {
        DataForm.Field field = getOrCreateField(DataForm.Field.Type.TEXT_SINGLE, MAX_HISTORY_FETCH);
        field.getValues().clear();
        field.getValues().add(Integer.toString(maxHistoryMessages));
    }

    private DataForm.Field getOrCreateField(DataForm.Field.Type type, String var) {
        DataForm.Field field = dataForm.findField(var);
        if (field == null) {
            field = new DataForm.Field(type, var);
            dataForm.getFields().add(field);
        }
        return field;
    }

    private int getAsInteger(String fieldName) {
        DataForm.Field field = dataForm.findField(fieldName);
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
     * Gets the roles that may send private messages.
     *
     * @return The roles.
     */
    public Set<Role> getAllowedRolesToSendPrivateMessages() {
        DataForm.Field field = dataForm.findField(ALLOW_PM);
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
     * Sets the roles that may send private messages.
     *
     * @param roles The roles.
     */
    public void setAllowedRolesToSendPrivateMessages(Set<Role> roles) {
        String value;
        if (roles.contains(Role.MODERATOR) && roles.contains(Role.PARTICIPANT)) {
            value = "anyone";
        } else if (roles.contains(Role.PARTICIPANT)) {
            value = "participants";
        } else if (roles.contains(Role.MODERATOR)) {
            value = "moderators";
        } else {
            value = "none";
        }
        DataForm.Field field = dataForm.findField(ALLOW_PM);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, ALLOW_PM);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(value);
    }

    /**
     * Indicates, whether to allow occupants to invite others.
     *
     * @return True, if occupants are allowed to invite others.
     */
    public boolean isAllowInvites() {
        DataForm.Field field = dataForm.findField(ALLOW_INVITES);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether to allow occupants to invite others.
     *
     * @param allowInvites True, if occupants are allowed to invite others.
     */
    public void setAllowInvites(boolean allowInvites) {
        DataForm.Field field = dataForm.findField(ALLOW_INVITES);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, ALLOW_INVITES);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(allowInvites ? "1" : "0");
    }

    /**
     * Indicates, whether to allow occupants to change subject.
     *
     * @return True, if occupants are allowed to change subject.
     */
    public boolean isChangeSubjectAllowed() {
        DataForm.Field field = dataForm.findField(CHANGE_SUBJECT);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether to allow occupants to change subject.
     *
     * @param changeSubjectAllowed True, if occupants are allowed to change subject.
     */
    public void setChangeSubjectAllowed(boolean changeSubjectAllowed) {
        DataForm.Field field = dataForm.findField(CHANGE_SUBJECT);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, CHANGE_SUBJECT);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(changeSubjectAllowed ? "1" : "0");
    }

    /**
     * Indicates, whether to enable public logging of room conversations.
     *
     * @return True, if public logging of room conversations is enabled.
     */
    public boolean isPublicLoggingEnabled() {
        DataForm.Field field = dataForm.findField(ENABLE_LOGGING);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether to enable public logging of room conversations.
     *
     * @param publicLoggingEnabled True, if public logging of room conversations is enabled.
     */
    public void setPublicLoggingEnabled(boolean publicLoggingEnabled) {
        DataForm.Field field = dataForm.findField(ENABLE_LOGGING);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, ENABLE_LOGGING);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(publicLoggingEnabled ? "1" : "0");
    }

    /**
     * Gets the roles which may retrieve member list.
     *
     * @return The roles which may retrieve member list.
     */
    public Set<Role> getRolesWhichMayRetrieveMemberList() {
        DataForm.Field field = dataForm.findField(GET_MEMBER_LIST);
        Set<Role> roles = new HashSet<>();
        if (field != null) {
            for (String value : field.getValues()) {
                switch (value.toLowerCase()) {
                    case "moderator":
                        roles.add(Role.MODERATOR);
                        break;
                    case "participant":
                        roles.add(Role.PARTICIPANT);
                        break;
                    case "visitor":
                        roles.add(Role.VISITOR);
                        break;
                    case "none":
                        roles.add(Role.NONE);
                        break;
                }
            }
        }
        return roles;
    }

    /**
     * Sets the roles for which presence is broadcast.
     *
     * @param roles The roles for which presence is broadcast.
     */
    public void setRolesWhichMayRetrieveMemberList(Set<Role> roles) {
        DataForm.Field field = dataForm.findField(GET_MEMBER_LIST);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.LIST_MULTI, GET_MEMBER_LIST);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        for (Role role : roles) {
            field.getValues().add(role.name().toLowerCase());
        }
    }

    /**
     * Gets the natural language for room discussions.
     *
     * @return The language.
     */
    public String getLanguage() {
        DataForm.Field field = dataForm.findField(LANGUAGE);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets the natural language for room discussions.
     *
     * @param language The language.
     */
    public void setLanguage(String language) {
        DataForm.Field field = dataForm.findField(LANGUAGE);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, LANGUAGE);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(language);
    }

    /**
     * Gets the XMPP URI of associated publish-subscribe node.
     *
     * @return The URI.
     */
    public URI getPubSubNode() {
        DataForm.Field field = dataForm.findField(PUBSUB);
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
     * Sets the XMPP URI of associated publish-subscribe node.
     *
     * @param pubSubNode The URI.
     */
    public void setPubSubNode(URI pubSubNode) {
        DataForm.Field field = dataForm.findField(PUBSUB);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, PUBSUB);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(pubSubNode.toString());
    }

    /**
     * Gets the maximum number of room occupants.
     *
     * @return The maximum number of room occupants.
     */
    public int getMaxOccupants() {
        return getAsInteger(MAX_USERS);
    }

    /**
     * Sets the maximum number of room occupants.
     *
     * @param maxOccupants The maximum number of room occupants.
     */
    public void setMaxOccupants(int maxOccupants) {
        DataForm.Field field = dataForm.findField(MAX_USERS);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, MAX_USERS);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(Integer.toString(maxOccupants));
    }

    /**
     * Indicates, whether the room is members-only.
     *
     * @return True, if the room is members-only.
     */
    public boolean isMembersOnly() {
        DataForm.Field field = dataForm.findField(MEMBERS_ONLY);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether the room is members-only.
     *
     * @param membersOnly True, if the room is members-only.
     */
    public void setMembersOnly(boolean membersOnly) {
        DataForm.Field field = dataForm.findField(MEMBERS_ONLY);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, MEMBERS_ONLY);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(membersOnly ? "1" : "0");
    }

    /**
     * Indicates, whether the room is moderated.
     *
     * @return True, if the room is moderated.
     */
    public boolean isModerated() {
        DataForm.Field field = dataForm.findField(MODERATED_ROOM);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether the room is moderated.
     *
     * @param moderated True, if the room is moderated.
     */
    public void setModerated(boolean moderated) {
        DataForm.Field field = dataForm.findField(MODERATED_ROOM);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, MODERATED_ROOM);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(moderated ? "1" : "0");
    }

    /**
     * Indicates, whether the room is password protected.
     *
     * @return True, if the room is password protected.
     */
    public boolean isPasswordProtected() {
        DataForm.Field field = dataForm.findField(PASSWORD_PROTECTED);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether the room is password protected.
     *
     * @param passwordProtected True, if the room is password protected.
     */
    public void setPasswordProtected(boolean passwordProtected) {
        DataForm.Field field = dataForm.findField(PASSWORD_PROTECTED);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, PASSWORD_PROTECTED);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(passwordProtected ? "1" : "0");
    }

    /**
     * Indicates, whether the room is persistent.
     *
     * @return True, if the room is persistent.
     */
    public boolean isPersistentRoom() {
        DataForm.Field field = dataForm.findField(PERSISTENT_ROOM);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether the room is persistent.
     *
     * @param persistentRoom True, if the room is persistent.
     */
    public void setPersistentRoom(boolean persistentRoom) {
        DataForm.Field field = dataForm.findField(PERSISTENT_ROOM);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, PERSISTENT_ROOM);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(persistentRoom ? "1" : "0");
    }

    /**
     * Gets the roles for which presence is broadcast.
     *
     * @return The roles for which presence is broadcast.
     */
    public Set<Role> getRolesForWhichPresenceIsBroadcast() {
        DataForm.Field field = dataForm.findField(PRESENCE_BROADCAST);
        Set<Role> roles = new HashSet<>();
        if (field != null) {
            for (String value : field.getValues()) {
                switch (value.toLowerCase()) {
                    case "moderator":
                        roles.add(Role.MODERATOR);
                        break;
                    case "participant":
                        roles.add(Role.PARTICIPANT);
                        break;
                    case "visitor":
                        roles.add(Role.VISITOR);
                        break;
                    case "none":
                        roles.add(Role.NONE);
                        break;
                }
            }
        }
        return roles;
    }

    /**
     * Sets the roles for which presence is broadcast.
     *
     * @param roles The roles for which presence is broadcast.
     */
    public void setRolesForWhichPresenceIsBroadcast(Set<Role> roles) {
        DataForm.Field field = dataForm.findField(PRESENCE_BROADCAST);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.LIST_MULTI, PRESENCE_BROADCAST);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        for (Role role : roles) {
            field.getValues().add(role.name().toLowerCase());
        }
    }

    /**
     * Indicates, whether the room is public.
     *
     * @return True, if the room is public.
     */
    public boolean isPublicRoom() {
        DataForm.Field field = dataForm.findField(PUBLIC_ROOM);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether the room is public.
     *
     * @param publicRoom True, if the room is public.
     */
    public void setPublicRoom(boolean publicRoom) {
        DataForm.Field field = dataForm.findField(PUBLIC_ROOM);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, PUBLIC_ROOM);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(publicRoom ? "1" : "0");
    }

    /**
     * Gets the administrators.
     *
     * @return The administrators.
     */
    public List<Jid> getAdministrators() {
        DataForm.Field field = dataForm.findField(ROOM_ADMINS);
        List<Jid> admins = new ArrayList<>();
        if (field != null) {
            for (String value : field.getValues()) {
                admins.add(Jid.valueOf(value, true));
            }
        }
        return admins;
    }

    /**
     * Sets the administrators.
     *
     * @param administrators The administrators.
     */
    public void setAdministrators(List<Jid> administrators) {
        DataForm.Field field = dataForm.findField(ROOM_ADMINS);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.JID_MULTI, ROOM_ADMINS);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        for (Jid admin : administrators) {
            field.getValues().add(admin.toString());
        }
    }

    /**
     * Gets a short description.
     *
     * @return The description.
     */
    public String getRoomDescription() {
        DataForm.Field field = dataForm.findField(ROOM_DESC);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets a short description.
     *
     * @param description The description.
     */
    public void setRoomDescription(String description) {
        DataForm.Field field = dataForm.findField(ROOM_DESC);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, ROOM_DESC);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(description);
    }

    /**
     * Gets the natural-language room name.
     *
     * @return The room name.
     */
    public String getRoomName() {
        DataForm.Field field = dataForm.findField(ROOM_NAME);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets the natural-language room name.
     *
     * @param description The room name.
     */
    public void setRoomName(String description) {
        DataForm.Field field = dataForm.findField(ROOM_NAME);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, ROOM_NAME);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(description);
    }

    /**
     * Gets the owners.
     *
     * @return The owners.
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
     * Sets the owners.
     *
     * @param owners The owners.
     */
    public void setOwners(List<Jid> owners) {
        DataForm.Field field = dataForm.findField(ROOM_OWNERS);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.JID_MULTI, ROOM_OWNERS);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        for (Jid admin : owners) {
            field.getValues().add(admin.toString());
        }
    }

    /**
     * Gets the room password.
     *
     * @return The password.
     */
    public String getPassword() {
        DataForm.Field field = dataForm.findField(ROOM_SECRET);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets the room password.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        DataForm.Field field = dataForm.findField(ROOM_SECRET);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, ROOM_SECRET);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(password);
    }

    /**
     * Get the roles, which may discover real JIDs.
     *
     * @return The roles, which may discover real JIDs.
     */
    public Set<Role> getRolesWhichMayDiscoverRealJids() {
        DataForm.Field field = dataForm.findField(WHOIS);
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
     * Get the roles, which may discover real JIDs.
     *
     * @param roles The roles, which may discover real JIDs.
     */
    public void setRolesWhichMayDiscoverRealJids(Set<Role> roles) {
        String value;
        if (roles.contains(Role.MODERATOR) && roles.contains(Role.PARTICIPANT)) {
            value = "anyone";
        } else if (roles.contains(Role.PARTICIPANT)) {
            value = "participants";
        } else if (roles.contains(Role.MODERATOR)) {
            value = "moderators";
        } else {
            value = "none";
        }
        DataForm.Field field = dataForm.findField(WHOIS);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, WHOIS);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(value);
    }
}
