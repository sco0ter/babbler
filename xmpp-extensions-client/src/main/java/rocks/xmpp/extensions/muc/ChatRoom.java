/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.delay.model.DelayedDelivery;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.muc.conference.model.DirectInvitation;
import rocks.xmpp.extensions.muc.model.Actor;
import rocks.xmpp.extensions.muc.model.Affiliation;
import rocks.xmpp.extensions.muc.model.DiscussionHistory;
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
import rocks.xmpp.im.chat.Chat;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.AsyncResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * Represents a chat room.
 *
 * @author Christian Schudt
 */
public final class ChatRoom extends Chat implements Comparable<ChatRoom> {

    private final Set<Consumer<InvitationDeclineEvent>> invitationDeclineListeners = new CopyOnWriteArraySet<>();

    private final Set<Consumer<SubjectChangeEvent>> subjectChangeListeners = new CopyOnWriteArraySet<>();

    private final Set<Consumer<OccupantEvent>> occupantListeners = new CopyOnWriteArraySet<>();

    private final Map<String, Occupant> occupantMap = new HashMap<>();

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final MultiUserChatManager multiUserChatManager;

    private final String name;

    private final Jid roomJid;

    private final XmppSession xmppSession;

    private final Consumer<MessageEvent> messageListener;

    private final Consumer<PresenceEvent> presenceListener;

    private String nick;

    private boolean entered;

