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

import java.util.Collections;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.DataBindingException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.util.LanguageUnmarshallerListener;
import rocks.xmpp.util.XmppUtils;

/**
 * @author Christian Schudt
 */
@ApplicationScoped
public class DefaultServerConfiguration implements ServerConfiguration {

    private static final JAXBContext JAXB_CONTEXT;

    private static final ThreadLocal<Marshaller> MARSHALLER = ThreadLocal.withInitial(() -> {
        try {
            Marshaller marshaller = DefaultServerConfiguration.JAXB_CONTEXT.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            return marshaller;
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        }
    });

    static {
        JAXB_CONTEXT = XmppUtils.createContext(Collections.emptyList());
    }

    @Override
    public JAXBContext getJAXBContext() {
        return JAXB_CONTEXT;
    }

    @Override
    public Marshaller getMarshaller() {
        return MARSHALLER.get();
    }

    @Override
    public Unmarshaller getUnmarshaller(Locale locale) {
        try {
            Unmarshaller unmarshaller = DefaultServerConfiguration.JAXB_CONTEXT.createUnmarshaller();
            unmarshaller.setListener(new LanguageUnmarshallerListener(locale));
            return unmarshaller;
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        }
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
