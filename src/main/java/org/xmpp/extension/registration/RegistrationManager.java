package org.xmpp.extension.registration;

import org.xmpp.Connection;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public final class RegistrationManager extends ExtensionManager {
    protected RegistrationManager(Connection connection) {
        super(connection);
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return Arrays.asList();
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
