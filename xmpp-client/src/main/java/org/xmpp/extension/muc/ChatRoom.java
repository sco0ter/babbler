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
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.delay.DelayedDelivery;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.extension.muc.admin.MucAdmin;
import org.xmpp.extension.muc.conference.DirectInvitation;
import org.xmpp.extension.muc.owner.MucOwner;
import org.xmpp.extension.muc.user.Invite;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.extension.muc.user.Status;
import org.xmpp.stanza.*;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.client.Message;
import org.xmpp.stanza.client.Presence;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class ChatRoom implements MucRoom {

    private static final Logger logger = Logger.getLogger(ChatRoom.class.getName());

    private final Set<SubjectChangeListener> subjectChangeListeners = new CopyOnWriteArraySet<>();

    private final Set<OccupantListener> occupantListeners = new CopyOnWriteArraySet<>();

    private final Set<OccupantExitListener> occupantExitListeners = new CopyOnWriteArraySet<>();

    private final Set<MessageListener> messageListeners = new CopyOnWriteArraySet<>();

    private final Map<String, Occupant> occupantMap = new HashMap<>();

    private ServiceDiscoveryManager serviceDiscoveryManager;

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

    private XmppSession xmppSession;

    private volatile String nick;

    private boolean entered;

    public ChatRoom() {
    }

    public ChatRoom(String name, final Jid roomJid, XmppSession xmppSession) {
        this.name = name;
        this.roomJid = roomJid;
        this.xmppSession = xmppSession;
        this.serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);

        xmppSession.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming()) {
                    Message message = e.getMessage();
                    if (message.getFrom().asBareJid().equals(roomJid)) {
                        // This is a <message/> stanza from the room JID (or from the occupant JID of the entity that set the subject), with a <subject/> element but no <body/> element
                        Occupant occupant = occupantMap.get(message.getFrom().getResource());
                        if (message.getSubject() != null && message.getBody() == null) {

                            Date date;
                            DelayedDelivery delayedDelivery = message.getExtension(DelayedDelivery.class);
                            if (delayedDelivery != null) {
                                date = delayedDelivery.getTimeStamp();
                            } else {
                                date = new Date();
                            }
                            notifySubjectChangeListeners(new SubjectChangeEvent(ChatRoom.this, message.getSubject(), occupant, delayedDelivery != null, date));
                        } else {
                            notifyMessageListeners(new MessageEvent(ChatRoom.this, message, true));
                        }
                    }
                }
            }
        });

        xmppSession.addPresenceListener(new PresenceListener() {
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
                                    Occupant occupant = new Occupant(presence);
                                    Occupant previousOccupant = occupantMap.put(nick, occupant);
                                    // A new occupant entered the room.
                                    if (previousOccupant == null) {
                                        // Only notify about "joins", if it's not our own join.
                                        if (!isSelfPresence(presence)) {
                                            notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, true, false));
                                        }
                                    } else {

                                    }
                                } else if (presence.getType() == Presence.Type.UNAVAILABLE) {
                                    boolean isSelfPresence = isSelfPresence(presence);
                                    // Occupant has exited the room.
                                    Occupant occupant = occupantMap.remove(nick);
                                    if (occupant != null) {
                                        if (!mucUser.getStatusCodes().isEmpty()) {
                                            if (mucUser.getStatusCodes().contains(Status.kicked())) {
                                                notifyOccupantExitListeners(new OccupantExitEvent(ChatRoom.this, occupant, OccupantExitEvent.Reason.KICKED, isSelfPresence));
                                            } else if (mucUser.getStatusCodes().contains(Status.banned())) {
                                                notifyOccupantExitListeners(new OccupantExitEvent(ChatRoom.this, occupant, OccupantExitEvent.Reason.BANNED, isSelfPresence));
                                            } else if (mucUser.getStatusCodes().contains(Status.membershipRevoked())) {
                                                notifyOccupantExitListeners(new OccupantExitEvent(ChatRoom.this, occupant, OccupantExitEvent.Reason.MEMBERSHIP_REVOKED, isSelfPresence));
                                            } else if (mucUser.getStatusCodes().contains(Status.nicknameChanged())) {
                                                notifyOccupantExitListeners(new OccupantExitEvent(ChatRoom.this, occupant, OccupantExitEvent.Reason.NICKNAME_CHANGED, isSelfPresence));
                                            } else if (mucUser.getStatusCodes().contains(Status.systemShutdown())) {
                                                notifyOccupantExitListeners(new OccupantExitEvent(ChatRoom.this, occupant, OccupantExitEvent.Reason.SYSTEM_SHUTDOWN, isSelfPresence));
                                            }
                                        } else {
                                            notifyOccupantExitListeners(new OccupantExitEvent(ChatRoom.this, occupant, OccupantExitEvent.Reason.NONE, isSelfPresence));
                                        }
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

    private void notifyOccupantExitListeners(OccupantExitEvent occupantExitEvent) {
        for (OccupantExitListener occupantExitListener : occupantExitListeners) {
            try {
                occupantExitListener.occupantExited(occupantExitEvent);
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

    private boolean isSelfPresence(Presence presence) {
        boolean isSelfPresence = false;
        MucUser mucUser = presence.getExtension(MucUser.class);
        if (mucUser != null) {
            // If the presence is self-presence (110) or if the service assigned another nickname (210) to the user (but didn't include 110).
            isSelfPresence = mucUser.getStatusCodes().contains(Status.self()) || mucUser.getStatusCodes().contains(Status.serviceHasAssignedOrModifiedNick());
        }
        return isSelfPresence || nick != null && presence.getFrom() != null && nick.equals(presence.getFrom().getResource());
    }

    @Override
    public void enter(String nick) throws XmppException {
        enter(nick, null, null);
    }

    @Override
    public void enter(String nick, String password) throws XmppException {
        enter(nick, password, null);
    }

    @Override
    public void enter(String nick, History history) throws XmppException {
        enter(nick, null, history);
    }

    @Override
    public synchronized void enter(final String nick, String password, History history) throws XmppException {
        if (nick == null) {
            throw new IllegalArgumentException("nick must not be null.");
        }

        final Presence enterPresence = new Presence();
        enterPresence.setTo(roomJid.withResource(nick));
        enterPresence.getExtensions().add(new Muc(password, history));
        this.nick = nick;
        xmppSession.sendAndAwait(enterPresence, new Predicate<Presence>() {
            @Override
            public boolean test(Presence presence) {
                Jid room = presence.getFrom().asBareJid();
                return room.equals(roomJid) && isSelfPresence(presence);
            }
        }, 5000);

        entered = true;
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

    /**
     * Adds a message listener, which allows to listen for incoming messages in this room.
     *
     * @param messageListener The listener.
     * @see #removeMessageListener(org.xmpp.stanza.MessageListener)
     */
    public void addMessageListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    /**
     * Removes a previously added message listener.
     *
     * @param messageListener The listener.
     * @see #addMessageListener(org.xmpp.stanza.MessageListener)
     */
    public void removeMessageListener(MessageListener messageListener) {
        messageListeners.remove(messageListener);
    }

    /**
     * Adds an occupant listener, which allows to listen for presence changes of occupants, e.g. "joins" and "leaves".
     *
     * @param occupantListener The listener.
     * @see #removeOccupantListener(OccupantListener)
     */
    public void addOccupantListener(OccupantListener occupantListener) {
        occupantListeners.add(occupantListener);
    }

    /**
     * Removes a previously added occupant listener.
     *
     * @param occupantListener The listener.
     * @see #addOccupantListener(OccupantListener)
     */
    public void removeOccupantListener(OccupantListener occupantListener) {
        occupantListeners.remove(occupantListener);
    }


    @Override
    public void changeSubject(String subject) {
        //        Message message = new Message(roomJid, Message.Type.GROUPCHAT);
        //        message.setSubject(subject);
        //        connection.send(message);
    }

    @Override
    public void sendMessage(String message) {
        //        Message m = new Message(roomJid, Message.Type.GROUPCHAT);
        //        m.setBody(message);
        //        connection.send(m);
    }

    @Override
    public void sendMessage(Message message) {
        message.setType(Message.Type.GROUPCHAT);
        message.setTo(roomJid);
        xmppSession.send(message);
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
        xmppSession.send(presence);
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
            message.getExtensions().add(MucUser.withInvites(new Invite(invitee, reason)));
        }
        xmppSession.send(message);
    }

    @Override
    public DataForm getRegistrationForm() throws XmppException {
        return null;
    }

    @Override
    public void submitRegistrationForm() throws XmppException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets your reserved room nickname.
     *
     * @return The reserved nickname or null, if you don't have a reserved nickname.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public String getReservedNickname() throws XmppException {
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
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
    public synchronized void exit(String message) {

        if (!entered) {
            throw new IllegalStateException("You can't exit a room, when you didn't enter it.");
        }
        Presence presence = new Presence(Presence.Type.UNAVAILABLE);
        presence.setTo(roomJid.withResource(nick));
        presence.setStatus(message);
        xmppSession.send(presence);
        nick = null;
        entered = false;
        occupantMap.clear();
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
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(Role.NONE, nickname, reason)));
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
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(Role.PARTICIPANT, nickname, reason)));
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
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(Role.VISITOR, nickname, reason)));
    }

    public List<? extends Item> getVoiceList() throws XmppException {
        IQ result = xmppSession.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(Role.PARTICIPANT, null, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    public void modifyVoiceList(List<Item> items) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItems(items)));
    }

    @Override
    public void banUser(Jid user, String reason) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(Affiliation.OUTCAST, user, reason)));
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
        IQ result = xmppSession.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(Affiliation.OUTCAST, null, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    public void modifyBanList(List<Item> items) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItems(items)));
    }

    /**
     * Grants membership to a user.
     *
     * @param user   The user.
     * @param reason The reason.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantmember">9.3 Granting Membership</a>
     */
    public void grantMembership(Jid user, String reason) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(Affiliation.MEMBER, user, reason)));
    }

    /**
     * Revokes a user's membership.
     *
     * @param user   The user.
     * @param reason The reason.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokemember">9.4 Revoking Membership</a>
     */
    public void revokeMembership(Jid user, String reason) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(Affiliation.NONE, user, reason)));
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
        IQ result = xmppSession.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(Affiliation.MEMBER, null, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    public void modifyMemberList(List<Item> items) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItems(items)));
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
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(Role.MODERATOR, nick, reason)));
    }

    /**
     * Revokes a user's moderator status.
     *
     * @param nick   The nick.
     * @param reason The reason.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokemod">9.7 Revoking Moderator Status</a>
     */
    public void revokeModeratorStatus(String nick, String reason) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(Role.PARTICIPANT, nick, reason)));
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
        IQ result = xmppSession.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(Role.MODERATOR, null, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    /**
     * Creates an instant room.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-instant">10.1.2 Creating an Instant Room</a>
     */
    public void createRoom(String room, String nick) throws XmppException {
        enter(nick);
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, new MucOwner(new DataForm(DataForm.Type.SUBMIT))));
    }

    /**
     * Gets the room information for this chat room.
     *
     * @return The room info.
     * @throws XmppException
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-roominfo">6.4 Querying for Room Information</a>
     */
    public RoomInfo getRoomInfo() throws XmppException {
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(roomJid);

        Identity identity = null;
        Set<MucFeature> mucFeatures = new HashSet<>();
        RoomInfoForm roomInfoForm = null;

        if (infoNode != null) {
            Set<Identity> identities = infoNode.getIdentities();
            Iterator<Identity> iterator = identities.iterator();
            if (iterator.hasNext()) {
                identity = iterator.next();
            }
            for (Feature feature : infoNode.getFeatures()) {
                for (MucFeature mucFeature : MucFeature.values()) {
                    if (mucFeature.getServiceDiscoveryFeature().equals(feature.getVar())) {
                        mucFeatures.add(mucFeature);
                    }
                }
            }

            for (DataForm dataForm : infoNode.getExtensions()) {
                DataForm.Field formType = dataForm.findField("FORM_TYPE");
                if (formType != null && !formType.getValues().isEmpty() && formType.getValues().get(0).equals("http://jabber.org/protocol/muc#roominfo")) {
                    roomInfoForm = new RoomInfoForm(dataForm);
                    break;
                }
            }
        }

        return new RoomInfo(identity, mucFeatures, roomInfoForm);
    }

    /**
     * Gets the occupants in this room, i.e. their nicknames.
     *
     * @return The occupants.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-roomitems">6.5 Querying for Room Items</a>
     */
    public List<String> getOccupants() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(roomJid);
        List<String> occupants = new ArrayList<>();
        List<org.xmpp.extension.disco.items.Item> items = itemNode.getItems();
        for (org.xmpp.extension.disco.items.Item item : items) {
            if (item.getJid() != null) {
                String nickname = item.getJid().getResource();
                if (nickname != null) {
                    occupants.add(nickname);
                }
            }
        }
        return occupants;
    }

    /**
     * Gets the configuration form for the room.
     */
    public DataForm getConfigurationForm() throws XmppException {
        IQ result = xmppSession.query(new IQ(roomJid, IQ.Type.GET, new MucOwner()));
        MucOwner mucOwner = result.getExtension(MucOwner.class);
        return mucOwner.getConfigurationForm();
    }
}
