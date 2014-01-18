package org.xmpp.extension.avatar;

import org.xmpp.Jid;

import java.util.EventObject;

/**
 * @author Christian Schudt
 */
public final class AvatarChangeEvent extends EventObject {
    private final Jid contact;

    private final Avatar avatar;


    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    AvatarChangeEvent(AvatarManager source, Jid contact, Avatar avatar) {
        super(source);
        this.contact = contact;
        this.avatar = avatar;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public Jid getContact() {
        return contact;
    }
}

