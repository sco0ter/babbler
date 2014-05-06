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
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.disco.items.Item;
import org.xmpp.extension.disco.items.ItemNode;
import org.xmpp.extension.muc.conference.DirectInvitation;
import org.xmpp.extension.muc.user.Invite;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;
import org.xmpp.stanza.PresenceEvent;
import org.xmpp.stanza.PresenceListener;
import org.xmpp.stanza.client.Message;
import org.xmpp.stanza.client.Presence;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class MultiUserChatManager extends ExtensionManager {
    private static final Logger logger = Logger.getLogger(MultiUserChatManager.class.getName());

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private final Set<InvitationListener> invitationListeners = new CopyOnWriteArraySet<>();

    protected MultiUserChatManager(final Connection connection) {
        super(connection, Muc.NAMESPACE);
        connection.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {

            }
        });
        connection.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming()) {
                    Message message = e.getMessage();

                    // Check, if the message contains a direct invitation.
                    DirectInvitation directInvitation = message.getExtension(DirectInvitation.class);
                    if (directInvitation != null) {
                        notifyListeners(new InvitationEvent(MultiUserChatManager.this, connection, message.getFrom(), directInvitation.getRoomAddress(), directInvitation.getReason(), directInvitation.getPassword(), directInvitation.isContinue(), directInvitation.getThread(), false));
                    } else {
                        // Check, if the message contains a mediated invitation.
                        MucUser mucUser = message.getExtension(MucUser.class);
                        if (mucUser != null) {
                            for (Invite invite : mucUser.getInvites()) {
                                notifyListeners(new InvitationEvent(MultiUserChatManager.this, connection, invite.getFrom(), message.getFrom(), invite.getReason(), mucUser.getPassword(), invite.isContinue(), invite.getThread(), true));
                            }
                        }
                    }
                }
            }
        });
        this.serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
    }

    private void notifyListeners(InvitationEvent invitationEvent) {
        for (InvitationListener invitationListener : invitationListeners) {
            try {
                invitationListener.invitationReceived(invitationEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Adds a invitation listener, which allows to listen for incoming multi-user chat invitations.
     *
     * @param invitationListener The listener.
     * @see #removeInvitationListener(InvitationListener)
     */
    public void addInvitationListener(InvitationListener invitationListener) {
        invitationListeners.add(invitationListener);
    }

    /**
     * Removes a previously added invitation listener.
     *
     * @param invitationListener The listener.
     * @see #addInvitationListener(InvitationListener)
     */
    public void removeInvitationListener(InvitationListener invitationListener) {
        invitationListeners.remove(invitationListener);
    }

    public Set<Item> getMucServices() throws XmppException {
        ItemNode itemDiscovery = serviceDiscoveryManager.discoverItems(null);
        Set<Item> identities = new HashSet<>();

        for (Item item : itemDiscovery.getItems()) {
            InfoNode infoDiscovery = serviceDiscoveryManager.discoverInformation(item.getJid());
            if (infoDiscovery.getFeatures().contains(new Feature(Muc.NAMESPACE))) {
                identities.add(item);
            }
        }
        return identities;
    }

    public ChatService createChatService(Jid chatService) {
        return new ChatService(chatService, connection, serviceDiscoveryManager);
    }

    public ChatRoom getRoomInfo(Jid room) throws XmppException {
        InfoNode infoNode = serviceDiscoveryManager.discoverInformation(room);
        ChatRoom chatRoom = new ChatRoom();
        if (infoNode != null) {
            for (Identity identity : infoNode.getIdentities()) {

            }
            Set<MucFeature> mucFeatures = new HashSet<>();
            for (Feature feature : infoNode.getFeatures()) {
                for (MucFeature mucFeature : MucFeature.values()) {
                    if (mucFeature.getServiceDiscoveryFeature().equals(feature.getVar())) {
                        mucFeatures.add(mucFeature);
                    }
                }
            }
            chatRoom.setFeatures(mucFeatures);

            for (DataForm dataForm : infoNode.getExtensions()) {
                DataForm.Field formType = dataForm.findField("FORM_TYPE");
                if (formType != null && !formType.getValues().isEmpty() && formType.getValues().get(0).equals("http://jabber.org/protocol/muc#roominfo")) {
                    for (DataForm.Field field : dataForm.getFields()) {
                        try {
                            if (field.getVar().equals("muc#maxhistoryfetch")) {
                                chatRoom.setMaxHistory(Integer.parseInt(field.getValues().get(0)));
                            } else if (field.getVar().equals("muc#roominfo_contactjid")) {
                                List<Jid> contacts = new ArrayList<>();
                                for (String value : field.getValues()) {
                                    contacts.add(Jid.valueOf(value));
                                }
                                chatRoom.setContacts(contacts);
                            } else if (field.getVar().equals("muc#roominfo_description")) {
                                chatRoom.setDescription(field.getValues().get(0));
                            } else if (field.getVar().equals("muc#roominfo_lang")) {
                                chatRoom.setLanguage(field.getValues().get(0));
                            } else if (field.getVar().equals("muc#roominfo_ldapgroup")) {
                                chatRoom.setLdapGroup(field.getValues().get(0));
                            } else if (field.getVar().equals("muc#roominfo_logs")) {
                                chatRoom.setLogs(new URL(field.getValues().get(0)));
                            } else if (field.getVar().equals("muc#roominfo_occupants")) {
                                chatRoom.setCurrentNumberOfOccupants(Integer.parseInt(field.getValues().get(0)));
                            } else if (field.getVar().equals("muc#roominfo_subject")) {
                                chatRoom.setSubject(field.getValues().get(0));
                            } else if (field.getVar().equals("muc#roominfo_subjectmod")) {
                                chatRoom.setChangeSubjectAllowed(field.getValues().get(0).equals("1"));
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Could not process MUC field: " + field.getVar(), e);
                        }
                    }
                }
            }
        }
        return chatRoom;
    }


    public void enterRoom(String room, String service, String nick) {
        Presence presence = new Presence();
        presence.setTo(new Jid(room, service, nick));
        presence.getExtensions().add(new Muc());
        connection.send(presence);
    }

    public void getFeatures(Jid jid) throws XmppException {
        serviceDiscoveryManager.discoverInformation(jid);
    }
}
