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

package rocks.xmpp.core.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.InboundIQHandler;
import rocks.xmpp.core.stanza.InboundMessageHandler;
import rocks.xmpp.core.stanza.InboundPresenceHandler;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.OutboundIQHandler;
import rocks.xmpp.core.stanza.OutboundMessageHandler;
import rocks.xmpp.core.stanza.OutboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;

/**
 * Manages the enabling and disabling of {@linkplain ExtensionProtocol extension protocols}.
 *
 * <p>Disabled extensions are not advertised by service discovery and usually also don't listen to inbound stanzas.</p>
 *
 * @see XmppSession#enableFeature(String)
 * @see XmppSession#disableFeature(String)
 */
final class ExtensionProtocolRegistry implements DiscoverableInfo {

    private final Map<String, ExtensionProtocol> extensionProtocolMap = new HashMap<>();

    private final Map<OutboundPresenceHandler, Consumer<PresenceEvent>> outboundPresenceHandlerConsumerMap =
            new HashMap<>();

    private final Map<OutboundMessageHandler, Consumer<MessageEvent>> outboundMessageHandlerConsumerMap =
            new HashMap<>();

    private final Map<OutboundIQHandler, Consumer<IQEvent>> outboundIQeHandlerConsumerMap = new HashMap<>();

    private final Map<InboundPresenceHandler, Consumer<PresenceEvent>> inboundPresenceHandlerConsumerMap =
            new HashMap<>();

    private final Map<InboundMessageHandler, Consumer<MessageEvent>> inboundMessageHandlerConsumerMap = new HashMap<>();

    private final Map<InboundIQHandler, Consumer<IQEvent>> inboundIQHandlerConsumerMap = new HashMap<>();

    private final XmppSession xmppSession;

    private ExtensionProtocolRegistry(XmppSession xmppSession) {
        this.xmppSession = xmppSession;
    }

    synchronized void registerExtension(ExtensionProtocol extensionProtocol) {
        extensionProtocolMap.put(extensionProtocol.getNamespace(), extensionProtocol);
        if (extensionProtocol instanceof InboundMessageHandler) {
            inboundMessageHandlerConsumerMap.put((InboundMessageHandler) extensionProtocol,
                    ((InboundMessageHandler) extensionProtocol)::handleInboundMessage);
        }
        if (extensionProtocol instanceof OutboundMessageHandler) {
            outboundMessageHandlerConsumerMap.put((OutboundMessageHandler) extensionProtocol,
                    ((OutboundMessageHandler) extensionProtocol)::handleOutboundMessage);
        }
        if (extensionProtocol instanceof InboundPresenceHandler) {
            inboundPresenceHandlerConsumerMap.put((InboundPresenceHandler) extensionProtocol,
                    ((InboundPresenceHandler) extensionProtocol)::handleInboundPresence);
        }
        if (extensionProtocol instanceof OutboundPresenceHandler) {
            outboundPresenceHandlerConsumerMap.put((OutboundPresenceHandler) extensionProtocol,
                    ((OutboundPresenceHandler) extensionProtocol)::handleOutboundPresence);
        }
        if (extensionProtocol instanceof InboundIQHandler) {
            inboundIQHandlerConsumerMap.put((InboundIQHandler) extensionProtocol,
                    ((InboundIQHandler) extensionProtocol)::handleInboundIQ);
        }
        if (extensionProtocol instanceof OutboundIQHandler) {
            outboundIQeHandlerConsumerMap.put((OutboundIQHandler) extensionProtocol,
                    ((OutboundIQHandler) extensionProtocol)::handleOutboundIQ);
        }
        if (extensionProtocol.isEnabled()) {
            enableExtension(extensionProtocol.getNamespace());
        }
    }

