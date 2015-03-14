/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.bytestreams.s5b;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.IntegrationTest;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.extensions.bytestreams.ByteStreamEvent;
import rocks.xmpp.extensions.bytestreams.ByteStreamListener;
import rocks.xmpp.extensions.bytestreams.ByteStreamSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Christian Schudt
 */
public class Socks5ByteStreamIT extends IntegrationTest {

    @Test
    public void test() throws XmppException, IOException {

        final XmppSession xmppSession1 = new XmppSession(DOMAIN);
        final XmppSession xmppSession2 = new XmppSession(DOMAIN);

        xmppSession1.connect();
        xmppSession1.login(USER_1, PASSWORD_1);
        xmppSession1.send(new Presence());

        xmppSession2.connect();
        xmppSession2.login(USER_2, PASSWORD_2);
        xmppSession2.send(new Presence());

        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Socks5ByteStreamManager socks5ByteStreamManager2 = xmppSession2.getManager(Socks5ByteStreamManager.class);
        socks5ByteStreamManager2.addByteStreamListener(new ByteStreamListener() {
            @Override
            public void byteStreamRequested(final ByteStreamEvent e) {
                final ByteStreamSession s5bSession;

                try {
                    s5bSession = e.accept();

                    new Thread() {
                        @Override
                        public void run() {

                            InputStream inputStream;
                            try {
                                inputStream = s5bSession.getInputStream();
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

        Socks5ByteStreamManager socks5ByteStreamManager1 = xmppSession1.getManager(Socks5ByteStreamManager.class);
        ByteStreamSession s5bSession = socks5ByteStreamManager1.initiateSession(xmppSession2.getConnectedResource(), "sid");
        OutputStream os = s5bSession.getOutputStream();
        os.write(new byte[]{1, 2, 3, 4});
        os.flush();
        os.close();

        try {
            lock.lock();
            condition.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
        Assert.assertEquals(outputStream.toByteArray(), new byte[]{1, 2, 3, 4});
    }
}
