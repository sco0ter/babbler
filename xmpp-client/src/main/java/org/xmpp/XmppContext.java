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
import org.xmpp.extension.activity.Activity;
import org.xmpp.extension.address.Addresses;
import org.xmpp.extension.attention.Attention;
import org.xmpp.extension.attention.AttentionManager;
import org.xmpp.extension.avatar.AvatarManager;
import org.xmpp.extension.avatar.vcard.AvatarUpdate;
import org.xmpp.extension.blocking.BlockList;
import org.xmpp.extension.blocking.BlockingManager;
import org.xmpp.extension.bob.Data;
import org.xmpp.extension.bytestreams.ibb.InBandByteStream;
import org.xmpp.extension.bytestreams.ibb.InBandByteStreamManager;
import org.xmpp.extension.bytestreams.s5b.Socks5ByteStream;
import org.xmpp.extension.caps.EntityCapabilities;
import org.xmpp.extension.caps.EntityCapabilitiesManager;
import org.xmpp.extension.carbons.*;
import org.xmpp.extension.chatstates.*;
import org.xmpp.extension.compress.Compress;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.data.layout.Page;
import org.xmpp.extension.data.media.Media;
import org.xmpp.extension.data.validate.Validation;
import org.xmpp.extension.delay.DelayedDelivery;
import org.xmpp.extension.disco.info.InfoDiscovery;
import org.xmpp.extension.disco.items.ItemDiscovery;
import org.xmpp.extension.featureneg.FeatureNegotiation;
import org.xmpp.extension.forward.Forwarded;
import org.xmpp.extension.forward.StanzaForwardingManager;
import org.xmpp.extension.geoloc.GeoLocation;
import org.xmpp.extension.geoloc.GeoLocationManager;
import org.xmpp.extension.httpbind.Body;
import org.xmpp.extension.invisible.Invisible;
import org.xmpp.extension.invisible.Visible;
import org.xmpp.extension.jingle.Jingle;
import org.xmpp.extension.jingle.transports.iceudp.IceUdpTransportMethod;
import org.xmpp.extension.json.Json;
import org.xmpp.extension.last.LastActivity;
import org.xmpp.extension.last.LastActivityManager;
import org.xmpp.extension.messagecorrect.MessageCorrectionManager;
import org.xmpp.extension.messagecorrect.Replace;
import org.xmpp.extension.mood.Mood;
import org.xmpp.extension.muc.Muc;
import org.xmpp.extension.muc.admin.MucAdmin;
import org.xmpp.extension.muc.conference.DirectInvitation;
import org.xmpp.extension.muc.owner.MucOwner;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.extension.nick.Nickname;
import org.xmpp.extension.offline.OfflineMessage;
import org.xmpp.extension.oob.OutOfBandFileTransferManager;
import org.xmpp.extension.oob.iq.OobIQ;
import org.xmpp.extension.oob.x.OobX;
import org.xmpp.extension.ping.Ping;
import org.xmpp.extension.ping.PingManager;
import org.xmpp.extension.privacy.Privacy;
import org.xmpp.extension.privatedata.PrivateData;
import org.xmpp.extension.privatedata.PrivateDataManager;
import org.xmpp.extension.privatedata.annotations.Annotation;
import org.xmpp.extension.privatedata.bookmarks.BookmarkStorage;
import org.xmpp.extension.privatedata.rosterdelimiter.RosterDelimiter;
import org.xmpp.extension.pubsub.PubSub;
import org.xmpp.extension.pubsub.PubSubManager;
import org.xmpp.extension.reach.Reachability;
import org.xmpp.extension.reach.ReachabilityManager;
import org.xmpp.extension.receipts.MessageDeliveryReceiptsManager;
import org.xmpp.extension.receipts.Received;
import org.xmpp.extension.receipts.Request;
import org.xmpp.extension.register.Registration;
import org.xmpp.extension.register.feature.RegisterFeature;
import org.xmpp.extension.rosterx.ContactExchange;
import org.xmpp.extension.rosterx.ContactExchangeManager;
import org.xmpp.extension.rpc.Rpc;
import org.xmpp.extension.rsm.ResultSet;
import org.xmpp.extension.rtt.RealTimeText;
import org.xmpp.extension.search.Search;
import org.xmpp.extension.search.SearchManager;
import org.xmpp.extension.shim.HeaderManager;
import org.xmpp.extension.shim.Headers;
import org.xmpp.extension.si.StreamInitiation;
import org.xmpp.extension.si.StreamInitiationManager;
import org.xmpp.extension.si.profile.filetransfer.SIFileTransferOffer;
import org.xmpp.extension.sm.StreamManagement;
import org.xmpp.extension.time.EntityTime;
import org.xmpp.extension.time.EntityTimeManager;
import org.xmpp.extension.tune.Tune;
import org.xmpp.extension.vcard.VCard;
import org.xmpp.extension.vcard.VCardManager;
import org.xmpp.extension.version.SoftwareVersion;
import org.xmpp.extension.version.SoftwareVersionManager;
import org.xmpp.im.Roster;
import org.xmpp.im.preapproval.SubscriptionPreApproval;
import org.xmpp.im.rosterver.RosterVersioning;
import org.xmpp.im.session.Session;
import org.xmpp.sasl.Mechanisms;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.client.Message;
import org.xmpp.stanza.client.Presence;
import org.xmpp.stream.Features;
import org.xmpp.stream.StreamException;
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
        core.addAll(Arrays.asList(Features.class, StreamException.class, Message.class, Presence.class, IQ.class, Session.class, Roster.class, Bind.class, Mechanisms.class, StartTls.class, SubscriptionPreApproval.class, RosterVersioning.class));
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

            // XEP-0013: Flexible Offline Message Retrieval
            registerExtension(OfflineMessage.class);

            // XEP-0016: Privacy Lists
            registerExtension(Privacy.class);

            // XEP-0020: Feature Negotiation
            registerExtension(FeatureNegotiation.class);

            // XEP-0030: Service Discovery
            registerExtension(InfoDiscovery.class, ItemDiscovery.class);

            // XEP-0033: Extended Stanza Addressing
            registerExtension(Addresses.class);

            // XEP-0045: Multi-User Chat
            registerExtension(Muc.class);
            registerExtension(MucUser.class, MucAdmin.class, MucOwner.class);

            // XEP-0047: In-Band Bytestreams
            registerExtension(InBandByteStream.class);
            registerManager(InBandByteStreamManager.class);

            // XEP-0048: BookmarkStorage
            registerExtension(BookmarkStorage.class);

            // XEP-0049: Private XML Storage
            registerExtension(PrivateData.class);
            registerManager(PrivateDataManager.class);

            // XEP-0054: vcard-temp
            registerExtension(VCard.class);
            registerManager(VCardManager.class);

            // XEP-0055: Jabber Search
            registerExtension(Search.class);
            registerManager(SearchManager.class);

            // XEP-0059: Result Set Management
            registerExtension(ResultSet.class);

            // XEP-0060: Publish-Subscribe
            registerExtension(PubSub.class);
            registerManager(PubSubManager.class);

            // XEP-0065: SOCKS5 Bytestreams
            registerExtension(Socks5ByteStream.class);

            // XEP-0066: Out of Band Data
            registerExtension(OobIQ.class, OobX.class);
            registerManager(OutOfBandFileTransferManager.class);

            // XEP-0077: In-Band Registration
            registerExtension(RegisterFeature.class, Registration.class);

            // XEP-0080: User Location
            registerExtension(GeoLocation.class);
            registerManager(GeoLocationManager.class);

            // XEP-0083: Nested Roster Groups
            registerExtension(RosterDelimiter.class);

            // XEP-0085: Chat State Notifications
            registerExtension(Active.class, Composing.class, Gone.class, Inactive.class, Paused.class);

            // XEP-0092: Software Version
            registerExtension(SoftwareVersion.class);
            registerManager(SoftwareVersionManager.class);

            // XEP-0095: Stream Initiation
            registerExtension(StreamInitiation.class);
            registerManager(StreamInitiationManager.class);

            // XEP-0096: SI File Transfer
            registerExtension(SIFileTransferOffer.class);

            // XEP-0107: User Mood
            registerExtension(Mood.class);

            // XEP-0108: User Activity
            registerExtension(Activity.class);

            // XEP-0115: Entity Capabilities
            registerExtension(EntityCapabilities.class);
            registerManager(EntityCapabilitiesManager.class);

            // XEP-0118: User Tune
            registerExtension(Tune.class);

            // XEP-0122: Data Forms Validation
            registerExtension(Validation.class);

            // XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)
            registerExtension(Body.class);

            // XEP-0131: Stanza Headers and Internet Metadata
            registerExtension(Headers.class);
            registerManager(HeaderManager.class);

            // XEP-0138: Stream Compression
            registerExtension(Compress.class);

            // XEP-0141: Data Forms Layout
            registerExtension(Page.class);

            // XEP-0144: Roster Item Exchange
            registerExtension(ContactExchange.class);
            registerManager(ContactExchangeManager.class);

            // XEP-0145: Annotations
            registerExtension(Annotation.class);

            // XEP-0152: Reachability Addresses
            registerExtension(Reachability.class);
            registerManager(ReachabilityManager.class);

            // XEP-0153: vCard-Based Avatars
            registerExtension(AvatarUpdate.class);
            registerManager(AvatarManager.class);

            // XEP-0166: Jingle
            registerExtension(Jingle.class);

            // XEP-0172: User Nickname
            registerExtension(Nickname.class);

            // XEP-0176: Jingle ICE-UDP Transport Method
            registerExtension(IceUdpTransportMethod.class);

            // XEP-0184: Message Delivery Receipts
            registerManager(MessageDeliveryReceiptsManager.class);
            registerExtension(Received.class, Request.class);

            // XEP-0186: Invisible Command
            registerExtension(Invisible.class, Visible.class);

            // XEP-0191: Blocking Command
            registerExtension(BlockList.class);
            registerManager(BlockingManager.class);

            // XEP-0198: Stream Management
            registerExtension(StreamManagement.class);

            // XEP-0199: XMPP Ping
            registerExtension(Ping.class);
            registerManager(PingManager.class);

            // XEP-0202: Entity Time
            registerExtension(EntityTime.class);
            registerManager(EntityTimeManager.class);

            // XEP-0203: Delayed Delivery
            registerExtension(DelayedDelivery.class);

            // XEP-0221: Data Forms Media Element
            registerExtension(Media.class);

            // XEP-0224: Attention
            registerExtension(Attention.class);
            registerManager(AttentionManager.class);

            // XEP-0231: Bits of Binary
            registerExtension(Data.class);

            // XEP-0249: Direct MUC Invitations
            registerExtension(DirectInvitation.class);

            // XEP-0280: Message Carbons
            registerExtension(Enable.class, Disable.class, Private.class, org.xmpp.extension.carbons.Received.class, Sent.class);
            registerManager(MessageCarbonsManager.class);

            // XEP-0297: Stanza Forwarding
            registerExtension(Forwarded.class);
            registerManager(StanzaForwardingManager.class);

            // XEP-0301: In-Band Real Time Text
            registerExtension(RealTimeText.class);

            // XEP-0308: Last Message Correction
            registerExtension(Replace.class);
            registerManager(MessageCorrectionManager.class);

            // XEP-0335: JSON Containers
            registerExtension(Json.class);
        }
    }
}
