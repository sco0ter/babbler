/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.sample.disco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.client.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.debug.gui.VisualDebugger;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.items.DiscoverableItem;
import rocks.xmpp.extensions.rsm.ResultSetProvider;

/**
 * @author Christian Schudt
 */
public final class DiscoSampleUser1 {

    private DiscoSampleUser1() {
    }

    public static void main(String[] args) {

        Executors.newFixedThreadPool(1).execute(() -> {
            try {

                TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                        .port(5222)
                        .channelEncryption(ChannelEncryption.DISABLED)
                        .build();

                XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                        .debugger(VisualDebugger.class)
                        .build();

                XmppClient xmppSession = XmppClient.create("localhost", configuration, tcpConfiguration);

                // Connect
                xmppSession.connect();
                // Login
                xmppSession.login("111", "111", "disco");

                List<DiscoverableItem> myItems = Collections.synchronizedList(new ArrayList<>());
                for (int i = 0; i < 100; i++) {
                    int finalI = i;
                    myItems.add(new DiscoverableItem() {
                        @Override
                        public String getId() {
                            return "test" + finalI;
                        }

                        @Override
                        public String getNode() {
                            return "myNode";
                        }

                        @Override
                        public Jid getJid() {
                            return Jid.of("test");
                        }

                        @Override
                        public String getName() {
                            return "myNode" + finalI;
                        }
                    });
                }

                ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
                serviceDiscoveryManager
                        .addItemProvider((to, from, node, locale) -> ResultSetProvider.forItems(myItems));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
