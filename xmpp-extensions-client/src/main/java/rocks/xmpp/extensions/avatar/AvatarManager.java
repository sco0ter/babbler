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

package rocks.xmpp.extensions.avatar;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.XmppUtils;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.PresenceListener;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.core.util.cache.DirectoryCache;
import rocks.xmpp.extensions.avatar.model.data.AvatarData;
import rocks.xmpp.extensions.avatar.model.metadata.AvatarMetadata;
import rocks.xmpp.extensions.muc.model.user.MucUser;
import rocks.xmpp.extensions.pubsub.PubSubManager;
import rocks.xmpp.extensions.pubsub.PubSubService;
import rocks.xmpp.extensions.pubsub.model.Item;
import rocks.xmpp.extensions.pubsub.model.event.Event;
import rocks.xmpp.extensions.vcard.avatar.model.AvatarUpdate;
import rocks.xmpp.extensions.vcard.temp.VCardManager;
import rocks.xmpp.extensions.vcard.temp.model.VCard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class manages avatar updates as described in <a href="http://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a> and <a href="http://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a>.
 * <p>
 * Whenever an avatar update is received by a contact, either via the presence based avatar extension (XEP-0153) or the PEP-based notification (XEP-0084), the registered listeners are triggered.
 * </p>
 * <p>
 * By default this manager is not enabled.
 * </p>
 *
 * @author Christian Schudt
 */
public final class AvatarManager extends ExtensionManager implements SessionStatusListener, PresenceListener, MessageListener {

    private static final Logger logger = Logger.getLogger(VCardManager.class.getName());

    /**
     * Stores the current hash for a user. The Jid is a bare Jid.
     */
    private final Map<Jid, String> userHashes = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Jid, Lock> requestingAvatarLocks = new ConcurrentHashMap<>();

    private final Set<AvatarChangeListener> avatarChangeListeners = new CopyOnWriteArraySet<>();

    private final ExecutorService avatarRequester;

    private final VCardManager vCardManager;

    private final Set<String> nonConformingResources = Collections.synchronizedSet(new HashSet<String>());

    private final Map<String, byte[]> avatarCache;

