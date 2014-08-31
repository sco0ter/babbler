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

import javax.xml.bind.annotation.XmlAttribute;

/**
 * The implementation of the {@code <status/>} element.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#statuscodes">11. Status Codes</a>
 */
public final class Status {
    @XmlAttribute(name = "code")
    private Integer code;

    private Status() {
    }

    /**
     * Creates a 'status' element with a code.
     *
     * @param code The code.
     */
    private Status(int code) {
        this.code = code;
    }

    public static Status roomIsNonAnonymous() {
        return new Status(100);
    }

    public static Status affiliationChanged() {
        return new Status(101);
    }

    public static Status roomShowsUnavailableMembers() {
        return new Status(102);
    }

    public static Status roomDoesNotShowUnavailableMembers() {
        return new Status(103);
    }

    public static Status roomConfigurationChange() {
        return new Status(104);
    }

    public static Status self() {
        return new Status(110);
    }

    public static Status roomLoggingEnabled() {
        return new Status(170);
    }

    public static Status roomLoggingDisabled() {
        return new Status(171);
    }

    public static Status roomNonAnonymous() {
        return new Status(172);
    }

    public static Status roomSemiAnonymous() {
        return new Status(173);
    }

    public static Status roomFullyAnonymous() {
        return new Status(174);
    }

    public static Status newRoomCreated() {
        return new Status(201);
    }

    public static Status serviceHasAssignedOrModifiedNick() {
        return new Status(210);
    }

    public static Status banned() {
        return new Status(301);
    }

    public static Status nicknameChanged() {
        return new Status(303);
    }

    public static Status kicked() {
        return new Status(307);
    }

    public static Status membershipRevoked() {
        return new Status(321);
    }

    public static Status roomChangedToMembersOnly() {
        return new Status(322);
    }

    public static Status systemShutdown() {
        return new Status(332);
    }

    /**
     * Gets the status code.
     *
     * @return The status code.
     */
    public int getCode() {
        return code != null ? code : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Status)) {
            return false;
        }
        Status other = (Status) o;

        return (code == null ? other.code == null : code.equals(other.code));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ((code == null) ? 0 : code.hashCode());
        return result;
    }
}
