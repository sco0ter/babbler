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

package org.xmpp.extension.avatar;

import org.xmpp.*;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.avatar.data.AvatarData;
import org.xmpp.extension.avatar.metadata.AvatarMetadata;
import org.xmpp.extension.avatar.vcard.AvatarUpdate;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.extension.pubsub.Item;
import org.xmpp.extension.pubsub.PubSubManager;
import org.xmpp.extension.pubsub.PubSubService;
import org.xmpp.extension.pubsub.event.Event;
import org.xmpp.extension.vcard.VCard;
import org.xmpp.extension.vcard.VCardManager;
import org.xmpp.stanza.*;
import org.xmpp.stanza.client.Message;
import org.xmpp.stanza.client.Presence;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages avatar updates as described in <a href="http://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a>.
 * <p>
 * A future implementation will also manage <a href="http://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a>
 * </p>
 *
 * @author Christian Schudt
 */
public final class AvatarManager extends ExtensionManager {

    private static final Map<String, Avatar> CACHED_AVATARS = new ConcurrentHashMap<>();

    private static final Logger logger = Logger.getLogger(VCardManager.class.getName());

    private final Map<Jid, String> userHashes = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Jid, Lock> requestingAvatarLocks = new ConcurrentHashMap<>();

    private final Set<AvatarChangeListener> avatarChangeListeners = new CopyOnWriteArraySet<>();

    private final Executor avatarRequester;

    private final VCardManager vCardManager;

    private final Set<String> nonConformingResources = Collections.synchronizedSet(new HashSet<String>());

