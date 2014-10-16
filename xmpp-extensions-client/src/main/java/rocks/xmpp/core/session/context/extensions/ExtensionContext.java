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

package rocks.xmpp.core.session.context.extensions;

import rocks.xmpp.core.session.context.CoreContext;
import rocks.xmpp.extensions.activity.model.Activity;
import rocks.xmpp.extensions.address.model.Addresses;
import rocks.xmpp.extensions.attention.model.Attention;
import rocks.xmpp.extensions.avatar.AvatarManager;
import rocks.xmpp.extensions.avatar.model.data.AvatarData;
import rocks.xmpp.extensions.avatar.model.metadata.AvatarMetadata;
import rocks.xmpp.extensions.blocking.BlockingManager;
import rocks.xmpp.extensions.blocking.model.BlockList;
import rocks.xmpp.extensions.bookmarks.model.BookmarkStorage;
import rocks.xmpp.extensions.bytestreams.ibb.InBandByteStreamManager;
import rocks.xmpp.extensions.bytestreams.ibb.model.InBandByteStream;
import rocks.xmpp.extensions.bytestreams.s5b.model.Socks5ByteStream;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;
import rocks.xmpp.extensions.caps.model.EntityCapabilities;
import rocks.xmpp.extensions.carbons.model.Disable;
import rocks.xmpp.extensions.carbons.model.Enable;
import rocks.xmpp.extensions.carbons.model.Private;
import rocks.xmpp.extensions.carbons.model.Sent;
import rocks.xmpp.extensions.chatstates.model.*;
import rocks.xmpp.extensions.data.layout.model.Page;
import rocks.xmpp.extensions.data.mediaelement.model.Media;
import rocks.xmpp.extensions.data.validate.model.Validation;
import rocks.xmpp.extensions.delay.model.DelayedDelivery;
import rocks.xmpp.extensions.featureneg.model.FeatureNegotiation;
import rocks.xmpp.extensions.forward.model.Forwarded;
import rocks.xmpp.extensions.geoloc.GeoLocationManager;
import rocks.xmpp.extensions.geoloc.model.GeoLocation;
import rocks.xmpp.extensions.hashes.HashManager;
import rocks.xmpp.extensions.hashes.model.Hash;
import rocks.xmpp.extensions.httpauth.model.ConfirmationRequest;
import rocks.xmpp.extensions.invisible.model.Invisible;
import rocks.xmpp.extensions.invisible.model.Visible;
import rocks.xmpp.extensions.jingle.apps.filetransfer.model.JingleFileTransfer;
import rocks.xmpp.extensions.jingle.apps.rtp.model.Rtp;
import rocks.xmpp.extensions.jingle.model.Jingle;
import rocks.xmpp.extensions.jingle.transports.ibb.model.InBandBytestreamsTransportMethod;
import rocks.xmpp.extensions.jingle.transports.iceudp.model.IceUdpTransportMethod;
import rocks.xmpp.extensions.jingle.transports.s5b.model.S5bTransportMethod;
import rocks.xmpp.extensions.json.model.Json;
import rocks.xmpp.extensions.last.LastActivityManager;
import rocks.xmpp.extensions.last.model.LastActivity;
import rocks.xmpp.extensions.messagecorrect.model.Replace;
import rocks.xmpp.extensions.mood.model.Mood;
import rocks.xmpp.extensions.muc.conference.model.DirectInvitation;
import rocks.xmpp.extensions.muc.model.Muc;
import rocks.xmpp.extensions.nick.model.Nickname;
import rocks.xmpp.extensions.offline.model.OfflineMessage;
import rocks.xmpp.extensions.oob.OutOfBandFileTransferManager;
import rocks.xmpp.extensions.oob.model.iq.OobIQ;
import rocks.xmpp.extensions.oob.model.x.OobX;
import rocks.xmpp.extensions.ping.PingManager;
import rocks.xmpp.extensions.ping.model.Ping;
import rocks.xmpp.extensions.privacy.model.Privacy;
import rocks.xmpp.extensions.privatedata.model.PrivateData;
import rocks.xmpp.extensions.privatedata.rosterdelimiter.model.RosterDelimiter;
import rocks.xmpp.extensions.privatedata.rosternotes.model.Annotation;
import rocks.xmpp.extensions.pubsub.model.PubSub;
import rocks.xmpp.extensions.reach.ReachabilityManager;
import rocks.xmpp.extensions.reach.model.Reachability;
import rocks.xmpp.extensions.receipts.MessageDeliveryReceiptsManager;
import rocks.xmpp.extensions.receipts.model.Received;
import rocks.xmpp.extensions.receipts.model.Request;
import rocks.xmpp.extensions.register.model.Registration;
import rocks.xmpp.extensions.register.model.feature.RegisterFeature;
import rocks.xmpp.extensions.rosterx.ContactExchangeManager;
import rocks.xmpp.extensions.rosterx.model.ContactExchange;
import rocks.xmpp.extensions.rpc.model.Rpc;
import rocks.xmpp.extensions.rsm.model.ResultSet;
import rocks.xmpp.extensions.rtt.model.RealTimeText;
import rocks.xmpp.extensions.search.model.Search;
import rocks.xmpp.extensions.shim.model.Headers;
import rocks.xmpp.extensions.si.StreamInitiationManager;
import rocks.xmpp.extensions.si.model.StreamInitiation;
import rocks.xmpp.extensions.si.profile.filetransfer.model.SIFileTransferOffer;
import rocks.xmpp.extensions.sm.model.StreamManagement;
import rocks.xmpp.extensions.time.EntityTimeManager;
import rocks.xmpp.extensions.time.model.EntityTime;
import rocks.xmpp.extensions.tune.model.Tune;
import rocks.xmpp.extensions.vcard.avatar.model.AvatarUpdate;
import rocks.xmpp.extensions.vcard.temp.VCardManager;
import rocks.xmpp.extensions.vcard.temp.model.VCard;
import rocks.xmpp.extensions.version.SoftwareVersionManager;
import rocks.xmpp.extensions.version.model.SoftwareVersion;

