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

package rocks.xmpp.core.stanza.model.client;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.stanza.model.AbstractPresence;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.Text;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.model.ClientStreamElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.Collections;

/**
 * The implementation of the {@code <presence/>} element for the client namespace ('jabber:client').
 *
 * @author Christian Schudt
 */
@XmlRootElement
@XmlType(propOrder = {"from", "id", "to", "type", "lang", "show", "status", "priority", "extensions", "error"})
public final class Presence extends AbstractPresence implements ClientStreamElement {
    /**
     * Constructs an empty presence to indicate availability.
     */
    public Presence() {
    }

    /**
     * Constructs a presence with a priority.
     *
     * @param priority The priority.
     */
    public Presence(Byte priority) {
        this(null, null, null, null, priority, null, null, null, null, null);
    }

    /**
     * Constructs a presence with a specific 'show' value.
     *
     * @param show The 'show' value.
     */
    public Presence(Show show) {
        this(show, null);
    }

    /**
     * Constructs a presence with a specific 'show' value and priority.
     *
     * @param show     The 'show' value.
     * @param priority The priority.
     */
    public Presence(Show show, Byte priority) {
        this(null, null, show, null, priority, null, null, null, null, null);
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
        this(null, type, null, null, priority, null, null, null, null, null);
    }

    /**
     * Constructs a directed presence.
     *
     * @param to The recipient.
     */
    public Presence(Jid to) {
        this(to, null, null, null);
    }

    /**
     * Constructs a directed presence with a specific 'show' attribute and status.
     *
     * @param to     The recipient.
     * @param show   The 'show' value.
     * @param status The status.
     */
    public Presence(Jid to, Show show, String status) {
        this(to, null, show, status != null ? Collections.singleton(new Text(status)) : null, null, null, null, null, null, null);
    }

    /**
     * Constructs a directed presence, which is useful for requesting subscription or for exiting a multi-user chat.
     *
     * @param to     The recipient.
     * @param type   The type.
     * @param status The status.
     */
    public Presence(Jid to, Type type, String status) {
        this(to, type, status, null);
    }

    /**
     * Constructs a directed presence, which is useful for requesting subscription or for exiting a multi-user chat.
     *
     * @param to     The recipient.
     * @param type   The type.
     * @param status The status.
     * @param id     The id.
     */
    public Presence(Jid to, Type type, String status, String id) {
        this(to, type, null, status != null ? Collections.singleton(new Text(status)) : null, null, id, null, null, null, null);
    }

    /**
     * Constructs a presence with all possible values.
     *
     * @param to         The recipient.
     * @param type       The type.
     * @param show       The 'show' value.
     * @param status     The status.
     * @param priority   The priority.
     * @param id         The id.
     * @param from       The 'from' attribute.
     * @param language   The language.
     * @param extensions The extensions.
     * @param error      The stanza error.
     */
    public Presence(Jid to, Type type, Show show, Collection<Text> status, Byte priority, String id, Jid from, String language, Collection<?> extensions, StanzaError error) {
        super(to, type, show, status, priority, id, from, language, extensions, error);
    }

    @Override
    public final Presence createError(StanzaError error) {
        return new Presence(getTo(), Presence.Type.ERROR, getShow(), getStatuses(), getPriority(), getId(), getFrom(), getLanguage(), getExtensions(), error);
    }

    @Override
    public final Presence createError(Condition condition) {
        return createError(new StanzaError(condition));
    }

    @Override
    public final Presence withFrom(Jid from) {
        return new Presence(getTo(), getType(), getShow(), getStatuses(), getPriority(), getId(), from, getLanguage(), getExtensions(), getError());
    }
}
