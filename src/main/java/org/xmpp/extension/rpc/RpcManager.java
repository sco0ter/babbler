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

package org.xmpp.extension.rpc;

import org.xmpp.Connection;
import org.xmpp.ConnectionEvent;
import org.xmpp.ConnectionListener;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This manager allows you to call remote procedures and handle incoming calls, if enabled.
 * <p>
 * By default this manager is disabled. If you want to support RPC (i.e. handle incoming calls), {@linkplain #setEnabled(boolean) enable} it and {@linkplain #setRpcHandler(RpcHandler) set a RPC handler}, which allows you to handle incoming calls.
 * </p>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0009.html">XEP-0009: Jabber-RPC</a>
 */
public final class RpcManager extends ExtensionManager {
    private static final String FEATURE = "jabber:iq:rpc";

    private static final Logger logger = Logger.getLogger(RpcManager.class.getName());

    private RpcHandler rpcHandler;

    private RpcManager(final Connection connection) {
        super(connection);
        // Reset the rpcHandler, when the connection is closed, to avoid memory leaks.
        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == Connection.Status.CLOSED) {
                    rpcHandler = null;
                }
            }
        });

        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && iq.getType() == IQ.Type.SET) {
                    Rpc rpc = iq.getExtension(Rpc.class);
                    // If there's an incoming RPC
                    if (rpc != null) {
                        if (isEnabled()) {
                            synchronized (RpcManager.this) {
                                if (rpcHandler != null) {
                                    Rpc.MethodCall methodCall = rpc.getMethodCall();
                                    List<Value> parameters = new ArrayList<>();
                                    for (Parameter parameter : methodCall.getParameters()) {
                                        parameters.add(parameter.getValue());
                                    }
                                    try {
                                        Value value = rpcHandler.process(methodCall.getMethodName(), parameters);
                                        IQ result = iq.createResult();
                                        result.setExtension(new Rpc(value));
                                        connection.send(result);
                                    } catch (StanzaException e1) {
                                        connection.send(iq.createError(e1.getError()));
                                    } catch (RpcException e1) {
                                        IQ result = iq.createResult();
                                        result.setExtension(new Rpc(new Rpc.MethodResponse.Fault(e1.getFaultCode(), e1.getFaultString())));
                                        connection.send(result);
                                    } catch (Exception e1) {
                                        logger.log(Level.WARNING, e1.getMessage(), e1);
                                        connection.send(iq.createError(new Stanza.Error(new Stanza.Error.InternalServerError())));
                                    }
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

    /**
     * Calls a remote procedure.
     *
     * @param jid        The JID, which will receive the RPC.
     * @param methodName The method name.
     * @param parameters The parameters.
     * @return The result.
     * @throws TimeoutException If the operation timed out.
     * @throws StanzaException  If the RPC returned with an XMPP stanza error.
     * @throws RpcException     If the RPC returned with an application-level error ({@code <fault/>} element).
     */
    public Value call(Jid jid, String methodName, Value... parameters) throws TimeoutException, StanzaException, RpcException {
        IQ result = connection.query(new IQ(jid, IQ.Type.SET, new Rpc(methodName, parameters)));
        if (result != null) {
            if (result.getType() == IQ.Type.RESULT) {
                Rpc rpc = result.getExtension(Rpc.class);
                if (rpc != null) {
                    Rpc.MethodResponse methodResponse = rpc.getMethodResponse();
                    if (methodResponse != null) {
                        if (methodResponse.getFault() != null) {
                            throw new RpcException(methodResponse.getFault().getFaultCode(), methodResponse.getFault().getFaultString());
                        }
                        return methodResponse.getResponse();
                    }
                }
            } else if (result.getType() == IQ.Type.ERROR) {
                throw new StanzaException(result.getError());
            }
        }
        return null;
    }

    /**
     * Sets the RPC handler, which will handle RPCs.
     *
     * @param rpcHandler The RPC handler.
     */
    public synchronized void setRpcHandler(RpcHandler rpcHandler) {
        this.rpcHandler = rpcHandler;
    }

    @Override
    protected Collection<String> getFeatureNamespaces() {
        return Arrays.asList(FEATURE);
    }
}