import java.util.Arrays;

/**
 * @author Christian Schudt
 */
public class ExtensionContext extends CoreContext {

    public ExtensionContext() {
        // no-args-default constructor needed for implicit instantiation
        this(new Class[0]);
    }

    public ExtensionContext(Class<?>... extensions) {
        super(Arrays.asList(
                LastActivityManager.class,
                InBandByteStreamManager.class,
                VCardManager.class,
                OutOfBandFileTransferManager.class,
                GeoLocationManager.class,
                SoftwareVersionManager.class,
                StreamInitiationManager.class,
                EntityCapabilitiesManager.class,
                ContactExchangeManager.class,
                ReachabilityManager.class,
                AvatarManager.class,
                MessageDeliveryReceiptsManager.class,
                BlockingManager.class,
                PingManager.class,
                EntityTimeManager.class,
                HashManager.class
        ), concatenateArrays(extensions,

                // XEP-0009: Jabber-RPC
                Rpc.class,

                // XEP-0012: Last Activity
                LastActivity.class,

                // XEP-0013: Flexible Offline Message Retrieval
                OfflineMessage.class,

                // XEP-0016: Privacy Lists
                Privacy.class,

                // XEP-0020: Feature Negotiation
                FeatureNegotiation.class,

                // XEP-0033: Extended Stanza Addressing
                Addresses.class,

                // XEP-0045: Multi-User Chat
                Muc.class,

                // XEP-0047: In-Band Bytestreams
                InBandByteStream.class,

                // XEP-0048: BookmarkStorage
                BookmarkStorage.class,

                // XEP-0049: Private XML Storage
                PrivateData.class,

                // XEP-0054: vcard-temp
                VCard.class,

                // XEP-0055: Jabber Search
                Search.class,

                // XEP-0059: Result Set Management
                ResultSet.class,

                // XEP-0060: Publish-Subscribe
                PubSub.class,

                // XEP-0065: SOCKS5 Bytestreams
                Socks5ByteStream.class,

                // XEP-0066: Out of Band Data
                OobIQ.class, OobX.class,

                // XEP-0070: Verifying HTTP Requests via XMPP
                ConfirmationRequest.class,

                // XEP-0077: In-Band Registration
                RegisterFeature.class, Registration.class,

                // XEP-0080: User Location
                GeoLocation.class,

                // XEP-0083: Nested Roster Groups
                RosterDelimiter.class,

                // XEP-0084: User Avatar
                AvatarData.class, AvatarMetadata.class,

                // XEP-0085: Chat State Notifications
                Active.class, Composing.class, Gone.class, Inactive.class, Paused.class,

                // XEP-0092: Software Version
                SoftwareVersion.class,

                // XEP-0095: Stream Initiation
                StreamInitiation.class,

                // XEP-0096: SI File Transfer
                SIFileTransferOffer.class,

                // XEP-0107: User Mood
                Mood.class,

                // XEP-0108: User Activity
                Activity.class,

                // XEP-0115: Entity Capabilities
                EntityCapabilities.class,

                // XEP-0118: User Tune
                Tune.class,

                // XEP-0122: Data Forms Validation
                Validation.class,

                // XEP-0131: Stanza Headers and Internet Metadata
                Headers.class,

                // XEP-0141: Data Forms Layout
                Page.class,

                // XEP-0144: Roster Item Exchange
                ContactExchange.class,

                // XEP-0145: Annotations
                Annotation.class,

                // XEP-0152: Reachability Addresses
                Reachability.class,

                // XEP-0153: vCard-Based Avatars
                AvatarUpdate.class,

                // XEP-0166: Jingle
                Jingle.class,

                // XEP-0167: Jingle RTP Sessions
                Rtp.class,

                // XEP-0172: User Nickname
                Nickname.class,

                // XEP-0176: Jingle ICE-UDP Transport Method
                IceUdpTransportMethod.class,

                // XEP-0184: Message Delivery Receipts
                Received.class, Request.class,

                // XEP-0186: Invisible Command
                Invisible.class, Visible.class,

                // XEP-0191: Blocking Command
                BlockList.class,

                // XEP-0198: Stream Management
                StreamManagement.class,

                // XEP-0199: XMPP Ping
                Ping.class,

                // XEP-0202: Entity Time
                EntityTime.class,

                // XEP-0203: Delayed Delivery
                DelayedDelivery.class,

                // XEP-0221: Data Forms Media Element
                Media.class,

                // XEP-0224: Attention
                Attention.class,

                // XEP-0231: Bits of Binary
                InBandByteStream.Data.class,

                // XEP-0234: Jingle File Transfer
                JingleFileTransfer.class,

                // XEP-0249: Direct MUC Invitations
                DirectInvitation.class,

                // XEP-0260: Jingle SOCKS5 Bytestreams Transport Method
                S5bTransportMethod.class,

                // XEP-0261: Jingle In-Band Bytestreams Transport Method
                InBandBytestreamsTransportMethod.class,

                // XEP-0280: Message Carbons
                Enable.class, Disable.class, Private.class, rocks.xmpp.extensions.carbons.model.Received.class, Sent.class,

                // XEP-0297: Stanza Forwarding
                Forwarded.class,

                // XEP-0300: Use of Cryptographic Hash Functions in XMPP
                Hash.class,

                // XEP-0301: In-Band Real Time Text
                RealTimeText.class,

                // XEP-0308: Last Message Correction
                Replace.class,

                // XEP-0335: JSON Containers
                Json.class
        ));
    }

    private static Class<?>[] concatenateArrays(Class<?>[] customExtensions, Class<?>... xmppExtensions) {
        Class<?>[] combined = Arrays.copyOf(customExtensions, customExtensions.length + xmppExtensions.length);
        System.arraycopy(xmppExtensions, 0, combined, customExtensions.length, xmppExtensions.length);
        return combined;
    }
}
