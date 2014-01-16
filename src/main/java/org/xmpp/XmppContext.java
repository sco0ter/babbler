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

package org.xmpp;

import org.xmpp.bind.Bind;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.attention.Attention;
import org.xmpp.extension.attention.AttentionManager;
import org.xmpp.extension.bob.Data;
import org.xmpp.extension.bosh.Body;
import org.xmpp.extension.chatstate.*;
import org.xmpp.extension.compression.Compression;
import org.xmpp.extension.dataforms.DataForm;
import org.xmpp.extension.delayeddelivery.DelayedDelivery;
import org.xmpp.extension.ibb.Close;
import org.xmpp.extension.ibb.InBandByteStreamManager;
import org.xmpp.extension.ibb.Open;
import org.xmpp.extension.lastactivity.LastActivity;
import org.xmpp.extension.lastactivity.LastActivityManager;
import org.xmpp.extension.messagecorrection.MessageCorrectionManager;
import org.xmpp.extension.messagecorrection.Replace;
import org.xmpp.extension.messagedeliveryreceipts.MessageDeliveryReceiptsManager;
import org.xmpp.extension.messagedeliveryreceipts.Received;
import org.xmpp.extension.messagedeliveryreceipts.Request;
import org.xmpp.extension.ping.Ping;
import org.xmpp.extension.ping.PingManager;
import org.xmpp.extension.privacylists.Privacy;
import org.xmpp.extension.pubsub.PubSub;
import org.xmpp.extension.registration.Register;
import org.xmpp.extension.registration.Registration;
import org.xmpp.extension.rosterx.RosterExchange;
import org.xmpp.extension.rpc.Rpc;
import org.xmpp.extension.search.Search;
import org.xmpp.extension.search.SearchManager;
import org.xmpp.extension.servicediscovery.info.InfoDiscovery;
import org.xmpp.extension.servicediscovery.items.ItemDiscovery;
import org.xmpp.extension.stanzaforwarding.Forwarded;
import org.xmpp.extension.stanzaforwarding.StanzaForwardingManager;
import org.xmpp.extension.time.EntityTime;
import org.xmpp.extension.time.EntityTimeManager;
import org.xmpp.extension.vcard.VCard;
import org.xmpp.extension.vcard.VCardManager;
import org.xmpp.extension.version.SoftwareVersion;
import org.xmpp.extension.version.SoftwareVersionManager;
import org.xmpp.im.Roster;
import org.xmpp.im.session.Session;
import org.xmpp.sasl.Mechanisms;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.Presence;
import org.xmpp.stream.Features;
import org.xmpp.stream.StreamError;
import org.xmpp.tls.StartTls;

import java.util.*;

/**
 * The XMPP context provides XMPP extensions and manager classes to manage extensions for a connection.
 *
 * @author Christian Schudt
 */
public abstract class XmppContext {

    private static volatile XmppContext defaultContext;

    private final Set<Class<?>> extensions = new HashSet<>();

    private final Set<Class<?>> core = new HashSet<>();

    private final Set<Class<? extends ExtensionManager>> managers = new HashSet<>();

    protected XmppContext() {
        core.addAll(Arrays.asList(Features.class, StreamError.class, Message.class, Presence.class, IQ.class, Session.class, Roster.class, Bind.class, Mechanisms.class, StartTls.class));
    }

    public static XmppContext getDefault() {
        // Use double-checked locking idiom
        if (defaultContext == null) {
            synchronized (XmppContext.class) {
                if (defaultContext == null) {
                    defaultContext = new DefaultXmppContext();
                }
            }
        }
        return defaultContext;
    }

    public static void setDefault(XmppContext xmppContext) {
        synchronized (XmppContext.class) {
            defaultContext = xmppContext;
        }
    }

    public final void registerExtension(Class<?>... extensions) {
        this.extensions.addAll(Arrays.asList(extensions));
    }

    public final void registerManager(Class<? extends ExtensionManager> manager) {
        managers.add(manager);
    }

    public final Collection<Class<?>> getExtensions() {
        List<Class<?>> result = new ArrayList<>(core);
        result.addAll(extensions);
        return result;
    }

    public final Collection<Class<? extends ExtensionManager>> getExtensionManagers() {
        return managers;
    }

    private static class DefaultXmppContext extends XmppContext {
        private DefaultXmppContext() {

            // XEP-0004: Data Forms
            registerExtension(DataForm.class);

            // XEP-0009: Jabber-RPC
            registerExtension(Rpc.class);

            // XEP-0012: Last Activity
            registerExtension(LastActivity.class);
            registerManager(LastActivityManager.class);

            // XEP-0016: Privacy Lists
            registerExtension(Privacy.class);

            // XEP-0030: Service Discovery
            registerExtension(InfoDiscovery.class, ItemDiscovery.class);

            // XEP-0047: In-Band Bytestreams
            registerExtension(org.xmpp.extension.ibb.Data.class, Open.class, Close.class);
            registerManager(InBandByteStreamManager.class);

            // XEP-0054: vcard-temp
            registerExtension(VCard.class);
            registerManager(VCardManager.class);

            // XEP-0055: Jabber Search
            registerExtension(Search.class);
            registerManager(SearchManager.class);

            registerExtension(PubSub.class);

            // XEP-0077: In-Band Registration
            registerExtension(Register.class, Registration.class);

            // XEP-0085: Chat State Notifications
            registerExtension(Active.class, Composing.class, Gone.class, Inactive.class, Paused.class);

            // XEP-0092: Software Version
            registerExtension(SoftwareVersion.class);
            registerManager(SoftwareVersionManager.class);

            // XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)
            registerExtension(Body.class);

            // XEP-0138: Stream Compression
            registerExtension(Compression.class);

            // XEP-0144: Roster Item Exchange
            registerExtension(RosterExchange.class);

            // XEP-0184: Message Delivery Receipts
            registerManager(MessageDeliveryReceiptsManager.class);
            registerExtension(Received.class, Request.class);

            // XEP-0199: XMPP Ping
            registerExtension(Ping.class);
            registerManager(PingManager.class);

            // XEP-0202: Entity Time
            registerExtension(EntityTime.class);
            registerManager(EntityTimeManager.class);

            // XEP-0203: Delayed Delivery
            registerExtension(DelayedDelivery.class);

            // XEP-0231: Bits of Binary
            registerExtension(Data.class);

            // XEP-0224: Attention
            registerExtension(Attention.class);
            registerManager(AttentionManager.class);

            // XEP-0297: Stanza Forwarding
            registerExtension(Forwarded.class);
            registerManager(StanzaForwardingManager.class);

            // XEP-0308: Last Message Correction
            registerExtension(Replace.class);
            registerManager(MessageCorrectionManager.class);
        }
    }
}
