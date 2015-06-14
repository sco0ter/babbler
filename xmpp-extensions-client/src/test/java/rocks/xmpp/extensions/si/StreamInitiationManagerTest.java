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

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TestXmppXmpp;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.ExtensionTest;
import rocks.xmpp.extensions.bytestreams.ibb.model.InBandByteStream;
import rocks.xmpp.extensions.bytestreams.s5b.model.Socks5ByteStream;
import rocks.xmpp.extensions.filetransfer.FileTransferManager;
import rocks.xmpp.extensions.si.profile.filetransfer.model.SIFileTransferOffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Christian Schudt
 */
public class StreamInitiationManagerTest extends ExtensionTest {

    //@Test
    public void testStreamInitiationWithAcceptance() throws XmppException, IOException {

        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppXmpp(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppXmpp(JULIET, mockServer);

        FileTransferManager fileTransferManager = xmppSession2.getManager(FileTransferManager.class);
        fileTransferManager.addFileTransferOfferListener(e -> {
            if (!e.getName().equals("Filename") || e.getSize() != 123) {
                Assert.fail();
            }
            try {
                e.accept(new ByteArrayOutputStream());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        StreamInitiationManager streamInitiationManager1 = xmppSession1.getManager(StreamInitiationManager.class);
        OutputStream outputStream = streamInitiationManager1.initiateStream(JULIET, new SIFileTransferOffer("Filename", 123), "image/type", 2000);

        // Stream Initiation should have been succeeded, if we have OutputStream and no exception has occurred.
        Assert.assertNotNull(outputStream);
    }

    //@Test
    public void testStreamInitiationWithRejection() throws XmppException, IOException {

        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppXmpp(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppXmpp(JULIET, mockServer);

        FileTransferManager fileTransferManager = xmppSession2.getManager(FileTransferManager.class);
        fileTransferManager.addFileTransferOfferListener(e -> {
            if (!e.getName().equals("Filename") || e.getSize() != 123) {
                Assert.fail();
            }
            e.reject();
        });
        StreamInitiationManager streamInitiationManager1 = xmppSession1.getManager(StreamInitiationManager.class);

        try {
            streamInitiationManager1.initiateStream(JULIET, new SIFileTransferOffer("Filename", 123), "image/type", 2000);
        } catch (StanzaException e) {
            if (!(e.getStanza().getError().getCondition() == Condition.FORBIDDEN)) {
                Assert.fail();
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
}
