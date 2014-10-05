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
import org.xmpp.debug.ConsoleDebugger;
import org.xmpp.debug.XmppDebugger;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.activity.Activity;
import org.xmpp.extension.address.Addresses;
import org.xmpp.extension.attention.Attention;
import org.xmpp.extension.avatar.AvatarManager;
import org.xmpp.extension.avatar.data.AvatarData;
import org.xmpp.extension.avatar.metadata.AvatarMetadata;
import org.xmpp.extension.avatar.vcard.AvatarUpdate;
import org.xmpp.extension.blocking.BlockList;
import org.xmpp.extension.blocking.BlockingManager;
import org.xmpp.extension.bob.Data;
import org.xmpp.extension.bytestreams.ibb.InBandByteStream;
import org.xmpp.extension.bytestreams.ibb.InBandByteStreamManager;
import org.xmpp.extension.bytestreams.s5b.Socks5ByteStream;
import org.xmpp.extension.caps.EntityCapabilities;
import org.xmpp.extension.caps.EntityCapabilitiesManager;
import org.xmpp.extension.carbons.Disable;
import org.xmpp.extension.carbons.Enable;
import org.xmpp.extension.carbons.Private;
import org.xmpp.extension.carbons.Sent;
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
import org.xmpp.extension.hashes.Hash;
import org.xmpp.extension.hashes.HashManager;
import org.xmpp.extension.httpauth.ConfirmationRequest;
import org.xmpp.extension.httpbind.Body;
import org.xmpp.extension.invisible.Invisible;
import org.xmpp.extension.invisible.Visible;
import org.xmpp.extension.jingle.Jingle;
import org.xmpp.extension.jingle.apps.filetransfer.JingleFileTransfer;
import org.xmpp.extension.jingle.apps.rtp.Rtp;
import org.xmpp.extension.jingle.transports.ibb.InBandBytestreamsTransportMethod;
import org.xmpp.extension.jingle.transports.iceudp.IceUdpTransportMethod;
import org.xmpp.extension.jingle.transports.s5b.S5bTransportMethod;
import org.xmpp.extension.json.Json;
import org.xmpp.extension.last.LastActivity;
import org.xmpp.extension.last.LastActivityManager;
import org.xmpp.extension.messagecorrect.Replace;
import org.xmpp.extension.mood.Mood;
import org.xmpp.extension.muc.Muc;
import org.xmpp.extension.muc.conference.DirectInvitation;
import org.xmpp.extension.nick.Nickname;
import org.xmpp.extension.offline.OfflineMessage;
import org.xmpp.extension.oob.OutOfBandFileTransferManager;
import org.xmpp.extension.oob.iq.OobIQ;
import org.xmpp.extension.oob.x.OobX;
import org.xmpp.extension.ping.Ping;
import org.xmpp.extension.ping.PingManager;
import org.xmpp.extension.privacy.Privacy;
import org.xmpp.extension.privatedata.PrivateData;
import org.xmpp.extension.privatedata.annotations.Annotation;
import org.xmpp.extension.privatedata.bookmarks.BookmarkStorage;
import org.xmpp.extension.privatedata.rosterdelimiter.RosterDelimiter;
import org.xmpp.extension.pubsub.PubSub;
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
import org.xmpp.stream.StreamError;
import org.xmpp.tls.StartTls;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * A configuration for an {@link org.xmpp.XmppSession}.
 * <p>
 * Most importantly it allows you to introduce custom extensions to your {@link org.xmpp.XmppSession}, simply by passing your JAXB annotated classes to the constructor of this class
 * and then {@linkplain org.xmpp.XmppSession#XmppSession(String, XmppSessionConfiguration, Connection...) use this configuration for the session}.
 * </p>
 * Since creating the JAXB context is quite expensive, this class allows you to create the context once and reuse it by multiple sessions.
 * You can also {@linkplain #setDefault(XmppSessionConfiguration) set} an application-wide default configuration (used by all XMPP sessions).
 *
 * @author Christian Schudt
 * @see org.xmpp.XmppSession#XmppSession(String, XmppSessionConfiguration, Connection...)
 */
public final class XmppSessionConfiguration {

    private static final boolean IS_DEBUG_MODE;

    static {
        IS_DEBUG_MODE = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
    }

    private static final Class<?>[] defaultContext = new Class[]{
            // Core classes
            Features.class, StreamError.class, Message.class, Presence.class, IQ.class, Session.class, Roster.class, Bind.class, Mechanisms.class, StartTls.class, SubscriptionPreApproval.class, RosterVersioning.class,

            // XEP-0004: Data Forms
            DataForm.class,

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

            // XEP-0030: Service Discovery
            InfoDiscovery.class, ItemDiscovery.class,

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

            // XEP-0124: Bidirectional-streams Over Synchronous HTTP (BOSH)
            Body.class,

            // XEP-0131: Stanza Headers and Internet Metadata
            Headers.class,

            // XEP-0138: Stream Compression
            Compress.class,

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
            Data.class,

            // XEP-0234: Jingle File Transfer
            JingleFileTransfer.class,

            // XEP-0249: Direct MUC Invitations
            DirectInvitation.class,

            // XEP-0260: Jingle SOCKS5 Bytestreams Transport Method
            S5bTransportMethod.class,

            // XEP-0261: Jingle In-Band Bytestreams Transport Method
            InBandBytestreamsTransportMethod.class,

            // XEP-0280: Message Carbons
            Enable.class, Disable.class, Private.class, org.xmpp.extension.carbons.Received.class, Sent.class,

            // XEP-0297: Stanza Forwarding
            Forwarded.class,
            StanzaForwardingManager.class,

            // XEP-0300: Use of Cryptographic Hash Functions in XMPP
            Hash.class,

            // XEP-0301: In-Band Real Time Text
            RealTimeText.class,

            // XEP-0308: Last Message Correction
            Replace.class,

            // XEP-0335: JSON Containers
            Json.class
    };

    private static volatile XmppSessionConfiguration defaultConfiguration;

    private final Collection<Class<? extends ExtensionManager>> initialExtensionManagers = new HashSet<>();

    private final JAXBContext jaxbContext;

    private XmppDebugger xmppDebugger;

    private boolean debugMode;

    /**
     * Creates a configuration for an {@link org.xmpp.XmppSession}. If you want to add custom classes to the {@link JAXBContext}, you can pass them as parameters.
     *
     * @param classes The classes to be bound to the JAXBContext.
     */
    public XmppSessionConfiguration(Class<?>... classes) {
        this.debugMode = IS_DEBUG_MODE;
        this.xmppDebugger = new ConsoleDebugger();

        // These are the manager classes which are loaded immediately, when the XmppSession is initialized,
        // Typically the add listeners to the session, e.g. to automatically reply.
        initialExtensionManagers.addAll(Arrays.asList(
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
                HashManager.class));

        Class<?>[] classesToBeBound = Arrays.copyOf(defaultContext, defaultContext.length + classes.length);
        System.arraycopy(classes, 0, classesToBeBound, defaultContext.length, classes.length);

        try {
            jaxbContext = JAXBContext.newInstance(classesToBeBound);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the default configuration.
     *
     * @return The default configuration.
     */
    public static XmppSessionConfiguration getDefault() {
        // Use double-checked locking idiom
        if (defaultConfiguration == null) {
            synchronized (XmppSessionConfiguration.class) {
                if (defaultConfiguration == null) {
                    defaultConfiguration = new XmppSessionConfiguration();
                }
            }
        }
        return defaultConfiguration;
    }

    /**
     * Sets the default configuration.
     *
     * @param configuration The default configuration.
     */
    public static void setDefault(XmppSessionConfiguration configuration) {
        synchronized (XmppSessionConfiguration.class) {
            defaultConfiguration = configuration;
        }
    }

    /**
     * Gets the JAXB context.
     *
     * @return The JAXB context.
     */
    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }

    /**
     * Gets the initial extension managers. Theses managers are initialized when the session is initialized, thus allowing them to immediately add listeners to the session e.g. to react to incoming stanzas.
     *
     * @return The initial extension managers.
     */
    public Collection<Class<? extends ExtensionManager>> getInitialExtensionManagers() {
        return initialExtensionManagers;
    }

    /**
     * Gets the current debugger for this session. If no debugger was set, the default debugger is the {@link org.xmpp.debug.ConsoleDebugger}.
     *
     * @return The debugger.
     * @see #setDebugger(org.xmpp.debug.XmppDebugger)
     * @see #setDebugMode(boolean)
     */
    public final XmppDebugger getDebugger() {
        return xmppDebugger;
    }

    /**
     * Sets the debugger for this session.
     *
     * @param xmppDebugger The debugger.
     * @see #getDebugger()
     * @see #setDebugMode(boolean)
     */
    public final void setDebugger(XmppDebugger xmppDebugger) {
        this.xmppDebugger = xmppDebugger;
    }

    /**
     * Indicates, whether this session is in debug mode. By default every session is in debug mode, if the JVM was started in debug mode.
     *
     * @return True, if this session is in debug mode.
     * @see #setDebugMode(boolean)
     * @see #getDebugger()
     */
    public final boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Sets, if debug mode is enabled for this session.
     *
     * @param debugMode If this session is in debug mode.
     * @see #isDebugMode()
     * @see #setDebugger(org.xmpp.debug.XmppDebugger)
     */
    public final void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

}
