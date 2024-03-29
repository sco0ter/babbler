/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.extensions.muc;

import java.util.EventObject;
import java.util.function.Consumer;

import rocks.xmpp.addr.Jid;

/**
 * The multi-user chat invitation decline event, which is triggered when an invitee declines a multi-user chat
 * invitation.
 *
 * @author Christian Schudt
 * @see ChatRoom#addInvitationDeclineListener(Consumer)
 */
public final class InvitationDeclineEvent extends EventObject {

    private final Jid roomAddress;

    private final Jid invitee;

    private final String reason;

    InvitationDeclineEvent(Object source, Jid roomAddress, Jid invitee, String reason) {
        super(source);
        this.invitee = invitee;
        this.reason = reason;
        this.roomAddress = roomAddress;
    }

    /**
     * Gets the invitee, who declined the invitation.
     *
     * @return The invitee.
     */
    public Jid getInvitee() {
        return invitee;
    }

    /**
     * Gets the room address.
     *
     * @return The room address.
     */
    public Jid getRoomAddress() {
        return roomAddress;
    }

    /**
     * Gets the reason for the decline.
     *
     * @return The reason.
     */
    public String getReason() {
        return reason;
    }
}
