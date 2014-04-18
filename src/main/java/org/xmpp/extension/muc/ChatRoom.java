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
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.muc.admin.MucAdmin;
import org.xmpp.extension.muc.conference.DirectInvitation;
import org.xmpp.extension.muc.owner.MucOwner;
import org.xmpp.extension.muc.user.Invite;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.extension.muc.user.Status;
import org.xmpp.stanza.*;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final Set<OccupantListener> occupantListeners = new CopyOnWriteArraySet<>();

    private final Set<MessageListener> messageListeners = new CopyOnWriteArraySet<>();

    private final Map<String, Occupant> occupantMap = new HashMap<>();

    private String name;

    private Jid roomJid;

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

    private volatile String nick;

    public ChatRoom() {
    }

    public ChatRoom(String name, final Jid roomJid) {
        this.name = name;
        this.roomJid = roomJid;

        connection.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming()) {
                    Message message = e.getMessage();
                    if (message.getFrom().asBareJid().equals(roomJid)) {
                        // This is a <message/> stanza from the room JID (or from the occupant JID of the entity that set the subject), with a <subject/> element but no <body/> element
                        if (message.getSubject() != null && message.getBody() == null) {
                            Occupant occupant = occupantMap.get(message.getFrom().getResource());
                            notifySubjectChangeListeners(new SubjectChangeEvent(ChatRoom.this, message.getSubject(), occupant));
                        } else {
                            notifyMessageListeners(new MessageEvent(ChatRoom.this, message, true));
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
                    // If the presence came from the room.
                    if (presence.getFrom().asBareJid().equals(roomJid)) {
                        MucUser mucUser = presence.getExtension(MucUser.class);
                        if (mucUser != null) {
                            String nick = presence.getFrom().getResource();
                            if (nick != null) {
                                if (presence.isAvailable()) {
                                    Occupant occupant = occupantMap.get(nick);
                                    // A new occupant entered the room.
                                    if (occupant == null) {
                                        occupant = new Occupant(presence);
                                        occupantMap.put(nick, occupant);
                                        notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, true, false));
                                    } else {

                                    }
                                } else if (presence.getType() == Presence.Type.UNAVAILABLE) {
                                    Occupant occupant = occupantMap.remove(nick);
                                    if (occupant != null) {
                                        notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, false, true));
                                    }

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
            }
        });
    }

    private void notifyOccupantListeners(OccupantEvent occupantEvent) {
        for (OccupantListener occupantListener : occupantListeners) {
            try {
                occupantListener.occupantChanged(occupantEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
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

    private void notifyMessageListeners(MessageEvent messageEvent) {
        for (MessageListener messageListener : messageListeners) {
            try {
                messageListener.handle(messageEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    @Override
    public void join(String nick) throws XmppException {
        join(nick, null, null);
    }

    @Override
    public void join(String nick, String password) throws XmppException {
        join(nick, password, null);
    }

    @Override
    public void join(String nick, History history) throws XmppException {
        join(nick, null, history);
    }

    @Override
    public void join(String nick, String password, History history) throws XmppException {
        Presence presence = new Presence();
        presence.setTo(roomJid.withResource(nick));
        presence.getExtensions().add(new Muc(password, history));
        this.nick = nick;

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
        Message message = new Message(roomJid, Message.Type.GROUPCHAT);
        message.setSubject(subject);
        connection.send(message);
    }

    @Override
    public void sendMessage(String message) {
        Message m = new Message(roomJid, Message.Type.GROUPCHAT);
        m.setBody(message);
        connection.send(m);
    }

    @Override
    public void sendMessage(Message message) {
        message.setType(Message.Type.GROUPCHAT);
        message.setTo(roomJid);
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
        presence.setTo(roomJid.withResource(newNickname));
        connection.send(presence);
        // TODO: Wait for presence in order to check for error.
    }

    @Override
    public void changeAvailabilityStatus(Presence.Show show, String status) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void invite(Jid invitee, String reason) {
        invite(invitee, reason, false);
    }

    private void invite(Jid invitee, String reason, boolean direct) {
        Message message;
        if (direct) {
            message = new Message(invitee, Message.Type.NORMAL);
            message.getExtensions().add(new DirectInvitation(roomJid, null, reason));
        } else {
            message = new Message(roomJid, Message.Type.NORMAL);
            message.getExtensions().add(new MucUser(new Invite(invitee, reason)));
        }
        connection.send(message);
    }

    @Override
    public DataForm getRegistrationForm() throws XmppException {
        return null;
    }

    @Override
    public void submitRegistrationForm() throws XmppException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets your reserved room nickname.
     *
     * @return The reserved nickname or null, if you don't have a reserved nickname.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public String getReservedNickname() throws XmppException {
        ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(roomJid, "x-roomuser-item");
        if (infoNode != null) {
            for (Identity identity : infoNode.getIdentities()) {
                if ("conference".equals(identity.getCategory()) && "text".equals(identity.getType())) {
                    return identity.getName();
                }
            }
        }
        return null;
    }

    @Override
    public void requestVoice() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Exits the room.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#exit">7.14 Exiting a Room</a>
     */
    @Override
    public void exit() {
        exit(null);
    }

    /**
     * Exits the room with a custom message.
     *
     * @param message The exit message.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#exit">7.14 Exiting a Room</a>
     */
    @Override
    public void exit(String message) {
        Presence presence = new Presence(Presence.Type.UNAVAILABLE);
        presence.setTo(roomJid);
        presence.setStatus(message);
        connection.send(presence);
    }

    /**
     * Kicks an occupant. Note that you need to be a moderator in order to kick an occupant.
     *
     * @param nickname The occupant's nickname to kick.
     * @param reason   The reason for the kick.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#kick">8.2 Kicking an Occupant</a>
     */
    @Override
    public void kickOccupant(String nickname, String reason) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(nickname, Role.NONE, reason)));
    }

    /**
     * Grants voice to a visitor in a moderated room.
     *
     * @param nickname The occupant's nickname.
     * @param reason   The reason.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantvoice">8.3 Granting Voice to a Visitor</a>
     */
    public void grantVoice(String nickname, String reason) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(nickname, Role.PARTICIPANT, reason)));
    }

    /**
     * Revokes voice from a participant.
     *
     * @param nickname The occupant's nickname.
     * @param reason   The reason.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokevoice">8.4 Revoking Voice from a Participant</a>
     */
    public void revokeVoice(String nickname, String reason) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(nickname, Role.VISITOR, reason)));
    }

    public List<? extends Item> getVoiceList() throws XmppException {
        IQ result = connection.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(null, Role.PARTICIPANT, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    public void modifyVoiceList(List<Item> items) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItems(items)));
    }

    @Override
    public void banUser(Jid user, String reason) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(user, Affiliation.OUTCAST, reason)));
    }

    /**
     * Gets the ban list.
     *
     * @return The ban list.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyban">9.2 Modifying the Ban List</a>
     */
    public List<? extends Item> getBanList() throws XmppException {
        IQ result = connection.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(null, Affiliation.OUTCAST, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    public void modifyBanList(List<Item> items) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItems(items)));
    }

    /**
     * Grants membership to a user.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantmember">9.3 Granting Membership</a>
     */
    public void grantMembership(Jid user, String reason) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(user, Affiliation.MEMBER, reason)));
    }

    /**
     * Revokes a user's membership.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokemember">9.4 Revoking Membership</a>
     */
    public void revokeMembership(Jid user, String reason) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(user, Affiliation.NONE, reason)));
    }

    /**
     * Gets the members of the room.
     * <p>
     * In the context of a members-only room, the member list is essentially a "whitelist" of people who are allowed to enter the room.
     * </p>
     * <p>
     * In the context of an open room, the member list is simply a list of users (bare JID and reserved nick) who are registered with the room.
     * </p>
     *
     * @return The members.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymember">9.5 Modifying the Member List</a>
     */
    public List<? extends Item> getMembers() throws XmppException {
        IQ result = connection.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(null, Affiliation.MEMBER, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    public void modifyMemberList(List<Item> items) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItems(items)));
    }

    /**
     * Grants moderator status to an user.
     *
     * @param nick   The nick.
     * @param reason The reason.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantmod">9.6 Granting Moderator Status</a>
     */
    public void grantModeratorStatus(String nick, String reason) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(nick, Role.MODERATOR, reason)));
    }

    /**
     * Revokes a user's moderator status.
     *
     * @param nick   The nick.
     * @param reason The reason.
     * @throws XmppException
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokemod">9.7 Revoking Moderator Status</a>
     */
    public void revokeModeratorStatus(String nick, String reason) throws XmppException {
        connection.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(nick, Role.PARTICIPANT, reason)));
    }

    /**
     * Gets the moderators.
     *
     * @return The moderators.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymod">9.8 Modifying the Moderator List</a>
     */
    public List<? extends Item> getModerators() throws XmppException {
        IQ result = connection.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(null, Role.MODERATOR, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    /**
     * Creates an instant room.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-instant">10.1.2 Creating an Instant Room</a>
     */
    public void createRoom(String room, String nick) throws XmppException {
        join(nick);
        connection.query(new IQ(roomJid, IQ.Type.SET, new MucOwner(new DataForm(DataForm.Type.SUBMIT))));
    }

    /**
     * Gets the configuration form for the room.
     */
    public DataForm getConfigurationForm() throws XmppException {
        IQ result = connection.query(new IQ(roomJid, IQ.Type.GET, new MucOwner()));
        MucOwner mucOwner = result.getExtension(MucOwner.class);
        return mucOwner.getConfigurationForm();
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
