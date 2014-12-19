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

package rocks.xmpp.extensions.muc.model.user;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

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

    /**
     * Creates status code 100.
     *
     * @return The status code.
     */
    public static Status roomIsNonAnonymous() {
        return new Status(100);
    }

    /**
     * Creates status code 101.
     *
     * @return The status code.
     */
    public static Status affiliationChanged() {
        return new Status(101);
    }

    /**
     * Creates status code 102.
     *
     * @return The status code.
     */
    public static Status roomShowsUnavailableMembers() {
        return new Status(102);
    }

    /**
     * Creates status code 103.
     *
     * @return The status code.
     */
    public static Status roomDoesNotShowUnavailableMembers() {
        return new Status(103);
    }

    /**
     * Creates status code 104.
     *
     * @return The status code.
     */
    public static Status roomConfigurationChange() {
        return new Status(104);
    }

    /**
     * Creates status code 110.
     *
     * @return The status code.
     */
    public static Status self() {
        return new Status(110);
    }

    /**
     * Creates status code 170.
     *
     * @return The status code.
     */
    public static Status roomLoggingEnabled() {
        return new Status(170);
    }

    /**
     * Creates status code 171.
     *
     * @return The status code.
     */
    public static Status roomLoggingDisabled() {
        return new Status(171);
    }

    /**
     * Creates status code 172.
     *
     * @return The status code.
     */
    public static Status roomNonAnonymous() {
        return new Status(172);
    }

    /**
     * Creates status code 173.
     *
     * @return The status code.
     */
    public static Status roomSemiAnonymous() {
        return new Status(173);
    }

    /**
     * Creates status code 174.
     *
     * @return The status code.
     */
    public static Status roomFullyAnonymous() {
        return new Status(174);
    }

    /**
     * Creates status code 201.
     *
     * @return The status code.
     */
    public static Status newRoomCreated() {
        return new Status(201);
    }

    /**
     * Creates status code 210.
     *
     * @return The status code.
     */
    public static Status serviceHasAssignedOrModifiedNick() {
        return new Status(210);
    }

    /**
     * Creates status code 301.
     *
     * @return The status code.
     */
    public static Status banned() {
        return new Status(301);
    }

    /**
     * Creates status code 303.
     *
     * @return The status code.
     */
    public static Status nicknameChanged() {
        return new Status(303);
    }

    /**
     * Creates status code 307.
     *
     * @return The status code.
     */
    public static Status kicked() {
        return new Status(307);
    }

    /**
     * Creates status code 321.
     *
     * @return The status code.
     */
    public static Status membershipRevoked() {
        return new Status(321);
    }

    /**
     * Creates status code 322.
     *
     * @return The status code.
     */
    public static Status roomChangedToMembersOnly() {
        return new Status(322);
    }

    /**
     * Creates status code 332.
     *
     * @return The status code.
     */
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

        return Objects.equals(code, other.code);
    }

    @Override
    public int hashCode() {
       return Objects.hash(code);
    }
}