    private AvatarManager(final XmppSession xmppSession) {
        super(xmppSession, AvatarMetadata.NAMESPACE + "+notify", AvatarMetadata.NAMESPACE);

        vCardManager = xmppSession.getExtensionManager(VCardManager.class);
        avatarCache = new DirectoryCache(new File(xmppSession.getConfiguration().getCacheDirectory(), "avatars"));

        avatarRequester = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Avatar Request Thread");
                thread.setDaemon(true);
                return thread;
            }
        });

        xmppSession.addSessionStatusListener(this);
        xmppSession.addPresenceListener(this);
        xmppSession.addMessageListener(this);
        setEnabled(false);
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

    private void notifyListeners(Jid contact, byte[] avatar) {

        AvatarChangeEvent avatarChangeEvent = new AvatarChangeEvent(AvatarManager.this, contact, avatar);
        for (AvatarChangeListener avatarChangeListener : avatarChangeListeners) {
            try {
                avatarChangeListener.avatarChanged(avatarChangeEvent);
            } catch (Exception e1) {
                logger.log(Level.WARNING, e1.getMessage(), e1);
            }
        }
    }

    private byte[] getAvatarByVCard(Jid contact) throws XmppException {
        byte[] avatar = null;

        Lock lock = new ReentrantLock();
        Lock existingLock = requestingAvatarLocks.putIfAbsent(contact, lock);
        if (existingLock != null) {
            lock = existingLock;
        }

        lock.lock();
        try {
            // Let's see, if there's a stored image already.
            String hash = userHashes.get(contact);
            // "" means, the user is known to have no avatar. Therefore don't try to load anything.
            if (!"".equals(hash)) {
                if (hash != null) {
                    byte[] imageData = loadFromCache(hash);
                    if (imageData != null) {
                        avatar = imageData;
                    }
                }
                if (avatar == null) {
                    // If there's no avatar for that user, create an empty avatar and load it.
                    avatar = new byte[0];
                    hash = "";
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
                                avatar = image.getValue();
                            }
                        }
                    }
                    userHashes.put(contact, hash);
                    if (!Arrays.equals(avatar, new byte[0])) {
                        storeToCache(hash, avatar);
                    }
                }
            }
            return avatar;
        } finally {
            lock.unlock();
            requestingAvatarLocks.remove(contact);
        }
    }

    private synchronized byte[] loadFromCache(String hash) {
        return avatarCache.get(hash + ".avatar");
    }

    private synchronized void storeToCache(String hash, byte[] image) {
        avatarCache.put(hash + ".avatar", image);
    }

    /**
     * Gets the user avatar from the user's vCard.
     *
     * @param contact The contact.
     * @return The contact's avatar or null, if it has no avatar.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     */
    public byte[] getAvatar(Jid contact) throws XmppException {
        return getAvatarByVCard(contact.asBareJid());
    }

    /**
     * Publishes an avatar to your VCard.
     *
     * @param imageData The avatar image data, which must be in PNG format.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0153.html#publish">3.1 User Publishes Avatar</a>
     */
    public void publishAvatar(byte[] imageData) throws XmppException {

        if (imageData != null) {
            String hash = XmppUtils.hash(imageData);
            publishToVCard(imageData, null, hash);
            publishToPersonalEventingService(imageData, hash, new AvatarMetadata.Info(imageData.length, hash, hash));
        } else {
            publishToVCard(null, null, null);
            publishToPersonalEventingService(null, null, null);
        }
    }

    /**
     * Publishes an avatar to the VCard and uses XEP-0153 to notify the contacts about the update.
     *
     * @param avatar The avatar or null, if the avatar is reset.
     * @param type   The image type.
     * @param hash   The hash.
     * @throws rocks.xmpp.core.XmppException If an XMPP exception occurs.
     */
    private void publishToVCard(byte[] avatar, String type, String hash) throws XmppException {

        VCard vCard = vCardManager.getVCard();

        if (avatar != null) {
            // Within a given session, a client MUST NOT attempt to upload a given avatar image more than once.
            // The client MAY upload the avatar image to the vCard on login and after that MUST NOT upload the vCard again
            // unless the user actively changes the avatar image.
            if (vCard.getPhoto() == null || !Arrays.equals(vCard.getPhoto().getValue(), avatar)) {
                userHashes.put(xmppSession.getConnectedResource().asBareJid(), hash);
                // If either there is avatar yet, or the old avatar is different from the new one: update
                vCard.setPhoto(new VCard.Image(type, avatar));
                vCardManager.setVCard(vCard);
            }
        } else {
            userHashes.put(xmppSession.getConnectedResource().asBareJid(), "");
            // If there's currently a photo, we want to reset it.
            if (vCard.getPhoto() != null && vCard.getPhoto().getValue() != null) {
                vCard.setPhoto(null);
                vCardManager.setVCard(vCard);
            }
        }
    }

    /**
     * Publishes an avatar to the personal eventing service.
     *
     * @param avatar The avatar or null, if the avatar is reset.
     * @param itemId The item id.
     * @param info   The info element.
     * @throws rocks.xmpp.core.XmppException If an XMPP exception occurs.
     */
    private void publishToPersonalEventingService(byte[] avatar, String itemId, AvatarMetadata.Info info) throws XmppException {
        PubSubService personalEventingService = xmppSession.getExtensionManager(PubSubManager.class).createPersonalEventingService();
        if (avatar != null) {
            if (info.getUrl() == null) {
                // Publish image.
                personalEventingService.getNode(AvatarData.NAMESPACE).publish(itemId, new AvatarData(avatar));
            }
            // Publish meta data.
            personalEventingService.getNode(AvatarMetadata.NAMESPACE).publish(itemId, new AvatarMetadata(info));
        } else {
            personalEventingService.getNode(AvatarMetadata.NAMESPACE).publish(itemId, new AvatarMetadata());
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

    @Override
    public void handleMessage(MessageEvent e) {
        if (e.isIncoming() && isEnabled()) {
            Message message = e.getMessage();
            Event event = message.getExtension(Event.class);
            if (event != null) {
                for (Item item : event.getItems()) {
                    if (item.getPayload() instanceof AvatarMetadata) {
                        AvatarMetadata avatarMetadata = (AvatarMetadata) item.getPayload();

                        // Empty avatar
                        if (avatarMetadata.getInfoList().isEmpty()) {
                            notifyListeners(message.getFrom().asBareJid(), null);
                        } else {
                            try {
                                // Check if we have a cached avatar.
                                byte[] cachedImage = loadFromCache(item.getId());
                                if (cachedImage != null) {
                                    notifyListeners(message.getFrom().asBareJid(), cachedImage);
                                } else {
                                    // We don't have a cached copy, let's retrieve it.

                                    // Determine the best info
                                    AvatarMetadata.Info chosenInfo = null;
                                    // Check if there's an avatar, which is stored in PubSub node (and therefore must be in PNG format).
                                    for (AvatarMetadata.Info info : avatarMetadata.getInfoList()) {
                                        if (info.getUrl() == null) {
                                            chosenInfo = info;
                                        }
                                    }

                                    // If only URLs are available, choose the first URL.
                                    if (chosenInfo == null) {
                                        for (AvatarMetadata.Info info : avatarMetadata.getInfoList()) {
                                            if (info.getUrl() != null) {
                                                chosenInfo = info;
                                                break;
                                            }
                                        }
                                    }

                                    if (chosenInfo != null && chosenInfo.getUrl() != null) {
                                        URLConnection urlConnection = chosenInfo.getUrl().openConnection();
                                        String type = urlConnection.getContentType();
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        // Download the image file.
                                        try (InputStream in = urlConnection.getInputStream()) {
                                            byte data[] = new byte[4096];
                                            int n;
                                            while ((n = in.read(data, 0, 4096)) != -1) {
                                                baos.write(data, 0, n);
                                            }
                                        }
                                        byte[] data = baos.toByteArray();
                                        storeToCache(item.getId(), data);
                                        notifyListeners(message.getFrom().asBareJid(), data);

                                    } else {
                                        PubSubService pubSubService = xmppSession.getExtensionManager(PubSubManager.class).createPubSubService(message.getFrom());

                                        List<Item> items = pubSubService.getNode(AvatarData.NAMESPACE).getItems(item.getId());
                                        if (!items.isEmpty()) {
                                            Item i = items.get(0);
                                            if (i.getPayload() instanceof AvatarData) {
                                                AvatarData avatarData = (AvatarData) i.getPayload();
                                                storeToCache(item.getId(), avatarData.getData());
                                                notifyListeners(message.getFrom().asBareJid(), avatarData.getData());
                                            }
                                        }
                                    }
                                }
                            } catch (XmppException | IOException e1) {
                                logger.log(Level.WARNING, e1.getMessage(), e1);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handlePresence(PresenceEvent e) {
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
                        byte[] imageData = loadFromCache(avatarUpdate.getHash());
                        byte[] avatar = null;
                        if (imageData != null) {
                            avatar = imageData;
                        }
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
                                Presence lastPresence = xmppSession.getPresenceManager().getLastSentPresence();
                                Presence presence;
                                if (lastPresence != null) {
                                    presence = new Presence(null, lastPresence.getType(), lastPresence.getShow(), lastPresence.getStatuses(), lastPresence.getPriority(), null, null, lastPresence.getLanguage(), null, null);
                                } else {
                                    presence = new Presence();
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

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            avatarChangeListeners.clear();
            requestingAvatarLocks.clear();
            nonConformingResources.clear();
            userHashes.clear();
            avatarRequester.shutdown();
        }
    }

//    /**
//     * Gets the avatar cache.
//     *
//     * @return The avatar cache.
//     */
//    public synchronized AvatarCache getAvatarCache() {
//        return avatarCache;
//    }
//
//    /**
//     * Sets the avatar cache.
//     *
//     * @param avatarCache The avatar cache.
//     */
//    public synchronized void setAvatarCache(AvatarCache avatarCache) {
//        this.avatarCache = avatarCache;
//    }
}
