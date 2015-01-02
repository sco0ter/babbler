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

package rocks.xmpp.sample.disco;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.debug.gui.VisualDebugger;
import rocks.xmpp.extensions.disco.DefaultItemProvider;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.items.Item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author Christian Schudt
 */
public class DiscoSampleUser1 {

    public static void main(String[] args) throws IOException {

        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                try {

                    TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                            .port(5222)
                            .secure(false)
                            .build();

                    XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                            .debugger(VisualDebugger.class)
                            .defaultResponseTimeout(5000)
                            .build();

                    XmppSession xmppSession = new XmppSession("localhost", configuration, tcpConfiguration);

                    // Connect
                    xmppSession.connect();
                    // Login
                    xmppSession.login("111", "111", "disco");
                    // Send initial presence
                    xmppSession.send(new Presence());

                    List<Item> myItems = new ArrayList<>();
                    for (int i = 0; i < 100; i++) {
                        myItems.add(new Item(Jid.valueOf("test"), "myNode" + i, "test" + i));
                    }

                    ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
                    serviceDiscoveryManager.setItemProvider(new DefaultItemProvider(myItems));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
