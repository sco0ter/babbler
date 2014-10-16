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

package rocks.xmpp.extensions.muc.model;

import rocks.xmpp.core.Jid;
import rocks.xmpp.extensions.data.model.DataForm;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
public final class RoomInfoForm {
    /**
     * Maximum Number of History Messages Returned by Room
     */
    private static final String MAX_HISTORY_FETCH = "muc#maxhistoryfetch";

    /**
     * Contact Addresses (normally, room owner or owners)
     */
    private static final String CONTACT_JID = "muc#roominfo_contactjid";

    /**
     * Short Description of Room
     */
    private static final String DESCRIPTION = "muc#roominfo_description";

    /**
     * Natural Language for Room Discussions
     */
    private static final String LANGUAGE = "muc#roominfo_lang";

    /**
     * An associated LDAP group that defines
     * room membership; this should be an LDAP
     * Distinguished Name according to an
     * implementation-specific or
     * deployment-specific definition of a
     * group.
     */
    private static final String LDAP_GROUP = "muc#roominfo_ldapgroup";

    /**
     * URL for Archived Discussion Logs
     */
    private static final String LOGS = "muc#roominfo_logs";

    /**
     * Current Number of Occupants in Room
     */
    private static final String OCCUPANTS = "muc#roominfo_occupants";

    /**
     * Current Discussion Topic
     */
    private static final String SUBJECT = "muc#roominfo_subject";

    /**
     * The room subject can be modified by participants
     */
    private static final String SUBJECT_MOD = "muc#roominfo_subjectmod";

    private final DataForm dataForm;

    public RoomInfoForm(DataForm dataForm) {
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

    /**
     * Gets the contact addresses (normally, room owner or owners).
     *
     * @return The contact addresses.
     */
    public List<Jid> getContacts() {
        DataForm.Field field = dataForm.findField(CONTACT_JID);
        List<Jid> admins = new ArrayList<>();
        if (field != null) {
            for (String value : field.getValues()) {
                admins.add(Jid.valueOf(value, true));
            }
        }
        return admins;
    }

    /**
     * Gets the contact addresses (normally, room owner or owners).
     *
     * @param administrators The contact addresses.
     */
    public void setContacts(List<Jid> administrators) {
        DataForm.Field field = dataForm.findField(CONTACT_JID);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.JID_MULTI, CONTACT_JID);
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
    public String getDescription() {
        DataForm.Field field = dataForm.findField(DESCRIPTION);
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
    public void setDescription(String description) {
        DataForm.Field field = dataForm.findField(DESCRIPTION);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, DESCRIPTION);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(description);
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
        DataForm.Field field = dataForm.findField(LDAP_GROUP);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets an associated LDAP group that defines
     * room membership; this should be an LDAP
     * Distinguished Name according to an
     * implementation-specific or
     * deployment-specific definition of a
     * group.
     *
     * @param ldapGroup LDAP group.
     */
    public void setLdapGroup(String ldapGroup) {
        DataForm.Field field = dataForm.findField(LDAP_GROUP);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, LDAP_GROUP);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(ldapGroup);
    }

    /**
     * Gets an URL for archived discussion logs.
     *
     * @return The URL.
     */
    public URL getLogs() {
        DataForm.Field field = dataForm.findField(LOGS);
        if (field != null && !field.getValues().isEmpty()) {
            try {
                return new URL(field.getValues().get(0));
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Sets an URL for archived discussion logs.
     *
     * @param logs The URL.
     */
    public void setLogs(URL logs) {
        DataForm.Field field = dataForm.findField(LOGS);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, LOGS);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(logs.toString());
    }

    /**
     * Gets the current number of occupants in the room.
     *
     * @return The number of occupants.
     */
    public int getCurrentNumberOfOccupants() {
        return getAsInteger(OCCUPANTS);
    }

    /**
     * Sets the current number of occupants in the room.
     *
     * @param currentNumberOfOccupants The number of occupants.
     */
    public void setCurrentNumberOfOccupants(int currentNumberOfOccupants) {
        DataForm.Field field = getOrCreateField(DataForm.Field.Type.TEXT_SINGLE, OCCUPANTS);
        field.getValues().clear();
        field.getValues().add(Integer.toString(currentNumberOfOccupants));
    }

    /**
     * Gets the current discussion topic.
     *
     * @return The topic.
     */
    public String getSubject() {
        DataForm.Field field = dataForm.findField(SUBJECT);
        if (field != null && !field.getValues().isEmpty()) {
            return field.getValues().get(0);
        }
        return null;
    }

    /**
     * Sets the current discussion topic.
     *
     * @param subject The topic.
     */
    public void setSubject(String subject) {
        DataForm.Field field = dataForm.findField(SUBJECT);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.TEXT_SINGLE, SUBJECT);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(subject);
    }


    /**
     * Indicates, whether the room subject can be modified by participants.
     *
     * @return Whether the room subject can be modified by participants.
     */
    public boolean isChangeSubjectAllowed() {
        DataForm.Field field = dataForm.findField(SUBJECT_MOD);
        return field != null && !field.getValues().isEmpty() && DataForm.parseBoolean(field.getValues().get(0));
    }

    /**
     * Indicates, whether the room subject can be modified by participants.
     *
     * @param changeSubjectAllowed Whether the room subject can be modified by participants.
     */
    public void setChangeSubjectAllowed(boolean changeSubjectAllowed) {
        DataForm.Field field = dataForm.findField(SUBJECT_MOD);
        if (field == null) {
            field = new DataForm.Field(DataForm.Field.Type.BOOLEAN, SUBJECT_MOD);
            dataForm.getFields().add(field);
        }
        field.getValues().clear();
        field.getValues().add(changeSubjectAllowed ? "1" : "0");
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

}
