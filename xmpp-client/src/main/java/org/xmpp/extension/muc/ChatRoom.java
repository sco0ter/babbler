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

import org.xmpp.*;
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
import org.xmpp.extension.muc.user.Decline;
import org.xmpp.extension.muc.user.Invite;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.extension.muc.user.Status;
import org.xmpp.extension.register.Registration;
import org.xmpp.stanza.*;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.client.Message;
import org.xmpp.stanza.client.Presence;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a chat room.
 *
 * @author Christian Schudt
 */
public final class ChatRoom {

    private static final Logger logger = Logger.getLogger(ChatRoom.class.getName());

    private final Set<InvitationDeclineListener> invitationDeclineListeners = new CopyOnWriteArraySet<>();

    private final Set<SubjectChangeListener> subjectChangeListeners = new CopyOnWriteArraySet<>();

    private final Set<OccupantListener> occupantListeners = new CopyOnWriteArraySet<>();

    private final Set<MessageListener> messageListeners = new CopyOnWriteArraySet<>();

    private final Map<String, Occupant> occupantMap = new HashMap<>();

    private final MessageListener messageListener;

    private final PresenceListener presenceListener;

    private ServiceDiscoveryManager serviceDiscoveryManager;

    private String name;

    private Jid roomJid;

    private XmppSession xmppSession;

    private volatile String nick;

    private volatile boolean entered;

