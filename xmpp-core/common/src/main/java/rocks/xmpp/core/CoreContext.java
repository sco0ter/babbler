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

package rocks.xmpp.core;

import rocks.xmpp.core.bind.model.Bind;
import rocks.xmpp.core.sasl.model.Mechanisms;
import rocks.xmpp.core.session.model.Session;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.core.stanza.model.client.ClientPresence;
import rocks.xmpp.core.stanza.model.server.ServerIQ;
import rocks.xmpp.core.stanza.model.server.ServerMessage;
import rocks.xmpp.core.stanza.model.server.ServerPresence;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.core.tls.model.StartTls;
import rocks.xmpp.extensions.caps.model.EntityCapabilities1;
import rocks.xmpp.extensions.caps2.model.EntityCapabilities2;
import rocks.xmpp.extensions.compress.model.StreamCompression;
import rocks.xmpp.extensions.data.layout.model.Page;
import rocks.xmpp.extensions.data.mediaelement.model.Media;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.data.validate.model.Validation;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.disco.model.items.ItemDiscovery;
import rocks.xmpp.extensions.errors.model.ResourceLimitExceeded;
import rocks.xmpp.extensions.errors.model.StanzaTooBig;
import rocks.xmpp.extensions.errors.model.TooManyStanzas;
import rocks.xmpp.extensions.httpbind.model.Body;
import rocks.xmpp.extensions.privatedata.model.PrivateData;
import rocks.xmpp.extensions.privatedata.rosterdelimiter.model.RosterDelimiter;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;
import rocks.xmpp.im.roster.model.Roster;
import rocks.xmpp.im.roster.versioning.model.RosterVersioning;
import rocks.xmpp.im.subscription.preapproval.model.SubscriptionPreApproval;

import java.util.Arrays;

/**
 * Defines core classes to be bound to the JAXBContext.
 * <p>
 * This class is not intended to be used by end users.
 *
 * @author Christian Schudt
 */
public final class CoreContext implements XmppContext {

    @Override
    public final Iterable<Class<?>> getClasses() {

        return Arrays.asList(

                // Core
                StreamFeatures.class, StreamError.class, ClientMessage.class, ClientPresence.class, ClientIQ.class, ServerMessage.class, ServerIQ.class, ServerPresence.class, Session.class, Bind.class, Mechanisms.class, StartTls.class, SubscriptionPreApproval.class, RosterVersioning.class, Roster.class,

                // XEP-0004: Data Forms
                DataForm.class,

                // XEP-0030: Service Discovery
                InfoDiscovery.class, ItemDiscovery.class,

                // XEP-0059: Result Set Management
                ResultSetManagement.class,

                // XEP-0049: Private XML Storage
                PrivateData.class,

                // XEP-0083: Nested Roster Groups
                RosterDelimiter.class,

                // XEP-0115: Entity Capabilities
                EntityCapabilities1.class,

                // XEP-0122: Data Forms Validation
                Validation.class,

                // XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)
                Body.class,

                // XEP-0138: Stream Compression
                StreamCompression.class,

                // XEP-0141: Data Forms Layout
                Page.class,

                // XEP-0205: Best Practices to Discourage Denial of Service Attacks
                ResourceLimitExceeded.class, StanzaTooBig.class, TooManyStanzas.class,

                // XEP-0221: Data Forms Media Element
                Media.class,

                // XEP-0390 Entity Capabilities 2.0
                EntityCapabilities2.class
        );
    }
}
