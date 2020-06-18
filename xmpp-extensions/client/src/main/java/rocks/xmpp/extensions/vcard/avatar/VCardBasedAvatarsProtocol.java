/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.vcard.avatar;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.InboundPresenceHandler;
import rocks.xmpp.core.stanza.OutboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.extensions.avatar.AbstractAvatarManager;
import rocks.xmpp.extensions.muc.model.user.MucUser;
import rocks.xmpp.extensions.vcard.avatar.model.AvatarUpdate;
import rocks.xmpp.extensions.vcard.temp.VCardManager;
import rocks.xmpp.extensions.vcard.temp.model.VCard;
import rocks.xmpp.im.subscription.PresenceManager;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.cache.DirectoryCache;
import rocks.xmpp.util.concurrent.AsyncResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The implementation of <a href="https://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a>.
 *
 * @author Christian Schudt
 */
public final class VCardBasedAvatarsProtocol extends AbstractAvatarManager implements InboundPresenceHandler, OutboundPresenceHandler, ExtensionProtocol {

    private static final System.Logger logger = System.getLogger(VCardBasedAvatarsProtocol.class.getName());

    private final Consumer<PresenceEvent> inboundPresenceListener = this::handleInboundPresence;

    private final Consumer<PresenceEvent> outboundPresenceListener = this::handleOutboundPresence;

    final Set<String> nonConformingResources = Collections.synchronizedSet(new HashSet<>());

    /**
     * Stores the current hash for a user. The Jid is a bare Jid.
     */
    final Map<Jid, String> userHashes = new ConcurrentHashMap<>();

    private final Map<String, byte[]> hashesLocalStore;

    private final VCardManager vCardManager;

    private final Map<Jid, AsyncResult<byte[]>> avatarRequests = new ConcurrentHashMap<>();

    public VCardBasedAvatarsProtocol(XmppSession xmppSession) {
        this(xmppSession,
                xmppSession.getManager(VCardManager.class),
                xmppSession.getConfiguration().getCacheDirectory() != null ? new DirectoryCache(xmppSession.getConfiguration().getCacheDirectory().resolve("userhashes")) : Collections.synchronizedMap(new HashMap<>()));
    }

    VCardBasedAvatarsProtocol(XmppSession xmppSession,
                              VCardManager vCardManager,
                              Map<String, byte[]> hashesLocalStore) {
        super(xmppSession);
        this.vCardManager = vCardManager;
        this.hashesLocalStore = hashesLocalStore;
        restoreUserHashes();
    }

    @Override
    protected final void onEnable() {
        super.onEnable();
        xmppSession.addInboundPresenceListener(inboundPresenceListener);
        xmppSession.addOutboundPresenceListener(outboundPresenceListener);
    }

    @Override
    protected final void onDisable() {
        super.onDisable();
        xmppSession.removeInboundPresenceListener(inboundPresenceListener);
        xmppSession.removeOutboundPresenceListener(outboundPresenceListener);
    }

