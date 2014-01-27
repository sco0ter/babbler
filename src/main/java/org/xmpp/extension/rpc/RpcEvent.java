package org.xmpp.extension.rpc;

import org.xmpp.Connection;
import org.xmpp.stanza.IQ;

import java.util.EventObject;
import java.util.List;

/**
 * @author Christian Schudt
 */
public final class RpcEvent extends EventObject {

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    RpcEvent(Object source, IQ iq, String methodName, List<Value> parameters, Connection connection) {
        super(source);
        this.methodName = methodName;
        this.parameters = parameters;
        this.iq = iq;
        this.connection = connection;
    }

    private IQ iq;

    private Connection connection;

    private String methodName;

    private List<Value> parameters;

    /**
     * Gets the method name of the RPC.
     *
     * @return The method name.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Gets the parameter list.
     *
     * @return The parameters.
     */
    public List<Value> getParameters() {
        return parameters;
    }

    public void sendResponse(Value value) {
        IQ result = iq.createResult();
        result.setExtension(new Rpc.MethodResponse(value));
        connection.send(result);
    }
}
