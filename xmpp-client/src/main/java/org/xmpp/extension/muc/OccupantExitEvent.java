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

package org.xmpp.extension.muc;

import java.util.EventObject;

/**
 * @author Christian Schudt
 */
public final class OccupantExitEvent extends EventObject {

    private final Occupant occupant;

    private final Reason type;

    private final boolean selfPresence;

    /**
     * Constructs a prototypical Event.
     *
     * @param source   The object on which the Event initially occurred.
     * @param occupant The occupant.
     * @throws IllegalArgumentException if source is null.
     */
    public OccupantExitEvent(Object source, Occupant occupant, Reason exitType, boolean selfPresence) {
        super(source);
        this.occupant = occupant;
        this.type = exitType;
        this.selfPresence = selfPresence;
    }

    public Reason getType() {
        return type;
    }

    public Occupant getOccupant() {
        return occupant;
    }

    public boolean isSelfPresence() {
        return selfPresence;
    }

    public enum Reason {
        NONE,
        KICKED,
        BANNED,
        MEMBERSHIP_REVOKED,
        ROOM_BECAME_MEMBERS_ONLY,
        NICKNAME_CHANGED,
        SYSTEM_SHUTDOWN
    }
}
