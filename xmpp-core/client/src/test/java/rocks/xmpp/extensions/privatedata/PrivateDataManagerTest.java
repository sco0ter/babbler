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

package rocks.xmpp.extensions.privatedata;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.net.client.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.extensions.privatedata.model.PrivateData;
import rocks.xmpp.extensions.privatedata.rosterdelimiter.model.RosterDelimiter;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * Tests for {@link PrivateDataManager}.
 */
public class PrivateDataManagerTest {

    @Test
    public void testStoreData() {
        XmppSession xmppSession = Mockito.mock(XmppSession.class, Mockito.withSettings()
                .useConstructor("domain", XmppSessionConfiguration.builder().build(), new TcpConnectionConfiguration[0])
                .defaultAnswer(Mockito.CALLS_REAL_METHODS));

        PrivateDataManager privateDataManager = xmppSession.getManager(PrivateDataManager.class);
        privateDataManager.storeData(RosterDelimiter.of("::"));

        ArgumentCaptor<IQ> iqArgumentCaptor = ArgumentCaptor.forClass(IQ.class);
        Mockito.verify(xmppSession).query(iqArgumentCaptor.capture(), Mockito.eq(Void.class));

        Assert.assertNotNull(iqArgumentCaptor.getValue());
        Assert.assertEquals(iqArgumentCaptor.getValue().getType(), IQ.Type.SET);
        Assert.assertNotNull(iqArgumentCaptor.getValue().getExtension(PrivateData.class));
    }

    @Test
    public void testGetData() throws ExecutionException, InterruptedException {
        XmppSession xmppSession = Mockito.mock(XmppSession.class, Mockito.withSettings()
                .useConstructor("domain", XmppSessionConfiguration.builder().build(), new TcpConnectionConfiguration[0])
                .defaultAnswer(Mockito.CALLS_REAL_METHODS));

        Mockito.doReturn(new AsyncResult<>(
                CompletableFuture.completedFuture(new IQ(IQ.Type.RESULT, new PrivateData(RosterDelimiter.of("::"))))))
                .when(xmppSession)
                .query(Mockito.argThat(iq -> iq.hasExtension(PrivateData.class)));

        PrivateDataManager privateDataManager = xmppSession.getManager(PrivateDataManager.class);
        RosterDelimiter result = privateDataManager.getData(RosterDelimiter.class).get();

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getRosterDelimiter(), "::");
    }
}