    @Override
    public final void handleInboundPresence(PresenceEvent e) {
        final Presence presence = e.getPresence();

        // If the presence has an avatar update information.
        final AvatarUpdate avatarUpdate = presence.getExtension(AvatarUpdate.class);

        // 4.3 Multiple Resources
        if (presence.getFrom().asBareJid().equals(xmppSession.getLocalXmppAddress().asBareJid()) && presence.getFrom().getResource() != null && !presence.getFrom().getResource().equals(xmppSession.getLocalXmppAddress().getResource())) {
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
                if (avatarUpdate.getHash() != null && !avatarUpdate.getHash().equals(userHashes.get(xmppSession.getLocalXmppAddress().asBareJid()))) {
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
                    // If the avatar was either known before or could be successfully retrieved from the vCard.
                    getAvatar(contact).whenComplete((avatarResult, ex) -> {
                        if (ex == null) {
                            notifyListeners(contact, avatarResult);
                        } else {
                            logger.log(System.Logger.Level.WARNING, () -> "Failed to retrieve vCard based avatar for user: " + contact, ex);
                        }
                    });
                }
            }
        }
    }

    void resetHash() {
        // Remove our own hash and send an empty presence.
        // The lack of our own hash, will download the vCard and either broadcasts the image hash or an empty hash.
        userHashes.remove(xmppSession.getLocalXmppAddress().asBareJid());
        Presence presence = xmppSession.getManager(PresenceManager.class).getLastSentPresence();
        if (presence == null) {
            presence = new Presence();
        }
        presence.getExtensions().clear();
        xmppSession.send(presence);
    }

    @Override
    public final void handleOutboundPresence(PresenceEvent e) {
        final Presence presence = e.getPresence();
        if (presence.isAvailable() && nonConformingResources.isEmpty()) {
            // 1. If a client supports the protocol defined herein, it MUST include the update child element in every presence broadcast it sends and SHOULD also include the update child in directed presence stanzas.

            String myHash = userHashes.get(xmppSession.getLocalXmppAddress().asBareJid());

            if (myHash == null) {
                // 2. If a client is not yet ready to advertise an image, it MUST send an empty update child element:
                presence.putExtension(new AvatarUpdate());

                // Load my own avatar in order to advertise an image.
                getAvatar(xmppSession.getLocalXmppAddress().asBareJid()).whenComplete((avatarResult, ex) -> {
                    if (ex == null) {
                        notifyListeners(xmppSession.getLocalXmppAddress().asBareJid(), avatarResult);

                        // If the client subsequently obtains an avatar image (e.g., by updating or retrieving the vCard), it SHOULD then publish a new <presence/> stanza with character data in the <photo/> element.
                        Presence lastPresence = xmppSession.getManager(PresenceManager.class).getLastSentPresence();
                        Presence presence1;
                        if (lastPresence != null) {
                            presence1 = new Presence(null, lastPresence.getType(), lastPresence.getShow(), lastPresence.getStatuses(), lastPresence.getPriority(), null, null, lastPresence.getLanguage(), null, null);
                        } else {
                            presence1 = new Presence();
                        }
                        // Send out a presence, which will be filled with the extension later, because we now know or own avatar and have the hash for it.
                        xmppSession.send(presence1);
                    } else {
                        logger.log(System.Logger.Level.WARNING, "Failed to retrieve own vCard based avatar.");
                    }
                });
            } else if (!presence.hasExtension(AvatarUpdate.class)) {
                presence.putExtension(new AvatarUpdate(myHash));
            }
        }
    }

    @Override
    public final AsyncResult<byte[]> getAvatar(Jid contact) {

        // Let's see, if there's a stored image already.
        String hash = userHashes.get(contact.asBareJid());
        // "" means, the user is known to have no avatar. Therefore don't try to load anything.
        if (!"".equals(hash)) {
            if (hash != null) {
                byte[] imageData = loadFromCache(hash);
                if (imageData != null) {
                    return new AsyncResult<>(CompletableFuture.completedFuture(imageData));
                }
            }

            return avatarRequests.computeIfAbsent(contact.asBareJid(), key -> {

                // Load the vCard for that user
                AsyncResult<VCard> vCard = vCardManager.getVCard(contact);
                return vCard.handle((result, exc) -> {
                    // If the user has no vCard (e.g. server returned <item-not-found/> or <service-unavailable/>),
                    // the user also has no avatar obviously.
                    if (exc != null && !(exc instanceof StanzaErrorException)) {
                        throw new CompletionException(exc);
                    }
                    // If there's no avatar for that user, create an empty avatar and load it.
                    byte[] avatar = new byte[0];
                    String hash1 = "";

                    if (result != null) {
                        // And check if it has a photo.
                        VCard.Image image = result.getPhoto();
                        if (image != null && image.getValue() != null) {
                            hash1 = XmppUtils.hash(image.getValue());
                            avatar = image.getValue();
                        }
                    }
                    userHashes.put(contact.asBareJid(), hash1);
                    if (!Arrays.equals(avatar, new byte[0])) {
                        storeToCache(hash1, avatar);
                    }
                    return avatar;
                });
            }).whenComplete((avatar, exc) -> avatarRequests.remove(contact.asBareJid()));
        }

        return new AsyncResult<>(CompletableFuture.completedFuture(new byte[0]));
    }

    /**
     * Publishes an avatar to the VCard and uses XEP-0153 to notify the contacts about the update.
     *
     * @param avatar The avatar or null, if the avatar is reset.
     * @return The async result.
     */
    @Override
    public final AsyncResult<Void> publishAvatar(byte[] avatar) {

        String hash = avatar != null ? XmppUtils.hash(avatar) : null;
        return vCardManager.getVCard().handle((result, exc) -> {
            // If there's no vCard yet (e.g. <item-not-found/>), create a new one.
            return result != null ? result : new VCard();
        }).thenCompose(vCard -> {
            if (avatar != null) {
                // Within a given session, a client MUST NOT attempt to upload a given avatar image more than once.
                // The client MAY upload the avatar image to the vCard on login and after that MUST NOT upload the vCard again
                // unless the user actively changes the avatar image.
                if (vCard.getPhoto() == null || !Arrays.equals(vCard.getPhoto().getValue(), avatar)) {
                    userHashes.put(xmppSession.getLocalXmppAddress().asBareJid(), hash);
                    // If either there is avatar yet, or the old avatar is different from the new one: update
                    vCard.setPhoto(new VCard.Image(null, avatar));
                    return vCardManager.setVCard(vCard);
                }
            } else {
                userHashes.put(xmppSession.getLocalXmppAddress().asBareJid(), "");
                // If there's currently a photo, we want to reset it.
                if (vCard.getPhoto() != null && vCard.getPhoto().getValue() != null) {
                    vCard.setPhoto(null);
                    return vCardManager.setVCard(vCard);
                }
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    private void backupUserHashes() {
        this.userHashes.forEach((jid, hash) -> this.hashesLocalStore.put(jid + ".userhash", hash.getBytes(StandardCharsets.UTF_16)));
    }

    private void restoreUserHashes() {
        this.hashesLocalStore.forEach((jid, hash) -> this.userHashes.put(Jid.of(jid.replace(".userhash", "")), new String(hash, StandardCharsets.UTF_16)));
    }

    @Override
    public final Set<String> getFeatures() {
        return Collections.emptySet();
    }

    @Override
    protected void dispose() {
        backupUserHashes();
    }
}
