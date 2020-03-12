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

package rocks.xmpp.extensions.caps.client;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.extensions.caps.EntityCapabilitiesCache;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.hashes.model.Hash;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.cache.DirectoryCache;
import rocks.xmpp.util.cache.LruCache;
import rocks.xmpp.util.concurrent.AsyncResult;

import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public class ClientEntityCapabilitiesManager extends Manager implements EntityCapabilitiesCache, EntityCapabilitiesManager {

    private static final Logger logger = Logger.getLogger(ClientEntityCapabilitiesManager.class.getName());

    // Cache up to 100 capability hashes in memory.
    private static final Map<Hash, InfoNode> CAPS_CACHE = new LruCache<>(100);

    // Cache the capabilities of an entity.
    private static final Map<Jid, InfoNode> ENTITY_CAPABILITIES = new ConcurrentHashMap<>();

    private static final Map<Jid, AsyncResult<InfoNode>> REQUESTS = new ConcurrentHashMap<>();

    private final DirectoryCache directoryCache;

    public ClientEntityCapabilitiesManager(XmppSession xmppSession) {
        super(xmppSession);
        this.directoryCache = xmppSession.getConfiguration().getCacheDirectory() != null ? new DirectoryCache(xmppSession.getConfiguration().getCacheDirectory().resolve("caps")) : null;
    }

    @Override
    public InfoNode readCapabilities(Hash hash) {
        if (directoryCache != null) {
            // First check the in-memory cache.
            InfoNode infoNode = CAPS_CACHE.get(hash);
            if (infoNode != null) {
                return infoNode;
            }
            // If it's not present, check the persistent cache.
            String fileName = XmppUtils.hash(hash.toString().getBytes(StandardCharsets.UTF_8)) + ".caps";
            try {
                byte[] bytes = directoryCache.get(fileName);
                if (bytes != null) {
                    try (Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8)) {
                        infoNode = (InfoNode) xmppSession.createUnmarshaller().unmarshal(reader);
                        CAPS_CACHE.put(hash, infoNode);
                        return infoNode;
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, e, () -> "Could not read entity capabilities from persistent cache (file: " + fileName + ')');
            }
        }
        // The verification string is unknown, Service Discovery needs to be done.
        return null;
    }

    @Override
    public void writeCapabilities(Hash hash, InfoNode infoNode) {
        if (directoryCache != null) {
            // Write to in-memory cache.
            CAPS_CACHE.put(hash, infoNode);

            // Write to persistent cache.
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                XMLStreamWriter xmppStreamWriter = null;
                try {
                    xmppStreamWriter = XmppUtils.createXmppStreamWriter(xmppSession.getConfiguration().getXmlOutputFactory().createXMLStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8.name()));
                    xmppSession.createMarshaller().marshal(infoNode, xmppStreamWriter);
                    xmppStreamWriter.flush();
                } finally {
                    if (xmppStreamWriter != null) {
                        xmppStreamWriter.close();
                    }
                }
                directoryCache.put(XmppUtils.hash(hash.toString().getBytes(StandardCharsets.UTF_8)) + ".caps", byteArrayOutputStream.toByteArray());
            } catch (Exception e) {
                logger.log(Level.WARNING, e, () -> "Could not write entity capabilities to persistent cache. Reason: " + e.getMessage());
            }
        }
    }

    @Override
    public InfoNode readEntityCapabilities(Jid entity) {
        return ENTITY_CAPABILITIES.get(entity);
    }

    @Override
    public void writeEntityCapabilities(Jid entity, InfoNode infoNode) {
        ENTITY_CAPABILITIES.put(entity, infoNode);
    }

    /**
     * Discovers the capabilities of another XMPP entity.
     *
     * @param jid The JID, which should usually be a full JID.
     * @return The async result with the capabilities in form of a info node, which contains the identities, the features and service discovery extensions.
     * @see <a href="https://xmpp.org/extensions/xep-0115.html#discover">6.2 Discovering Capabilities</a>
     */
    @Override
    public final AsyncResult<InfoNode> discoverCapabilities(Jid jid) {
        InfoNode infoNode = readEntityCapabilities(jid);
        if (infoNode == null) {
            // Make sure, that for the same JID no multiple concurrent queries are sent. One is enough.
            return REQUESTS.computeIfAbsent(jid, key -> xmppSession.getManager(ServiceDiscoveryManager.class).discoverInformation(jid)
                    .whenComplete((result, e) -> {
                        if (result != null) {
                            writeEntityCapabilities(jid, result);
                        }
                        REQUESTS.remove(jid);
                    }));
        }
        return new AsyncResult<>(CompletableFuture.completedFuture(infoNode));
    }

    /**
     * Checks whether the entity supports the given feature. If the features are already known and cached
     *
     * @param feature The feature.
     * @param jid     The JID, which should usually be a full JID.
     * @return The async result with true, if this entity supports the feature.
     */
    @Override
    public final AsyncResult<Boolean> isSupported(String feature, Jid jid) {
        return discoverCapabilities(jid)
                .handle((infoNode, e) -> {
                    if (e == null) {
                        return infoNode.getFeatures().contains(feature);
                    } else {
                        if (e.getCause() instanceof StanzaErrorException) {
                            return false;
                        }
                        throw (CompletionException) e;
                    }
                });
    }
}
