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

package rocks.xmpp.core.session;

import java.util.Arrays;
import java.util.Collection;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.extensions.caps.client.ClientEntityCapabilities1Protocol;
import rocks.xmpp.extensions.caps.client.ClientEntityCapabilitiesManager;
import rocks.xmpp.extensions.caps2.client.ClientEntityCapabilities2Protocol;
import rocks.xmpp.extensions.disco.client.ClientServiceDiscoveryManager;
import rocks.xmpp.extensions.hashes.CryptographicHashFunctionsProtocol;
import rocks.xmpp.extensions.rsm.ResultSetManagementProtocol;
import rocks.xmpp.im.roster.RosterManager;
import rocks.xmpp.im.roster.model.Roster;
import rocks.xmpp.im.subscription.PresenceManager;

/**
 * The core module provides XMPP classes as well as manager classes which are associated with an XMPP session.
 * These are combined in an {@link Extension}. Although core classes are not extensions, they use the same concept as real extensions.
 *
 * @author Christian Schudt
 */
public final class CoreModule implements Module {

    @Override
    public final Collection<Extension> getExtensions() {
        return Arrays.asList(

                // XEP-0030: Service Discovery
                Extension.of(ClientServiceDiscoveryManager.class, true),
                Extension.of(ClientServiceDiscoveryManager.class, true),

                // XEP-0059: Result Set Management
                Extension.of(new ResultSetManagementProtocol(), true),

                // XEP-0106: JID Escaping
                Extension.of(Jid.ESCAPING_FEATURE, true),

                Extension.of(ClientEntityCapabilitiesManager.class, true),

                // XEP-0115: Entity Capabilities
                Extension.of(ClientEntityCapabilities1Protocol.class, true),

                // XEP-0300: Use of Cryptographic Hash Functions in XMPP
                Extension.of(new CryptographicHashFunctionsProtocol(), true),

                // XEP-0390 Entity Capabilities 2.0
                Extension.of(ClientEntityCapabilities2Protocol.class, true),

                Extension.of(PresenceManager.class, true),

                Extension.of(ReconnectionManager.class, true),

                Extension.of(Roster.NAMESPACE, RosterManager.class, true)
        );
    }
}
