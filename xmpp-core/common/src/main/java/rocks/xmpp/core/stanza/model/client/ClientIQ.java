/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

import java.util.Locale;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.StanzaError;

/**
 * The implementation of the {@code <iq/>} element in the {@code jabber:client} namespace.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "iq")
@XmlType(propOrder = {"from", "id", "to", "type", "lang", "extensions", "error"})
public final class ClientIQ extends IQ {

    /**
     * Default constructor for unmarshalling.
     */
    @SuppressWarnings("unused")
    private ClientIQ() {
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
    private ClientIQ(Jid to, Type type, Object extension, String id, Jid from, Locale language, StanzaError error) {
        super(to, type, extension, id, from, language, error);
    }

    /**
     * Creates an IQ for the {@code jabber:client} namespace from a generic IQ.
     *
     * @param iq The IQ.
     * @return The client IQ.
     */
    public static ClientIQ from(IQ iq) {
        if (iq instanceof ClientIQ) {
            return (ClientIQ) iq;
        }
        return new ClientIQ(iq.getTo(), iq.getType(), iq.getExtension(Object.class), iq.getId(), iq.getFrom(),
                iq.getLanguage(), iq.getError());
    }
}
