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

package rocks.xmpp.extensions.rpc;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.IQExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.rpc.model.Rpc;
import rocks.xmpp.extensions.rpc.model.Value;

import java.util.ArrayList;
import java.util.List;
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
public final class RpcManager extends IQExtensionManager implements SessionStatusListener {

    private static final Logger logger = Logger.getLogger(RpcManager.class.getName());

    private RpcHandler rpcHandler;

    private RpcManager(final XmppSession xmppSession) {
        super(xmppSession, AbstractIQ.Type.SET, Rpc.NAMESPACE);

        // Reset the rpcHandler, when the connection is closed, to avoid memory leaks.
        xmppSession.addSessionStatusListener(this);

        xmppSession.addIQHandler(Rpc.class, this);
    }

    /**
     * Calls a remote procedure.
     *
     * @param jid        The JID, which will receive the RPC.
     * @param methodName The method name.
     * @param parameters The parameters.
     * @return The result.
     * @throws rocks.xmpp.core.session.NoResponseException  If the entity did not respond.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the RPC returned with an XMPP stanza error.
     * @throws RpcException                                 If the RPC returned with an application-level error ({@code <fault/>} element).
     */
    public Value call(Jid jid, String methodName, Value... parameters) throws XmppException, RpcException {
        IQ result = xmppSession.query(new IQ(jid, IQ.Type.SET, new Rpc(methodName, parameters)));
        if (result != null) {
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
        setEnabled(rpcHandler != null);
    }

    @Override
    protected IQ processRequest(final IQ iq) {

        Rpc rpc = iq.getExtension(Rpc.class);
        // If there's an incoming RPC
        RpcHandler rpcHandler1;
        synchronized (this) {
            rpcHandler1 = rpcHandler;
        }
        if (rpcHandler1 != null) {
            final Rpc.MethodCall methodCall = rpc.getMethodCall();
            final List<Value> parameters = new ArrayList<>();
            for (Value parameter : methodCall.getParameters()) {
                parameters.add(parameter);
            }

            try {
                Value value = rpcHandler1.process(iq.getFrom(), methodCall.getMethodName(), parameters);
                return iq.createResult(new Rpc(value));
            } catch (RpcException e1) {
                return iq.createResult(new Rpc(new Rpc.MethodResponse.Fault(e1.getFaultCode(), e1.getFaultString())));
            } catch (Throwable e1) {
                logger.log(Level.WARNING, e1.getMessage(), e1);
                return iq.createError(new StanzaError(Condition.INTERNAL_SERVER_ERROR));
            }
        }
        return iq.createError(new StanzaError(Condition.SERVICE_UNAVAILABLE));
    }

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            rpcHandler = null;
        }
    }
}
