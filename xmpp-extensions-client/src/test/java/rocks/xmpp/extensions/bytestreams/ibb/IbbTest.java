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

package rocks.xmpp.extensions.bytestreams.ibb;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.extensions.ExtensionTest;
import rocks.xmpp.extensions.bytestreams.ByteStreamEvent;
import rocks.xmpp.extensions.bytestreams.ByteStreamListener;
import rocks.xmpp.extensions.bytestreams.ByteStreamSession;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.Feature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Christian Schudt
 */
public class IbbTest extends ExtensionTest {

    @Test
    public void testServiceDiscoveryEntry() {
        TestXmppSession connection1 = new TestXmppSession();
        InBandByteStreamManager inBandBytestreamManager = connection1.getExtensionManager(InBandByteStreamManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(inBandBytestreamManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("http://jabber.org/protocol/ibb");
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
        inBandBytestreamManager.setEnabled(false);
        Assert.assertFalse(inBandBytestreamManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
    }

    @Test
    public void testIbbSessionRejection() {
        MockServer mockServer = new MockServer();
        final XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        final XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);
        InBandByteStreamManager inBandBytestreamManager2 = xmppSession2.getExtensionManager(InBandByteStreamManager.class);
        inBandBytestreamManager2.addByteStreamListener(new ByteStreamListener() {
            @Override
            public void byteStreamRequested(final ByteStreamEvent e) {
                e.reject();
            }
        });

        InBandByteStreamManager inBandBytestreamManager1 = xmppSession1.getExtensionManager(InBandByteStreamManager.class);
        boolean rejected = false;
        try {
            inBandBytestreamManager1.initiateSession(JULIET, UUID.randomUUID().toString(), 4096);
        } catch (XmppException e) {
            if (e instanceof StanzaException) {
                if (((StanzaException) e).getStanza().getError().getCondition() == rocks.xmpp.core.stanza.model.errors.Condition.NOT_ACCEPTABLE) {
                    rejected = true;
                }
            }
        }

        if (!rejected) {
            Assert.fail("Should have been rejected");
        }
    }

    //@Test
    // Runs locally, but not on CI server...
    public void testInBandBytestreamManager() throws IOException {
        MockServer mockServer = new MockServer();
        final Lock lock = new ReentrantLock();
        final XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        final XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);
        final Condition condition = lock.newCondition();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        new Thread() {
            @Override
            public void run() {
                InBandByteStreamManager inBandBytestreamManager2 = xmppSession2.getExtensionManager(InBandByteStreamManager.class);
                inBandBytestreamManager2.addByteStreamListener(new ByteStreamListener() {
                    @Override
                    public void byteStreamRequested(final ByteStreamEvent e) {
                        final ByteStreamSession ibbSession;

                        try {
                            ibbSession = e.accept();

                            new Thread() {
                                @Override
                                public void run() {

                                    InputStream inputStream;
                                    try {
                                        inputStream = ibbSession.getInputStream();

                                        int b;
                                        while ((b = inputStream.read()) > -1) {
                                            outputStream.write(b);
                                        }
                                        outputStream.flush();
                                        outputStream.close();
                                        inputStream.close();

                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    } finally {
                                        try {
                                            lock.lock();
                                            condition.signal();
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
                                }
                            }.start();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                InBandByteStreamManager inBandBytestreamManager1 = xmppSession1.getExtensionManager(InBandByteStreamManager.class);
                ByteStreamSession ibbSession;
                try {
                    ibbSession = inBandBytestreamManager1.initiateSession(JULIET, "sid", 4096);
                    OutputStream os = ibbSession.getOutputStream();
                    os.write(new byte[]{1, 2, 3, 4});
                    os.flush();
                    os.close();
                } catch (IOException | XmppException e) {
                    e.printStackTrace();
                }
            }
        }.start();


        try {
            lock.lock();
            condition.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
        Assert.assertEquals(outputStream.toByteArray(), new byte[]{1, 2, 3, 4});
    }
}
