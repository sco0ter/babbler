/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.extensions.rpc.client;

import java.util.concurrent.CompletionException;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.extensions.rpc.AbstractRpcManager;
import rocks.xmpp.extensions.rpc.RpcException;
import rocks.xmpp.extensions.rpc.RpcHandler;
import rocks.xmpp.extensions.rpc.model.Rpc;
import rocks.xmpp.extensions.rpc.model.Value;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * This manager allows you to call remote procedures and handle inbound calls, if enabled.
 *
 * <p>By default this manager is disabled. If you want to support RPC (i.e. handle inbound calls) {@linkplain #setRpcHandler(RpcHandler) set a RPC handler}, which allows you to handle inbound calls.</p>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0009.html">XEP-0009: Jabber-RPC</a>
 */
public final class ClientRpcManager extends AbstractRpcManager {

    private XmppSession xmppSession;

    private ClientRpcManager(final XmppSession xmppSession) {
        this.xmppSession = xmppSession;
    }

    @Override
    public AsyncResult<Value> call(Jid jid, String methodName, Value... parameters) {
        AsyncResult<IQ> query = xmppSession.query(IQ.set(jid, Rpc.ofMethodCall(methodName, parameters)));
        return query.thenApply(result -> {
            if (result != null) {
                Rpc rpc = result.getExtension(Rpc.class);
                if (rpc != null) {
                    Rpc.MethodResponse methodResponse = rpc.getMethodResponse();
                    if (methodResponse != null) {
                        if (methodResponse.getFault() != null) {
                            throw new CompletionException(new RpcException(methodResponse.getFault().getFaultCode(), methodResponse.getFault().getFaultString()));
                        }
                        return methodResponse.getResponse();
                    }
                }
            }
            return null;
        });
    }
}
