/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.errors.Condition;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Locale;

/**
 * A stanza error explicitly bound to the {@code jabber:server} namespace.
 * This class has a {@code @XmlRootElement} annotation, so that it can be used outside the scope of a stanza, e.g. in <a href="https://xmpp.org/extensions/xep-0220.html">XEP-0220: Server Dialback</a>.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "error")
public final class ServerStanzaError extends StanzaError {

    private ServerStanzaError() {
    }

    private ServerStanzaError(Type type, Condition condition, String text, Locale language, Object extension, Jid by) {
        super(type, condition, text, language, extension, by);
    }

    /**
     * Creates a stanza error in the {@code jabber:server} namespace from an unqualified stanza error.
     *
     * @param e The stanza error.
     * @return The stanza error in the {@codejabber:server} namespace.
     */
    public static ServerStanzaError from(StanzaError e) {
        if (e instanceof ServerStanzaError) {
            return (ServerStanzaError) e;
        }
        return new ServerStanzaError(e.getType(), e.getCondition(), e.getText(), e.getLanguage(), e.getExtension(), e.getBy());
    }
}
