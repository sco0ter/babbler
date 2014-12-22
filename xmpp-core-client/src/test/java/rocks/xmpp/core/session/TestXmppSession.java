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

package rocks.xmpp.core.session;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.MockServer;
import rocks.xmpp.core.SameThreadExecutorService;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQListener;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stanza.model.StanzaException;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stream.model.ClientStreamElement;

import javax.xml.stream.XMLOutputFactory;
import java.io.IOException;
import java.net.Proxy;

/**
 * @author Christian Schudt
 */
public class TestXmppSession extends XmppSession {

    private MockServer mockServer;

    public TestXmppSession() {
        this(Jid.valueOf("test@domain/resource"), new MockServer());
    }

    public TestXmppSession(Jid jid, MockServer mockServer) {
        this(jid, mockServer, XmppSessionConfiguration.getDefault());
    }

    public TestXmppSession(Jid jid, MockServer mockServer, XmppSessionConfiguration configuration) {
        super(null, configuration);
        connectedResource = jid;
        //getExtensionManager(Socks5ByteStreamManager.class).setLocalHostEnabled(false);

        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();

//        XMLStreamWriter xmlStreamWriter = null;
//        try {
//            xmlStreamWriter = XmppUtils.createXmppStreamWriter(xmlOutputFactory.createXMLStreamWriter(System.out), true);
//
//        } catch (XMLStreamException e) {
//        }
//        final XMLStreamWriter finalXmlStreamWriter = xmlStreamWriter;
        activeConnection = new Connection("hostname", 5222, Proxy.NO_PROXY) {

            @Override
            protected void restartStream() {

            }

            @Override
            public void send(ClientStreamElement clientStreamElement) {
//                try {
//                    TestXmppSession.this.getMarshaller().marshal(clientStreamElement, finalXmlStreamWriter);
//                } catch (JAXBException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void connect() throws IOException {

            }

            @Override
            public void connect(Jid from) throws IOException {

            }

            @Override
            public void close() throws IOException {

            }
        };
        stanzaListenerExecutor = new SameThreadExecutorService();
        this.mockServer = mockServer;
        mockServer.registerConnection(this);

        // Auto-connect
        updateStatus(Status.CONNECTED);
    }

    @Override
    public void send(ClientStreamElement element) {
        super.send(element);
        if (mockServer != null && element instanceof Stanza) {
            mockServer.receive(((Stanza) element).withFrom(connectedResource));
        }
    }

    @Override
    public IQ query(final IQ iq) throws XmppException {
        final IQ[] result = new IQ[1];

        final IQListener iqListener = new IQListener() {
            @Override
            public void handleIQ(IQEvent e) {
                if (e.isIncoming() && e.getIQ().getId() != null && e.getIQ().getId().equals(iq.getId())) {
                    result[0] = e.getIQ();
                }
            }
        };

        addIQListener(iqListener);
        send(iq);

        removeIQListener(iqListener);
        IQ response = result[0];
        if (response.getType() == IQ.Type.ERROR) {
            throw new StanzaException(response);
        }
        return response;
    }

    @Override
    public void close() throws IOException {
        super.close();
        updateStatus(Status.CLOSED, null);
    }
}
