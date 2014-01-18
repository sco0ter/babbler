package org.xmpp.extension.avatar;

import java.util.EventListener;

/**
 * An avatar listener is notified, whenever the avatar of a user has changed.
 *
 * @author Christian Schudt
 * @see AvatarManager#addAvatarChangeListener(AvatarChangeListener)
 */
public interface AvatarChangeListener extends EventListener {
    /**
     * Notifies the observer for avatar updates.
     *
     * @param e The avatar change event.
     */
    void avatarChanged(AvatarChangeEvent e);
}
