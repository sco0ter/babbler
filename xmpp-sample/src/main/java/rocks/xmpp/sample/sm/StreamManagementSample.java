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

package rocks.xmpp.sample.sm;

import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.Trackable;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.session.debug.ConsoleDebugger;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration;
import rocks.xmpp.extensions.sm.model.StreamManagement;

import java.util.concurrent.Executors;

public class StreamManagementSample {

    public static void main(String[] args) {

        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                try {

                    TcpConnectionConfiguration tcpConnectionConfiguration = TcpConnectionConfiguration.builder()
                            .hostname("localhost")
                            .port(5222)
                            .secure(false) // Disable TLS only for simpler example here.
                            .build();

                    BoshConnectionConfiguration boshConfiguration = BoshConnectionConfiguration.builder()
                            .hostname("localhost")
                            .port(7070)
                                    //.sslContext(getTrustAllSslContext())
                            .secure(false)
                            .build();

                    XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                            .debugger(ConsoleDebugger.class)
                            .build();

                    XmppClient xmppSession = new XmppClient("localhost", configuration, tcpConnectionConfiguration);
                    xmppSession.enableFeature(StreamManagement.NAMESPACE);
                    // Connect
                    xmppSession.connect();

                    // Login
                    xmppSession.login("admin", "admin", "sm");
                    xmppSession.addMessageAcknowledgedListener(messageEvent -> {
                        System.out.println("Received by server!!!");
                    });
                    // Send a message to myself, which is caught by the listener above.
                    Trackable<Message> trackableMessage = xmppSession.sendMessage(new Message(xmppSession.getConnectedResource(), Message.Type.CHAT, "Hello World! Echo!"));
                    trackableMessage.onAcknowledged(message -> {
                        System.out.println("Received by server: " + message);
                    });

                    Trackable<Presence> trackablePresence = xmppSession.sendPresence(new Presence(Presence.Show.AWAY));
                    trackablePresence.onAcknowledged(presence -> {
                        System.out.println("Received by server: " + presence);
                    });

                } catch (XmppException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
