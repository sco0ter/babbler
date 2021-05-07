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

package rocks.xmpp.core.session.context.extensions;

import java.util.Arrays;
import java.util.Collection;

import rocks.xmpp.core.session.Extension;
import rocks.xmpp.core.session.Module;
import rocks.xmpp.extensions.activity.model.Activity;
import rocks.xmpp.extensions.avatar.UserAvatarProtocol;
import rocks.xmpp.extensions.blocking.BlockingManager;
import rocks.xmpp.extensions.bytestreams.ibb.InBandByteStreamManager;
import rocks.xmpp.extensions.bytestreams.s5b.Socks5ByteStreamManager;
import rocks.xmpp.extensions.carbons.MessageCarbonsManager;
import rocks.xmpp.extensions.carbons.model.MessageCarbons;
import rocks.xmpp.extensions.chatstates.ChatStateManager;
import rocks.xmpp.extensions.featureneg.model.FeatureNegotiation;
import rocks.xmpp.extensions.filetransfer.FileTransferManager;
import rocks.xmpp.extensions.forward.StanzaForwardingManager;
import rocks.xmpp.extensions.forward.model.Forwarded;
import rocks.xmpp.extensions.geoloc.GeoLocationManager;
import rocks.xmpp.extensions.geoloc.model.GeoLocation;
import rocks.xmpp.extensions.httpauth.HttpAuthenticationManager;
import rocks.xmpp.extensions.idle.model.Idle;
import rocks.xmpp.extensions.invisible.InvisibilityManager;
import rocks.xmpp.extensions.invisible.model.InvisibleCommand;
import rocks.xmpp.extensions.jingle.JingleManager;
import rocks.xmpp.extensions.jingle.apps.filetransfer.JingleFileTransferManager;
import rocks.xmpp.extensions.jingle.apps.filetransfer.model.JingleFileTransfer;
import rocks.xmpp.extensions.jingle.model.Jingle;
import rocks.xmpp.extensions.jingle.transports.ibb.model.InBandByteStreamsTransportMethod;
import rocks.xmpp.extensions.jingle.transports.s5b.model.S5bTransportMethod;
import rocks.xmpp.extensions.last.LastActivityManager;
import rocks.xmpp.extensions.mood.MoodManager;
import rocks.xmpp.extensions.mood.model.Mood;
import rocks.xmpp.extensions.muc.MultiUserChatManager;
import rocks.xmpp.extensions.muc.conference.model.DirectInvitation;
import rocks.xmpp.extensions.offline.OfflineMessageManager;
import rocks.xmpp.extensions.oob.OutOfBandFileTransferManager;
import rocks.xmpp.extensions.oob.model.iq.OobIQ;
import rocks.xmpp.extensions.ping.PingManager;
import rocks.xmpp.extensions.privacy.PrivacyListManager;
import rocks.xmpp.extensions.reach.ReachabilityManager;
import rocks.xmpp.extensions.reach.model.Reachability;
import rocks.xmpp.extensions.receipts.MessageDeliveryReceiptsManager;
import rocks.xmpp.extensions.receipts.model.MessageDeliveryReceipts;
import rocks.xmpp.extensions.register.RegistrationManager;
import rocks.xmpp.extensions.register.model.Registration;
import rocks.xmpp.extensions.rosterx.ContactExchangeManager;
import rocks.xmpp.extensions.rosterx.model.ContactExchange;
import rocks.xmpp.extensions.rpc.client.ClientRpcManager;
import rocks.xmpp.extensions.rtt.RealTimeTextManager;
import rocks.xmpp.extensions.search.SearchManager;
import rocks.xmpp.extensions.shim.client.ClientHeaderManager;
import rocks.xmpp.extensions.si.StreamInitiationManager;
import rocks.xmpp.extensions.si.model.StreamInitiation;
import rocks.xmpp.extensions.si.profile.filetransfer.model.SIFileTransferOffer;
import rocks.xmpp.extensions.sm.client.ClientStreamManager;
import rocks.xmpp.extensions.sm.model.StreamManagement;
import rocks.xmpp.extensions.softwareinfo.SoftwareInformationProtocol;
import rocks.xmpp.extensions.time.EntityTimeManager;
import rocks.xmpp.extensions.tune.model.Tune;
import rocks.xmpp.extensions.vcard.avatar.VCardBasedAvatarsProtocol;
import rocks.xmpp.extensions.vcard.temp.VCardManager;
import rocks.xmpp.extensions.version.client.ClientSoftwareVersionManager;

/**
 * Registers extensions and manager classes, (which should be initialized during the start of a session) to the {@link
 * rocks.xmpp.core.session.XmppSession}.
 *
 * @author Christian Schudt
 */
public final class ExtensionModule implements Module {

