/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.muc.model.user;

import rocks.xmpp.addr.Jid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * The implementation of the {@code <invite/>} element.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#invite-mediated">7.8.2 Mediated Invitation</a>
 */
public final class Invite {

    private String reason;

    @XmlAttribute
    private Jid from;

    @XmlAttribute
    private Jid to;

    @XmlElement(name = "continue")
    private Continue aContinue;

    private Invite() {
    }

    /**
     * Creates an invite element with a reason.
     *
     * @param to     The invitee, who will receive the invitation.
     * @param reason The reason.
     */
    public Invite(Jid to, String reason) {
        this.to = to;
        this.reason = reason;
    }

    /**
     * Creates an 'invite' element.
     *
     * @param to The invitee, who will receive the invitation.
     */
    public Invite(Jid to) {
        this(to, null);
    }

    /**
     * Creates an 'invite' element with a reason and a 'continue' element.
     *
     * @param to        The invitee, who will receive the invitation.
     * @param reason    The reason.
     * @param aContinue The 'continue' element.
     */
    public Invite(Jid to, String reason, Continue aContinue) {
        this.to = to;
        this.reason = reason;
        this.aContinue = aContinue;
    }

    /**
     * Gets the reason for the invitation.
     *
     * @return The reason.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets the inviter.
     *
     * @return The inviter.
     */
    public Jid getFrom() {
        return from;
    }

    /**
     * Gets the invitee.
     *
     * @return The invitee.
     */
    public Jid getTo() {
        return to;
    }

    /**
     * Indicates, whether a previous one-to-one chat session is continued.
     *
     * @return True, if a previous one-to-one chat session is continued.
     * @see #getThread()
     */
    public boolean isContinue() {
        return aContinue != null;
    }

    /**
     * Gets the thread of the previous one-to-one chat session.
     *
     * @return The thread of the previous one-to-one chat session (if any) or null.
     * @see #isContinue()
     */
    public String getThread() {
        return aContinue != null ? aContinue.getThread() : null;
    }
}
