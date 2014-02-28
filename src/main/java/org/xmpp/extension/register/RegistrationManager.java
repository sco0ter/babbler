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

package org.xmpp.extension.register;

import org.xmpp.Connection;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

/**
 * @author Christian Schudt
 */
public final class RegistrationManager extends ExtensionManager {
    private RegistrationManager(Connection connection) {
        super(connection);
    }

    /**
     * @return The registration form.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public Registration requestRegistrationFields() throws XmppException {
        IQ result = connection.query(new IQ(IQ.Type.GET, new Registration()));
        return result.getExtension(Registration.class);
    }

    /**
     * @param registration The registration.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void register(Registration registration) throws XmppException {
        if (registration == null) {
            throw new IllegalArgumentException("registration must not be null.");
        }
        IQ result = connection.query(new IQ(IQ.Type.SET, registration));
        if (result.getType() == IQ.Type.ERROR) {
            throw new StanzaException(result.getError());
        }
    }

    /**
     * Removes the account.
     *
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void removeAccount() throws XmppException {
        connection.query(new IQ(IQ.Type.SET, new Registration()));
    }

    /**
     * Changes the password.
     *
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public void changePassword(String username, String password) throws XmppException {
        connection.query(new IQ(IQ.Type.SET, new Registration(username, password)));
    }


}
