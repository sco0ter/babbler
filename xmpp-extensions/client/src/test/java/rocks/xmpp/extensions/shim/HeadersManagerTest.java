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

package rocks.xmpp.extensions.shim;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.BaseTest;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.shim.model.Headers;

/**
 * Tests for {@link HeaderManager}.
 */
public class HeadersManagerTest extends BaseTest {

    @Test
    public void testServiceDiscoveryIfHeadersAreDisabled() throws InterruptedException, ExecutionException {
        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());

        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        DiscoverableInfo discoverableInfo = serviceDiscoveryManager.discoverInformation(JULIET).get();
        Assert.assertFalse(discoverableInfo.getFeatures().contains(Headers.NAMESPACE));

        Assert.assertFalse(discoverableInfo.getFeatures().contains(Headers.NAMESPACE));
    }

    @Test
    public void testServiceDiscoveryIfHeadersAreEnabled() throws InterruptedException, ExecutionException {
        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());

        // JULIET supports the following headers:
        HeaderManager headerManager = xmppSession.getManager(HeaderManager.class);
        Assert.assertTrue(headerManager.addSupportedHeader("In-Reply-To"));
        Assert.assertTrue(headerManager.addSupportedHeader("Keywords"));

        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);

        DiscoverableInfo discoverableInfo = serviceDiscoveryManager.discoverInformation(JULIET).get();

        Assert.assertTrue(discoverableInfo.getFeatures().contains(Headers.NAMESPACE));

        discoverableInfo = serviceDiscoveryManager.discoverInformation(JULIET, "http://jabber.org/protocol/shim").get();
        Assert.assertTrue(discoverableInfo.getFeatures().contains("http://jabber.org/protocol/shim#In-Reply-To"));
        Assert.assertTrue(discoverableInfo.getFeatures().contains("http://jabber.org/protocol/shim#Keywords"));

    }

    @Test
    public void testDiscoverSupportedHeaders() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());

        // JULIET supports the following headers:
        HeaderManager headerManager = xmppSession.getManager(HeaderManager.class);
        headerManager.addSupportedHeader("In-Reply-To");
        headerManager.addSupportedHeader("Keywords");

        List<String> headers = headerManager.discoverSupportedHeaders(JULIET).get();

        Assert.assertEquals(headers.size(), 2);
        Assert.assertTrue(headers.contains("In-Reply-To"));
        Assert.assertTrue(headers.contains("Keywords"));
    }

    @Test
    public void testDiscoverSupportedHeadersIfProtocolIsNotEnabled() throws InterruptedException {
        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());

        HeaderManager headerManager = xmppSession.getManager(HeaderManager.class);
        try {
            headerManager.discoverSupportedHeaders(JULIET).get();
        } catch (ExecutionException e) {
            Assert.assertTrue(e.getCause() instanceof StanzaErrorException);
            return;
        }
        Assert.fail();
    }
}
