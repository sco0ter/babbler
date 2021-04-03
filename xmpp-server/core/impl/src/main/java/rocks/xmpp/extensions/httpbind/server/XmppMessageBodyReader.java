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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import rocks.xmpp.core.server.ServerConfiguration;
import rocks.xmpp.extensions.httpbind.model.Body;

/**
 * Reads the input stream to BOSH {@link Body} elements. If the was an error reading them, create a BOSH error, which will later be returned to the client.
 *
 * @author Christian Schudt
 */
@Provider
public class XmppMessageBodyReader implements MessageBodyReader<Body> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == Body.class;
    }

    @Override
    public Body readFrom(Class<Body> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws WebApplicationException {
        try {
            Unmarshaller unmarshaller = CDI.current().select(ServerConfiguration.class).get().getUnmarshaller(null);
            Object object = unmarshaller.unmarshal(entityStream);
            if (object instanceof Body) {
                return (Body) object;
            } else {
                return Body.builder().type(Body.Type.TERMINATE).condition(Body.Condition.BAD_REQUEST).build();
            }
        } catch (JAXBException e) {
            return Body.builder().type(Body.Type.TERMINATE).condition(Body.Condition.INTERNAL_SERVER_ERROR).build();
        }
    }
}
