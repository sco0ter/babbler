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
import org.xmpp.stanza.IQ;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public final class PubSubManager extends ExtensionManager {

    public PubSubManager(Connection connection) {
        super(connection);
    }

    public void publishItem(String node, Object object) {
        PubSub.Publish publish = new PubSub.Publish(node, null);
    }

    public List<Subscription> getSubscriptions(String node) throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub.Subscriptions(node)));
        PubSub.Subscriptions subscription = result.getExtension(PubSub.Subscriptions.class);

        return null;

    }

    public List<PubSub.Affiliations.Affiliation> getAffiliations(String node) throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new PubSub.Affiliations(node)));
        PubSub.Affiliations affiliations = result.getExtension(PubSub.Affiliations.class);

        return null;

    }

    public Subscription subscribe(String node) {
        IQ result = new IQ(IQ.Type.SET, new PubSub.Subscribe(node, connection.getConnectedResource().toBareJid()));
        return result.getExtension(Subscription.class);
    }

    public void unsubscribe(String node) {

    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
