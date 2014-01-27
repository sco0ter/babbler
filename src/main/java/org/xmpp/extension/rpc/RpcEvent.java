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