    private AvatarManager(final XmppSession xmppSession) {
        super(xmppSession, AvatarMetadata.NAMESPACE + "+notify", AvatarMetadata.NAMESPACE);

        vCardManager = xmppSession.getExtensionManager(VCardManager.class);

        avatarRequester = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Avatar Request Thread");
                thread.setDaemon(true);
                return thread;
            }
        });
        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    avatarChangeListeners.clear();
                    userHashes.clear();
                }
            }
        });
        xmppSession.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                // If vCard based avatars are enabled.
                if (isEnabled()) {
                    final Presence presence = e.getPresence();
                    if (e.isIncoming()) {

                        // If the presence has an avatar update information.
                        final AvatarUpdate avatarUpdate = presence.getExtension(AvatarUpdate.class);

                        // 4.3 Multiple Resources
                        if (presence.getFrom().asBareJid().equals(xmppSession.getConnectedResource().asBareJid()) && presence.getFrom().getResource() != null && !presence.getFrom().getResource().equals(xmppSession.getConnectedResource().getResource())) {
                            // We received a presence stanza from another resource of our own JID.

                            if (avatarUpdate == null) {
                                // 1. If the presence stanza received from the other resource does not contain the update child element, then the other resource does not support vCard-based avatars.
                                // That resource could modify the contents of the vCard (including the photo element);
                                // because polling for vCard updates is not allowed, the client MUST stop advertising the avatar image hash.
                                if (presence.isAvailable()) {
                                    nonConformingResources.add(presence.getFrom().getResource());
                                }
                                // However, the client MAY reset its hash if all instances of non-conforming resources have gone offline.
                                else if (presence.getType() == Presence.Type.UNAVAILABLE && nonConformingResources.remove(presence.getFrom().getResource()) && nonConformingResources.isEmpty()) {
                                    resetHash();
                                }
                            } else {
                                // If the presence stanza received from the other resource contains the update child element, then the other resource conforms to the protocol for vCard-based avatars. There are three possible scenarios.
                                // If the update child element contains a non-empty photo element, then the client MUST compare the image hashes.
                                if (avatarUpdate.getHash() != null && !avatarUpdate.getHash().equals(userHashes.get(xmppSession.getConnectedResource().asBareJid()))) {
                                    // If the hashes are different, then the client MUST NOT attempt to resolve the conflict by uploading its avatar image again. Instead, it MUST defer to the content of the retrieved vCard by resetting its image hash
                                    resetHash();
                                }
                            }
                        }

                        if (avatarUpdate != null && avatarUpdate.getHash() != null) {
                            final Jid contact;
                            MucUser mucUser = presence.getExtension(MucUser.class);
                            if (mucUser != null) {
                                if (mucUser.getItem() != null && mucUser.getItem().getJid() != null) {
                                    contact = mucUser.getItem().getJid().asBareJid();
                                } else {
                                    // Ignore presence received from anonymous MUC room.
                                    return;
                                }
                            } else {
                                contact = presence.getFrom().asBareJid();
                            }
                            // If the user sends the same hash as we already know, it's the same avatar. Therefore do nothing.
                            if (!avatarUpdate.getHash().equals(userHashes.put(contact, avatarUpdate.getHash()))) {
                                // When the recipient's client receives the hash of the avatar image, it SHOULD check the hash to determine if it already has a cached copy of that avatar image.
                                Avatar avatar = CACHED_AVATARS.get(avatarUpdate.getHash());
                                if (avatar != null) {
                                    notifyListeners(contact, avatar);
                                } else {
                                    // If not, it retrieves the sender's full vCard
                                    avatarRequester.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                // If the avatar was either known before or could be successfully retrieved from the vCard.
                                                notifyListeners(contact, getAvatarByVCard(contact));
                                            } catch (XmppException e1) {
                                                logger.log(Level.WARNING, e1.getMessage(), e1);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    } else if (presence.isAvailable() && nonConformingResources.isEmpty()) {
                        // 1. If a client supports the protocol defined herein, it MUST include the update child element in every presence broadcast it sends and SHOULD also include the update child in directed presence stanzas.

                        String myHash = userHashes.get(xmppSession.getConnectedResource().asBareJid());

                        if (myHash == null) {
                            // 2. If a client is not yet ready to advertise an image, it MUST send an empty update child element:
                            presence.getExtensions().add(new AvatarUpdate());

                            // Load my own avatar in order to advertise an image.
                            avatarRequester.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        getAvatarByVCard(xmppSession.getConnectedResource().asBareJid());
                                        // If the client subsequently obtains an avatar image (e.g., by updating or retrieving the vCard), it SHOULD then publish a new <presence/> stanza with character data in the <photo/> element.
                                        Presence lastSentPresence = xmppSession.getPresenceManager().getLastSentPresence();
                                        Presence presence = new Presence();
                                        if (lastSentPresence != null) {
                                            presence.setPriority(lastSentPresence.getPriority());
                                            presence.getStatuses().addAll(lastSentPresence.getStatuses());
                                            presence.setShow(lastSentPresence.getShow());
                                            presence.setLanguage(lastSentPresence.getLanguage());
                                        }
                                        // Send out a presence, which will be filled with the extension later, because we now know or own avatar and have the hash for it.
                                        xmppSession.send(presence);
                                    } catch (XmppException e1) {
                                        logger.log(Level.WARNING, e1.getMessage(), e1);
                                    }
                                }
                            });

                        } else if (presence.getExtension(AvatarUpdate.class) == null) {
                            presence.getExtensions().add(new AvatarUpdate(myHash));
                        }
                    }
                }
            }
        });

        xmppSession.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming() && isEnabled()) {
                    Message message = e.getMessage();
                    Event event = message.getExtension(Event.class);
                    if (event != null) {
                        for (Item item : event.getItems()) {
                            if (item.getPayload() instanceof AvatarMetadata) {
                                AvatarMetadata avatarMetadata = (AvatarMetadata) item.getPayload();
                                PubSubService pubSubService = xmppSession.getExtensionManager(PubSubManager.class).createPubSubService(message.getFrom());

                                try {
                                    AvatarCache avatarCache = AvatarCache.INSTANCE;
                                    byte[] imageData = null;
                                    try {
                                        imageData = avatarCache.load(item.getId());
                                    } catch (IOException e1) {
                                        logger.log(Level.WARNING, e1.getMessage(), e1);
                                    }
                                    if (imageData != null) {
                                        notifyListeners(message.getFrom().asBareJid(), new Avatar(item.getId(), imageData));
                                    } else {
                                        List<Item> items = pubSubService.getNode(AvatarData.NAMESPACE).getItems(item.getId());
                                        if (!items.isEmpty()) {
                                            Item i = items.get(0);
                                            if (i.getPayload() instanceof AvatarData) {
                                                AvatarData avatarData = (AvatarData) i.getPayload();
                                                try {
                                                    avatarCache.store(item.getId(), avatarData.getData());
                                                } catch (IOException e1) {
                                                    logger.log(Level.WARNING, e1.getMessage(), e1);
                                                }

                                                notifyListeners(message.getFrom().asBareJid(), new Avatar(item.getId(), avatarData.getData()));
                                            }
                                        }
                                    }
                                } catch (XmppException e1) {
                                    logger.log(Level.WARNING, e1.getMessage(), e1);
                                }
                            }
                        }
                    }
                }
            }
        });
        setEnabled(true);
    }

    private void resetHash() {
        // Remove our own hash and send an empty presence.
        // The lack of our own hash, will download the vCard and either broadcasts the image hash or an empty hash.
        userHashes.remove(xmppSession.getConnectedResource().asBareJid());
        Presence presence = xmppSession.getPresenceManager().getLastSentPresence();
        if (presence == null) {
            presence = new Presence();
        }
        presence.getExtensions().clear();
        xmppSession.send(presence);
    }

    private void notifyListeners(Jid contact, Avatar avatar) {

        for (AvatarChangeListener avatarChangeListener : avatarChangeListeners) {
            try {
                avatarChangeListener.avatarChanged(new AvatarChangeEvent(AvatarManager.this, contact, avatar));
            } catch (Exception e1) {
                logger.log(Level.WARNING, e1.getMessage(), e1);
            }
        }
    }

    private Avatar getAvatarByVCard(Jid contact) throws XmppException {
        Avatar avatar = null;

        Lock lock = new ReentrantLock();
        Lock existingLock = requestingAvatarLocks.putIfAbsent(contact, lock);
        if (existingLock != null) {
            lock = existingLock;
        }

        lock.lock();
        try {
            // Let's see, if there's a stored image already.
            String hash = userHashes.get(contact);
            if (hash != null) {
                avatar = CACHED_AVATARS.get(hash);
            }
            if (avatar == null) {
                // If there's no avatar for that user, create an empty avatar and load it.
                avatar = new Avatar(null, new byte[0]);
                hash = XmppUtils.hash(avatar.getImageData());
                VCardManager vCardManager = xmppSession.getExtensionManager(VCardManager.class);

                // Load the vCard for that user
                VCard vCard;
                if (contact.equals(xmppSession.getConnectedResource().asBareJid())) {
                    vCard = vCardManager.getVCard();
                } else {
                    vCard = vCardManager.getVCard(contact);
                }
                if (vCard != null) {
                    // And check if it has a photo.
                    VCard.Image image = vCard.getPhoto();
                    if (image != null && image.getValue() != null) {
                        hash = XmppUtils.hash(image.getValue());
                        if (hash != null) {
                            avatar = new Avatar(image.getType(), image.getValue());
                        }
                    }
                }
                userHashes.put(contact, hash);
                if (!Arrays.equals(avatar.getImageData(), new byte[0])) {
                    CACHED_AVATARS.put(hash, avatar);
                }
            }
            return avatar;
        } finally {
            lock.unlock();
            requestingAvatarLocks.remove(contact);
        }
    }

    /**
     * Gets the user avatar.
     *
     * @param contact The contact.
     * @return The contact's avatar or null, if it has no avatar.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public Avatar getAvatar(Jid contact) throws XmppException {
        return getAvatarByVCard(contact.asBareJid());
    }

    public void publishToPersonalEventingService(Avatar avatar) throws XmppException {

        PubSubService personalEventingService = xmppSession.getExtensionManager(PubSubManager.class).createPersonalEventingService();

        String itemId = XmppUtils.hash(avatar.getImageData());
        personalEventingService.getNode(AvatarData.NAMESPACE).publish(itemId, new AvatarData(avatar.getImageData()));

        List<AvatarMetadata.Info> infoList = new ArrayList<>();
        infoList.add(new AvatarMetadata.Info(avatar.getImageData().length, itemId, avatar.getType()));
        personalEventingService.getNode(AvatarMetadata.NAMESPACE).publish(itemId, new AvatarMetadata(infoList));
    }

    /**
     * Publishes an avatar to your VCard.
     *
     * @param avatar The avatar.
     * @throws XmppException
     * @see <a href="http://xmpp.org/extensions/xep-0153.html#publish">3.1 User Publishes Avatar</a>
     */
    public void publishAvatar(Avatar avatar) throws XmppException {
        VCard vCard = vCardManager.getVCard();
        if (avatar != null) {
            // Within a given session, a client MUST NOT attempt to upload a given avatar image more than once.
            // The client MAY upload the avatar image to the vCard on login and after that MUST NOT upload the vCard again
            // unless the user actively changes the avatar image.
            if (vCard.getPhoto() == null || !Arrays.equals(vCard.getPhoto().getValue(), avatar.getImageData())) {
                // If either there is avatar yet, or the old avatar is different from the new one: update
                vCard.setPhoto(new VCard.Image(avatar.getType(), avatar.getImageData()));
                vCardManager.setVCard(vCard);
                userHashes.put(xmppSession.getConnectedResource(), XmppUtils.hash(avatar.getImageData()));
            }
        } else {

            // If there's currently a photo, we want to reset it.
            if (vCard.getPhoto() != null && vCard.getPhoto().getValue() != null) {
                vCard.setPhoto(null);
                vCardManager.setVCard(vCard);
            }

            userHashes.put(xmppSession.getConnectedResource(), "");
            Presence presence = xmppSession.getPresenceManager().getLastSentPresence();
            if (presence == null) {
                presence = new Presence();
            }
            presence.getExtensions().clear();
            xmppSession.send(presence);
        }
    }

    /**
     * Adds an avatar listener, to listen for avatar updates.
     *
     * @param avatarChangeListener The avatar listener.
     */
    public void addAvatarChangeListener(AvatarChangeListener avatarChangeListener) {
        avatarChangeListeners.add(avatarChangeListener);
    }

    /**
     * Removes a previously added avatar listener.
     *
     * @param avatarChangeListener The avatar listener.
     */
    public void removeAvatarChangeListener(AvatarChangeListener avatarChangeListener) {
        avatarChangeListeners.remove(avatarChangeListener);
    }
}