    ChatRoom(String name, final Jid roomJid, XmppSession xmppSession) {
        this.name = name;
        this.roomJid = roomJid;
        this.xmppSession = xmppSession;
        this.serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);

        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    invitationDeclineListeners.clear();
                    subjectChangeListeners.clear();
                    occupantListeners.clear();
                    messageListeners.clear();
                    occupantMap.clear();
                }
            }
        });

        messageListener = new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                // Do not synchronize on ChatRoom.this, but on the messageListener instead, in order to not block the enter method, but to keep the incoming messages in order.
                synchronized (messageListener) {
                    if (e.isIncoming()) {
                        Message message = e.getMessage();
                        if (message.getFrom().asBareJid().equals(roomJid)) {
                            if (message.getType() == AbstractMessage.Type.GROUPCHAT) {
                                // This is a <message/> stanza from the room JID (or from the occupant JID of the entity that set the subject), with a <subject/> element but no <body/> element
                                if (message.getSubject() != null && message.getBody() == null) {
                                    Date date;
                                    DelayedDelivery delayedDelivery = message.getExtension(DelayedDelivery.class);
                                    if (delayedDelivery != null) {
                                        date = delayedDelivery.getTimeStamp();
                                    } else {
                                        date = new Date();
                                    }
                                    notifySubjectChangeListeners(new SubjectChangeEvent(ChatRoom.this, message.getSubject(), message.getFrom().getResource(), delayedDelivery != null, date));
                                } else {
                                    notifyMessageListeners(new MessageEvent(ChatRoom.this, message, true));
                                }
                            } else {
                                MucUser mucUser = message.getExtension(MucUser.class);
                                if (mucUser != null) {
                                    Decline decline = mucUser.getDecline();
                                    if (decline != null) {
                                        notifyInvitationDeclineListeners(new InvitationDeclineEvent(ChatRoom.this, roomJid, decline.getFrom(), decline.getReason()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };

        presenceListener = new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                Presence presence = e.getPresence();
                // If the presence came from the room.
                if (presence.getFrom() != null && presence.getFrom().asBareJid().equals(roomJid)) {
                    if (e.isIncoming()) {
                        MucUser mucUser = presence.getExtension(MucUser.class);
                        if (mucUser != null) {
                            String nick = presence.getFrom().getResource();

                            if (nick != null) {
                                boolean isSelfPresence = isSelfPresence(presence);
                                if (presence.isAvailable()) {
                                    Occupant occupant = new Occupant(presence, isSelfPresence);
                                    Occupant previousOccupant = occupantMap.put(nick, occupant);
                                    // A new occupant entered the room.
                                    if (previousOccupant == null) {
                                        // Only notify about "joins", if it's not our own join and we are already in the room.
                                        if (!isSelfPresence && entered) {
                                            notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.ENTERED, null, null, null));
                                        }
                                    } else {
                                        notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.STATUS_CHANGED, null, null, null));
                                    }
                                } else if (presence.getType() == Presence.Type.UNAVAILABLE) {
                                    // Occupant has exited the room.
                                    Occupant occupant = occupantMap.remove(nick);
                                    if (occupant != null) {
                                        if (mucUser.getItem() != null) {
                                            Actor actor = mucUser.getItem().getActor();
                                            String reason = mucUser.getItem().getReason();
                                            if (!mucUser.getStatusCodes().isEmpty()) {
                                                if (mucUser.getStatusCodes().contains(Status.kicked())) {
                                                    notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.KICKED, actor, reason, null));
                                                } else if (mucUser.getStatusCodes().contains(Status.banned())) {
                                                    notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.BANNED, actor, reason, null));
                                                } else if (mucUser.getStatusCodes().contains(Status.membershipRevoked())) {
                                                    notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.MEMBERSHIP_REVOKED, actor, reason, null));
                                                } else if (mucUser.getStatusCodes().contains(Status.nicknameChanged())) {
                                                    notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.NICKNAME_CHANGED, actor, reason, null));
                                                } else if (mucUser.getStatusCodes().contains(Status.systemShutdown())) {
                                                    notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.SYSTEM_SHUTDOWN, actor, reason, null));
                                                }
                                            } else if (mucUser.getDestroy() != null) {
                                                notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.ROOM_DESTROYED, actor, mucUser.getDestroy().getReason(), mucUser.getDestroy().getJid()));
                                            } else {
                                                notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.EXITED, null, null, null));
                                            }
                                        } else {
                                            notifyOccupantListeners(new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.EXITED, null, null, null));
                                        }
                                    }
                                    if (isSelfPresence) {
                                        userHasExited();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private void userHasExited() {
        xmppSession.removeMessageListener(messageListener);
        xmppSession.removePresenceListener(presenceListener);
    }

    private void notifyInvitationDeclineListeners(InvitationDeclineEvent invitationDeclineEvent) {
        for (InvitationDeclineListener invitationDeclineListener : invitationDeclineListeners) {
            try {
                invitationDeclineListener.invitationDeclined(invitationDeclineEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
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

    private boolean isSelfPresence(Presence presence) {
        boolean isSelfPresence = false;
        MucUser mucUser = presence.getExtension(MucUser.class);
        if (mucUser != null) {
            // If the presence is self-presence (110) or if the service assigned another nickname (210) to the user (but didn't include 110).
            isSelfPresence = mucUser.getStatusCodes().contains(Status.self()) || mucUser.getStatusCodes().contains(Status.serviceHasAssignedOrModifiedNick());
        }
        return isSelfPresence || nick != null && presence.getFrom() != null && nick.equals(presence.getFrom().getResource());
    }

    /**
     * Adds a invitation decline listener, which allows to listen for invitation declines.
     *
     * @param invitationDeclineListener The listener.
     * @see #removeInvitationDeclineListener(InvitationDeclineListener)
     */
    public void addInvitationDeclineListener(InvitationDeclineListener invitationDeclineListener) {
        invitationDeclineListeners.add(invitationDeclineListener);
    }

    /**
     * Removes a previously added invitation decline listener.
     *
     * @param invitationDeclineListener The listener.
     * @see #addInvitationDeclineListener(InvitationDeclineListener)
     */
    public void removeInvitationDeclineListener(InvitationDeclineListener invitationDeclineListener) {
        invitationDeclineListeners.remove(invitationDeclineListener);
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

    /**
     * Enters the room.
     *
     * @param nick The nickname.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void enter(String nick) throws XmppException {
        enter(nick, null, null);
    }

    /**
     * Enters the room with a password.
     *
     * @param nick     The nickname.
     * @param password The password.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void enter(String nick, String password) throws XmppException {
        enter(nick, password, null);
    }

    /**
     * Enters the room and requests history messages.
     *
     * @param nick    The nickname.
     * @param history The history.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void enter(String nick, History history) throws XmppException {
        enter(nick, null, history);
    }

    /**
     * Enters the room with a password and requests history messages.
     *
     * @param nick     The nickname.
     * @param password The password.
     * @param history  The history.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public synchronized void enter(final String nick, String password, History history) throws XmppException {
        if (nick == null) {
            throw new IllegalArgumentException("nick must not be null.");
        }

        if (entered) {
            throw new IllegalStateException("You already entered this room.");
        }

        try {
            xmppSession.addMessageListener(messageListener);
            xmppSession.addPresenceListener(presenceListener);

            final Presence enterPresence = new Presence();
            enterPresence.setTo(roomJid.withResource(nick));
            enterPresence.getExtensions().add(new Muc(password, history));
            this.nick = nick;
            xmppSession.sendAndAwait(enterPresence, new StanzaFilter<Presence>() {
                @Override
                public boolean accept(Presence presence) {
                    Jid room = presence.getFrom().asBareJid();
                    return room.equals(roomJid) && isSelfPresence(presence);
                }
            });
        } catch (XmppException e) {
            xmppSession.removeMessageListener(messageListener);
            xmppSession.removePresenceListener(presenceListener);
            throw e;
        }
        entered = true;
    }

    /**
     * Changes the room subject.
     *
     * @param subject The subject.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void changeSubject(final String subject) throws XmppException {
        Message message = new Message(roomJid, Message.Type.GROUPCHAT);
        message.setSubject(subject);
        xmppSession.sendAndAwait(message, new StanzaFilter<Message>() {
            @Override
            public boolean accept(Message message) {
                return message.getSubject() != null && message.getSubject().equals(subject);
            }
        });
    }

    /**
     * Sends a message to the room.
     *
     * @param message The message text.
     */
    public void sendMessage(String message) {
        Message m = new Message(roomJid, Message.Type.GROUPCHAT);
        m.setBody(message);
        xmppSession.send(m);
    }

    /**
     * Sends a message to the room.
     *
     * @param message The message.
     */
    public void sendMessage(Message message) {
        message.setType(Message.Type.GROUPCHAT);
        message.setTo(roomJid);
        xmppSession.send(message);
    }

    /**
     * Changes the nickname.
     *
     * @param newNickname The new nickname.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#changenick">7.6 Changing Nickname</a>
     */
    public synchronized void changeNickname(String newNickname) throws XmppException {
        if (!entered) {
            throw new IllegalStateException("You must have entered the room to change your nickname.");
        }
        final Presence changeNickNamePresence = new Presence();
        changeNickNamePresence.setTo(roomJid.withResource(newNickname));
        xmppSession.sendAndAwait(changeNickNamePresence, new StanzaFilter<Presence>() {
            @Override
            public boolean accept(Presence presence) {
                return presence.getFrom().equals(changeNickNamePresence.getTo());
            }
        });
    }

    /**
     * Changes the availability status.
     *
     * @param show   The 'show' value.
     * @param status The status.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#changepres">7.7 Changing Availability Status</a>
     */
    public synchronized void changeAvailabilityStatus(Presence.Show show, String status) {
        if (!entered) {
            throw new IllegalStateException("You must have entered the room to change the availability status.");
        }
        Presence presence = new Presence();
        presence.setTo(roomJid.withResource(nick));
        presence.setShow(show);
        presence.setStatus(status);
        xmppSession.send(presence);
    }

    /**
     * Invites another user to the room. The invitation will be mediated by the room.
     *
     * @param invitee The invitee.
     * @param reason  The reason.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#invite">7.8 Inviting Another User to a Room</a>
     */
    public void invite(Jid invitee, String reason) {
        invite(invitee, reason, false);
    }

    /**
     * @param invitee The invitee.
     * @param reason  The reason.
     * @param direct  True, if the message is sent directly to the invitee; false if it is mediated by the room.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#invite-direct">7.8.1 Direct Invitation</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#invite-mediated">7.8.2 Mediated Invitation</a>
     */
    public void invite(Jid invitee, String reason, boolean direct) {
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

    /**
     * Gets the data form necessary to register with the room.
     *
     * @return The data form.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#register">7.10 Registering with a Room</a>
     * @see org.xmpp.extension.muc.RoomRegistrationForm
     */
    public DataForm getRegistrationForm() throws XmppException {
        IQ iq = new IQ(roomJid, IQ.Type.GET, new Registration());
        IQ result = xmppSession.query(iq);
        Registration registration = result.getExtension(Registration.class);
        if (registration != null) {
            return registration.getRegistrationForm();
        }
        return null;
    }

    /**
     * Submits the registration form.
     *
     * @param dataForm The data form.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#register">7.10 Registering with a Room</a>
     * @see org.xmpp.extension.muc.RoomRegistrationForm
     */
    public void submitRegistrationForm(DataForm dataForm) throws XmppException {
        if (dataForm == null) {
            throw new IllegalArgumentException("dataForm must not be null.");
        }
        if (dataForm.getType() != DataForm.Type.SUBMIT) {
            throw new IllegalArgumentException("Data Form must be of type 'submit'");
        }
        if (!"http://jabber.org/protocol/muc#register".equals(dataForm.getFormType())) {
            throw new IllegalArgumentException("Data Form is not of type 'http://jabber.org/protocol/muc#register'");
        }
        Registration registration = new Registration(dataForm);
        IQ iq = new IQ(roomJid, IQ.Type.SET, registration);
        xmppSession.query(iq);
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

    /**
     * Requests voice in a moderated room.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#requestvoice">7.13 Requesting Voice</a>
     */
    public void requestVoice() {
        Message message = new Message(roomJid);
        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT);
        RequestVoiceForm requestVoiceForm = new RequestVoiceForm(dataForm);
        requestVoiceForm.setRole(Role.PARTICIPANT);
        message.getExtensions().add(dataForm);
        xmppSession.send(message);
    }

    /**
     * Exits the room.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#exit">7.14 Exiting a Room</a>
     */
    public void exit() {
        exit(null);
    }

    /**
     * Exits the room with a custom message.
     *
     * @param message The exit message.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#exit">7.14 Exiting a Room</a>
     */
    public synchronized void exit(String message) {

        if (!entered) {
            throw new IllegalStateException("You can't exit a room, when you didn't enter it.");
        }
        Presence presence = new Presence(Presence.Type.UNAVAILABLE);
        presence.setTo(roomJid.withResource(nick));
        presence.setStatus(message);
        xmppSession.send(presence);
        userHasExited();

        nick = null;
        entered = false;
        occupantMap.clear();
    }

    /**
     * Gets the voice list.
     *
     * @return The voice list.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyvoice">8.5 Modifying the Voice List</a>
     */
    public List<? extends Item> getVoiceList() throws XmppException {
        IQ result = xmppSession.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(Role.PARTICIPANT, null, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    /**
     * Changes multiple affiliations or roles.
     *
     * @param items The items.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyvoice">8.5 Modifying the Voice List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymember">9.5 Modifying the Member List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymod">9.8 Modifying the Moderator List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyowner">10.5 Modifying the Owner List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyadmin">10.8 Modifying the Admin List</a>
     */
    public void changeAffiliationsOrRoles(List<Item> items) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItems(items)));
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

    /**
     * Changes the affiliation for an user.
     * <p>
     * Use this method for one of the following use cases:
     * </p>
     * <ul>
     * <li>Banning a User (affiliation = {@link org.xmpp.extension.muc.Affiliation#OUTCAST})</li>
     * <li>Granting Membership (affiliation = {@link org.xmpp.extension.muc.Affiliation#MEMBER})</li>
     * <li>Revoking Membership (affiliation = {@link org.xmpp.extension.muc.Affiliation#NONE})</li>
     * <li>Granting Admin Status (affiliation = {@link org.xmpp.extension.muc.Affiliation#ADMIN})</li>
     * <li>Revoking Admin Status (affiliation = {@link org.xmpp.extension.muc.Affiliation#MEMBER})</li>
     * <li>Granting Owner Status (affiliation = {@link org.xmpp.extension.muc.Affiliation#OWNER})</li>
     * <li>Revoking Owner Status (affiliation = {@link org.xmpp.extension.muc.Affiliation#ADMIN})</li>
     * </ul>
     *
     * @param affiliation The new affiliation for the user.
     * @param user        The user.
     * @param reason      The reason.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#ban">9.1 Banning a User</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantmember">9.3 Granting Membership</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokemember">9.4 Revoking Membership</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantowner">10.3 Granting Owner Status</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokeowner">10.4 Revoking Owner Status</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantadmin">10.6 Granting Admin Status</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokeadmin">10.7 Revoking Admin Status</a>
     */
    public void changeAffiliation(Affiliation affiliation, Jid user, String reason) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(affiliation, user, reason)));
    }

    /**
     * Changes the role for an occupant.
     * <p>
     * Use this method for one of the following use cases:
     * </p>
     * <ul>
     * <li>Kicking an Occupant (role = {@link org.xmpp.extension.muc.Role#NONE})</li>
     * <li>Granting Voice to a Visitor (role = {@link org.xmpp.extension.muc.Role#PARTICIPANT})</li>
     * <li>Revoking Voice from a Participant (affiliation = {@link org.xmpp.extension.muc.Role#VISITOR})</li>
     * <li>Granting Moderator Status (role = {@link org.xmpp.extension.muc.Role#MODERATOR})</li>
     * <li>Revoking Moderator Status (role = {@link org.xmpp.extension.muc.Role#PARTICIPANT})</li>
     * </ul>
     *
     * @param role     The new role for the user.
     * @param nickname The occupant's nickname.
     * @param reason   The reason.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#kick">8.2 Kicking an Occupant</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantvoice">8.3 Granting Voice to a Visitor</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokevoice">8.4 Revoking Voice from a Participant</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantmod">9.6 Granting Moderator Status</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokemod">9.7 Revoking Moderator Status</a>
     */
    public void changeRole(Role role, String nickname, String reason) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItem(role, nickname, reason)));
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
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-instant">10.1.2 Creating an Instant Room</a>
     */
    public void createRoom() throws XmppException {
        enter(nick);
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, new MucOwner(new DataForm(DataForm.Type.SUBMIT))));
    }

    /**
     * Gets the room information for this chat room.
     *
     * @return The room info.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
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
     * Gets the occupants in this room, i.e. their nicknames. This method should be used, when you are not yet in the room.
     *
     * @return The occupants.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-roomitems">6.5 Querying for Room Items</a>
     * @see #getOccupants()
     */
    public List<String> discoverOccupants() throws XmppException {
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
     * Gets the occupants, while being in the room.
     *
     * @return The occupants.
     */
    public Collection<Occupant> getOccupants() {
        return occupantMap.values();
    }

    /**
     * Gets an occupant by nickname.
     *
     * @param nickname The occupant's nickname.
     * @return The occupant.
     */
    public Occupant getOccupant(String nickname) {
        return occupantMap.get(nickname);
    }

    /**
     * Gets the configuration form for the room.
     * You can wrap the form into {@link org.xmpp.extension.muc.RoomConfigurationForm} for easier processing.
     * <p>
     * Use this method if you want to create a reserved room or configure an existing room.
     * </p>
     *
     * @return The configuration form.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see org.xmpp.extension.muc.RoomConfigurationForm
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-reserved">10.1.3 Creating a Reserved Room</a>
     * @see #submitConfigurationForm(org.xmpp.extension.data.DataForm)
     */
    public DataForm getConfigurationForm() throws XmppException {
        IQ result = xmppSession.query(new IQ(roomJid, IQ.Type.GET, new MucOwner()));
        MucOwner mucOwner = result.getExtension(MucOwner.class);
        return mucOwner.getConfigurationForm();
    }

    /**
     * Submits the configuration form for this room.
     *
     * @param dataForm The data form.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-reserved">10.1.3 Creating a Reserved Room</a>
     * @see #getConfigurationForm()
     */
    public void submitConfigurationForm(DataForm dataForm) throws XmppException {
        if (dataForm == null) {
            throw new IllegalArgumentException("dataForm must not be null.");
        }
        if (dataForm.getType() != DataForm.Type.SUBMIT && dataForm.getType() != DataForm.Type.CANCEL) {
            throw new IllegalArgumentException("Data Form must be of type 'submit' or 'cancel'");
        }
        if (!"http://jabber.org/protocol/muc#roomconfig".equals(dataForm.getFormType())) {
            throw new IllegalArgumentException("Data Form is not of type 'http://jabber.org/protocol/muc#roomconfig'");
        }
        MucOwner mucOwner = new MucOwner(dataForm);
        IQ iq = new IQ(roomJid, IQ.Type.SET, mucOwner);
        xmppSession.query(iq);
    }

    /**
     * Gets the name for this room.
     *
     * @return The room name.
     */
    public String getName() {
        return name;
    }

    /**
     * Destroys the room.
     *
     * @param reason The reason for the room destruction.
     * @throws StanzaException     If the chat service returned a stanza error.
     * @throws NoResponseException If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#destroyroom">10.9 Destroying a Room</a>
     */
    public void destroy(String reason) throws XmppException {
        MucOwner mucOwner = MucOwner.withDestroy(roomJid, reason);
        IQ iq = new IQ(roomJid, IQ.Type.SET, mucOwner);
        xmppSession.query(iq);
    }
}
