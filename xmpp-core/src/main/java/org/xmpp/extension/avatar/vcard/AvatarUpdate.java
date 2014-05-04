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

package org.xmpp.extension.avatar.vcard;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the 'vcard-temp:x:update' presence extension as specified by <a href="http://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a>.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "x")
public final class AvatarUpdate {
    @XmlElement(name = "photo")
    private byte[] hash;

    /**
     * Creates an empty avatar update element to indicate, we are not yet ready to advertise an image.
     */
    public AvatarUpdate() {
    }

    /**
     * Creates an avatar update element with a hash value.
     *
     * @param hash The hash.
     */
    public AvatarUpdate(byte[] hash) {
        this.hash = hash;
    }

    /**
     * Gets the SHA-1 hash value of the avatar.
     *
     * @return The hash.
     */
    public byte[] getHash() {
        return hash;
    }
}
