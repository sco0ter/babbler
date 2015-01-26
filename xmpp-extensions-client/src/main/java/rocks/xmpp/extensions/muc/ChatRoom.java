/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Chat;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.PresenceListener;
import rocks.xmpp.core.stanza.StanzaFilter;
import rocks.xmpp.core.stanza.model.AbstractMessage;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.delay.model.DelayedDelivery;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.muc.conference.model.DirectInvitation;
import rocks.xmpp.extensions.muc.model.Actor;
import rocks.xmpp.extensions.muc.model.Affiliation;
import rocks.xmpp.extensions.muc.model.History;
import rocks.xmpp.extensions.muc.model.Muc;
import rocks.xmpp.extensions.muc.model.MucFeature;
import rocks.xmpp.extensions.muc.model.RequestVoice;
import rocks.xmpp.extensions.muc.model.Role;
import rocks.xmpp.extensions.muc.model.RoomConfiguration;
import rocks.xmpp.extensions.muc.model.RoomInfo;
import rocks.xmpp.extensions.muc.model.admin.MucAdmin;
import rocks.xmpp.extensions.muc.model.owner.MucOwner;
import rocks.xmpp.extensions.muc.model.user.Decline;
import rocks.xmpp.extensions.muc.model.user.Invite;
import rocks.xmpp.extensions.muc.model.user.MucUser;
import rocks.xmpp.extensions.muc.model.user.Status;
import rocks.xmpp.extensions.register.model.Registration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a chat room.
 *
 * @author Christian Schudt
 */
public final class ChatRoom extends Chat implements SessionStatusListener, MessageListener, PresenceListener, Comparable<ChatRoom> {

    private static final Logger logger = Logger.getLogger(ChatRoom.class.getName());

    private final Set<InvitationDeclineListener> invitationDeclineListeners = new CopyOnWriteArraySet<>();

    private final Set<SubjectChangeListener> subjectChangeListeners = new CopyOnWriteArraySet<>();

    private final Set<OccupantListener> occupantListeners = new CopyOnWriteArraySet<>();

    private final Map<String, Occupant> occupantMap = new HashMap<>();

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final MultiUserChatManager multiUserChatManager;

    private final String name;

    private final Jid roomJid;

    private final XmppSession xmppSession;

    private volatile String nick;

    private volatile boolean entered;

    ChatRoom(final Jid roomJid, String name, XmppSession xmppSession, ServiceDiscoveryManager serviceDiscoveryManager, MultiUserChatManager multiUserChatManager) {
        this.name = name;
        this.roomJid = roomJid;
        this.xmppSession = xmppSession;
        this.serviceDiscoveryManager = serviceDiscoveryManager;
        this.multiUserChatManager = multiUserChatManager;
    }

    void initialize() {
        xmppSession.addSessionStatusListener(this);
    }

