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
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.extensions.vcard.avatar.VCardBasedAvatarsProtocol;
import rocks.xmpp.extensions.vcard.temp.VCardManager;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.cache.DirectoryCache;
import rocks.xmpp.util.concurrent.AsyncResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * This class manages avatar updates as described in <a href="https://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a> and <a href="https://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a>.
 * <p>
 * Whenever an avatar update is received by a contact, either via the presence based avatar extension (XEP-0153) or the PEP-based notification (XEP-0084), the registered listeners are triggered.
 * </p>
 * <p>
 * By default this manager is not enabled.
 * </p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
public final class AvatarManager extends Manager {

    private static final Logger logger = Logger.getLogger(VCardManager.class.getName());

    private final Set<Consumer<AvatarChangeEvent>> avatarChangeListeners = new CopyOnWriteArraySet<>();

    private final Map<String, byte[]> avatarCache;

    private final Consumer<PresenceEvent> inboundPresenceListener;

    private final Consumer<PresenceEvent> outboundPresenceListener;

    private final Consumer<MessageEvent> inboundMessageListener;

    private final VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol;

    private final UserAvatarProtocol userAvatarProtocol;

    private AvatarManager(final XmppSession xmppSession) {
        super(xmppSession, true);

        this.avatarCache = xmppSession.getConfiguration().getCacheDirectory() != null ? new DirectoryCache(xmppSession.getConfiguration().getCacheDirectory().resolve("avatars")) : null;
        this.vCardBasedAvatarsProtocol = new VCardBasedAvatarsProtocol(xmppSession, this::notifyListeners, this::loadFromCache, this::storeToCache);
        this.userAvatarProtocol = new UserAvatarProtocol(xmppSession, this::notifyListeners, this::loadFromCache, this::storeToCache);
        this.inboundPresenceListener = vCardBasedAvatarsProtocol::handleInboundPresence;
        this.outboundPresenceListener = vCardBasedAvatarsProtocol::handleInboundPresence;
        this.inboundMessageListener = userAvatarProtocol::handleInboundMessage;
    }

    @Override
    protected final void onEnable() {
        super.onEnable();
        xmppSession.addInboundPresenceListener(inboundPresenceListener);
        xmppSession.addOutboundPresenceListener(outboundPresenceListener);
        xmppSession.addInboundMessageListener(inboundMessageListener);
    }

    @Override
    protected final void onDisable() {
        super.onDisable();
        xmppSession.removeInboundPresenceListener(inboundPresenceListener);
        xmppSession.removeOutboundPresenceListener(outboundPresenceListener);
        xmppSession.removeInboundMessageListener(inboundMessageListener);
    }

    private void notifyListeners(Jid contact, byte[] avatar) {
        XmppUtils.notifyEventListeners(avatarChangeListeners, new AvatarChangeEvent(AvatarManager.this, contact, avatar));
    }

    private byte[] loadFromCache(String hash) {
        if (avatarCache != null) {
            try {
                return avatarCache.get(hash + ".avatar");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not read avatar from cache.", e);
            }
        }
        return null;
    }

    private void storeToCache(String hash, byte[] image) {
        if (avatarCache != null) {
            try {
                avatarCache.put(hash + ".avatar", image);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not write avatar to cache.", e);
            }
        }
    }

    /**
     * Gets the user avatar from the user's vCard.
     *
     * @param contact The contact.
     * @return The async result with the contact's avatar or null, if it has no avatar.
     */
    public final AsyncResult<byte[]> getAvatar(Jid contact) {
        return vCardBasedAvatarsProtocol.getAvatarByVCard(contact.asBareJid());
    }

    /**
     * Gets the user avatar from the user's vCard.
     *
     * @param contact The contact. Must not be {@code null}.
     * @return The async result with the contact's avatar or null, if it has no avatar.
     */
    public final AsyncResult<BufferedImage> getAvatarImage(final Jid contact) {
        return this.getAvatar(requireNonNull(contact)).thenApply(bitmap -> {
            try {
                return bitmap == null ? null : asBufferedImage(bitmap);
            } catch (ConversionException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Publishes an avatar to your VCard.
     *
     * @param imageData The avatar image data, which must be in PNG format. {@code null} resets the avatar.
     * @return The async result.
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#publish">3.1 User Publishes Avatar</a>
     */
    public final AsyncResult<Void> publishAvatar(byte[] imageData) {

        String hash = imageData != null ? XmppUtils.hash(imageData) : null;

        // Try publishing to vCard first. If this fails, don't immediately throw an exception, but try PEP first.
        return vCardBasedAvatarsProtocol.publishToVCard(imageData, null, hash)
                .whenComplete((result, e) -> {
                    if (e != null) {
                        logger.warning("Failed to publish avatar to vCard.");
                    }
                })
                .thenCompose((aVoid) -> userAvatarProtocol.publishToPersonalEventingService(imageData, hash))
                .thenRun(() -> {
                });
    }

    /**
     * Publishes an avatar to your VCard.
     *
     * @param bufferedImage The avatar image, which must be in PNG format. {@code null}
     *                      resets the avatar.
     * @return The async result.
     * @throws XmppException If the image could not be converted to PNG.
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#publish">3.1 User Publishes Avatar</a>
     */
    public final AsyncResult<Void> publishAvatarImage(final BufferedImage bufferedImage) throws XmppException {
        try {
            return this.publishAvatar(bufferedImage == null ? null : asPNG(bufferedImage));
        } catch (final ConversionException e) {
            throw new XmppException(e);
        }
    }

    /**
     * Adds an avatar listener, to listen for avatar updates.
     *
     * @param avatarChangeListener The avatar listener.
     */
    public final void addAvatarChangeListener(Consumer<AvatarChangeEvent> avatarChangeListener) {
        avatarChangeListeners.add(avatarChangeListener);
    }

    /**
     * Removes a previously added avatar listener.
     *
     * @param avatarChangeListener The avatar listener.
     */
    public final void removeAvatarChangeListener(Consumer<AvatarChangeEvent> avatarChangeListener) {
        avatarChangeListeners.remove(avatarChangeListener);
    }

    /**
     * Converts {@code bitmap} into {@link BufferedImage}.
     *
     * @param bitmap The bitmap to convert. Must not be {@code null}.
     * @return Instance of {@link BufferedImage} created from {@code bitmap}.
     * Never {@code null}.
     * @throws ConversionException if conversion failed
     */
    static final BufferedImage asBufferedImage(final byte[] bitmap) throws ConversionException {
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(requireNonNull(bitmap))) {
            return ofNullable(ImageIO.read(inputStream)).orElseThrow(ConversionException::new);
        } catch (final IOException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Converts {@code image} into {@code byte[]}.
     *
     * @param bufferedImage The image to convert. Must not be {@code null}.
     * @return PNG bitmap created from {@code image}. Never {@code null}.
     * @throws ConversionException if conversion failed
     */
    private static final byte[] asPNG(final BufferedImage bufferedImage) throws ConversionException {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (!ImageIO.write(requireNonNull(bufferedImage), "png", outputStream))
                throw new ConversionException();
            return outputStream.toByteArray();
        } catch (final IOException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Indicates the inability to convert a value from one data type into
     * another.
     *
     * @author Markus KARG (markus@headcrashing.eu)
     */
    @SuppressWarnings("serial")
    static final class ConversionException extends Exception {

        ConversionException() {
            // Intentionally left blank.
        }

        ConversionException(final Throwable cause) {
            super(cause);
        }
    }

    @Override
    protected void dispose() {
        vCardBasedAvatarsProtocol.backupUserHashes();
        avatarChangeListeners.clear();
    }
}
