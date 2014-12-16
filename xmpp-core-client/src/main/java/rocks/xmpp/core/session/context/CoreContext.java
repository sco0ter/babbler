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

package rocks.xmpp.core.session.context;

import rocks.xmpp.core.bind.model.Bind;
import rocks.xmpp.core.roster.model.Roster;
import rocks.xmpp.core.roster.versioning.model.RosterVersioning;
import rocks.xmpp.core.sasl.model.Mechanisms;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.model.Session;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.core.subscription.preapproval.model.SubscriptionPreApproval;
import rocks.xmpp.core.tls.model.StartTls;
import rocks.xmpp.extensions.compress.model.Compress;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.items.ItemDiscovery;
import rocks.xmpp.extensions.httpbind.model.Body;
import rocks.xmpp.extensions.rsm.ResultSetManager;

import java.util.*;

/**
 * The context provides XMPP classes as well as manager classes which are associated with an XMPP session.
 * Registered classes are used to marshal and unmarshal XML to objects. Registered manager classes are initialized as soon as an XMPP session is created, in order to start listening for stanzas immediately e.g. to automatically respond to IQ requests.
 *
 * @author Christian Schudt
 */
public class CoreContext {

    private final Set<Class<?>> extensions = new HashSet<>();

    private final Set<Class<? extends ExtensionManager>> managers = new HashSet<>();

    public CoreContext(Class<?>... extensions) {
        this(Collections.<Class<? extends ExtensionManager>>emptyList(), extensions);
    }

    public CoreContext(Collection<Class<? extends ExtensionManager>> extensionManagers, Class<?>... extensions) {
        this.extensions.addAll(Arrays.asList(
                // Core
                StreamFeatures.class, StreamError.class, Message.class, Presence.class, IQ.class, Session.class, Roster.class, Bind.class, Mechanisms.class, StartTls.class, SubscriptionPreApproval.class, RosterVersioning.class,

                // Extensions

                // XEP-0004: Data Forms
                DataForm.class,

                // XEP-0030: Service Discovery
                InfoDiscovery.class, ItemDiscovery.class,

                // XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)
                Body.class,

                // XEP-0138: Stream Compression
                Compress.class

        ));
        this.extensions.addAll(Arrays.asList(extensions));
        managers.add(ServiceDiscoveryManager.class);
        managers.add(ResultSetManager.class);
        managers.addAll(extensionManagers);
    }

    /**
     * Gets the class context.
     *
     * @return The context.
     */
    public final Collection<Class<?>> getExtensions() {
        return Collections.unmodifiableCollection(extensions);
    }

    /**
     * Gets the initial extension managers.
     *
     * @return The initial extension managers.
     */
    public final Collection<Class<? extends ExtensionManager>> getExtensionManagers() {
        return Collections.unmodifiableCollection(managers);
    }
}
