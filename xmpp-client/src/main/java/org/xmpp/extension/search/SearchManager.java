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

package org.xmpp.extension.search;

import org.xmpp.XmppSession;
import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.IQ;

/**
 * The search manager allows to perform search requests on a server or service component according to <a href="http://xmpp.org/extensions/xep-0055.html">XEP-0055: Jabber Search</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0055.html#intro">1. Introduction</a></cite></p>
 * <p>The basic functionality is to query an information repository regarding the possible search fields, to send a search query, and to receive search results. Note well that there is currently no mechanism for paging through results or limiting the number of "hits", and that the allowable search fields are limited to those defined in the XML schema; however, extensibility MAY be provided via the <a href="http://xmpp.org/extensions/xep-0004.html">Data Forms (XEP-0004)</a> protocol.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
public final class SearchManager extends ExtensionManager {

    /**
     * Creates the search manager.
     *
     * @param xmppSession The search manager.
     */
    private SearchManager(XmppSession xmppSession) {
        super(xmppSession);
    }

    /**
     * Discovers the supported search fields of a service.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0055.html#usecases-search">2.1 Searching</a></cite></p>
     * <p>In order to search an information respository, a user first needs to discover what search fields are supported by the service.</p>
     * <p>The service MUST then return the possible search fields to the user, and MAY include instructions.</p>
     * </blockquote>
     *
     * @param service The service address.
     * @return The possible search fields and instructions or null, if search is not supported. Search fields are supported if they are not null.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public Search discoverSearchFields(Jid service) throws XmppException {
        IQ result = xmppSession.query(new IQ(service, IQ.Type.GET, new Search()));
        return result.getExtension(Search.class);
    }

    /**
     * Performs a search on the given service with given search parameters.
     * Note that this method blocks the current thread until a response has received.
     *
     * @param search  The search parameters.
     * @param service The service, which will perform the search, usually a server or server component.
     * @return The search result (see {@link Search#getItems()}) or null if search is not supported.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public Search search(Search search, Jid service) throws XmppException {
        IQ result = xmppSession.query(new IQ(service, IQ.Type.SET, search));
        return result.getExtension(Search.class);
    }
}
