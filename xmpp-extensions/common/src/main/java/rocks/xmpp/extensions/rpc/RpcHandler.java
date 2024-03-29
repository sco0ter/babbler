/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

import java.util.List;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.extensions.rpc.model.Value;

/**
 * Handles an inbound remote procedure call.
 *
 * @author Christian Schudt
 * @see AbstractRpcManager#setRpcHandler(RpcHandler)
 */
@FunctionalInterface
public interface RpcHandler {

    /**
     * Processes the remote procedure call.
     *
     * @param requester  The requester.
     * @param methodName The method name.
     * @param parameters The parameter list.
     * @return The result.
     * @throws RpcException         If this exception is thrown, an application-level error (fault) is returned in the
     *                              XML-RPC structure.
     * @throws StanzaErrorException If this exception is thrown, a stanza error is returned to the caller.
     */
    Value process(Jid requester, String methodName, List<Value> parameters) throws RpcException, StanzaErrorException;
}
