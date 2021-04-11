package rocks.xmpp.sample;

import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

/**
 * @author Christian Schudt
 */
public class JBossLocalUserSaslClient implements SaslClient {

    @Override
    public String getMechanismName() {
        return "JBOSS-LOCAL-USER";
    }

    @Override
    public boolean hasInitialResponse() {
        return true;
    }

    @Override
    public byte[] evaluateChallenge(byte[] bytes) throws SaslException {
        return new byte[0];
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public byte[] unwrap(byte[] bytes, int i, int i1) throws SaslException {
        return new byte[0];
    }

    @Override
    public byte[] wrap(byte[] bytes, int i, int i1) throws SaslException {
        return new byte[0];
    }

    @Override
    public Object getNegotiatedProperty(String s) {
        return null;
    }

    @Override
    public void dispose() throws SaslException {

    }
}
