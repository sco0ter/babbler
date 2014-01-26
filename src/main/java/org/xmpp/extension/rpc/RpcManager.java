package org.xmpp.extension.rpc;

/**
 * @author Christian Schudt
 */
public class RpcManager {

    public void call(String methodName, Value... parameters) {
        Rpc rpc = new Rpc(methodName);

    }
}
