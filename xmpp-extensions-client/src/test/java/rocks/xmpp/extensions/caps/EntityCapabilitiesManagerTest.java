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

package rocks.xmpp.extensions.caps;

import org.testng.Assert;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.ExtensionTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Christian Schudt
 */
public class EntityCapabilitiesManagerTest extends ExtensionTest {

    /**
     * This is for checking the double-checked locking of discoverCapabilities method (through debugging).
     *
     * @param args
     */
    public static void main(String[] args) {

        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {

                MockServer mockServer = new MockServer();

                final XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
                XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);

                final EntityCapabilitiesManager entityCapabilitiesManager = xmppSession1.getExtensionManager(EntityCapabilitiesManager.class);

                ExecutorService executorService = Executors.newCachedThreadPool();
                for (int i = 0; i < 100; i++) {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Assert.assertNotNull(entityCapabilitiesManager.discoverCapabilities(JULIET));
                            } catch (XmppException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }
}
