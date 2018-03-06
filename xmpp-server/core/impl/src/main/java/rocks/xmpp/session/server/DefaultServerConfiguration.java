/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.session.server;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.bind.model.Bind;
import rocks.xmpp.core.sasl.model.Mechanisms;
import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.core.session.model.Session;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.core.stanza.model.client.ClientPresence;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.core.tls.model.StartTls;
import rocks.xmpp.extensions.compress.model.StreamCompression;
import rocks.xmpp.extensions.httpbind.model.Body;
import rocks.xmpp.extensions.last.model.LastActivity;
import rocks.xmpp.extensions.ping.model.Ping;
import rocks.xmpp.extensions.time.model.EntityTime;
import rocks.xmpp.im.roster.model.Roster;
import rocks.xmpp.websocket.model.Close;
import rocks.xmpp.websocket.model.Open;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class DefaultServerConfiguration implements ServerConfiguration {

    public static final JAXBContext JAXB_CONTEXT;

    public static final ThreadLocal<Marshaller> MARSHALLER = ThreadLocal.withInitial(() -> {
        try {
            Marshaller marshaller = DefaultServerConfiguration.JAXB_CONTEXT.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            return marshaller;
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        }
    });

    public static final ThreadLocal<Unmarshaller> UNMARSHALLER = ThreadLocal.withInitial(() -> {
        try {
            return DefaultServerConfiguration.JAXB_CONTEXT.createUnmarshaller();
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        }
    });

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(StreamFeatures.class, StreamError.class, ClientMessage.class, ClientPresence.class, ClientIQ.class, Session.class, Bind.class, Mechanisms.class, StartTls.class, StreamCompression.class, Roster.class, Open.class, Close.class, Body.class, Ping.class, EntityTime.class, LastActivity.class);
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    @Override
    public JAXBContext getJAXBContext(){
        return JAXB_CONTEXT;
    }

    @Override
    public Marshaller getMarshaller() {
        return MARSHALLER.get();
    }

    @Override
    public Unmarshaller getUnmarshaller() {
        return UNMARSHALLER.get();
    }

    @Override
    public int getPort() {
        return 5222;
    }

    @Override
    public Jid getDomain() {
        return Jid.ofDomain("domain");
    }
}
