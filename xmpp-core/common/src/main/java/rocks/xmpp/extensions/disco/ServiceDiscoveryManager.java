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

package rocks.xmpp.extensions.disco;

import java.util.List;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.disco.model.info.InfoProvider;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.disco.model.items.ItemNode;
import rocks.xmpp.extensions.disco.model.items.ItemProvider;
import rocks.xmpp.extensions.rsm.model.ResultSetManagement;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * @author Christian Schudt
 */
public interface ServiceDiscoveryManager extends IQHandler, ExtensionProtocol {

    /**
     * Discovers information about another XMPP entity.
     *
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/extensions/xep-0030.html#info">3. Discovering Information About a Jabber
     * Entity</a></cite></p>
     * <p>A requesting entity may want to discover information about another entity on the network. The information
     * desired generally is of two kinds:</p>
     * <ol>
     * <li>The target entity's identity.</li>
     * <li>The features offered and protocols supported by the target entity.</li>
     * </ol>
     * </blockquote>
     *
     * @param jid The entity's JID.
     * @return The async service discovery result.
     */
    AsyncResult<DiscoverableInfo> discoverInformation(Jid jid);

    /**
     * Discovers information about another XMPP entity targeted at a specific node.
     *
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/extensions/xep-0030.html#info-nodes">3.2 Info Nodes</a></cite></p>
     * <p>A disco#info query MAY also be directed to a specific node identifier associated with a JID.</p>
     * </blockquote>
     *
     * @param jid  The entity's JID.
     * @param node The node.
     * @return The async service discovery result.
     * @see #discoverInformation(Jid)
     */
    AsyncResult<DiscoverableInfo> discoverInformation(Jid jid, String node);

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid The JID.
     * @return The async result with the discovered items.
     */
    AsyncResult<ItemNode> discoverItems(Jid jid);

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid       The JID.
     * @param resultSet The result set management.
     * @return The async result with the discovered items.
     */
    AsyncResult<ItemNode> discoverItems(Jid jid, ResultSetManagement resultSet);

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid  The JID.
     * @param node The node.
     * @return The async result with the discovered items.
     */
    AsyncResult<ItemNode> discoverItems(Jid jid, String node);

    /**
     * Discovers item associated with another XMPP entity.
     *
     * @param jid                 The JID.
     * @param node                The node.
     * @param resultSetManagement The result set management.
     * @return The async result with the discovered items.
     */
    AsyncResult<ItemNode> discoverItems(Jid jid, String node, ResultSetManagement resultSetManagement);

    /**
     * Discovers a service on the given entity by its identity.
     *
     * <p>E.g. to discover MUC services you could call this method with {@link Identity#conferenceText()};</p>
     *
     * <p>This method is generally preferred over {@link #discoverServices(Jid, String)}.</p>
     *
     * @param identity The identity.
     * @param jid      The XMPP entity, usually a server.
     * @return The services, that belong to the namespace.
     */
    AsyncResult<List<Item>> discoverServices(Jid jid, Identity identity);

    /**
     * Discovers a service on the given entity by its feature namespace.
     *
     * @param feature The feature namespace.
     * @param jid     The XMPP entity, usually a server.
     * @return The async result with the services, that belong to the namespace.
     */
    AsyncResult<List<Item>> discoverServices(Jid jid, String feature);

    /**
     * Adds an identity.
     *
     * @param identity The identity.
     * @see #removeIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     */
    void addIdentity(Identity identity);

    /**
     * Removes an identity.
     *
     * @param identity The identity.
     * @see #addIdentity(rocks.xmpp.extensions.disco.model.info.Identity)
     */
    void removeIdentity(Identity identity);

    /**
     * Adds a feature. Features should not be added or removed directly. Instead enable or disable the respective
     * extension manager, which will then add or remove the feature. That way, supported features are consistent with
     * enabled extension managers and service discovery won't reveal features, that are in fact not supported.
     *
     * @param feature The feature.
     * @see #removeFeature(String)
     */
    void addFeature(String feature);

    /**
     * Removes a feature.
     *
     * @param feature The feature.
     * @see #addFeature(String)
     */
    void removeFeature(String feature);

    /**
     * Adds an info provider.
     *
     * @param infoProvider The info provider.
     * @return true, if it has been successfully added.
     * @see #removeInfoProvider(InfoProvider)
     */
    boolean addInfoProvider(InfoProvider infoProvider);

    /**
     * Removes an info provider.
     *
     * @param infoProvider The info provider.
     * @return true, if it has been successfully remove.
     * @see #addInfoProvider(InfoProvider)
     */
    boolean removeInfoProvider(InfoProvider infoProvider);

    /**
     * Gets the root node.
     *
     * @return The root node.
     */
    DiscoverableInfo getDefaultInfo();

    /**
     * Adds an item provider. Requests to this handler will return items returned by the provider if appropriate.
     *
     * @param itemProvider The item provider.
     * @return If the provider could be added.
     * @see #removeItemProvider(ItemProvider)
     */
    boolean addItemProvider(ItemProvider itemProvider);

    /**
     * Removes an item provider.
     *
     * @param itemProvider The item provider.
     * @return If the provider could be removed .
     * @see #addItemProvider(ItemProvider)
     */
    boolean removeItemProvider(ItemProvider itemProvider);
}
