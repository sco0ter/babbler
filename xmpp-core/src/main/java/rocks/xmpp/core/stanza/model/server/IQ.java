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

package rocks.xmpp.core.stanza.model.server;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stream.model.ServerStreamElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The implementation of the {@code <iq/>} element for the server namespace ('jabber:server').
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "iq")
@XmlType(propOrder = {"from", "id", "to", "type", "extension", "error"})
public final class IQ extends AbstractIQ implements ServerStreamElement {
    /**
     * Default constructor for unmarshalling.
     */
    @SuppressWarnings("unused")
    private IQ() {
    }

    /**
     * Creates an IQ stanza with the given type. The id attribute will be generated randomly.
     *
     * @param type The type.
     */
    public IQ(Type type) {
        // The 'id' attribute is REQUIRED for IQ stanzas.
        super(type);
    }

    /**
     * Creates an IQ stanza with the given type and extension. The id attribute will be generated randomly.
     *
     * @param type      The type.
     * @param extension The extension.
     */
    public IQ(Type type, Object extension) {
        super(type, extension);
    }

    /**
     * Creates an IQ stanza with the given id and type.
     * Not that, if the type is {@link rocks.xmpp.core.stanza.model.AbstractIQ.Type#SET} or {@link rocks.xmpp.core.stanza.model.AbstractIQ.Type#GET}, you will have to also set an extension.
     *
     * @param id   The id.
     * @param type The type.
     */
    public IQ(String id, Type type) {
        super(id, type, null);
    }

    /**
     * Creates an IQ stanza with the given id, type and extension.
     *
     * @param id        The id.
     * @param type      The type.
     * @param extension The extension.
     */
    public IQ(String id, Type type, Object extension) {
        super(null, null, id, type, null, extension, null);
    }

    /**
     * Creates an IQ stanza with the given receiver, type and extension. The id attribute will be generated randomly.
     *
     * @param to        The receiver.
     * @param type      The type.
     * @param extension The extension.
     */
    public IQ(Jid to, Type type, Object extension) {
        super(to, type, extension);
    }

    /**
     * Creates an IQ stanza with the given receiver, id, type and extension.
     *
     * @param to        The receiver.
     * @param from      The sender.
     * @param id        The id.
     * @param type      The type.
     * @param extension The extension.
     */
    public IQ(Jid to, Jid from, String id, Type type, Object extension) {
        super(to, from, id, type, null, extension, null);
    }

    /**
     * Creates an IQ stanza with the given receiver, id, type, extension and error.
     *
     * @param to        The receiver.
     * @param from      The sender.
     * @param id        The id.
     * @param type      The type.
     * @param extension The extension.
     * @param error     The error.
     */
    public IQ(Jid to, Jid from, String id, Type type, Object extension, StanzaError error) {
        super(to, from, id, type, null, extension, error);
    }

    /**
     * Creates a result IQ stanza, i.e. it uses the same id as this IQ, sets the type to 'result' and switches the 'to' and 'from' attribute.
     *
     * @return The result IQ stanza.
     */
    public final IQ createResult() {
        return createResult(null);
    }

    /**
     * Creates a result IQ stanza with a payload, i.e. it uses the same id as this IQ, sets the type to 'result' and switches the 'to' and 'from' attribute.
     *
     * @param extension The extension.
     * @return The result IQ stanza.
     */
    public final IQ createResult(Object extension) {
        return new IQ(getFrom(), getTo(), getId(), Type.RESULT, extension);
    }

    @Override
    public final IQ createError(StanzaError error) {
        error.setBy(getTo());
        return new IQ(getFrom(), getTo(), getId(), Type.ERROR, null, error);
    }
}
