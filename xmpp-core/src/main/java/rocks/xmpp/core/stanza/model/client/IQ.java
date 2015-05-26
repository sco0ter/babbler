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
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.model.ClientStreamElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The implementation of the {@code <iq/>} element for the client namespace ('jabber:client').
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "iq")
@XmlType(propOrder = {"from", "id", "to", "type", "lang", "extension", "error"})
public final class IQ extends AbstractIQ implements ClientStreamElement {
    /**
     * Default constructor for unmarshalling.
     */
    @SuppressWarnings("unused")
    private IQ() {
    }

    /**
     * Creates an IQ stanza with the given type and extension. The id attribute will be generated randomly.
     *
     * @param type      The type.
     * @param extension The extension.
     */
    public IQ(Type type, Object extension) {
        this(type, extension, null);
    }

    /**
     * Creates an IQ stanza with the given id, type and extension.
     *
     * @param type      The type.
     * @param extension The extension.
     * @param id        The id.
     */
    public IQ(Type type, Object extension, String id) {
        this(null, type, extension, id);
    }

    /**
     * Creates an IQ stanza with the given receiver, type and extension. The id attribute will be generated randomly.
     *
     * @param to        The receiver.
     * @param type      The type.
     * @param extension The extension.
     */
    public IQ(Jid to, Type type, Object extension) {
        this(to, type, extension, null);
    }

    /**
     * Creates an IQ stanza with the given receiver, id, type and extension.
     *
     * @param to        The receiver.
     * @param type      The type.
     * @param extension The extension.
     * @param id        The id.
     */
    public IQ(Jid to, Type type, Object extension, String id) {
        this(to, type, extension, id, null, null, null);
    }

    /**
     * Creates an IQ stanza with the given receiver, id, type, extension and error.
     *
     * @param to        The receiver.
     * @param type      The type.
     * @param extension The extension.
     * @param id        The id.
     * @param from      The sender.
     * @param language  The language.
     * @param error     The error.
     */
    public IQ(Jid to, Type type, Object extension, String id, Jid from, String language, StanzaError error) {
        super(to, type, extension, id, from, language, error);
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
        return new IQ(getFrom(), Type.RESULT, extension, getId(), getTo(), getLanguage(), null);
    }

    @Override
    public final IQ createError(StanzaError error) {
        return new IQ(getFrom(), Type.ERROR, null, getId(), getTo(), getLanguage(), error);
    }

    @Override
    public final IQ createError(Condition condition) {
        return createError(new StanzaError(condition));
    }

    @Override
    public final IQ withFrom(Jid from) {
        return new IQ(getTo(), getType(), getExtension(Object.class), getId(), from, getLanguage(), getError());
    }
}
