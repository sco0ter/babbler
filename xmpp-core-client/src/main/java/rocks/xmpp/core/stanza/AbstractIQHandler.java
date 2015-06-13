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

package rocks.xmpp.core.stanza;

import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;

/**
 * This class is an IQ handler for extension managers.
 * <p>
 * If an extension is disabled, IQs with this extension are automatically responded to with {@code <service-unavailable}.
 * <p>
 * If the extension is known, but the request contains a wrong type (e.g. 'set' instead of 'get') this manager automatically responds with a {@code <bad-request/>} error.
 *
 * @author Christian Schudt
 */
public abstract class AbstractIQHandler implements IQHandler {

    private final AbstractIQ.Type type;

    /**
     * @param type The IQ type which is handled by this handler (get or set).
     */
    protected AbstractIQHandler(AbstractIQ.Type type) {
        if (type != AbstractIQ.Type.GET && type != AbstractIQ.Type.SET) {
            throw new IllegalArgumentException("type must be 'get' or 'set'");
        }
        this.type = type;
    }

    @Override
    public final AbstractIQ handleRequest(AbstractIQ iq) {
        if (iq.getType() == type) {
            return processRequest(iq);
        } else {
            return iq.createError(new StanzaError(Condition.BAD_REQUEST, String.format("Type was '%s', but expected '%s'.", iq.getType().toString().toLowerCase(), type.toString().toLowerCase())));
        }
    }

    /**
     * Processes the IQ, after checking if the extension is enabled and after checking if the IQ has correct type, which is specified for the extension.
     *
     * @param iq The IQ request.
     * @return The IQ response.
     */
    protected abstract AbstractIQ processRequest(AbstractIQ iq);
}
