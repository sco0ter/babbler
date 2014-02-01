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
import org.xmpp.stanza.IQ;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public final class PubSubManager extends ExtensionManager {

    private PubSubManager(Connection connection) {
        super(connection);
    }

    public void publishItem(String node, Object object) {
        PubSub.Publish publish = new PubSub.Publish(node, null);
    }

    public List<Subscription> getSubscriptions(String node) throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub.Subscriptions(node)));
        PubSub.Subscriptions subscriptions = result.getExtension(PubSub.Subscriptions.class);
        return subscriptions.getSubscriptions();
    }

    public List<Affiliation> getAffiliations(String node) throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub.Affiliations(node)));
        PubSub.Affiliations affiliations = result.getExtension(PubSub.Affiliations.class);
        return affiliations.getAffiliations();
    }

    public Subscription subscribe(String node) throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.SET, new PubSub.Subscribe(node, connection.getConnectedResource().toBareJid())));
        if (result.getType() == IQ.Type.RESULT) {
            PubSub pubSub = result.getExtension(PubSub.class);
            return pubSub.getSubscription();
        }
        return null;
    }

    public void unsubscribe(String node) throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.SET, new PubSub.Unsubscribe(node, connection.getConnectedResource().toBareJid())));
        if (result.getType() == IQ.Type.RESULT) {
            PubSub pubSub = result.getExtension(PubSub.class);

        }
    }

    public DataForm requestSubscriptionOptions(String node) throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub.Options(node)));
        PubSub pubSub = result.getExtension(PubSub.class);
        if (pubSub != null) {
            return pubSub.getOptions().getDataForm();
        }
        return null;
    }

    public DataForm requestDefaultSubscriptionConfigurationOptions(String node) throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub.Default(node)));
        PubSub pubSub = result.getExtension(PubSub.class);
        if (pubSub != null) {
            return pubSub.getOptions().getDataForm();
        }
        return null;
    }

    public List<Item> getItems(String node) throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub.Items(node)));

        return null;
    }
}
