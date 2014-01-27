package org.xmpp.extension.rpc;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public final class RpcManager extends ExtensionManager {
    private final Set<RpcListener> rpcListeners = new CopyOnWriteArraySet<>();

    protected RpcManager(final Connection connection) {
        super(connection);
        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && iq.getType() == IQ.Type.SET) {
                    Rpc rpc = iq.getExtension(Rpc.class);
                    if (rpc != null) {
                        if (isEnabled()) {
                            Rpc.MethodCall methodCall = rpc.getMethodCall();
                            List<Value> parameters = new ArrayList<>();
                            for (Parameter parameter : methodCall.getParameters()) {
                                parameters.add(parameter.getValue());
                            }
                            RpcEvent rpcEvent = new RpcEvent(RpcManager.this, iq, methodCall.getMethodName(), parameters, connection);

                            for (RpcListener rpcListener : rpcListeners) {
                                try {
                                    rpcListener.processRpc(rpcEvent);
                                } catch (Exception e1) {

                                }
                            }
                        } else {
                            sendServiceUnavailable(iq);
                        }
                    }
                }
            }
        });
    }

    private static final String FEATURE = "jabber:iq:rpc";

    public Value execute(Jid jid, String methodName, Value... parameters) throws TimeoutException {
        IQ result = connection.query(new IQ(jid, IQ.Type.SET, new Rpc(methodName, parameters)));
        if (result != null && result.getType() == IQ.Type.RESULT) {
            Rpc rpc = result.getExtension(Rpc.class);
            if (rpc != null) {
                Rpc.MethodResponse methodResponse = rpc.getMethodResponse();
                if (methodResponse != null) {
                    return methodResponse.getResponse();
                }
            }
        }
        return null;
    }

    /**
     * Adds a RPC listener, which allows to listen for incoming RPCs.
     *
     * @param rpcListener The listener.
     * @see #removeRpcListener(RpcListener)
     */
    public void addRpcListener(RpcListener rpcListener) {
        rpcListeners.add(rpcListener);
    }

    /**
     * Removes a previously added RPC listener.
     *
     * @param rpcListener The listener.
     * @see #addRpcListener(RpcListener)
     */
    public void removeRpcListener(RpcListener rpcListener) {
        rpcListeners.remove(rpcListener);
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return Arrays.asList(FEATURE);
    }
}
