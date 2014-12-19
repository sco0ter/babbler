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

package rocks.xmpp.core.stanza.model.client;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.stanza.model.AbstractPresence;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stream.model.ClientStreamElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * The implementation of the {@code <presence/>} element for the client namespace ('jabber:client').
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "presence")
@XmlType(propOrder = {"from", "id", "to", "type", "status", "show", "priority", "extensions", "error"})
public final class Presence extends AbstractPresence implements ClientStreamElement {
    /**
     * Constructs an empty presence.
     */
    public Presence() {
    }

    public Presence(Jid to) {
        this(null, to, null, null);
    }

    public Presence(Byte priority) {
        this(null, null, null, null, Collections.<Status>emptyList(), priority, null, null, null);
    }

    /**
     * Constructs a presence with a specific 'show' attribute.
     *
     * @param show The 'show' attribute.
     */
    public Presence(Show show) {
        this(show, null);
    }

    /**
     * Constructs a presence with a specific 'show' attribute and priority.
     *
     * @param show     The 'show' attribute.
     * @param priority The priority.
     */
    public Presence(Show show, Byte priority) {
        this(null, show, null, null, Collections.<Status>emptyList(), priority, null, null, null);
    }

    /**
     * Constructs a directed presence with a specific 'show' attribute and status.
     *
     * @param show   The 'show' attribute.
     * @param to     The 'to' attribute.
     * @param status The status.
     */
    public Presence(Show show, Jid to, String status) {
        this(null, show, to, null, status != null ? Arrays.asList(new Status(status)) : Collections.<Status>emptyList(), null, null, null, null);
    }

    /**
     * Constructs a presence of a specific type.
     *
     * @param type The type.
     */
    public Presence(Type type) {
        this(type, null);
    }

    /**
     * Constructs a presence of a specific type.
     *
     * @param type     The type.
     * @param priority The priority.
     */
    public Presence(Type type, Byte priority) {
        this(type, null, null, null, Collections.<Status>emptyList(), priority, null, null, null);
    }

    /**
     * Constructs a directed presence, which is useful for requesting subscription or for exiting a multi-user chat.
     *
     * @param type   The type.
     * @param to     The 'to' attribute.
     * @param status The status.
     */
    public Presence(Type type, Jid to, String status) {
        this(type, to, status, null);
    }

    /**
     * Constructs a directed presence, which is useful for requesting subscription or for exiting a multi-user chat.
     *
     * @param type   The type.
     * @param to     The 'to' attribute.
     * @param status The status.
     * @param id     The id.
     */
    public Presence(Type type, Jid to, String status, String id) {
        this(type, null, to, null, status != null ? Arrays.asList(new Status(status)) : Collections.<Status>emptyList(), null, id, null, null);
    }

    /**
     * Constructs a presence with all possible values.
     *
     * @param type     The type.
     * @param show     The 'show' attribute.
     * @param to       The 'to' attribute.
     * @param from     The 'from' attribute.
     * @param status   The status.
     * @param priority The priority.
     * @param id       The id.
     * @param language The language.
     * @param error    The stanza error.
     */
    public Presence(Type type, Show show, Jid to, Jid from, Collection<Status> status, Byte priority, String id, String language, StanzaError error) {
        super(type, show, to, from, status, priority, id, language, error);
    }

    @Override
    public Presence createError(StanzaError error) {
        return new Presence(Presence.Type.ERROR, getShow(), getFrom(), getTo(), getStatuses(), getPriority(), getId(), getLanguage(), error);
    }
}
