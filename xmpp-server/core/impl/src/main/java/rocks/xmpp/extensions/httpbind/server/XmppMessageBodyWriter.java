/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.extensions.httpbind.server;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.extensions.httpbind.model.Body;
import rocks.xmpp.util.XmppUtils;

/**
 * Writes BOSH {@link Body} elements in XMPP-style XML.
 *
 * @author Christian Schudt
 */
@Provider
public class XmppMessageBodyWriter implements MessageBodyWriter<Body> {

    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newFactory();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == Body.class;
    }

    @Override
    public void writeTo(Body body, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws WebApplicationException {
        Marshaller marshaller = CDI.current().select(ServerConfiguration.class).get().getMarshaller();
        try {
            XMLStreamWriter xmlStreamWriter = null;
            try {
                xmlStreamWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(entityStream);
                marshaller.marshal(body, XmppUtils.createXmppStreamWriter(xmlStreamWriter,
                        body.getWrappedObjects().stream().map(Object::getClass)
                                .anyMatch(clazz -> clazz == StreamFeatures.class || clazz == StreamError.class)));
                xmlStreamWriter.flush();
            } finally {
                if (xmlStreamWriter != null) {
                    xmlStreamWriter.close();
                }
            }
        } catch (XMLStreamException | JAXBException e) {
            // Try to respond with BOSH error.
            try {
                marshaller.marshal(
                        Body.builder().type(Body.Type.TERMINATE).condition(Body.Condition.INTERNAL_SERVER_ERROR)
                                .build(), entityStream);
            } catch (JAXBException e1) {
                // Otherwise respond with a HTTP error.
                throw new WebApplicationException(e.getMessage(), e, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }
    }
}

