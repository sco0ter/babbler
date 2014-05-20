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

import org.xmpp.XmppSession;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.extension.register.feature.RegisterFeature;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.IQ;

/**
 * This manager allows to register, cancel an existing registration (i.e. remove an account) or change the password with a host.
 *
 * @author Christian Schudt
 */
public final class RegistrationManager extends ExtensionManager {

    private RegistrationManager(XmppSession xmppSession) {
        super(xmppSession);
    }

    /**
     * Determines, if in-band registration is supported by the server.
     *
     * @return True if registration is supported by the server; otherwise false.
     * @throws StanzaException     If the server returned a stanza error. Common errors are {@link org.xmpp.stanza.errors.Conflict} (username is already in use) or {@link org.xmpp.stanza.errors.NotAcceptable} (some required information not provided).
     * @throws NoResponseException If the server did not respond.
     */
    public boolean isRegistrationSupported() throws XmppException {
        // server returns a stream header to the client and MAY announce support for in-band registration by including the relevant stream feature.
        boolean isSupported = xmppSession.getFeaturesManager().getFeatures().containsKey(RegisterFeature.class);

        // Since the stream feature is only optional, discover the server features, too.
        if (!isSupported) {
            ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
            InfoNode infoNode = serviceDiscoveryManager.discoverInformation(null);
            isSupported = infoNode.getFeatures().contains(new Feature("jabber:iq:register"));
        }
        return isSupported;
    }

    /**
     * Gets the registration data (instructions, fields and form) from the server.
     * <p>
     * In order to check if a field is required, you should check if a field is not null.
     * </p>
     * If you are already registered to the server, this method returns your registration data and {@link org.xmpp.extension.register.Registration#isRegistered()} returns true.
     *
     * @return The registration data.
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0077.html#usecases-register">3.1 Entity Registers with a Host</a>
     * @see Registration
     */
    public Registration getRegistration() throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, new Registration()));
        return result.getExtension(Registration.class);
    }

    /**
     * Registers a new account. Call this method before authenticating.
     *
     * @param registration The registration.
     * @throws StanzaException     If the server returned a stanza error. Common errors are {@link org.xmpp.stanza.errors.Conflict} (username is already in use) or {@link org.xmpp.stanza.errors.NotAcceptable} (some required information not provided).
     * @throws NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0077.html#usecases-register">3.1 Entity Registers with a Host</a>
     */
    public void register(Registration registration) throws XmppException {
        if (registration == null) {
            throw new IllegalArgumentException("registration must not be null.");
        }
        xmppSession.query(new IQ(IQ.Type.SET, registration));
    }

    /**
     * Cancels a registration. This method must be called after having authenticated to the server.
     *
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0077.html#usecases-cancel">3.2 Entity Cancels an Existing Registration</a>
     */
    public void cancelRegistration() throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, new Registration(true)));
    }

    /**
     * Changes the password for the current user. This method must be called after having authenticated to the server.
     *
     * @param username The user name.
     * @param password The password.
     * @throws StanzaException     If the server returned a stanza error.
     * @throws NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0077.html#usecases-changepw">3.3 User Changes Password</a>
     */
    public void changePassword(String username, String password) throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, new Registration(username, password)));
    }
}
