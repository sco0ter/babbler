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

package rocks.xmpp.extensions.disco.model.items;

import java.util.Locale;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.rsm.ResultSetProvider;

/**
 * Provides items, which can be discovered via Service Discovery.
 *
 * @author Christian Schudt
 * @see DiscoverableItem
 */
public interface ItemProvider {

    /**
     * Provides the result set of items, in order to manage the result set returned to the requesting entity. Requesting
     * entities may include result set information in their request, e.g. to limit the returned items.
     *
     * <p>If this method returns null and not other providers are found which return a non-null result for the same
     * parameters, then {@link Condition#ITEM_NOT_FOUND} is returned to the requesting entity.</p>
     *
     * @param to     The receiving entity.
     * @param from   The requesting entity.
     * @param node   The requested node, if any. May be null.
     * @param locale The locale of the requesting entity's stream or stanza.
     * @return The result set provider or null, if no result set can be returned for the given parameters.
     * @throws StanzaErrorException If an error should be returned to the requesting entity.
     */
    ResultSetProvider<DiscoverableItem> getItems(Jid to, Jid from, String node, Locale locale)
            throws StanzaErrorException;
}
