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

package rocks.xmpp.extensions.sm;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.Session;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.extensions.sm.model.StreamManagement;

/**
 * Tests for the {@link AbstractStreamManager} class.
 *
 * @author Christian Schudt
 */
public class AbstractStreamManagerTest {

    @Test
    public void testIncrementation() {
        AbstractStreamManager streamManager = Mockito.mock(AbstractStreamManager.class, Mockito.CALLS_REAL_METHODS);
        streamManager.inboundCount = 0;
        streamManager.incrementInboundStanzaCount();
        Assert.assertEquals(streamManager.inboundCount, 1);

        streamManager.inboundCount = 0xFFFFFFFFL; // 2^32-1
        streamManager.incrementInboundStanzaCount();
        Assert.assertEquals(streamManager.inboundCount, 0);
    }

    @Test
    public void testDiffAfter32BitLimit() {
        Assert.assertEquals(AbstractStreamManager.diff(123, 120), 3);
        Assert.assertEquals(AbstractStreamManager.diff(0xFFFFFFFFL, 0xFFFFFFFFL - 3), 3);
        Assert.assertEquals(AbstractStreamManager.diff(2, 0xFFFFFFFFL), 3);
    }

    @Test
    public void processRequest() throws StreamNegotiationException {
        Session session = Mockito.mock(Session.class);
        AbstractStreamManager streamManager = Mockito.mock(AbstractStreamManager.class,
                Mockito.withSettings()
                        .useConstructor(session)
                        .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        streamManager.incrementInboundStanzaCount();
        streamManager.incrementInboundStanzaCount();
        streamManager.processNegotiation(StreamManagement.REQUEST);
        ArgumentCaptor<StreamElement> argumentCaptor = ArgumentCaptor.forClass(StreamElement.class);
        Mockito.verify(session).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof StreamManagement.Answer);
        Assert.assertEquals(((StreamManagement.Answer) argumentCaptor.getValue()).getLastHandledStanza(),
                Long.valueOf(2));
    }

    @Test
    public void processAnswer() throws StreamNegotiationException {
        Session session = Mockito.mock(Session.class);
        AbstractStreamManager streamManager = Mockito.mock(AbstractStreamManager.class,
                Mockito.withSettings()
                        .useConstructor(session)
                        .defaultAnswer(Mockito.CALLS_REAL_METHODS));

        streamManager.enabledByClient.set(true);
        Message message = new Message();
        streamManager.markUnacknowledged(message);
        streamManager.markUnacknowledged(new Presence());
        Assert.assertEquals(streamManager.outboundCount, 2);

        streamManager.processNegotiation(new StreamManagement.Answer(1));
        ArgumentCaptor<Stanza> argumentCaptor = ArgumentCaptor.forClass(Stanza.class);
        Mockito.verify(streamManager).onAcknowledged(argumentCaptor.capture());
        Assert.assertSame(message, argumentCaptor.getValue());
    }
}
