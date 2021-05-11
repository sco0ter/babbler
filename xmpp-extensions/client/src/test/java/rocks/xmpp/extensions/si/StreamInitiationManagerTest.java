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

package rocks.xmpp.extensions.si;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.BaseTest;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.bytestreams.ibb.model.InBandByteStream;
import rocks.xmpp.extensions.bytestreams.s5b.model.Socks5ByteStream;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.filetransfer.FileTransferManager;
import rocks.xmpp.extensions.si.model.StreamInitiation;
import rocks.xmpp.extensions.si.profile.filetransfer.model.SIFileTransferOffer;

/**
 * @author Christian Schudt
 */
public class StreamInitiationManagerTest extends BaseTest {

    @Test
    public void testStreamInitiationWithAcceptance() throws IOException, ExecutionException, InterruptedException {

        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);
        xmppSession2.enableFeature(StreamInitiation.NAMESPACE);
        
        FileTransferManager fileTransferManager = xmppSession2.getManager(FileTransferManager.class);

        fileTransferManager.addFileTransferOfferListener(e -> {
            if (!e.getName().equals("Filename") || e.getSize() != 123) {
                Assert.fail();
            }
            e.accept(new ByteArrayOutputStream());
        });
        StreamInitiationManager streamInitiationManager1 = xmppSession1.getManager(StreamInitiationManager.class);
        OutputStream outputStream = streamInitiationManager1
                .initiateStream(JULIET, new SIFileTransferOffer("Filename", 123), "image/type", Duration.ofSeconds(2))
                .get().getOutputStream();

        // Stream Initiation should have been succeeded, if we have OutputStream and no exception has occurred.
        Assert.assertNotNull(outputStream);
    }

    @Test
    public void testStreamInitiationWithRejection() throws InterruptedException {

        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);
        xmppSession2.enableFeature(StreamInitiation.NAMESPACE);
        
        FileTransferManager fileTransferManager = xmppSession2.getManager(FileTransferManager.class);
        fileTransferManager.addFileTransferOfferListener(e -> {
            if (!e.getName().equals("Filename") || e.getSize() != 123) {
                Assert.fail();
            }
            e.reject();
        });
        StreamInitiationManager streamInitiationManager1 = xmppSession1.getManager(StreamInitiationManager.class);

        try {
            streamInitiationManager1.initiateStream(JULIET, new SIFileTransferOffer("Filename", 123), "image/type",
                    Duration.ofSeconds(2)).get();
        } catch (ExecutionException e) {
            if (!(((StanzaErrorException) e.getCause()).getCondition() == Condition.FORBIDDEN)) {
                Assert.fail(e.getMessage(), e.getCause());
            } else {
                return;
            }
        }
        Assert.fail();
    }

    @Test
    public void testSupportedStreamMethods() {
        StreamInitiationManager streamInitiationManager = xmppSession.getManager(StreamInitiationManager.class);
        // By default Socks5 and IBB should be supported.
        Assert.assertEquals(streamInitiationManager.getSupportedStreamMethods().size(), 2);
        // If IBB gets disabled...
        xmppSession.disableFeature(InBandByteStream.NAMESPACE);
        // Only Socks5 should be advertises by Stream Initiation.
        Assert.assertEquals(streamInitiationManager.getSupportedStreamMethods().size(), 1);
        Assert.assertTrue(streamInitiationManager.getSupportedStreamMethods().contains(Socks5ByteStream.NAMESPACE));
        // If IBB gets enabled again...
        xmppSession.enableFeature(InBandByteStream.NAMESPACE);
        // and Socks5 gets disabled...
        xmppSession.disableFeature(Socks5ByteStream.NAMESPACE);
        Assert.assertEquals(streamInitiationManager.getSupportedStreamMethods().size(), 1);
        // Only IBB should be advertised by SI
        Assert.assertTrue(streamInitiationManager.getSupportedStreamMethods().contains(InBandByteStream.NAMESPACE));
    }

    @Test
    public void testServiceDiscoveryEntry() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = new TestXmppSession(JULIET, new MockServer());
        // By default, the manager should be disabled.
        Assert.assertFalse(xmppSession.getEnabledFeatures().contains(StreamInitiation.NAMESPACE));
        Assert.assertFalse(xmppSession.getEnabledFeatures().contains(SIFileTransferOffer.NAMESPACE));
        
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
        DiscoverableInfo discoverableInfo = serviceDiscoveryManager.discoverInformation(JULIET).get();
        Assert.assertFalse(discoverableInfo.getFeatures().contains(StreamInitiation.NAMESPACE));
        Assert.assertFalse(discoverableInfo.getFeatures().contains(SIFileTransferOffer.NAMESPACE));
        xmppSession.enableFeature(StreamInitiation.NAMESPACE);
        Assert.assertTrue(xmppSession.getEnabledFeatures().contains(StreamInitiation.NAMESPACE));
        Assert.assertTrue(xmppSession.getEnabledFeatures().contains(SIFileTransferOffer.NAMESPACE));
        discoverableInfo = serviceDiscoveryManager.discoverInformation(JULIET).get();
        Assert.assertTrue(discoverableInfo.getFeatures().contains(StreamInitiation.NAMESPACE));
        Assert.assertTrue(discoverableInfo.getFeatures().contains(SIFileTransferOffer.NAMESPACE));
    }
}
