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

package org.xmpp.extension.muc.user;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * The implementation of the {@code <decline/>} element.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#invite-mediated">7.8.2 Mediated Invitation</a>
 */
public final class Decline {

    @XmlElement(name = "reason")
    private String reason;

    @XmlAttribute(name = "from")
    private Jid from;

    @XmlAttribute(name = "to")
    private Jid to;

    private Decline() {
    }

    /**
     * Creates a 'decline' element with a reason.
     *
     * @param to     The inviter, who will receive the declination.
     * @param reason The reason.
     */
    public Decline(Jid to, String reason) {
        this.to = to;
        this.reason = reason;
    }

    /**
     * Gets the reason for the declination.
     *
     * @return The reason.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets the invitee, who declined the invitation.
     *
     * @return The invitee.
     */
    public Jid getFrom() {
        return from;
    }

    /**
     * Sets the invitee, who declined the invitation.
     *
     * @param from The invitee.
     */
    public void setFrom(Jid from) {
        this.from = from;
    }

    /**
     * Gets the inviter, who sent the invitation.
     *
     * @return The inviter.
     */
    public Jid getTo() {
        return to;
    }

    /**
     * Sets the inviter, who sent the invitation.
     *
     * @param to The inviter.
     */
    public void setTo(Jid to) {
        this.to = to;
    }
}
