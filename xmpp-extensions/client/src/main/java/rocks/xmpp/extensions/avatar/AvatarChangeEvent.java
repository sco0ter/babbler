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

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EventObject;

import rocks.xmpp.addr.Jid;

/**
 * The avatar change event to notify about avatar updates.
 *
 * @author Christian Schudt
 */
public final class AvatarChangeEvent extends EventObject {

    private static final System.Logger LOGGER = System.getLogger(AvatarChangeEvent.class.getName());

    private final Jid contact;

    private final byte[] avatar;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    AvatarChangeEvent(AvatarManager source, Jid contact, byte[] avatar) {
        super(source);
        this.contact = contact;
        this.avatar = Arrays.copyOf(avatar, avatar.length);
    }

    /**
     * Gets the avatar image data.
     *
     * @return The avatar.
     */
    public final byte[] getAvatar() {
        return avatar;
    }

    /**
     * Gets the avatar image, or {@code null} if there is none.
     *
     * @return The avatar image.
     */
    public final BufferedImage getAvatarImage() {
        try {
            return this.avatar == null ? null : AbstractAvatarManager.asBufferedImage(this.avatar);
        } catch (final AbstractAvatarManager.ConversionException e) {
            LOGGER.log(System.Logger.Level.ERROR, "Cannot convert avatar image");
            return null;
        }
    }

    /**
     * Gets the bare JID of the contact who's associated with the avatar, i.e. who changed his or her avatar.
     *
     * @return The contact.
     */
    public final Jid getContact() {
        return contact;
    }
}

