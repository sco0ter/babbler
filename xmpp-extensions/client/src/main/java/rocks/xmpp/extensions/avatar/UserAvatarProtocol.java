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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

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
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * The implementation of <a href="https://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a>.
 *
 * @author Christian Schudt
 */
public final class UserAvatarProtocol extends AbstractAvatarManager implements InboundMessageHandler, ExtensionProtocol {

    private static final System.Logger logger = System.getLogger(UserAvatarProtocol.class.getName());

    private Consumer<MessageEvent> inboundMessageListener = this::handleInboundMessage;

    public UserAvatarProtocol(XmppSession xmppSession) {
        super(xmppSession);
    }

    @Override
    protected final void onEnable() {
        super.onEnable();
        xmppSession.addInboundMessageListener(inboundMessageListener);
    }

    @Override
    protected final void onDisable() {
        super.onDisable();
        xmppSession.removeInboundMessageListener(inboundMessageListener);
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
            handleMetaData(event.getItems(), message.getFrom().asBareJid()).thenAccept(avatar -> {
                notifyListeners(message.getFrom().asBareJid(), avatar);
            });
        }
    }

    private CompletionStage<byte[]> handleMetaData(Iterable<Item> metadata, Jid contact) {
        for (final Item item : metadata) {
            if (item.getPayload() instanceof AvatarMetadata) {
                AvatarMetadata avatarMetadata = (AvatarMetadata) item.getPayload();

                // Empty avatar
                if (avatarMetadata.getInfoList().isEmpty()) {
                    return CompletableFuture.completedFuture(new byte[0]);
                } else {

                    // Check if we have a cached avatar.
                    byte[] cachedImage = loadFromCache(item.getId());
                    if (cachedImage != null) {
                        return CompletableFuture.completedFuture(cachedImage);
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
                                storeToCache(item.getId(), data);
                                return CompletableFuture.completedFuture(data);
                            } catch (IOException e1) {
                                logger.log(System.Logger.Level.WARNING, "Failed to download avatar from advertised URL: {0}.", chosenInfo.getUrl());
                            }
                        } else {
                            PubSubService pubSubService = xmppSession.getManager(PubSubManager.class).createPubSubService(contact.asBareJid());
                            return pubSubService.node(AvatarData.NAMESPACE).getItems(item.getId()).thenCompose(items -> {
                                if (!items.isEmpty()) {
                                    Item i = items.get(0);
                                    if (i.getPayload() instanceof AvatarData) {
                                        AvatarData avatarData = (AvatarData) i.getPayload();
                                        storeToCache(item.getId(), avatarData.getData());
                                        return CompletableFuture.completedFuture(avatarData.getData());
                                    }
                                }
                                return CompletableFuture.completedFuture(new byte[0]);
                            }).whenComplete((items, ex) -> {
                                if (ex != null) {
                                    logger.log(System.Logger.Level.WARNING, () -> String.format("Failed to retrieve avatar '%s' from PEP service for user '%s'", item.getId(), contact.asBareJid()));
                                }
                            });
                        }
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(new byte[0]);
    }

    @Override
    public final AsyncResult<byte[]> getAvatar(Jid contact) {
        PubSubService pubSubService = xmppSession.getManager(PubSubManager.class).createPubSubService(contact.asBareJid());
        return pubSubService.node(AvatarMetadata.NAMESPACE).getItems(1).thenCompose(items -> handleMetaData(items, contact));
    }

    /**
     * Publishes an avatar to the personal eventing service.
     *
     * @param imageData The avatar or null, if the avatar is reset.
     * @return The async result.
     */
    @Override
    public final AsyncResult<Void> publishAvatar(final byte[] imageData) {
        final String hash = imageData != null ? XmppUtils.hash(imageData) : null;
        final PubSubService personalEventingService = xmppSession.getManager(PubSubManager.class).createPersonalEventingService();
        final AsyncResult<String> publishResult;
        if (imageData != null) {
            // See https://xmpp.org/extensions/xep-0084.html#process-pubdata
            publishResult = personalEventingService.node(AvatarData.NAMESPACE).publish(hash, new AvatarData(imageData))
                    .thenCompose(nodeId -> {
                        // Publish image.
                        final AvatarMetadata.Info info = new AvatarMetadata.Info(imageData.length, hash, "image/png");
                        // Publish meta data.
                        // See https://xmpp.org/extensions/xep-0084.html#process-pubmeta
                        return personalEventingService.node(AvatarMetadata.NAMESPACE).publish(hash, new AvatarMetadata(info));
                    });
        } else {
            // See https://xmpp.org/extensions/xep-0084.html#pub-disable
            publishResult = personalEventingService.node(AvatarMetadata.NAMESPACE).publish(new AvatarMetadata());
        }
        return publishResult.thenRun(() -> {
        });
    }
}
