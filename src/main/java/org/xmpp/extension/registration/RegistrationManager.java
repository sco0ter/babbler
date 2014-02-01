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

package org.xmpp.extension.registration;

import org.xmpp.Connection;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public final class RegistrationManager extends ExtensionManager {
    private RegistrationManager(Connection connection) {
        super(connection);
    }

    public Registration requestRegistrationFields() throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new Registration()));
        if (result.getType() == IQ.Type.ERROR) {
            throw new StanzaException(result.getError());
        } else {
            return result.getExtension(Registration.class);
        }
    }

    public void register(Registration registration) throws TimeoutException, StanzaException {
        if (registration == null) {
            throw new IllegalArgumentException("registration must not be null.");
        }
        IQ result = connection.query(new IQ(IQ.Type.SET, registration));
        if (result.getType() == IQ.Type.ERROR) {
            throw new StanzaException(result.getError());
        }
    }

    public void removeAccount() throws TimeoutException {
        IQ result = connection.query(new IQ(IQ.Type.SET, new Registration()));
    }

    public void changePassword(String username, String password) throws TimeoutException, StanzaException {
        IQ result = connection.query(new IQ(IQ.Type.SET, new Registration(username, password)));
        if (result.getType() == IQ.Type.ERROR) {
            throw new StanzaException(result.getError());
        }
    }


}
