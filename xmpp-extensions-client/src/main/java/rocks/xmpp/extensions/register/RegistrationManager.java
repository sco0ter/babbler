/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.register;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stream.StreamFeaturesManager;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.info.InfoNode;
import rocks.xmpp.extensions.register.model.Registration;
import rocks.xmpp.extensions.register.model.feature.RegisterFeature;

/**
 * This manager allows to register, cancel an existing registration (i.e. remove an account) or change the password with a host.
 *
 * @author Christian Schudt
 */
public final class RegistrationManager extends Manager {

    private RegistrationManager(XmppSession xmppSession) {
        super(xmppSession);
    }

    /**
     * Determines, if in-band registration is supported by the server.
     *
     * @return True if registration is supported by the server; otherwise false.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the server returned a stanza error. Common errors are {@link rocks.xmpp.core.stanza.model.errors.Condition#CONFLICT} (username is already in use) or {@link rocks.xmpp.core.stanza.model.errors.Condition#NOT_ACCEPTABLE} (some required information not provided).
     * @throws rocks.xmpp.core.session.NoResponseException If the server did not respond.
     */
    public boolean isRegistrationSupported() throws XmppException {
        // server returns a stream header to the client and MAY announce support for in-band registration by including the relevant stream feature.
        boolean isSupported = xmppSession.getManager(StreamFeaturesManager.class).getFeatures().containsKey(RegisterFeature.class);

        // Since the stream feature is only optional, discover the server features, too.
        if (!isSupported) {
            ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getManager(ServiceDiscoveryManager.class);
            InfoNode infoNode = serviceDiscoveryManager.discoverInformation(Jid.valueOf(xmppSession.getDomain()));
            isSupported = infoNode.getFeatures().contains(Registration.NAMESPACE);
        }
        return isSupported;
    }

    /**
     * Gets the registration data (instructions, fields and form) from the server.
     * <p>
     * In order to check if a field is required, you should check if a field is not null.
     * </p>
     * If you are already registered to the server, this method returns your registration data and {@link rocks.xmpp.extensions.register.model.Registration#isRegistered()} returns true.
     *
     * @return The registration data.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the server returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0077.html#usecases-register">3.1 Entity Registers with a Host</a>
     * @see rocks.xmpp.extensions.register.model.Registration
     */
    public Registration getRegistration() throws XmppException {
        IQ result = xmppSession.query(new IQ(IQ.Type.GET, new Registration()));
        return result.getExtension(Registration.class);
    }

    /**
     * Registers a new account. Call this method before authenticating.
     *
     * @param registration The registration.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the server returned a stanza error. Common errors are {@link rocks.xmpp.core.stanza.model.errors.Condition#CONFLICT} (username is already in use) or {@link rocks.xmpp.core.stanza.model.errors.Condition#NOT_ACCEPTABLE} (some required information not provided).
     * @throws rocks.xmpp.core.session.NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0077.html#usecases-register">3.1 Entity Registers with a Host</a>
     */
    public void register(Registration registration) throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, registration));
    }

    /**
     * Cancels a registration. This method must be called after having authenticated to the server.
     *
     * @throws rocks.xmpp.core.stanza.StanzaException      If the server returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0077.html#usecases-cancel">3.2 Entity Cancels an Existing Registration</a>
     */
    public void cancelRegistration() throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, Registration.remove()));
    }

    /**
     * Changes the password for the current user. This method must be called after having authenticated to the server.
     *
     * @param username The user name.
     * @param password The password.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the server returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the server did not respond.
     * @see <a href="http://xmpp.org/extensions/xep-0077.html#usecases-changepw">3.3 User Changes Password</a>
     */
    public void changePassword(String username, String password) throws XmppException {
        xmppSession.query(new IQ(IQ.Type.SET, Registration.builder().username(username).password(password).build()));
    }
}
