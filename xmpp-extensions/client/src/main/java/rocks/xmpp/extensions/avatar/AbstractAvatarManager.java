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

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.cache.DirectoryCache;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * An abstract avatar manager, which provides a common implementation for both avatar protocols.
 *
 * @author Christian Schudt
 * @see AvatarManager
 */
public abstract class AbstractAvatarManager extends Manager implements AvatarManager {

    private static final System.Logger logger = System.getLogger(AbstractAvatarManager.class.getName());

    private final Set<Consumer<AvatarChangeEvent>> avatarChangeListeners = new CopyOnWriteArraySet<>();

    private final Map<String, byte[]> avatarCache;

    protected AbstractAvatarManager(XmppSession xmppSession) {
        super(xmppSession, true);
        this.avatarCache = xmppSession.getConfiguration().getCacheDirectory() != null ? new DirectoryCache(
                xmppSession.getConfiguration().getCacheDirectory().resolve("avatars")) : null;
    }

    protected void notifyListeners(Jid contact, byte[] avatar) {
        XmppUtils.notifyEventListeners(avatarChangeListeners,
                new AvatarChangeEvent(AbstractAvatarManager.this, contact, avatar));
    }

    public final byte[] loadFromCache(String hash) {
        if (avatarCache != null) {
            try {
                return avatarCache.get(hash + ".avatar");
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING, "Could not read avatar from cache.", e);
            }
        }
        return null;
    }

    public final void storeToCache(String hash, byte[] image) {
        if (avatarCache != null) {
            try {
                avatarCache.put(hash + ".avatar", image);
            } catch (Exception e) {
                logger.log(System.Logger.Level.WARNING, "Could not write avatar to cache.", e);
            }
        }
    }

    @Override
    public final AsyncResult<BufferedImage> getAvatarImage(Jid contact) {
        return this.getAvatar(requireNonNull(contact)).thenApply(bitmap -> {
            try {
                return bitmap == null ? null : asBufferedImage(bitmap);
            } catch (ConversionException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public final AsyncResult<Void> publishAvatarImage(BufferedImage bufferedImage) throws XmppException {
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
    @Override
    public void addAvatarChangeListener(Consumer<AvatarChangeEvent> avatarChangeListener) {
        avatarChangeListeners.add(avatarChangeListener);
    }

    /**
     * Removes a previously added avatar listener.
     *
     * @param avatarChangeListener The avatar listener.
     */
    @Override
    public void removeAvatarChangeListener(Consumer<AvatarChangeEvent> avatarChangeListener) {
        avatarChangeListeners.remove(avatarChangeListener);
    }

    /**
     * Converts {@code bitmap} into {@link BufferedImage}.
     *
     * @param bitmap The bitmap to convert. Must not be {@code null}.
     * @return Instance of {@link BufferedImage} created from {@code bitmap}. Never {@code null}.
     * @throws ConversionException if conversion failed
     */
    static BufferedImage asBufferedImage(final byte[] bitmap) throws ConversionException {
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
    private static byte[] asPNG(final BufferedImage bufferedImage) throws ConversionException {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (!ImageIO.write(requireNonNull(bufferedImage), "png", outputStream)) {
                throw new ConversionException();
            }
            return outputStream.toByteArray();
        } catch (final IOException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Indicates the inability to convert a value from one data type into another.
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
}
