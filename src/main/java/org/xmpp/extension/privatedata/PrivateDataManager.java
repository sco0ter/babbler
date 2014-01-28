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

package org.xmpp.extension.privatedata;

import org.xmpp.Connection;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * This class implements <a href="http://xmpp.org/extensions/xep-0049.html">XEP-0049: Private XML Storage</a>.
 * <p>
 * It allows to retrieve and store private data in the server's private XML storage.
 * </p>
 *
 * @author Christian Schudt
 */
public final class PrivateDataManager extends ExtensionManager {

    public PrivateDataManager(Connection connection) {
        super(connection);
    }

    /**
     * Gets private data, which is stored on the server.
     *
     * @param type The class of the private data. Note that this class needs a no-arg default constructor.
     * @param <T>  The type of private data.
     * @return The list of stored items of the given type.
     * @throws TimeoutException If the data could not be retrieved in time.
     * @throws StanzaException  If the server returned an error, e.g. if the used namespace is reserved.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getData(Class<T> type) throws TimeoutException, StanzaException {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            IQ result = connection.query(new IQ(IQ.Type.GET, new PrivateData(instance)));
            if (result.getType() == IQ.Type.RESULT) {
                PrivateData privateData = result.getExtension(PrivateData.class);
                return (List<T>) privateData.getItems();
            } else if (result.getType() == IQ.Type.ERROR) {
                throw new StanzaException(result.getError());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Cannot instantiate class.", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not find no-args default constructor.", e);
        }
        return null;
    }

    /**
     * Stores private data.
     *
     * @param privateData The private data. The class of this object must be annotated with JAXB annotations and must known to the XMPP context in order to marshal und unmarshal it.
     * @throws TimeoutException If the operation timed out.
     */
    public void storeData(Object privateData) throws TimeoutException {
        connection.query(new IQ(IQ.Type.SET, new PrivateData(privateData)));
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return null;
    }
}
