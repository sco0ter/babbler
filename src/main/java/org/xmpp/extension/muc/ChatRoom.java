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

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.extension.muc.user.Status;
import org.xmpp.stanza.*;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class ChatRoom implements MucRoom {

    private static final Logger logger = Logger.getLogger(ChatRoom.class.getName());

    private final Set<SubjectChangeListener> subjectChangeListeners = new CopyOnWriteArraySet<>();

    private String name;

    private Jid jid;

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

    private Connection connection;

    public ChatRoom() {
    }

    public ChatRoom(String name, final Jid jid) {
        this.name = name;
        this.jid = jid;

        connection.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming()) {
                    Message message = e.getMessage();
                    if (message.getFrom().asBareJid().equals(jid)) {

                        // This is a <message/> stanza from the room JID (or from the occupant JID of the entity that set the subject), with a <subject/> element but no <body/> element
                        if (message.getSubject() != null && message.getBody() == null) {
                            notifySubjectChangeListeners(new SubjectChangeEvent(ChatRoom.this, message.getSubject(), null));
                        }
                    }
                }
            }
        });

        connection.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                if (e.isIncoming()) {
                    Presence presence = e.getPresence();
                    if (presence.getFrom().asBareJid().equals(jid)) {
                        MucUser mucUser = presence.getExtension(MucUser.class);
                        if (mucUser != null) {
                            Occupant occupant = new Occupant(presence);
                            if (presence.isAvailable()) {
                                // enter
                            } else if (presence.getType() == Presence.Type.UNAVAILABLE) {

                                if (mucUser.getStatusCodes().contains(new Status(303))) {
                                    // nickname change
                                } else {
                                    // exit
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void notifySubjectChangeListeners(SubjectChangeEvent subjectChangeEvent) {
        for (SubjectChangeListener subjectChangeListener : subjectChangeListeners) {
            try {
                subjectChangeListener.subjectChanged(subjectChangeEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    @Override
    public void join(String room, String nick) throws XmppException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void join(String room, String nick, String password) throws XmppException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void join(String room, String nick, History history) throws XmppException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void join(String room, String nick, String password, History history) throws XmppException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Adds a subject change listener, which allows to listen for subject changes.
     *
     * @param subjectChangeListener The listener.
     * @see #removeSubjectChangeListener(SubjectChangeListener)
     */
    public void addSubjectChangeListener(SubjectChangeListener subjectChangeListener) {
        subjectChangeListeners.add(subjectChangeListener);
    }

    /**
     * Removes a previously added subject change listener.
     *
     * @param subjectChangeListener The listener.
     * @see #addSubjectChangeListener(SubjectChangeListener)
     */
    public void removeSubjectChangeListener(SubjectChangeListener subjectChangeListener) {
        subjectChangeListeners.remove(subjectChangeListener);
    }

    @Override
    public void changeSubject(String subject) {
        Message message = new Message(jid, Message.Type.GROUPCHAT);
        message.setSubject(subject);
        connection.send(message);
    }

    @Override
    public void sendMessage(String message) {
        Message m = new Message(jid, Message.Type.GROUPCHAT);
        m.setBody(message);
        connection.send(m);
    }

    @Override
    public void sendMessage(Message message) {
        message.setType(Message.Type.GROUPCHAT);
        message.setTo(jid);
        connection.send(message);
    }

    @Override
    public void sendPrivateMessage(String message, String nick) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void sendPrivateMessage(Message message, String nick) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void changeNickname(String newNickname) throws XmppException {
        Presence presence = new Presence();
        presence.setTo(jid.withResource(newNickname));
        connection.send(presence);
        // TODO: Wait for presence in order to check for error.
    }

    @Override
    public void changeAvailabilityStatus(Presence.Show show, String status) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void invite(Jid invitee, String reason) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DataForm getRegistrationForm() throws XmppException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void submitRegistrationForm() throws XmppException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void requestVoice() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void leave() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void kickOccupant(String nickname, String reason) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void banUser(Jid user, String reason) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

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