    private void userHasExited() {
        xmppSession.removeMessageListener(this);
        xmppSession.removePresenceListener(this);
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public void enter(String nick) throws XmppException {
        enter(nick, null, null);
    }

    /**
     * Enters the room with a password.
     *
     * @param nick     The nickname.
     * @param password The password.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public void enter(String nick, String password) throws XmppException {
        enter(nick, password, null);
    }

    /**
     * Enters the room and requests history messages.
     *
     * @param nick    The nickname.
     * @param history The history.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public synchronized void enter(final String nick, String password, History history) throws XmppException {
        Objects.requireNonNull(nick, "nick must not be null.");

        if (entered) {
            throw new IllegalStateException("You already entered this room.");
        }

        try {
            xmppSession.addMessageListener(this);
            xmppSession.addPresenceListener(this);

            final Presence enterPresence = new Presence(roomJid.withResource(nick));
            enterPresence.getExtensions().add(new Muc(password, history));
            this.nick = nick;
            xmppSession.sendAndAwaitPresence(enterPresence, new StanzaFilter<Presence>() {
                @Override
                public boolean accept(Presence presence) {
                    Jid room = presence.getFrom().asBareJid();
                    return room.equals(roomJid) && isSelfPresence(presence);
                }
            });
        } catch (XmppException e) {
            xmppSession.removeMessageListener(this);
            xmppSession.removePresenceListener(this);
            throw e;
        }
        multiUserChatManager.roomEntered(this, nick);
        entered = true;
    }

    /**
     * Changes the room subject.
     *
     * @param subject The subject.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public void changeSubject(final String subject) throws XmppException {
        Message message = new Message(roomJid, Message.Type.GROUPCHAT, null, subject, null);
        xmppSession.sendAndAwaitMessage(message, new StanzaFilter<Message>() {
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
        xmppSession.send(new Message(roomJid, Message.Type.GROUPCHAT, message));
    }

    /**
     * Sends a message to the room.
     *
     * @param message The message.
     */
    public void sendMessage(Message message) {
        xmppSession.send(new Message(roomJid, Message.Type.GROUPCHAT, message.getBodies(), message.getSubjects(), message.getThread(), message.getParentThread(), message.getId(), message.getFrom(), message.getLanguage(), message.getExtensions(), message.getError()));
    }

    /**
     * Changes the nickname.
     *
     * @param newNickname The new nickname.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#changenick">7.6 Changing Nickname</a>
     */
    public synchronized void changeNickname(String newNickname) throws XmppException {
        if (!entered) {
            throw new IllegalStateException("You must have entered the room to change your nickname.");
        }

        final Presence changeNickNamePresence = new Presence(roomJid.withResource(newNickname));
        xmppSession.sendAndAwaitPresence(changeNickNamePresence, new StanzaFilter<Presence>() {
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
        xmppSession.send(new Presence(roomJid.withResource(nick), show, status));
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
     * Invites another user to the room. The invitation will be either mediated by the room or direct.
     *
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#register">7.10 Registering with a Room</a>
     * @see rocks.xmpp.extensions.muc.model.RoomRegistration
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#register">7.10 Registering with a Room</a>
     * @see rocks.xmpp.extensions.muc.model.RoomRegistration
     * @deprecated Use {@link #register(rocks.xmpp.extensions.register.model.Registration)}
     */
    @Deprecated
    public void submitRegistrationForm(DataForm dataForm) throws XmppException {
        register(Registration.builder().registrationForm(dataForm).build());
    }

    /**
     * Registers with the room.
     *
     * @param registration The registration.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#register">7.10 Registering with a Room</a>
     * @see rocks.xmpp.extensions.muc.model.RoomRegistration
     */
    public void register(Registration registration) throws XmppException {
        Objects.requireNonNull(registration, "registration must not be null.");
        if (registration.getRegistrationForm() != null) {
            if (registration.getRegistrationForm().getType() != DataForm.Type.SUBMIT) {
                throw new IllegalArgumentException("Data Form must be of type 'submit'");
            }
            if (!"http://jabber.org/protocol/muc#register".equals(registration.getRegistrationForm().getFormType())) {
                throw new IllegalArgumentException("Data Form is not of type 'http://jabber.org/protocol/muc#register'");
            }
        }
        IQ iq = new IQ(roomJid, IQ.Type.SET, registration);
        xmppSession.query(iq);
    }

    /**
     * Gets your reserved room nickname.
     *
     * @return The reserved nickname or null, if you don't have a reserved nickname.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public String discoverReservedNickname() throws XmppException {
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
        RequestVoice requestVoice = RequestVoice.builder().role(Role.PARTICIPANT).build();
        message.getExtensions().add(requestVoice.getDataForm());
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
        xmppSession.send(new Presence(roomJid.withResource(nick), Presence.Type.UNAVAILABLE, message));
        userHasExited();

        nick = null;
        multiUserChatManager.roomExited(this);
        entered = false;
        occupantMap.clear();
    }

    /**
     * Gets the voice list.
     *
     * @return The voice list.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyvoice">8.5 Modifying the Voice List</a>
     */
    public List<? extends rocks.xmpp.extensions.muc.model.Item> getVoiceList() throws XmppException {
        IQ result = xmppSession.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(Role.PARTICIPANT, null, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    /**
     * Changes multiple affiliations or roles.
     *
     * @param items The items.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyvoice">8.5 Modifying the Voice List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymember">9.5 Modifying the Member List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymod">9.8 Modifying the Moderator List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyowner">10.5 Modifying the Owner List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyadmin">10.8 Modifying the Admin List</a>
     */
    public void changeAffiliationsOrRoles(List<rocks.xmpp.extensions.muc.model.Item> items) throws XmppException {
        xmppSession.query(new IQ(roomJid, IQ.Type.SET, MucAdmin.withItems(items)));
    }

    /**
     * Gets the ban list.
     *
     * @return The ban list.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyban">9.2 Modifying the Ban List</a>
     */
    public List<? extends rocks.xmpp.extensions.muc.model.Item> getBanList() throws XmppException {
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
     * <li>Banning a User (affiliation = {@link Affiliation#OUTCAST})</li>
     * <li>Granting Membership (affiliation = {@link Affiliation#MEMBER})</li>
     * <li>Revoking Membership (affiliation = {@link Affiliation#NONE})</li>
     * <li>Granting Admin Status (affiliation = {@link Affiliation#ADMIN})</li>
     * <li>Revoking Admin Status (affiliation = {@link Affiliation#MEMBER})</li>
     * <li>Granting Owner Status (affiliation = {@link Affiliation#OWNER})</li>
     * <li>Revoking Owner Status (affiliation = {@link Affiliation#ADMIN})</li>
     * </ul>
     *
     * @param affiliation The new affiliation for the user.
     * @param user        The user.
     * @param reason      The reason.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
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
     * <li>Kicking an Occupant (role = {@link Role#NONE})</li>
     * <li>Granting Voice to a Visitor (role = {@link Role#PARTICIPANT})</li>
     * <li>Revoking Voice from a Participant (affiliation = {@link Role#VISITOR})</li>
     * <li>Granting Moderator Status (role = {@link Role#MODERATOR})</li>
     * <li>Revoking Moderator Status (role = {@link Role#PARTICIPANT})</li>
     * </ul>
     *
     * @param role     The new role for the user.
     * @param nickname The occupant's nickname.
     * @param reason   The reason.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymember">9.5 Modifying the Member List</a>
     */
    public List<? extends rocks.xmpp.extensions.muc.model.Item> getMembers() throws XmppException {
        IQ result = xmppSession.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(Affiliation.MEMBER, null, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    /**
     * Gets the moderators.
     *
     * @return The moderators.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymod">9.8 Modifying the Moderator List</a>
     */
    public List<? extends rocks.xmpp.extensions.muc.model.Item> getModerators() throws XmppException {
        IQ result = xmppSession.query(new IQ(roomJid, IQ.Type.GET, MucAdmin.withItem(Role.MODERATOR, null, null)));
        MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
        return mucAdmin.getItems();
    }

    /**
     * Creates an instant room.
     *
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-roominfo">6.4 Querying for Room Information</a>
     */
    public RoomInformation getRoomInformation() throws XmppException {
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(roomJid);

        Identity identity = null;
        Set<MucFeature> mucFeatures = new HashSet<>();
        RoomInfo roomInfo = null;

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
                String formType = dataForm.getFormType();
                if (RoomInfo.FORM_TYPE.equals(formType)) {
                    roomInfo = new RoomInfo(dataForm);
                    break;
                }
            }
        }

        return new RoomInformation(identity, mucFeatures, roomInfo);
    }

    /**
     * Gets the occupants in this room, i.e. their nicknames. This method should be used, when you are not yet in the room.
     *
     * @return The occupants.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-roomitems">6.5 Querying for Room Items</a>
     * @see #getOccupants()
     */
    public List<String> discoverOccupants() throws XmppException {
        ItemNode itemNode = serviceDiscoveryManager.discoverItems(roomJid);
        List<String> occupants = new ArrayList<>();
        List<Item> items = itemNode.getItems();
        for (Item item : items) {
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
     * You can wrap the form into {@link rocks.xmpp.extensions.muc.model.RoomConfiguration} for easier processing.
     * <p>
     * Use this method if you want to create a reserved room or configure an existing room.
     * </p>
     *
     * @return The configuration form.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see rocks.xmpp.extensions.muc.model.RoomConfiguration
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-reserved">10.1.3 Creating a Reserved Room</a>
     * @see #configure(rocks.xmpp.extensions.muc.model.RoomConfiguration)
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-reserved">10.1.3 Creating a Reserved Room</a>
     * @see #getConfigurationForm()
     * @deprecated Use {@link #configure(rocks.xmpp.extensions.muc.model.RoomConfiguration)}
     */
    @Deprecated
    public void submitConfigurationForm(DataForm dataForm) throws XmppException {
        Objects.requireNonNull(dataForm, "dataForm must not be null.");
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
     * Configures this room.
     *
     * @param roomConfiguration The room configuration form.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-reserved">10.1.3 Creating a Reserved Room</a>
     * @see #getConfigurationForm()
     */
    public void configure(RoomConfiguration roomConfiguration) throws XmppException {
        Objects.requireNonNull(roomConfiguration, "roomConfiguration must not be null.");
        MucOwner mucOwner = new MucOwner(roomConfiguration.getDataForm());
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
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#destroyroom">10.9 Destroying a Room</a>
     */
    public void destroy(String reason) throws XmppException {
        MucOwner mucOwner = MucOwner.withDestroy(roomJid, reason);
        IQ iq = new IQ(roomJid, IQ.Type.SET, mucOwner);
        xmppSession.query(iq);
    }

    /**
     * Gets the room address.
     *
     * @return The room address.
     */
    public Jid getAddress() {
        return roomJid;
    }

    /**
     * Discovers the allowable traffic, i.e. the allowed extensions.
     *
     * @return The list of allowable features.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the chat service returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the chat service did not respond.
     * @see <a href="http://www.xmpp.org/extensions/xep-0045.html#impl-service-traffic">17.1.1 Allowable Traffic</a>
     */
    public Set<Feature> discoverAllowableTraffic() throws XmppException {
        return serviceDiscoveryManager.discoverInformation(roomJid, "http://jabber.org/protocol/muc#traffic").getFeatures();
    }

    @Override
    public void handleMessage(MessageEvent e) {
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

    @Override
    public void handlePresence(PresenceEvent e) {
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

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            invitationDeclineListeners.clear();
            subjectChangeListeners.clear();
            occupantListeners.clear();
            messageListeners.clear();
            occupantMap.clear();
        }
    }

    @Override
    public String toString() {
        return roomJid != null ? roomJid.toString() : super.toString();
    }

    /**
     * Compares this chat service first by their name and then by their service address.
     *
     * @param o The other chat service.
     * @return The comparison result.
     */
    @Override
    public int compareTo(ChatRoom o) {
        if (this == o) {
            return 0;
        }
        if (o != null) {
            int result;
            // First compare name.
            if (name != null) {
                if (o.name != null) {
                    result = name.compareTo(o.name);
                } else {
                    result = -1;
                }
            } else {
                if (o.name != null) {
                    result = 1;
                } else {
                    result = 0;
                }
            }
            // If the names are equal, compare addresses.
            if (result == 0) {
                if (roomJid != null) {
                    if (o.roomJid != null) {
                        result = roomJid.compareTo(o.roomJid);
                    } else {
                        result = -1;
                    }
                } else {
                    if (o.roomJid != null) {
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
