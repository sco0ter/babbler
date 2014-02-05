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

package org.xmpp.extension.pubsub;

import org.xmpp.Connection;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.dataforms.DataForm;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public final class PubSubManager extends ExtensionManager {

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private PubSubManager(Connection connection) {
        super(connection);
        serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
    }

    public List<Subscription> getSubscriptions(String node) throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub.Subscriptions(node)));
        PubSub.Subscriptions subscriptions = result.getExtension(PubSub.Subscriptions.class);
        return subscriptions.getSubscriptions();
    }

    public List<Affiliation> getAffiliations(String node) throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub.Affiliations(node)));
        PubSub.Affiliations affiliations = result.getExtension(PubSub.Affiliations.class);
        return affiliations.getAffiliations();
    }

    /**
     * Subscribes to a node.
     *
     * @param node The node to subscribe to.
     * @return The subscription.
     * @throws TimeoutException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-subscribe">6.1 Subscribe to a Node</a>
     */
    public Subscription subscribe(String node) throws TimeoutException, StanzaException {
        return subscribe(node, null);
    }

    /**
     * Subscribes to and configures a node.
     *
     * @param node The node to subscribe to.
     * @return The subscription.
     * @throws TimeoutException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-subandconfig">6.3.7 Subscribe and Configure</a>
     */
    public Subscription subscribe(String node, DataForm dataForm) throws TimeoutException, StanzaException {
        if (node == null) {
            throw new IllegalArgumentException("node must not be null");
        }
        PubSub.Options options = null;
        if (dataForm != null) {
            options = new PubSub.Options(dataForm);
        }
        IQ result = connection.query(new IQ(IQ.Type.SET, new PubSub(new PubSub.Subscribe(node, connection.getConnectedResource().toBareJid()), options)));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getSubscription();
    }

    /**
     * Unsubscribes from a node.
     *
     * @param node The node to unsubscribe from.
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-unsubscribe">6.2 Unsubscribe from a Node</a>
     */
    public void unsubscribe(String node) throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.SET, new PubSub(new PubSub.Unsubscribe(node, connection.getConnectedResource().toBareJid()))));
    }

    /**
     * @param node
     * @return
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-request">6.3.2 Request</a>
     */
    public DataForm requestSubscriptionOptions(String node) throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub.Options(node, connection.getConnectedResource().toBareJid())));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getOptions().getDataForm();
    }

    /**
     * @param node
     * @return
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.3.5 Form Submission</a>
     */
    public void submitSubscriptionOptions(String node, DataForm dataForm) throws TimeoutException, StanzaException {
        connection.query(new IQ(IQ.Type.SET, new PubSub(new PubSub.Options(node, connection.getConnectedResource().toBareJid()))));
    }

    /**
     * @param node
     * @return
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.4 Request Default Subscription Configuration Options</a>
     */
    public DataForm requestDefaultSubscriptionConfigurationOptions(String node) throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub(new PubSub.Default(node))));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getOptions().getDataForm();
    }

    /**
     * @param node
     * @return
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestall">6.5.2 Requesting All Items</a>
     */
    public List<Item> getItems(String node) throws TimeoutException, StanzaException {
        return getItems(node, null);
    }

    /**
     * @param node
     * @return
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestrecent">6.5.7 Requesting the Most Recent Items</a>
     */
    public List<Item> getItems(String node, Long maxItems) throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub(new PubSub.Items(node, maxItems))));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getItems().getItems();
    }

    /**
     * @param id
     * @return
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestrecent">6.5.7 Requesting the Most Recent Items</a>
     */
    public Item getItem(String node, String id) throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub(new PubSub.Items(node, new Item(id)))));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getItems().getItems().get(0);
    }

    /**
     * @param node
     * @return
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish">7.1 Publish an Item to a Node</a>
     */
    public String publish(String node, Object item) throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.SET, new PubSub(new PubSub.Publish(node, new Item(item)))));
        PubSub pubSub = result.getExtension(PubSub.class);
        if (pubSub != null && pubSub.getPublish() != null && pubSub.getPublish().getItem() != null) {
            return pubSub.getPublish().getItem().getId();
        }
        return null;
    }

    /**
     * @param node
     * @return
     * @throws TimeoutException
     * @throws StanzaException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-delete">7.2 Delete an Item from a Node</a>
     */
    public void deleteItem(String node, String id, boolean notify) throws TimeoutException, StanzaException {
        connection.query(new IQ(IQ.Type.SET, new PubSub(new PubSub.Retract(node, new Item(id), notify))));
    }

    /**
     * Creates a node.
     *
     * @param node The node to subscribe to.
     * @throws TimeoutException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create">8.1 Create a Node</a>
     */
    public void create(String node) throws TimeoutException, StanzaException {
        create(node, null);
    }

    /**
     * Creates and configures a node.
     *
     * @param node The node to subscribe to.
     * @throws TimeoutException
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create-and-configure">8.1.3 Create and Configure a Node</a>
     */
    public void create(String node, DataForm dataForm) throws TimeoutException, StanzaException {
        if (node == null) {
            throw new IllegalArgumentException("node must not be null");
        }
        PubSub.Configure configure = null;
        if (dataForm != null) {
            configure = new PubSub.Configure(dataForm);
        }
        connection.query(new IQ(IQ.Type.SET, new PubSub(new PubSub.Create(node), configure)));
    }

    public DataForm getConfigurationForm(String node) throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub(new PubSub.Configure(node))));
        PubSub pubSub = result.getExtension(PubSub.class);
        return pubSub.getConfigure().getDataForm();
    }

    public void submitConfigurationForm(String node, DataForm dataForm) throws TimeoutException, StanzaException {
        connection.query(new IQ(IQ.Type.SET, new PubSub(new PubSub.Configure(node, dataForm))));
    }

    public void deleteNode(String node) throws TimeoutException, StanzaException {
        connection.query(new IQ(IQ.Type.SET, new org.xmpp.extension.pubsub.owner.PubSub(new org.xmpp.extension.pubsub.owner.PubSub.Delete(node))));
    }

}
