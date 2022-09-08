/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.rpc.model.Rpc;
import rocks.xmpp.extensions.rpc.model.Value;

/**
 * Tests for the {@link AbstractRpcManager} class.
 *
 * @author Christian Schudt
 */
public class AbstractRpcManagerTest {

    @Test
    public void processRequest() throws RpcException, StanzaErrorException {
        AbstractRpcManager rpcManager = Mockito.mock(AbstractRpcManager.class, Mockito.CALLS_REAL_METHODS);
        Assert.assertFalse(rpcManager.isEnabled());

        RpcHandler rpcHandler = Mockito.mock(RpcHandler.class);
        Mockito.when(rpcHandler.process(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Value.of(1));
        rpcManager.setRpcHandler(rpcHandler);

        Assert.assertTrue(rpcManager.isEnabled());
        IQ request = IQ.get(Rpc.ofMethodCall("method1",
                Value.of(1),
                Value.of(true),
                Value.of(OffsetDateTime.of(2021, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
                Value.of(2.0),
                Value.of(new byte[]{1, 2, 3, 4}),
                Value.of(Collections.singletonList(Value.of(1))),
                Value.of(Map.of("Hello", Value.of("World")))
        ));
        request.setFrom(Jid.of("requester"));
        IQ result = rpcManager.processRequest(request);
        Rpc rpc = result.getExtension(Rpc.class);
        Assert.assertNotNull(rpc);
        Assert.assertNotNull(rpc.getMethodResponse());
        Assert.assertNotNull(rpc.getMethodResponse().getResponse());
        Assert.assertNull(rpc.getMethodResponse().getFault());
        Assert.assertEquals(rpc.getMethodResponse().getResponse().getAsInteger(), Integer.valueOf(1));

        List<Value> list = new ArrayList<>(Arrays.asList(
                Value.of(1),
                Value.of(true),
                Value.of(OffsetDateTime.of(2021, 3, 22, 0, 0, 0, 0, ZoneOffset.UTC)),
                Value.of(2.0),
                Value.of(new byte[]{1, 2, 3, 4}),
                Value.of(Collections.singletonList(Value.of(1))),
                Value.of(Map.of("Hello", Value.of("World")))
        ));

        Mockito.verify(rpcHandler).process(Mockito.eq(request.getFrom()), Mockito.eq("method1"), Mockito.eq(list));
    }

    @Test
    public void processRequestWithFault() throws RpcException, StanzaErrorException {
        AbstractRpcManager rpcManager = Mockito.mock(AbstractRpcManager.class, Mockito.CALLS_REAL_METHODS);
        Assert.assertFalse(rpcManager.isEnabled());

        RpcHandler rpcHandler = Mockito.mock(RpcHandler.class);
        Mockito.when(rpcHandler.process(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenThrow(new RpcException(400, "Bad Request"));
        rpcManager.setRpcHandler(rpcHandler);

        Assert.assertTrue(rpcManager.isEnabled());
        IQ request = IQ.get(Rpc.ofMethodCall("method1"));
        request.setFrom(Jid.of("requester"));
        IQ result = rpcManager.processRequest(request);
        Rpc rpc = result.getExtension(Rpc.class);
        Assert.assertNotNull(rpc);
        Assert.assertNotNull(rpc.getMethodResponse());
        Assert.assertNull(rpc.getMethodResponse().getResponse());
        Assert.assertNotNull(rpc.getMethodResponse().getFault());
        Assert.assertEquals(rpc.getMethodResponse().getFault().getFaultCode(), 400);
        Assert.assertEquals(rpc.getMethodResponse().getFault().getFaultString(), "Bad Request");
    }

    @Test
    public void processRequestWithoutHandler() {
        AbstractRpcManager rpcManager = Mockito.mock(AbstractRpcManager.class, Mockito.CALLS_REAL_METHODS);
        Assert.assertFalse(rpcManager.isEnabled());

        IQ request = IQ.get(Rpc.ofMethodCall("method1"));
        request.setFrom(Jid.of("requester"));
        IQ result = rpcManager.processRequest(request);
        Assert.assertSame(result.getError().getCondition(), Condition.SERVICE_UNAVAILABLE);
    }

    @Test
    public void getInfo() {
        AbstractRpcManager rpcManager = Mockito.mock(AbstractRpcManager.class, Mockito.CALLS_REAL_METHODS);
        Assert.assertFalse(rpcManager.isEnabled());
        rpcManager.setRpcHandler(Mockito.mock(RpcHandler.class));

        IQ request = IQ.get(Rpc.ofMethodCall("method1"));
        request.setFrom(Jid.of("requester"));

        Assert.assertEquals(rpcManager.getIdentities().iterator().next(), Identity.automationRpc());
    }
}
