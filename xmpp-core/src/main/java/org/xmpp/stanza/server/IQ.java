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

package org.xmpp.stanza.server;

import org.xmpp.Jid;
import org.xmpp.stanza.AbstractIQ;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stream.ServerStreamElement;

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
     * Not that, if the type is {@link org.xmpp.stanza.AbstractIQ.Type#SET} or {@link org.xmpp.stanza.AbstractIQ.Type#GET}, you will have to also set an extension.
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
        super(null, id, type, extension);
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
     * @param id        The id.
     * @param type      The type.
     * @param extension The extension.
     */
    public IQ(Jid to, String id, Type type, Object extension) {
        super(to, id, type, extension);
    }

    /**
     * Creates a result IQ stanza, i.e. it uses the same id as this IQ, sets the type to 'result' and switches the 'to' and 'from' attribute.
     *
     * @return The result IQ stanza.
     */
    public final IQ createResult() {
        IQ responseIQ = new IQ(id, Type.RESULT);
        responseIQ.setTo(from);
        responseIQ.setFrom(to);
        return responseIQ;
    }

    @Override
    public final IQ createError(StanzaError error) {
        IQ responseIQ = new IQ(id, Type.ERROR);
        createError(responseIQ, error);
        return responseIQ;
    }
}
