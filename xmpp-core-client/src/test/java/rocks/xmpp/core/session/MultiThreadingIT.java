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

package rocks.xmpp.core.session;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.IntegrationTest;
import rocks.xmpp.core.XmppException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Christian Schudt
 */
public class MultiThreadingIT extends IntegrationTest {

    @Test
    public void shouldNotCloseMoreThanOnce() throws XmppException, InterruptedException {

        final XmppSession xmppSession = new XmppSession(DOMAIN, TcpConnectionConfiguration.getDefault());

        final AtomicInteger closing = new AtomicInteger();
        final AtomicInteger closed = new AtomicInteger();
        xmppSession.addSessionStatusListener(new SessionStatusListener() {
            @Override
            public void sessionStatusChanged(SessionStatusEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSING) {
                    closing.incrementAndGet();
                }
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    closed.incrementAndGet();
                }
            }
        });

        xmppSession.connect();

        Executor executor = Executors.newCachedThreadPool();
        final CountDownLatch countDownLatch = new CountDownLatch(100);
        final AtomicReference<Exception> exception = new AtomicReference<>();
        // Multiple threads try to close the session concurrently.
        for (int i = 0; i < 100; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        xmppSession.close();
                    } catch (XmppException e) {
                        exception.set(e);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        countDownLatch.await();
        Assert.assertEquals(closing.get(), 1);
        Assert.assertEquals(closed.get(), 1);
        if (exception.get() != null) {
            Assert.fail(exception.get().getMessage(), exception.get());
        }
    }

    @Test
    public void shouldNotConnectMoreThanOnceWhileConnected() throws Exception {

        final XmppSession xmppSession = new XmppSession(DOMAIN, TcpConnectionConfiguration.getDefault());

        final AtomicInteger connecting = new AtomicInteger();
        final AtomicInteger connected = new AtomicInteger();
        xmppSession.addSessionStatusListener(new SessionStatusListener() {
            @Override
            public void sessionStatusChanged(SessionStatusEvent e) {
                if (e.getStatus() == XmppSession.Status.CONNECTING) {
                    connecting.incrementAndGet();
                }
                if (e.getStatus() == XmppSession.Status.CONNECTED) {
                    connected.incrementAndGet();
                }
            }
        });

        Executor executor = Executors.newCachedThreadPool();
        final CountDownLatch countDownLatch = new CountDownLatch(100);
        final AtomicReference<Exception> exception = new AtomicReference<>();
        // Multiple threads try to connect the session concurrently.
        for (int i = 0; i < 100; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        xmppSession.connect();
                    } catch (XmppException e) {
                        exception.set(e);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        countDownLatch.await();
        Assert.assertEquals(connecting.get(), 1);
        Assert.assertEquals(connected.get(), 1);
        if (exception.get() != null) {
            throw exception.get();
        }
    }

    @Test
    public void shouldNotConnectWhileLoggedIn() throws Exception {

        final XmppSession xmppSession = new XmppSession(DOMAIN, TcpConnectionConfiguration.getDefault());

        final AtomicInteger connecting = new AtomicInteger();
        final AtomicInteger connected = new AtomicInteger();
        xmppSession.addSessionStatusListener(new SessionStatusListener() {
            @Override
            public void sessionStatusChanged(SessionStatusEvent e) {
                if (e.getStatus() == XmppSession.Status.CONNECTING) {
                    connecting.incrementAndGet();
                }
                if (e.getStatus() == XmppSession.Status.CONNECTED) {
                    connected.incrementAndGet();
                }
            }
        });

        xmppSession.connect();
        xmppSession.loginAnonymously();

        Executor executor = Executors.newCachedThreadPool();
        final CountDownLatch countDownLatch = new CountDownLatch(100);
        final AtomicReference<Exception> exception = new AtomicReference<>();
        // Multiple threads try to connect the session concurrently, although already connected.
        for (int i = 0; i < 100; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        xmppSession.connect();
                    } catch (XmppException e) {
                        exception.set(e);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        countDownLatch.await();
        Assert.assertEquals(connecting.get(), 1);
        Assert.assertEquals(connected.get(), 1);
        if (exception.get() != null) {
            throw exception.get();
        }
    }

    @Test
    public void shouldOnlyLoginOnce() throws Exception {

        final XmppSession xmppSession = new XmppSession(DOMAIN, TcpConnectionConfiguration.getDefault());

        final AtomicInteger authenticating = new AtomicInteger();
        final AtomicInteger authenticated = new AtomicInteger();
        final AtomicInteger exceptions = new AtomicInteger();
        xmppSession.addSessionStatusListener(new SessionStatusListener() {
            @Override
            public void sessionStatusChanged(SessionStatusEvent e) {
                if (e.getStatus() == XmppSession.Status.AUTHENTICATING) {
                    authenticating.incrementAndGet();
                }
                if (e.getStatus() == XmppSession.Status.AUTHENTICATED) {
                    authenticated.incrementAndGet();
                }
            }
        });

        xmppSession.connect();

        Executor executor = Executors.newCachedThreadPool();
        final CountDownLatch countDownLatch = new CountDownLatch(100);
        // Multiple threads try to connect the session concurrently, although already connected.
        for (int i = 0; i < 100; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        xmppSession.login(USER_1, PASSWORD_1);
                    } catch (Exception e) {
                        exceptions.incrementAndGet();
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        countDownLatch.await();
        Assert.assertEquals(authenticating.get(), 1);
        Assert.assertEquals(authenticated.get(), 1);
        Assert.assertEquals(exceptions.get(), 99);
    }
}
