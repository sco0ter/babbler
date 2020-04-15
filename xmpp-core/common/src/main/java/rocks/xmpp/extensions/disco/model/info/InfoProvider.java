/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.disco.model.info;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.extensions.disco.model.items.ItemProvider;

import java.util.Locale;

/**
 * Provides discoverable info, so that it can be discovered via Service Discovery (disco#info).
 * <p>
 * This is the pendant to {@link ItemProvider}.
 *
 * @author Christian Schudt
 * @see ItemProvider
 * @see DiscoverableInfo
 */
public interface InfoProvider {

    /**
     * Gets the info appropriate to the given parameters.
     *
     * @param to     The receiving entity.
     * @param from   The requesting entity.
     * @param node   The requested node, if any. May be null.
     * @param locale The locale of the requesting entity's stream or stanza.
     * @return The info or null, if no information could be found.
     * @throws StanzaErrorException If an error should be returned to the requesting entity.
     */
    DiscoverableInfo getInfo(Jid to, Jid from, String node, Locale locale) throws StanzaErrorException;
}
