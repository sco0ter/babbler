package org.xmpp;/*
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

import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.Stanza;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.IQ;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Proxy;
import java.util.concurrent.Executor;

/**
 * @author Christian Schudt
 */
public class TestConnection extends Connection {

    private MockServer mockServer;

    public TestConnection() {
        this(Jid.valueOf("test@domain/resource"), new MockServer());
    }

    public TestConnection(Jid jid, MockServer mockServer) {
        super("", Proxy.NO_PROXY);
        connectedResource = jid;
        stanzaListenerExecutor = new Executor() {
            @Override
            public void execute(Runnable runnable) {
                runnable.run();
            }
        };
        this.mockServer = mockServer;
        mockServer.registerConnection(this);
    }

    @Override
    protected void secureConnection() {
    }

    @Override
    public void send(Object element) {
        super.send(element);
        if (mockServer != null && element instanceof Stanza) {
            ((Stanza) element).setFrom(connectedResource);
            mockServer.receive(((Stanza) element));
        }
    }

    @Override
    public IQ query(final IQ iq) throws XmppException {
        final IQ[] result = new IQ[1];

        final IQListener iqListener = new IQListener() {
            @Override
            public void handle(IQEvent e) {
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
    protected void restartStream() {
    }

    @Override
    public void close() throws IOException {
        super.close();
        updateStatus(Status.CLOSED);
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(OutputStream outputStream) throws XMLStreamException {
        return super.createXMLStreamWriter(outputStream);
    }

    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    public Marshaller getMarhaller() {
        return marshaller;
    }
}
