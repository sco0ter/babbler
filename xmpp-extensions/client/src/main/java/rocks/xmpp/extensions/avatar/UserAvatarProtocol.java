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

package rocks.xmpp.extensions.avatar;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.InboundMessageHandler;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.extensions.address.model.Address;
import rocks.xmpp.extensions.address.model.Addresses;
import rocks.xmpp.extensions.avatar.model.data.AvatarData;
import rocks.xmpp.extensions.avatar.model.metadata.AvatarMetadata;
import rocks.xmpp.extensions.pubsub.PubSubManager;
import rocks.xmpp.extensions.pubsub.PubSubService;
import rocks.xmpp.extensions.pubsub.model.Item;
import rocks.xmpp.extensions.pubsub.model.event.Event;
import rocks.xmpp.util.concurrent.AsyncResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The implementation of <a href="https://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a>.
 *
 * @author Christian Schudt
 */
public final class UserAvatarProtocol implements InboundMessageHandler, ExtensionProtocol {

    private static final Logger logger = Logger.getLogger(UserAvatarProtocol.class.getName());

    private final XmppSession xmppSession;

    private final BiConsumer<Jid, byte[]> notifyListeners;

    private final Function<String, byte[]> loadFromCache;

    private final BiConsumer<String, byte[]> storeToCache;

    public UserAvatarProtocol(XmppSession xmppSession,
                              BiConsumer<Jid, byte[]> notifyListeners,
                              Function<String, byte[]> loadFromCache,
                              BiConsumer<String, byte[]> storeToCache) {
        this.xmppSession = xmppSession;
        this.notifyListeners = notifyListeners;
        this.loadFromCache = loadFromCache;
        this.storeToCache = storeToCache;
    }

    @Override
    public final boolean isEnabled() {
        return true;
    }

    @Override
    public final Set<String> getFeatures() {
        return Collections.emptySet();
    }

    @Override
    public final void handleInboundMessage(MessageEvent e) {
        final Message message = e.getMessage();
        Event event = message.getExtension(Event.class);
        if (event != null) {
            Addresses addresses = message.getExtension(Addresses.class);
            if (addresses != null) {
                // See https://xmpp.org/extensions/xep-0163.html#notify-addressing
                for (Address address : addresses.getAddresses()) {
                    if (address.getType() == Address.Type.REPLYTO && xmppSession.getConnectedResource().equals(address.getJid())) {
                        // Don't notify if the message came from our own connected resource.
                        return;
                    }
                }
            }
            for (final Item item : event.getItems()) {
                if (item.getPayload() instanceof AvatarMetadata) {
                    AvatarMetadata avatarMetadata = (AvatarMetadata) item.getPayload();

                    // Empty avatar
                    if (avatarMetadata.getInfoList().isEmpty()) {
                        notifyListeners.accept(message.getFrom().asBareJid(), null);
                    } else {

                        // Check if we have a cached avatar.
                        byte[] cachedImage = loadFromCache.apply(item.getId());
                        if (cachedImage != null) {
                            notifyListeners.accept(message.getFrom().asBareJid(), cachedImage);
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
                                try {
                                    URLConnection urlConnection = chosenInfo.getUrl().openConnection();
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    // Download the image file.
                                    try (InputStream in = urlConnection.getInputStream()) {
                                        byte[] data = new byte[4096];
                                        int n;
                                        while ((n = in.read(data, 0, 4096)) != -1) {
                                            baos.write(data, 0, n);
                                        }
                                    }
                                    byte[] data = baos.toByteArray();
                                    storeToCache.accept(item.getId(), data);
                                    notifyListeners.accept(message.getFrom().asBareJid(), data);
                                } catch (IOException e1) {
                                    logger.log(Level.WARNING, "Failed to download avatar from advertised URL: {0}.", chosenInfo.getUrl());
                                }
                            } else {
                                PubSubService pubSubService = xmppSession.getManager(PubSubManager.class).createPubSubService(message.getFrom());
                                pubSubService.node(AvatarData.NAMESPACE).getItems(item.getId()).whenComplete((items, ex) -> {
                                    if (ex != null) {
                                        logger.log(Level.WARNING, () -> String.format("Failed to retrieve avatar '%s' from PEP service for user '%s'", item.getId(), message.getFrom()));
                                    } else {
                                        if (!items.isEmpty()) {
                                            Item i = items.get(0);
                                            if (i.getPayload() instanceof AvatarData) {
                                                AvatarData avatarData = (AvatarData) i.getPayload();
                                                storeToCache.accept(item.getId(), avatarData.getData());
                                                notifyListeners.accept(message.getFrom().asBareJid(), avatarData.getData());
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Publishes an avatar to the personal eventing service.
     *
     * @param avatar The avatar or null, if the avatar is reset.
     * @param itemId The item id.
     * @return The async result.
     */
    public AsyncResult<String> publishToPersonalEventingService(byte[] avatar, String itemId) {
        AvatarMetadata.Info info = avatar != null ? new AvatarMetadata.Info(avatar.length, itemId, itemId) : null;
        PubSubService personalEventingService = xmppSession.getManager(PubSubManager.class).createPersonalEventingService();
        if (avatar != null) {
            if (info.getUrl() == null) {
                // Publish image.
                return personalEventingService.node(AvatarData.NAMESPACE).publish(itemId, new AvatarData(avatar));
            }
            // Publish meta data.
            return personalEventingService.node(AvatarMetadata.NAMESPACE).publish(itemId, new AvatarMetadata(info));
        } else {
            return personalEventingService.node(AvatarMetadata.NAMESPACE).publish(itemId, new AvatarMetadata());
        }
    }
}