    @Override
    public final Collection<Extension> getExtensions() {
        return Arrays.asList(

                // XEP-0009: Jabber-RPC
                Extension.of(ClientRpcManager.class, false),

                // XEP-0012: Last Activity
                Extension.of(LastActivityManager.class, true),

                // XEP-0013: Flexible Offline Message Retrieval
                Extension.of(OfflineMessageManager.class, false),

                // XEP-0016: Privacy Lists
                Extension.of(PrivacyListManager.class, true),

                // XEP-0020: Feature Negotiation
                Extension.of(FeatureNegotiation.NAMESPACE, true),

                // XEP-0045: Multi-User Chat
                Extension.of(MultiUserChatManager.class, true),

                // XEP-0047: In-Band Bytestreams
                Extension.of(InBandByteStreamManager.class, true),

                // XEP-0054: vcard-temp
                Extension.of(VCardManager.class, false),

                // XEP-0055: Jabber Search
                Extension.of(SearchManager.class, true),

                // XEP-0065: SOCKS5 Bytestreams
                Extension.of(Socks5ByteStreamManager.class, true),

                // XEP-0066: Out of Band Data
                Extension.of(OobIQ.NAMESPACE, OutOfBandFileTransferManager.class, true),

                // XEP-0070: Verifying HTTP Requests via XMPP
                Extension.of(HttpAuthenticationManager.class, false),

                // XEP-0077: In-Band Registration
                Extension.of(Registration.NAMESPACE, RegistrationManager.class, false),

                // XEP-0080: User Location
                Extension.of(GeoLocation.NAMESPACE, GeoLocationManager.class, true, false),

                // XEP-0084: User Avatar
                Extension.of(UserAvatarProtocol.class, false),

                // XEP-0085: Chat State Notifications
                Extension.of(ChatStateManager.class, false),

                // XEP-0092: Software Version
                Extension.of(ClientSoftwareVersionManager.class, true),

                // XEP-0095: Stream Initiation
                Extension.of(StreamInitiation.NAMESPACE, StreamInitiationManager.class, true),

                // XEP-0096: SI File Transfer
                Extension.of(SIFileTransferOffer.NAMESPACE, FileTransferManager.class, true),

                // XEP-0107: User Mood
                Extension.of(Mood.NAMESPACE, MoodManager.class, true, false),

                // XEP-0108: User Activity
                Extension.of(Activity.NAMESPACE, null, true, false),

                // XEP-0118: User Tune
                Extension.of(Tune.NAMESPACE, null, true, false),

                // XEP-0131: Stanza Headers and Internet Metadata
                Extension.of(ClientHeaderManager.class, false),

                // XEP-0144: Roster Item Exchange
                Extension.of(ContactExchange.NAMESPACE, ContactExchangeManager.class, false),

                // XEP-0152: Reachability Addresses
                Extension.of(Reachability.NAMESPACE, ReachabilityManager.class, false),

                // XEP-0153: vCard-Based Avatars
                Extension.of(VCardBasedAvatarsProtocol.class, false),

                // XEP-0166: Jingle
                Extension.of(Jingle.NAMESPACE, JingleManager.class, false),

                // XEP-0184: Message Delivery Receipts
                Extension.of(MessageDeliveryReceipts.NAMESPACE, MessageDeliveryReceiptsManager.class, false),

                // XEP-0186: Invisible Command
                Extension.of(InvisibleCommand.NAMESPACE, InvisibilityManager.class, false),

                // XEP-0191: Blocking Command
                Extension.of(BlockingManager.class, false),

                // XEP-0198: Stream Management
                Extension.of(StreamManagement.NAMESPACE, ClientStreamManager.class, false),

                // XEP-0199: XMPP Ping
                Extension.of(PingManager.class, true),

                // XEP-0202: Entity Time
                Extension.of(EntityTimeManager.class, true),

                // XEP-0232: Software Information
                Extension.of(SoftwareInformationProtocol.class, true),

                // XEP-0234: Jingle File Transfer
                Extension.of(JingleFileTransfer.NAMESPACE, JingleFileTransferManager.class, false),

                // XEP-0249: Direct MUC Invitations
                Extension.of(DirectInvitation.NAMESPACE, MultiUserChatManager.class, true),

                // XEP-0260: Jingle SOCKS5 Bytestreams Transport Method
                Extension.of(S5bTransportMethod.NAMESPACE, null, false),

                // XEP-0261: Jingle In-Band Bytestreams Transport Method
                Extension.of(InBandByteStreamsTransportMethod.NAMESPACE, null, false),

                // XEP-0280: Message Carbons
                Extension.of(MessageCarbons.NAMESPACE, MessageCarbonsManager.class, false),

                // XEP-0297: Stanza Forwarding
                Extension.of(Forwarded.NAMESPACE, StanzaForwardingManager.class, false),

                // XEP-0301: In-Band Real Time Text
                Extension.of(RealTimeTextManager.class, false),

                // XEP-0319: Last User Interaction in Presence
                Extension.of(Idle.NAMESPACE, true)
        );
    }
}