    synchronized void enableExtension(String namespace) {
        ExtensionProtocol extensionProtocol = extensionProtocolMap.get(namespace);
        if (extensionProtocol != null) {
            if (!extensionProtocol.isEnabled()) {
                if (extensionProtocol instanceof Manager) {
                    ((Manager) extensionProtocol).setEnabled(true);
                }
            }
            if (extensionProtocol instanceof IQHandler) {
                xmppSession.addIQHandler((IQHandler) extensionProtocol);
            }
            if (extensionProtocol instanceof InboundMessageHandler) {
                xmppSession.addInboundMessageListener(inboundMessageHandlerConsumerMap.get(extensionProtocol));
            }
            if (extensionProtocol instanceof OutboundMessageHandler) {
                xmppSession.addOutboundMessageListener(outboundMessageHandlerConsumerMap.get(extensionProtocol));
            }
            if (extensionProtocol instanceof InboundPresenceHandler) {
                xmppSession.addInboundPresenceListener(inboundPresenceHandlerConsumerMap.get(extensionProtocol));
            }
            if (extensionProtocol instanceof OutboundPresenceHandler) {
                xmppSession.addOutboundPresenceListener(outboundPresenceHandlerConsumerMap.get(extensionProtocol));
            }
            if (extensionProtocol instanceof InboundIQHandler) {
                xmppSession.addInboundIQListener(inboundIQHandlerConsumerMap.get(extensionProtocol));
            }
            if (extensionProtocol instanceof OutboundIQHandler) {
                xmppSession.addOutboundIQListener(outboundIQeHandlerConsumerMap.get(extensionProtocol));
            }
        }
    }

    synchronized void enableExtension(Class<?> clazz) {
        for (ExtensionProtocol extensionProtocol : extensionProtocolMap.values()) {
            if (extensionProtocol.getClass() == clazz) {
                enableExtension(extensionProtocol.getNamespace());
                return;
            }
        }
    }

    synchronized void disableExtension(String namespace) {
        ExtensionProtocol extensionProtocol = extensionProtocolMap.get(namespace);
        if (extensionProtocol != null) {
            if (extensionProtocol.isEnabled()) {
                if (extensionProtocol instanceof Manager) {
                    ((Manager) extensionProtocol).setEnabled(false);
                }
            }
            if (extensionProtocol instanceof IQHandler) {
                xmppSession.removeIQHandler((IQHandler) extensionProtocol);
            }
            if (extensionProtocol instanceof InboundMessageHandler) {
                xmppSession.removeInboundMessageListener(inboundMessageHandlerConsumerMap.get(extensionProtocol));
            }
            if (extensionProtocol instanceof OutboundMessageHandler) {
                xmppSession.removeOutboundMessageListener(outboundMessageHandlerConsumerMap.get(extensionProtocol));
            }
            if (extensionProtocol instanceof InboundPresenceHandler) {
                xmppSession.removeInboundPresenceListener(inboundPresenceHandlerConsumerMap.get(extensionProtocol));
            }
            if (extensionProtocol instanceof OutboundPresenceHandler) {
                xmppSession.removeOutboundPresenceListener(outboundPresenceHandlerConsumerMap.get(extensionProtocol));
            }
            if (extensionProtocol instanceof InboundIQHandler) {
                xmppSession.removeInboundIQListener(inboundIQHandlerConsumerMap.get(extensionProtocol));
            }
            if (extensionProtocol instanceof OutboundIQHandler) {
                xmppSession.removeOutboundIQListener(outboundIQeHandlerConsumerMap.get(extensionProtocol));
            }
        }
    }

    synchronized void disableExtension(Class<?> clazz) {
        for (ExtensionProtocol extensionProtocol : extensionProtocolMap.values()) {
            if (extensionProtocol.getClass() == clazz) {
                disableExtension(extensionProtocol.getNamespace());
                return;
            }
        }
    }

    @Override
    public final Set<Identity> getIdentities() {
        return this.extensionProtocolMap.values()
                .stream()
                .filter(e -> e instanceof DiscoverableInfo && e.isEnabled())
                .flatMap(extension -> ((DiscoverableInfo) extension).getIdentities().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public final Set<String> getFeatures() {
        return this.extensionProtocolMap.values()
                .stream()
                .filter(ExtensionProtocol::isEnabled)
                .flatMap(extension -> extension.getFeatures().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public final List<DataForm> getExtensions() {
        return this.extensionProtocolMap.values()
                .stream()
                .filter(e -> e instanceof DiscoverableInfo && e.isEnabled())
                .flatMap(extension -> ((DiscoverableInfo) extension).getExtensions().stream())
                .collect(Collectors.toList());
    }
}
