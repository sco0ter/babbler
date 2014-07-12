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

package org.xmpp.extension.bytestreams.ibb;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.MockServer;
import org.xmpp.TestXmppSession;
import org.xmpp.XmppSession;
import org.xmpp.extension.bytestreams.ByteStreamEvent;
import org.xmpp.extension.bytestreams.ByteStreamListener;
import org.xmpp.extension.bytestreams.ByteStreamSession;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;

import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Christian Schudt
 */
public class IbbTest extends BaseTest {

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

    //@Test
    public void testInBandBytestreamManager() throws IOException {
        MockServer mockServer = new MockServer();
        final Lock lock = new ReentrantLock();
        final XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        final XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);
        final Condition condition = lock.newCondition();
        new Thread() {
            @Override
            public void run() {
                InBandByteStreamManager inBandBytestreamManager2 = xmppSession2.getExtensionManager(InBandByteStreamManager.class);
                inBandBytestreamManager2.addByteStreamListener(new ByteStreamListener() {
                    @Override
                    public void byteStreamRequested(final ByteStreamEvent e) {
                        final ByteStreamSession ibbSession;

                        ibbSession = e.accept();

                        new Thread() {
                            @Override
                            public void run() {

                                InputStream inputStream = ibbSession.getInputStream();
                                int b;
                                File file = new File("test1.png");
                                try {

                                    FileOutputStream outputStream = new FileOutputStream(file);

                                    try {
                                        while ((b = inputStream.read()) > -1) {
                                            outputStream.write(b);
                                        }
                                        outputStream.flush();
                                        outputStream.close();
                                        inputStream.close();

                                        try {
                                            lock.lock();
                                            condition.signal();
                                        } finally {
                                            lock.unlock();
                                        }

                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }

                        }.start();

                    }
                });
                InBandByteStreamManager inBandBytestreamManager1 = xmppSession1.getExtensionManager(InBandByteStreamManager.class);
                IbbSession ibbSession = null;
                try {
                    ibbSession = inBandBytestreamManager1.createInBandByteStreamSession(JULIET, 4096, "sid");
                    ibbSession.open();
                    InputStream inputStream = new FileInputStream(new File("xmpp.png"));
                    OutputStream os = ibbSession.getOutputStream();

                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    os.flush();
                    inputStream.close();
                    os.close();

                } catch (IOException e) {
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
        int i = 0;
    }
}
