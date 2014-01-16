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

package org.xmpp.extension.privatestorage;

import org.xmpp.Connection;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public class PrivateDataManager extends ExtensionManager {

    public PrivateDataManager(Connection connection) {
        super(connection);
    }

    public <T> List<T> getData(Class<T> type) throws TimeoutException, StanzaException {
        try {
            T instance = type.newInstance();
            IQ result = connection.query(new IQ(IQ.Type.GET, new PrivateData(instance)));
            if (result.getType() == IQ.Type.RESULT) {
                PrivateData privateData = result.getExtension(PrivateData.class);
                return (List<T>) privateData.getPrivateData();
            }
            else if (result.getType() == IQ.Type.ERROR)
            {
                                                      throw new StanzaException(result.getError());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot instantiate class. Is there a public no-args default constructor?", e);
        }
        return null;
    }

    public void storeData(Object privateData) throws TimeoutException {
        connection.query(new IQ(IQ.Type.SET, new PrivateData(privateData)));
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return null;
    }
}
