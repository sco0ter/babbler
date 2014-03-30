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

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Schudt
 */
public class ChatRoom {

    private String name;

    private String description;

    private Set<MucFeature> features;

    private int maxHistory;

    private List<Jid> contacts;

    private String language;

    private String ldapGroup;

    private URL logs;

    private int currentNumberOfOccupants;

    private boolean changeSubjectAllowed;

    private String subject;

    public void setFeatures(Set<MucFeature> features) {
        this.features = features;
    }

    /**
     * Gets the maximum number of history messages returned by the room.
     *
     * @return Gets the maximum number of history messages returned by the room.
     */
    public int getMaxHistoryMessages() {
        // muc#maxhistoryfetch
        return maxHistory;
    }

    /**
     * Gets the contact addresses (normally, room owner or owners).
     *
     * @return The contact addresses.
     */
    public List<Jid> getContacts() {
        // muc#roominfo_contactjid
        return contacts;
    }

    public void setContacts(List<Jid> contacts) {
        this.contacts = contacts;
    }

    /**
     * Get a short description.
     *
     * @return The description.
     */
    public String getDescription() {
        // muc#roominfo_description
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the natural language for room discussions
     *
     * @return The language.
     */
    public String getLanguage() {
        // muc#roominfo_lang
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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
        // muc#roominfo_ldapgroup
        return ldapGroup;
    }

    public void setLdapGroup(String ldapGroup) {
        this.ldapGroup = ldapGroup;
    }

    /**
     * Gets an URL for archived discussion logs
     *
     * @return The URL.
     */
    public URL getLogs() {
        // muc#roominfo_logs
        return logs;
    }

    public void setLogs(URL logs) {
        this.logs = logs;
    }

    public int getCurrentNumberOfOccupants() {
        // muc#roominfo_occupants
        return currentNumberOfOccupants;
    }

    public void setCurrentNumberOfOccupants(int currentNumberOfOccupants) {
        this.currentNumberOfOccupants = currentNumberOfOccupants;
    }

    /**
     * Indicates, whether the room subject can be modified by participants.
     *
     * @return Whether the room subject can be modified by participants.
     */
    public boolean isChangeSubjectAllowed() {
        // muc#roominfo_subjectmod
        return changeSubjectAllowed;
    }

    public void setChangeSubjectAllowed(boolean changeSubjectAllowed) {
        this.changeSubjectAllowed = changeSubjectAllowed;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
          /*
    public boolean isPrivateMessagesAllowed() {

    }

    public boolean isEnableLogging() {

    }

    // getmemberlist

    public URI getPubSubUri() {

    }

    public boolean isMembersOnly() {

    }

    public boolean isPersistent() {

    }

    public boolean isPublic() {

    }

    public List<Jid> getAdmins() {

    }

    public List<Jid> getOwners() {

    }

    public String getPassword() {

    }
         */
}
