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

package rocks.xmpp.core.stanza;

import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.ClientStreamElement;

import java.util.EventObject;

/**
 * An event which is triggered for inbound and outbound stanzas.
 *
 * @author Christian Schudt
 */
public abstract class StanzaEvent<S extends Stanza & ClientStreamElement> extends EventObject {

    protected final S stanza;

    private final boolean incoming;

    public StanzaEvent(Object source, S stanza, boolean incoming) {
        super(source);
        this.stanza = stanza;
        this.incoming = incoming;
    }

    /**
     * Indicates, whether the stanza has been received (incoming) or is about to being sent (outgoing).
     *
     * @return True, if the stanza is incoming; false if it is outgoing.
     */
    public final boolean isIncoming() {
        return incoming;
    }

    /**
     * Gets the stanza.
     *
     * @return The stanza.
     * @deprecated Use getMessage(), getPresence() or getIQ().
     */
    @Deprecated
    public final S getStanza() {
        return stanza;
    }
}
