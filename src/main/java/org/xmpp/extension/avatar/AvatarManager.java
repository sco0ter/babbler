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

import org.xmpp.Connection;
import org.xmpp.ConnectionEvent;
import org.xmpp.ConnectionListener;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.avatar.vcard.AvatarUpdate;
import org.xmpp.extension.vcard.VCard;
import org.xmpp.extension.vcard.VCardManager;
import org.xmpp.stanza.Presence;
import org.xmpp.stanza.PresenceEvent;
import org.xmpp.stanza.PresenceListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeoutException;
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

    private static final Logger logger = Logger.getLogger(VCardManager.class.getName());

    private final Map<byte[], Avatar> avatars = new ConcurrentHashMap<>();

    private final Map<Jid, byte[]> userAvatars = new ConcurrentHashMap<>();

    private final Set<AvatarChangeListener> avatarChangeListeners = new CopyOnWriteArraySet<>();

    protected AvatarManager(final Connection connection) {
        super(connection);
        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == Connection.Status.CLOSED) {
                    avatarChangeListeners.clear();
                    avatars.clear();
                }
            }
        });
        connection.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                final VCardManager vCardManager = connection.getExtensionManager(VCardManager.class);
                // If vCard base avatars are enabled.
                if (vCardManager != null && vCardManager.isEnabled()) {
                    final Presence presence = e.getPresence();
                    AvatarUpdate avatarUpdate = presence.getExtension(AvatarUpdate.class);
                    // If the presence has a avatar update information.
                    if (e.isIncoming() && avatarUpdate != null && avatarUpdate.getHash() != null) {
                        synchronized (avatars) {
                            // Check, if we already know this avatar.
                            Avatar avatar = avatars.get(avatarUpdate.getHash());
                            // If the hash is unknown.
                            Jid contact = presence.getFrom().toBareJid();
                            if (avatar == null) {
                                try {
                                    // Get the avatar for this user.
                                    avatar = getAvatar(contact);
                                } catch (TimeoutException e1) {
                                    logger.log(Level.WARNING, e1.getMessage(), e1);
                                }
                            }
                            // If the avatar was either known before or could be successfully retrieved from the vCard.
                            if (avatar != null) {
                                byte[] hash = userAvatars.get(contact);
                                // Compare the old hash and the new hash. If both are different notify listeners.
                                if (!Arrays.equals(hash, avatarUpdate.getHash())) {
                                    for (AvatarChangeListener avatarChangeListener : avatarChangeListeners) {
                                        try {
                                            avatarChangeListener.avatarChanged(new AvatarChangeEvent(AvatarManager.this, contact, avatar));
                                        } catch (Exception e1) {
                                            logger.log(Level.WARNING, e1.getMessage(), e1);
                                        }
                                    }
                                    // Then store the new hash for that user.
                                    userAvatars.put(contact, avatarUpdate.getHash());
                                }
                            }
                        }
                    } else if (!e.isIncoming() && vCardManager.isEnabled() && presence.isAvailable()) {
                        // 1. If a client supports the protocol defined herein, it MUST include the update child element in every presence broadcast it sends and SHOULD also include the update child in directed presence stanzas.

                        // 2. If a client is not yet ready to advertise an image, it MUST send an empty update child element, i.e.:
                        final Jid me = connection.getConnectedResource().toBareJid();
                        byte[] myHash = userAvatars.get(me);
                        if (myHash == null) {
                            // Load my own avatar in order to advertise an image.
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        getAvatar(me);
                                        // If the client subsequently obtains an avatar image (e.g., by updating or retrieving the vCard), it SHOULD then publish a new <presence/> stanza with character data in the <photo/> element.
                                        // As soon as the vCard has been loaded, broadcast presence, in order to update the avatar.
                                        Presence presence = connection.getPresenceManager().getLastSentPresence();
                                        if (presence == null) {
                                            presence = new Presence();
                                        }
                                        presence.getExtensions().clear();
                                        connection.send(presence);
                                    } catch (TimeoutException e1) {
                                        logger.log(Level.WARNING, e1.getMessage(), e1);
                                    }
                                }
                            }.start();
                            // Append an empty element, to indicate, we are not yet ready (vCard is being loaded).
                            presence.getExtensions().add(new AvatarUpdate());
                        } else {
                            try {
                                VCard vCard = vCardManager.getVCard();
                                // 3. If there is no avatar image to be advertised, the photo element MUST be empty
                                byte[] currentHash = new byte[0];
                                // If we have a avatar, include its hash.
                                if (vCard != null && vCard.getPhoto() != null && vCard.getPhoto().getValue() != null) {
                                    currentHash = getHash(vCard.getPhoto().getValue());
                                }
                                presence.getExtensions().add(new AvatarUpdate(currentHash));
                            } catch (TimeoutException e1) {
                                logger.log(Level.WARNING, e1.getMessage(), e1);
                            }
                        }
                    }
                }
            }
        });
    }

    private byte[] getHash(byte[] photo) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("sha-1");
            messageDigest.reset();
            messageDigest.update(photo);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return new byte[0];
    }

    /**
     * Gets the user avatar.
     *
     * @param user The user.
     * @return The user's avatar or null, if it has no avatar.
     * @throws TimeoutException If the operation timed out.
     */
    public Avatar getAvatar(Jid user) throws TimeoutException {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null.");
        }
        Avatar avatar = new Avatar(null, null);
        user = user.toBareJid();
        synchronized (avatars) {
            // Let's see, if there's a stored image already.
            byte[] hash = userAvatars.get(user);
            if (hash != null) {
                return avatars.get(hash);
            } else {
                // If there's no avatar for that user, load it.
                VCardManager vCardManager = connection.getExtensionManager(VCardManager.class);
                hash = new byte[0];

                // Load the vCard for that user
                VCard vCard = vCardManager.getVCard(user);
                if (vCard != null) {
                    // And check if it has a photo.
                    VCard.Image image = vCard.getPhoto();
                    if (image != null && image.getValue() != null) {
                        hash = getHash(image.getValue());
                        if (hash != null) {
                            avatar = new Avatar(image.getType(), image.getValue());
                        }
                    }
                }
                userAvatars.put(user, hash);
                avatars.put(hash, avatar);
            }
        }
        return avatar;
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return null;
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
