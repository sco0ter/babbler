package org.xmpp.extension.vcard.avatar;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of the 'vcard-temp:x:update' presence extension as specified by <a href="http://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a>.
 * <p>
 * Avatar updates are managed by {@link org.xmpp.extension.avatar.AvatarManager}, in order to have a single manager for both <a href="http://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a> and <a href="http://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a>.
 * </p>
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
