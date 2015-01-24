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

package rocks.xmpp.core.session;

import java.util.EventObject;

/**
 * An XMPP session event is fired, whenever the status of a session has changed, e.g. when it is abnormally disconnected.
 *
 * @author Christian Schudt
 * @see SessionStatusListener
 */
public final class SessionStatusEvent extends EventObject {
    private final Exception exception;

    private final XmppSession.Status status;

    private final XmppSession.Status oldStatus;

    /**
     * Constructs a connection event.
     *
     * @param source    The object on which the event initially occurred.
     * @param status    The session status.
     * @param exception An optionally exception.
     * @throws IllegalArgumentException if source is null.
     */
    SessionStatusEvent(XmppSession source, XmppSession.Status status, XmppSession.Status oldStatus, Exception exception) {
        super(source);
        this.exception = exception;
        this.status = status;
        this.oldStatus = oldStatus;
    }

    /**
     * Gets the session status.
     *
     * @return The connection status.
     */
    public XmppSession.Status getStatus() {
        return status;
    }

    /**
     * Gets the exception if the session abnormally disconnected or null.
     *
     * @return The exception, which caused a disconnection or null.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Gets the old session status.
     *
     * @return The old session status.
     */
    public XmppSession.Status getOldStatus() {
        return oldStatus;
    }
}
