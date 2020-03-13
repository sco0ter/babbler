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
import rocks.xmpp.util.concurrent.AsyncResult;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * In XMPP two extension protocols are defined which both deal with avatars.
 * <p>
 * This interface provides a common abstraction for both of these protocols, which involves these use cases:
 * Publishing avatars, retrieving avatars and listening for avatar updates.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a>
 * @see <a href="https://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a>
 */
public interface AvatarManager {

    /**
     * Gets the user avatar as byte array.
     *
     * @param contact The contact. Must not be {@code null}.
     * @return The async result with the contact's avatar or null, if it has no avatar.
     */
    AsyncResult<byte[]> getAvatar(Jid contact);

    /**
     * Gets the user avatar.
     *
     * @param contact The contact. Must not be {@code null}.
     * @return The async result with the contact's avatar or null, if it has no avatar.
     */
    AsyncResult<BufferedImage> getAvatarImage(Jid contact);

    /**
     * Publishes an avatar.
     *
     * @param imageData The avatar image data, which must be in PNG format. {@code null}
     *                  resets the avatar.
     * @return The async result.
     */
    AsyncResult<Void> publishAvatar(byte[] imageData);

    /**
     * Publishes an avatar.
     *
     * @param bufferedImage The avatar image, which must be in PNG format. {@code null}
     *                      resets the avatar.
     * @return The async result.
     * @throws XmppException If the image could not be converted to PNG.
     */
    AsyncResult<Void> publishAvatarImage(BufferedImage bufferedImage) throws XmppException;

    /**
     * Adds an avatar change listener which listens for avatar updates from other contacts.
     *
     * @param avatarChangeListener The listener.
     * @see #removeAvatarChangeListener(Consumer)
     */
    void addAvatarChangeListener(Consumer<AvatarChangeEvent> avatarChangeListener);

    /**
     * Removes an avatar change listener.
     *
     * @param avatarChangeListener The listener.
     * @see #addAvatarChangeListener(Consumer)
     */
    void removeAvatarChangeListener(Consumer<AvatarChangeEvent> avatarChangeListener);
}
