/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

package rocks.xmpp.extensions.shim;

import java.util.List;
import java.util.Set;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.extensions.disco.model.info.InfoProvider;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * Manages <a href="https://xmpp.org/extensions/xep-0131.html">XEP-0131: Stanza Headers and Internet Metadata</a>.
 *
 * @author Christian Schudt
 */
public interface HeaderManager extends InfoProvider, ExtensionProtocol {

    /**
     * Discovers the supported headers of another entity.
     *
     * @param jid The JID.
     * @return The async result with the list of supported headers.
     */
    AsyncResult<List<String>> discoverSupportedHeaders(Jid jid);

    /**
     * Gets the supported headers as unmodifiable set.
     *
     * <p>If you want to advertise support for a specific header, add it to this set.
     * Service discovery requests to the 'header' node will then reveal supported headers.</p>
     *
     * @return The supported headers.
     */
    Set<String> getSupportedHeaders();

    /**
     * Adds a supported header. Adding a header automatically includes this protocol in service discovery responses.
     *
     * @param header The header.
     * @return True, if the header was added.
     * @see #removeSupportedHeader(String)
     */
    boolean addSupportedHeader(String header);

    /**
     * Removes a supported header. If there are no headers left, this protocol is no longer included in service
     * discovery responses.
     *
     * @param header The header.
     * @return True, if the header was removed.
     * @see #addSupportedHeader(String)
     */
    boolean removeSupportedHeader(String header);
}
