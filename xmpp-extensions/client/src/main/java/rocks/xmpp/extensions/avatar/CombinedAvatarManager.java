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

import java.util.function.Consumer;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.vcard.avatar.VCardBasedAvatarsProtocol;
import rocks.xmpp.extensions.vcard.temp.VCardManager;
import rocks.xmpp.util.concurrent.AsyncResult;

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
public final class CombinedAvatarManager extends AbstractAvatarManager {

    private static final System.Logger logger = System.getLogger(VCardManager.class.getName());

    private final VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol;

    private final UserAvatarProtocol userAvatarProtocol;

    private CombinedAvatarManager(final XmppSession xmppSession) {
        super(xmppSession);
        this.vCardBasedAvatarsProtocol = xmppSession.getManager(VCardBasedAvatarsProtocol.class);
        this.userAvatarProtocol = xmppSession.getManager(UserAvatarProtocol.class);
    }

    @Override
    protected final void onEnable() {
        super.onEnable();
        this.vCardBasedAvatarsProtocol.setEnabled(true);
        this.userAvatarProtocol.setEnabled(true);
    }

    @Override
    protected final void onDisable() {
        super.onDisable();
        this.vCardBasedAvatarsProtocol.setEnabled(false);
        this.userAvatarProtocol.setEnabled(false);
    }

    /**
     * Gets the user avatar from the user's vCard.
     *
     * @param contact The contact.
     * @return The async result with the contact's avatar or null, if it has no avatar.
     */
    @Override
    public final AsyncResult<byte[]> getAvatar(Jid contact) {
        return vCardBasedAvatarsProtocol.getAvatar(contact.asBareJid());
    }

    /**
     * Publishes an avatar.
     *
     * @param imageData The avatar image data, which must be in PNG format. {@code null} resets the avatar.
     * @return The async result.
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#publish">3.1 User Publishes Avatar</a>
     */
    @Override
    public final AsyncResult<Void> publishAvatar(byte[] imageData) {

        // Try publishing to vCard first. If this fails, don't immediately throw an exception, but try PEP first.
        return vCardBasedAvatarsProtocol.publishAvatar(imageData)
                .whenComplete((result, e) -> {
                    if (e != null) {
                        logger.log(System.Logger.Level.WARNING, "Failed to publish avatar to vCard.");
                    }
                })
                .thenCompose(aVoid -> userAvatarProtocol.publishAvatar(imageData))
                .thenRun(() -> {
                });
    }

    /**
     * Adds an avatar listener, to listen for avatar updates.
     *
     * @param avatarChangeListener The avatar listener.
     */
    @Override
    public final void addAvatarChangeListener(Consumer<AvatarChangeEvent> avatarChangeListener) {
        vCardBasedAvatarsProtocol.addAvatarChangeListener(avatarChangeListener);
        userAvatarProtocol.addAvatarChangeListener(avatarChangeListener);
    }

    /**
     * Removes a previously added avatar listener.
     *
     * @param avatarChangeListener The avatar listener.
     */
    @Override
    public final void removeAvatarChangeListener(Consumer<AvatarChangeEvent> avatarChangeListener) {
        vCardBasedAvatarsProtocol.removeAvatarChangeListener(avatarChangeListener);
        userAvatarProtocol.removeAvatarChangeListener(avatarChangeListener);
    }
}