    ChatRoom(final Jid roomJid, String name, XmppSession xmppSession, ServiceDiscoveryManager serviceDiscoveryManager, MultiUserChatManager multiUserChatManager) {
        if (Objects.requireNonNull(roomJid).getLocal() == null) {
            throw new IllegalArgumentException("roomJid must have a local part.");
        }
        if (roomJid.getResource() != null) {
            throw new IllegalArgumentException("roomJid must not have a resource part: " + roomJid.getResource());
        }
        this.name = name;
        this.roomJid = roomJid;
        this.xmppSession = xmppSession;
        this.serviceDiscoveryManager = serviceDiscoveryManager;
        this.multiUserChatManager = multiUserChatManager;
        this.messageListener = e -> {
            Message message = e.getMessage();
            if (message.getFrom().asBareJid().equals(roomJid)) {
                if (message.getType() == Message.Type.GROUPCHAT) {
                    // This is a <message/> stanza from the room JID (or from the occupant JID of the entity that set the subject), with a <subject/> element but no <body/> element
                    if (message.getSubject() != null && message.getBody() == null) {
                        XmppUtils.notifyEventListeners(subjectChangeListeners, new SubjectChangeEvent(ChatRoom.this, message.getSubject(), message.getFrom().getResource(), message.hasExtension(DelayedDelivery.class), DelayedDelivery.sendDate(message)));
                    } else {
                        XmppUtils.notifyEventListeners(inboundMessageListeners, new MessageEvent(ChatRoom.this, message, true));
                    }
                } else {
                    MucUser mucUser = message.getExtension(MucUser.class);
                    if (mucUser != null) {
                        Decline decline = mucUser.getDecline();
                        if (decline != null) {
                            XmppUtils.notifyEventListeners(invitationDeclineListeners, new InvitationDeclineEvent(ChatRoom.this, roomJid, decline.getFrom(), decline.getReason()));
                        }
                    }
                }
            }
        };

        this.presenceListener = e -> {
            Presence presence = e.getPresence();
            // If the presence came from the room.
            if (presence.getFrom() != null && presence.getFrom().asBareJid().equals(roomJid)) {
                MucUser mucUser = presence.getExtension(MucUser.class);
                if (mucUser != null) {
                    String nick = presence.getFrom().getResource();

                    if (nick != null) {
                        boolean isSelfPresence = isSelfPresence(presence);
                        if (presence.isAvailable()) {
                            Occupant occupant = new Occupant(presence, isSelfPresence);
                            Occupant previousOccupant = occupantMap.put(nick, occupant);
                            OccupantEvent.Type type;
                            // A new occupant entered the room.
                            if (previousOccupant == null) {
                                // Only notify about "joins", if it's not our own join and we are already in the room.
                                if (!isSelfPresence && hasEntered()) {
                                    type = OccupantEvent.Type.ENTERED;
                                } else {
                                    type = OccupantEvent.Type.STATUS_CHANGED;
                                }
                            } else {
                                type = OccupantEvent.Type.STATUS_CHANGED;
                            }
                            XmppUtils.notifyEventListeners(occupantListeners, new OccupantEvent(ChatRoom.this, occupant, type, null, null, null));
                        } else if (presence.getType() == Presence.Type.UNAVAILABLE) {
                            // Occupant has exited the room.
                            Occupant occupant = occupantMap.remove(nick);
                            if (occupant != null) {
                                OccupantEvent occupantEvent = null;
                                if (mucUser.getItem() != null) {
                                    Actor actor = mucUser.getItem().getActor();
                                    String reason = mucUser.getItem().getReason();
                                    if (!mucUser.getStatusCodes().isEmpty()) {
                                        if (mucUser.getStatusCodes().contains(Status.KICKED)) {
                                            occupantEvent = new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.KICKED, actor, reason, null);
                                        } else if (mucUser.getStatusCodes().contains(Status.BANNED)) {
                                            occupantEvent = new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.BANNED, actor, reason, null);
                                        } else if (mucUser.getStatusCodes().contains(Status.MEMBERSHIP_REVOKED)) {
                                            occupantEvent = new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.MEMBERSHIP_REVOKED, actor, reason, null);
                                        } else if (mucUser.getStatusCodes().contains(Status.NICK_CHANGED)) {
                                            occupantEvent = new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.NICKNAME_CHANGED, actor, reason, null);
                                        } else if (mucUser.getStatusCodes().contains(Status.SERVICE_SHUT_DOWN)) {
                                            occupantEvent = new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.SYSTEM_SHUTDOWN, actor, reason, null);
                                        }
                                    } else if (mucUser.getDestroy() != null) {
                                        occupantEvent = new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.ROOM_DESTROYED, actor, mucUser.getDestroy().getReason(), mucUser.getDestroy().getJid());
                                    } else {
                                        occupantEvent = new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.EXITED, null, null, null);
                                    }
                                } else {
                                    occupantEvent = new OccupantEvent(ChatRoom.this, occupant, OccupantEvent.Type.EXITED, null, null, null);
                                }
                                if (occupantEvent != null) {
                                    XmppUtils.notifyEventListeners(occupantListeners, occupantEvent);
                                }
                            }
                            if (isSelfPresence) {
                                userHasExited();
                            }
                        }
                    }
                }
            }
        };
    }

    void initialize() {
        xmppSession.addSessionStatusListener(e -> {
            if (e.getStatus() == XmppSession.Status.CLOSED) {
                invitationDeclineListeners.clear();
                subjectChangeListeners.clear();
                occupantListeners.clear();
                inboundMessageListeners.clear();
                occupantMap.clear();
            }
        });
    }

    private void userHasExited() {
        xmppSession.removeInboundMessageListener(messageListener);
        xmppSession.removeInboundPresenceListener(presenceListener);
        synchronized (this) {
            entered = false;
            nick = null;
            multiUserChatManager.roomExited(this);
            occupantMap.clear();
        }
    }

    private synchronized boolean isSelfPresence(Presence presence) {
        boolean isSelfPresence = false;
        MucUser mucUser = presence.getExtension(MucUser.class);
        if (mucUser != null) {
            // If the presence is self-presence (110) or if the service assigned another nickname (210) to the user (but didn't include 110).
            boolean nicknameChanged = mucUser.getStatusCodes().contains(Status.SERVICE_HAS_ASSIGNED_OR_MODIFIED_NICK);
            if (nicknameChanged) {
                nick = presence.getFrom().getResource();
            }
            isSelfPresence = mucUser.getStatusCodes().contains(Status.SELF_PRESENCE) || nicknameChanged;
        }
        return isSelfPresence || nick != null && presence.getFrom() != null && nick.equals(presence.getFrom().getResource());
    }

    /**
     * Adds a invitation decline listener, which allows to listen for invitation declines.
     *
     * @param invitationDeclineListener The listener.
     * @see #removeInvitationDeclineListener(Consumer)
     */
    public void addInvitationDeclineListener(Consumer<InvitationDeclineEvent> invitationDeclineListener) {
        invitationDeclineListeners.add(invitationDeclineListener);
    }

    /**
     * Removes a previously added invitation decline listener.
     *
     * @param invitationDeclineListener The listener.
     * @see #addInvitationDeclineListener(Consumer)
     */
    public void removeInvitationDeclineListener(Consumer<InvitationDeclineEvent> invitationDeclineListener) {
        invitationDeclineListeners.remove(invitationDeclineListener);
    }

    /**
     * Adds a subject change listener, which allows to listen for subject changes.
     *
     * @param subjectChangeListener The listener.
     * @see #removeSubjectChangeListener(Consumer)
     */
    public void addSubjectChangeListener(Consumer<SubjectChangeEvent> subjectChangeListener) {
        subjectChangeListeners.add(subjectChangeListener);
    }

    /**
     * Removes a previously added subject change listener.
     *
     * @param subjectChangeListener The listener.
     * @see #addSubjectChangeListener(Consumer)
     */
    public void removeSubjectChangeListener(Consumer<SubjectChangeEvent> subjectChangeListener) {
        subjectChangeListeners.remove(subjectChangeListener);
    }

    /**
     * Adds an occupant listener, which allows to listen for presence changes of occupants, e.g. "joins" and "leaves".
     *
     * @param occupantListener The listener.
     * @see #removeOccupantListener(Consumer)
     */
    public void addOccupantListener(Consumer<OccupantEvent> occupantListener) {
        occupantListeners.add(occupantListener);
    }

    /**
     * Removes a previously added occupant listener.
     *
     * @param occupantListener The listener.
     * @see #addOccupantListener(Consumer)
     */
    public void removeOccupantListener(Consumer<OccupantEvent> occupantListener) {
        occupantListeners.remove(occupantListener);
    }

    /**
     * Enters the room.
     *
     * @param nick The nickname.
     * @return The async result with the self-presence returned by the chat room.
     */
    public AsyncResult<Presence> enter(String nick) {
        return enter(nick, null, null);
    }

    /**
     * Enters the room with a password.
     *
     * @param nick     The nickname.
     * @param password The password.
     * @return The async result with the self-presence returned by the chat room.
     */
    public AsyncResult<Presence> enter(String nick, String password) {
        return enter(nick, password, null);
    }

    /**
     * Enters the room and requests history messages.
     *
     * @param nick    The nickname.
     * @param history The history.
     * @return The async result with the self-presence returned by the chat room.
     */
    public AsyncResult<Presence> enter(String nick, DiscussionHistory history) {
        return enter(nick, null, history);
    }

    /**
     * Enters the room with a password and requests history messages.
     *
     * @param nick     The nickname.
     * @param password The password.
     * @param history  The history.
     * @return The async result with the self-presence returned by the chat room.
     */
    public synchronized AsyncResult<Presence> enter(final String nick, String password, DiscussionHistory history) {
        Objects.requireNonNull(nick, "nick must not be null.");

        if (entered) {
            throw new IllegalStateException("You already entered this room.");
        }
        xmppSession.addInboundMessageListener(messageListener);
        xmppSession.addInboundPresenceListener(presenceListener);

        final Presence enterPresence = new Presence(roomJid.withResource(nick));
        enterPresence.getExtensions().add(Muc.withPasswordAndHistory(password, history));
        this.nick = nick;
        return xmppSession.sendAndAwaitPresence(enterPresence, presence -> {
            Jid room = presence.getFrom().asBareJid();
            return presence.isAvailable() && room.equals(roomJid) && isSelfPresence(presence);
        }).whenComplete((presence, e) -> {
            if (e != null) {
                xmppSession.removeInboundMessageListener(messageListener);
                xmppSession.removeInboundPresenceListener(presenceListener);
            } else {
                multiUserChatManager.roomEntered(this, nick);
                synchronized (this) {
                    entered = true;
                }
            }
        });
    }

    /**
     * Changes the room subject.
     *
     * @param subject The subject.
     * @return The async result with the message returned by the chat room.
     */
    public AsyncResult<Message> changeSubject(final String subject) {
        Message message = new Message(roomJid, Message.Type.GROUPCHAT, null, subject, null);
        return xmppSession.sendAndAwaitMessage(message, message1 -> message1.getSubject() != null && message1.getSubject().equals(subject));
    }

    /**
     * Sends a message to the room.
     *
     * @param message The message text.
     * @return The sent message.
     */
    public Message sendMessage(String message) {
        Message m = new Message(roomJid, Message.Type.GROUPCHAT, message);
        xmppSession.send(m);
        return m;
    }

    /**
     * Sends a message to the room.
     *
     * @param message The message.
     * @return The sent message.
     */
    public Message sendMessage(Message message) {
        Message m = new Message(roomJid, Message.Type.GROUPCHAT, message.getBodies(), message.getSubjects(), message.getThread(), message.getParentThread(), message.getId(), message.getFrom(), message.getLanguage(), message.getExtensions(), message.getError());
        xmppSession.send(m);
        return m;
    }

    /**
     * Changes the nickname.
     *
     * @param newNickname The new nickname.
     * @return The async result with the presence returned by the chat room.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#changenick">7.6 Changing Nickname</a>
     */
    public synchronized AsyncResult<Presence> changeNickname(String newNickname) {
        if (!entered) {
            throw new IllegalStateException("You must have entered the room to change your nickname.");
        }

        final Presence changeNickNamePresence = new Presence(roomJid.withResource(newNickname));
        return xmppSession.sendAndAwaitPresence(changeNickNamePresence, presence -> presence.getFrom().equals(changeNickNamePresence.getTo()));
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
            message.addExtension(new DirectInvitation(roomJid, null, reason));
        } else {
            message = new Message(roomJid, Message.Type.NORMAL);
            message.addExtension(MucUser.withInvites(new Invite(invitee, reason)));
        }
        xmppSession.send(message);
    }

    /**
     * Gets the data form necessary to register with the room.
     *
     * @return The async result with the data form.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#register">7.10 Registering with a Room</a>
     * @see rocks.xmpp.extensions.muc.model.RoomRegistration
     */
    public AsyncResult<DataForm> getRegistrationForm() {
        return xmppSession.query(IQ.get(roomJid, Registration.empty())).thenApply(result -> {
            Registration registration = result.getExtension(Registration.class);
            if (registration != null) {
                return registration.getRegistrationForm();
            }
            return null;
        });
    }

    /**
     * Registers with the room.
     *
     * @param registration The registration.
     * @return The async result.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#register">7.10 Registering with a Room</a>
     * @see rocks.xmpp.extensions.muc.model.RoomRegistration
     */
    public AsyncResult<IQ> register(Registration registration) {
        Objects.requireNonNull(registration, "registration must not be null.");
        if (registration.getRegistrationForm() != null) {
            if (registration.getRegistrationForm().getType() != DataForm.Type.SUBMIT) {
                throw new IllegalArgumentException("Data Form must be of type 'submit'");
            }
            if (!"http://jabber.org/protocol/muc#register".equals(registration.getRegistrationForm().getFormType())) {
                throw new IllegalArgumentException("Data Form is not of type 'http://jabber.org/protocol/muc#register'");
            }
        }
        return xmppSession.query(IQ.set(roomJid, registration));
    }

    /**
     * Gets your reserved room nickname.
     *
     * @return The async result with the reserved nickname or null, if you don't have a reserved nickname.
     */
    public AsyncResult<String> discoverReservedNickname() {
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        return serviceDiscoveryManager.discoverInformation(roomJid, "x-roomuser-item").thenApply(infoNode -> {
            if (infoNode != null) {
                for (Identity identity : infoNode.getIdentities()) {
                    if ("conference".equals(identity.getCategory()) && "text".equals(identity.getType())) {
                        return identity.getName();
                    }
                }
            }
            return null;
        });
    }

    /**
     * Requests voice in a moderated room.
     *
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#requestvoice">7.13 Requesting Voice</a>
     */
    public void requestVoice() {
        Message message = new Message(roomJid);
        RequestVoice requestVoice = RequestVoice.builder().role(Role.PARTICIPANT).build();
        message.addExtension(requestVoice.getDataForm());
        xmppSession.send(message);
    }

    /**
     * Exits the room.
     *
     * @return The async result.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#exit">7.14 Exiting a Room</a>
     */
    public AsyncResult<Void> exit() {
        return exit(null);
    }

    /**
     * Exits the room with a custom message.
     *
     * @param message The exit message.
     * @return The async result.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#exit">7.14 Exiting a Room</a>
     */
    public synchronized AsyncResult<Void> exit(String message) {

        if (!entered) {
            return new AsyncResult<>(CompletableFuture.completedFuture(null));
        }
        return xmppSession.sendAndAwaitPresence(new Presence(roomJid.withResource(nick), Presence.Type.UNAVAILABLE, message), presence -> {
            Jid room = presence.getFrom().asBareJid();
            return !presence.isAvailable() && room.equals(roomJid) && isSelfPresence(presence);
        }).handle((result, throwable) -> {
            userHasExited();
            return null;
        });
    }

    /**
     * Indicates, if you have entered the room. When you exit the room, this method returns false.
     *
     * @return If you entered the room.
     * @see #enter(String)
     * @see #exit()
     */
    public final synchronized boolean hasEntered() {
        return entered;
    }

    /**
     * Gets the voice list.
     *
     * @return The async result with the voice list.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyvoice">8.5 Modifying the Voice List</a>
     */
    public AsyncResult<List<rocks.xmpp.extensions.muc.model.Item>> getVoiceList() {
        return xmppSession.query(IQ.get(roomJid, MucAdmin.withItem(Role.PARTICIPANT, null, null))).thenApply(result -> {
            MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
            return mucAdmin.getItems();
        });
    }

    /**
     * Changes multiple affiliations or roles.
     *
     * @param items The items.
     * @return The async result.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyvoice">8.5 Modifying the Voice List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymember">9.5 Modifying the Member List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymod">9.8 Modifying the Moderator List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyowner">10.5 Modifying the Owner List</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyadmin">10.8 Modifying the Admin List</a>
     */
    public AsyncResult<IQ> changeAffiliationsOrRoles(List<rocks.xmpp.extensions.muc.model.Item> items) {
        return xmppSession.query(IQ.set(roomJid, MucAdmin.withItems(items)));
    }

    /**
     * Gets the ban list.
     *
     * @return The async result with the ban list.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifyban">9.2 Modifying the Ban List</a>
     */
    public AsyncResult<List<rocks.xmpp.extensions.muc.model.Item>> getBanList() {
        return xmppSession.query(IQ.get(roomJid, MucAdmin.withItem(Affiliation.OUTCAST, null, null))).thenApply(result -> {
            MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
            return mucAdmin.getItems();
        });
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
     * @return The async result.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#ban">9.1 Banning a User</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantmember">9.3 Granting Membership</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokemember">9.4 Revoking Membership</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantowner">10.3 Granting Owner Status</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokeowner">10.4 Revoking Owner Status</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantadmin">10.6 Granting Admin Status</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokeadmin">10.7 Revoking Admin Status</a>
     */
    public AsyncResult<IQ> changeAffiliation(Affiliation affiliation, Jid user, String reason) {
        return xmppSession.query(IQ.set(roomJid, MucAdmin.withItem(affiliation, user, reason)));
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
     * @return The async result.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#kick">8.2 Kicking an Occupant</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantvoice">8.3 Granting Voice to a Visitor</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokevoice">8.4 Revoking Voice from a Participant</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#grantmod">9.6 Granting Moderator Status</a>
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#revokemod">9.7 Revoking Moderator Status</a>
     */
    public AsyncResult<IQ> changeRole(Role role, String nickname, String reason) {
        return xmppSession.query(IQ.set(roomJid, MucAdmin.withItem(role, nickname, reason)));
    }

    /**
     * Gets the owners of the room.
     *
     * @return The async result with the owners.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymember">9.5 Modifying the Member List</a>
     */
    public AsyncResult<List<? extends rocks.xmpp.extensions.muc.model.Item>> getOwners() {
        return getByAffiliation(Affiliation.OWNER);
    }

    /**
     * Gets the outcasts of the room.
     *
     * @return The async result with the outcasts.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymember">9.5 Modifying the Member List</a>
     */
    public AsyncResult<List<? extends rocks.xmpp.extensions.muc.model.Item>> getOutcasts() {
        return getByAffiliation(Affiliation.OUTCAST);
    }

    /**
     * Gets the admins of the room.
     *
     * @return The async result with the admins.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymember">9.5 Modifying the Member List</a>
     */
    public AsyncResult<List<? extends rocks.xmpp.extensions.muc.model.Item>> getAdmins() {
        return getByAffiliation(Affiliation.ADMIN);
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
     * @return The async result with the members.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymember">9.5 Modifying the Member List</a>
     */
    public AsyncResult<List<? extends rocks.xmpp.extensions.muc.model.Item>> getMembers() {
        return getByAffiliation(Affiliation.MEMBER);
    }

    private AsyncResult<List<? extends rocks.xmpp.extensions.muc.model.Item>> getByAffiliation(Affiliation affiliation) {
        return xmppSession.query(IQ.get(roomJid, MucAdmin.withItem(affiliation, null, null))).thenApply(result -> {
            MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
            return mucAdmin.getItems();
        });
    }

    /**
     * Gets the moderators.
     *
     * @return The async result with the moderators.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#modifymod">9.8 Modifying the Moderator List</a>
     */
    public AsyncResult<List<rocks.xmpp.extensions.muc.model.Item>> getModerators() {
        return xmppSession.query(IQ.get(roomJid, MucAdmin.withItem(Role.MODERATOR, null, null))).thenApply(result -> {
            MucAdmin mucAdmin = result.getExtension(MucAdmin.class);
            return mucAdmin.getItems();
        });
    }

    /**
     * Creates an instant room.
     *
     * @return The async result.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-instant">10.1.2 Creating an Instant Room</a>
     */
    public synchronized AsyncResult<IQ> createRoom() {
        return enter(nick).thenCompose(presence ->
                xmppSession.query(IQ.set(roomJid, MucOwner.withConfiguration(new DataForm(DataForm.Type.SUBMIT)))));
    }

    /**
     * Gets the room information for this chat room.
     *
     * @return The async result with the room info.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-roominfo">6.4 Querying for Room Information</a>
     */
    public AsyncResult<RoomInformation> getRoomInformation() {
        return serviceDiscoveryManager.discoverInformation(roomJid).thenApply(infoNode -> {

            Identity identity = null;
            Set<MucFeature> mucFeatures = new HashSet<>();
            RoomInfo roomInfo = null;

            if (infoNode != null) {
                Set<Identity> identities = infoNode.getIdentities();
                Iterator<Identity> iterator = identities.iterator();
                if (iterator.hasNext()) {
                    identity = iterator.next();
                }
                for (String feature : infoNode.getFeatures()) {
                    for (MucFeature mucFeature : MucFeature.values()) {
                        if (mucFeature.getServiceDiscoveryFeature().equals(feature)) {
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
        });
    }

    /**
     * Gets the occupants in this room, i.e. their nicknames. This method should be used, when you are not yet in the room.
     *
     * @return The async result with the occupants.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#disco-roomitems">6.5 Querying for Room Items</a>
     * @see #getOccupants()
     */
    public AsyncResult<List<String>> discoverOccupants() {
        return serviceDiscoveryManager.discoverItems(roomJid).thenApply(itemNode -> {
            List<String> occupants = new ArrayList<>();
            List<Item> items = itemNode.getItems();
            items.stream().filter(item -> item.getJid() != null).forEach(item -> {
                String nickname = item.getJid().getResource();
                if (nickname != null) {
                    occupants.add(nickname);
                }
            });
            return occupants;
        });
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
     * @return The async result with the configuration form.
     * @see rocks.xmpp.extensions.muc.model.RoomConfiguration
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-reserved">10.1.3 Creating a Reserved Room</a>
     * @see #configure(rocks.xmpp.extensions.muc.model.RoomConfiguration)
     */
    public AsyncResult<DataForm> getConfigurationForm() {
        return xmppSession.query(IQ.get(roomJid, MucOwner.empty())).thenApply(result -> {
            MucOwner mucOwner = result.getExtension(MucOwner.class);
            return mucOwner.getConfigurationForm();
        });
    }

    /**
     * Configures this room.
     *
     * @param roomConfiguration The async result with the room configuration form.
     * @return The async result.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#createroom-reserved">10.1.3 Creating a Reserved Room</a>
     * @see #getConfigurationForm()
     */
    public AsyncResult<IQ> configure(RoomConfiguration roomConfiguration) {
        Objects.requireNonNull(roomConfiguration, "roomConfiguration must not be null.");
        MucOwner mucOwner = MucOwner.withConfiguration((roomConfiguration.getDataForm()));
        return xmppSession.query(IQ.set(roomJid, mucOwner));
    }

    /**
     * Gets the name for this room.
     *
     * @return The room name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the nickname in this room. Usually this is the nick used to enter the room, but can also be a nickname assigned by the chat service.
     *
     * @return The nickname in this room or {@code null}, if not entered.
     */
    public final synchronized String getNick() {
        return nick;
    }

    /**
     * Destroys the room.
     *
     * @param reason The reason for the room destruction.
     * @return The async result.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#destroyroom">10.9 Destroying a Room</a>
     */
    public AsyncResult<IQ> destroy(String reason) {
        MucOwner mucOwner = MucOwner.withDestroy(roomJid, reason);
        return xmppSession.query(IQ.set(roomJid, mucOwner));
    }

    /**
     * Destroys the room.
     *
     * @return The async result.
     * @see <a href="http://xmpp.org/extensions/xep-0045.html#destroyroom">10.9 Destroying a Room</a>
     */
    public final AsyncResult<IQ> destroy() {
        return destroy(null);
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
     * @return The async result with the list of allowable features.
     * @see <a href="http://www.xmpp.org/extensions/xep-0045.html#impl-service-traffic">17.1.1 Allowable Traffic</a>
     */
    public AsyncResult<Set<String>> discoverAllowableTraffic() {
        return serviceDiscoveryManager.discoverInformation(roomJid, "http://jabber.org/protocol/muc#traffic").thenApply(InfoNode::getFeatures);
    }

    @Override
    public String toString() {
        return roomJid.toString();
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
                result = o.name != null ? name.compareTo(o.name) : -1;
            } else {
                result = o.name != null ? 1 : 0;
            }
            // If the names are equal, compare addresses.
            if (result == 0) {
                return roomJid.compareTo(o.roomJid);
            }
            return result;
        }
        return -1;
    }
}
