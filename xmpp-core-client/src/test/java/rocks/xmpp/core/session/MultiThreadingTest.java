/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Christian Schudt
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

import rocks.xmpp.core.XmppException;
import rocks.xmpp.extensions.compress.CompressionManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Christian Schudt
 */
public class MultiThreadingTest {

    public static void main(String[] args) {
        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                Executor executor = Executors.newCachedThreadPool();
                try {
                    TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                            .port(5222)
                            .compressionMethods(CompressionManager.ZLIB)
                            .secure(false)
                            .build();

                    final XmppSession xmppSession = new XmppSession("localhost", tcpConfiguration);

                    // Listen for incoming messages.
                    xmppSession.addSessionStatusListener(new SessionStatusListener() {
                        @Override
                        public void sessionStatusChanged(SessionStatusEvent e) {
                            System.out.println(e.getStatus());
                        }
                    });

                    try {
                        xmppSession.connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < 120; i++) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    xmppSession.close();
                                } catch (XmppException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
