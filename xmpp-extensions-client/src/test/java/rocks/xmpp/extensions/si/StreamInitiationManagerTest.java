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

package rocks.xmpp.extensions.si;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.ExtensionTest;
import rocks.xmpp.extensions.filetransfer.FileTransferManager;
import rocks.xmpp.extensions.filetransfer.FileTransferOfferEvent;
import rocks.xmpp.extensions.filetransfer.FileTransferOfferListener;
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

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);

        FileTransferManager fileTransferManager = xmppSession2.getExtensionManager(FileTransferManager.class);
        fileTransferManager.addFileTransferOfferListener(new FileTransferOfferListener() {
            @Override
            public void fileTransferOffered(FileTransferOfferEvent e) {
                if (!e.getName().equals("Filename") || e.getSize() != 123) {
                    Assert.fail();
                }
                try {
                    e.accept(new ByteArrayOutputStream());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        StreamInitiationManager streamInitiationManager1 = xmppSession1.getExtensionManager(StreamInitiationManager.class);
        OutputStream outputStream = streamInitiationManager1.initiateStream(JULIET, new SIFileTransferOffer("Filename", 123), "image/type", 2000);

        // Stream Initiation should have been succeeded, if we have OutputStream and no exception has occurred.
        Assert.assertNotNull(outputStream);
    }

    //@Test
    public void testStreamInitiationWithRejection() throws XmppException, IOException {

        MockServer mockServer = new MockServer();

        XmppSession xmppSession1 = new TestXmppSession(ROMEO, mockServer);
        XmppSession xmppSession2 = new TestXmppSession(JULIET, mockServer);

        FileTransferManager fileTransferManager = xmppSession2.getExtensionManager(FileTransferManager.class);
        fileTransferManager.addFileTransferOfferListener(new FileTransferOfferListener() {
            @Override
            public void fileTransferOffered(FileTransferOfferEvent e) {
                if (!e.getName().equals("Filename") || e.getSize() != 123) {
                    Assert.fail();
                }
                e.reject();
            }
        });
        StreamInitiationManager streamInitiationManager1 = xmppSession1.getExtensionManager(StreamInitiationManager.class);

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
}
