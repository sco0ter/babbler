/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.session.server;

import java.util.stream.Stream;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import jakarta.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.model.StreamElement;

/**
 * @author Christian Schudt
 */
public class IQRouterTest extends XmlTest {

    private static final Jid JID_1_FULL = Jid.of("juliet@im.example.com/balcony");

    private static final Jid JID_2_FULL = Jid.of("romeo@example.net/orchard");

    private static final Jid JID_2_UNAVAILABLE = Jid.of("romeo@example.net/balcony");

    @Spy
    @InjectMocks
    private IQRouter iqRouter;

    @Mock
    private UserManager userManager;

    @Mock
    private Event<IQEvent> iqEvent;

    @Mock
    private SessionManager sessionManager;

    @Spy
    private DummyIQHandler dummyIQHandler = new DummyIQHandler();

    @Spy
    private ExceptionIQHandler exceptionIQHandler = new ExceptionIQHandler();

    @Spy
    private NullIQHandler nullIQHandler = new NullIQHandler();

    @Mock
    private InboundClientSession testSession1;

    @Mock
    private InboundClientSession testSession2;

    @Mock
    private Instance<IQHandler> iqHandlers;

    @Captor
    private ArgumentCaptor<StreamElement> argumentCaptor;

    protected IQRouterTest() throws JAXBException {
        super("jabber:client", ClientIQ.class);
    }

    @BeforeClass
    public void before() {
        MockitoAnnotations.openMocks(this);

        testSession1.setAddress(JID_1_FULL);
        testSession2.setAddress(JID_2_FULL);

        Mockito.when(userManager.userExists(Mockito.anyString()))
                .thenAnswer(invocationOnMock -> !invocationOnMock.getArgument(0, String.class).equals("nosuchuser"));
        Mockito.when(sessionManager.getSession(JID_1_FULL)).thenReturn(testSession1);
        Mockito.when(sessionManager.getSession(JID_2_FULL)).thenReturn(testSession2);
    }

    @BeforeMethod
    public void clear() {
        Mockito.when(iqHandlers.stream()).thenReturn(Stream.of(dummyIQHandler, nullIQHandler, exceptionIQHandler));

        Mockito.reset(testSession1);
        Mockito.reset(testSession2);
        Mockito.reset(dummyIQHandler);
        Mockito.reset(nullIQHandler);
        Mockito.reset(exceptionIQHandler);
    }

    /**
     * Tests if bad-request error is returned, if IQ has an unknown type.
     */
    @Test
    public void testBadRequestDueToUnknownType() throws JAXBException, XMLStreamException {
        String xml = "<iq from='juliet@im.example.com/balcony'\n" +
                "       id='zj3v142b'\n" +
                "       to='im.example.com'\n" +
                "       type='subscribe'>\n" +
                "     <ping xmlns='urn:xmpp:ping'/>\n" +
                "   </iq>";
        IQ iq = unmarshal(xml, ClientIQ.class);
        iqRouter.process(iq);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof IQ);
        IQ resultIQ = (IQ) argumentCaptor.getValue();
        Assert.assertSame(resultIQ.getType(), IQ.Type.ERROR);
        Assert.assertNotNull(resultIQ.getError());
        Assert.assertSame(resultIQ.getError().getCondition(), Condition.BAD_REQUEST);
    }

    /**
     * Tests that a bad-request error is returned, if there's no IQ payload.
     */
    @Test
    public void testBadRequestDueToMissingPayload() throws JAXBException, XMLStreamException {
        String xml = "<iq from='juliet@im.example.com/balcony'\n" +
                "       id='zj3v142b'\n" +
                "       to='im.example.com'\n" +
                "       type='get'>\n" +
                "   </iq>";
        IQ iq = unmarshal(xml, ClientIQ.class);
        iqRouter.process(iq);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof IQ);
        IQ resultIQ = (IQ) argumentCaptor.getValue();
        Assert.assertSame(resultIQ.getType(), IQ.Type.ERROR);
        Assert.assertNotNull(resultIQ.getError());
        Assert.assertSame(resultIQ.getError().getCondition(), Condition.BAD_REQUEST);
    }

    /**
     * Tests, that known IQ namespaces are correctly handled by a handler.
     */
    @Test
    public void testKnownNamespace() {
        IQ iq = IQ.get(JID_1_FULL.asBareJid(), new DummyIQ());
        iq.setFrom(JID_1_FULL);
        iqRouter.process(iq);
        Mockito.verify(dummyIQHandler, Mockito.times(1)).handleRequest((IQ) argumentCaptor.capture());
        Assert.assertSame(argumentCaptor.getValue(), iq);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof IQ);
        IQ resultIQ = (IQ) argumentCaptor.getValue();
        Assert.assertTrue(resultIQ.isResponse());
    }

    /**
     * Tests, that known IQ namespaces are correctly handled by a handler. If that handler returns null, the handler is
     * responsible for sending the response IQ.
     */
    @Test
    public void testKnownNamespaceHandlerReturnsNull() {
        IQ iq = IQ.get(JID_1_FULL.asBareJid(), new NullIQ());
        iq.setFrom(JID_1_FULL);
        iqRouter.process(iq);
        Mockito.verify(nullIQHandler, Mockito.times(1)).handleRequest((IQ) argumentCaptor.capture());
        Assert.assertSame(argumentCaptor.getValue(), iq);
        Mockito.verifyNoMoreInteractions(testSession1);
    }

    /**
     * Tests, that known IQ namespaces are correctly handled by a handler. If that handler returns null, the handler is
     * responsible for sending the response IQ.
     */
    @Test
    public void testKnownNamespaceHandlerThrows() {
        IQ iq = IQ.get(JID_1_FULL.asBareJid(), new ExceptionIQ());
        iq.setFrom(JID_1_FULL);
        iqRouter.process(iq);
        Mockito.verify(exceptionIQHandler, Mockito.times(1)).handleRequest((IQ) argumentCaptor.capture());
        Assert.assertSame(argumentCaptor.getValue(), iq);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof IQ);
        IQ resultIQ = (IQ) argumentCaptor.getValue();
        Assert.assertSame(resultIQ.getType(), IQ.Type.ERROR);
        Assert.assertNotNull(resultIQ.getError());
        Assert.assertSame(resultIQ.getError().getCondition(), Condition.INTERNAL_SERVER_ERROR);
    }

    /**
     * Tests, that unknown IQ namespaces are responded to with service-unavailable.
     */
    @Test
    public void testToAttributeAndUnknownNamespace() {
        IQ iq = IQ.get(JID_1_FULL.asBareJid(), "");
        iq.setFrom(JID_1_FULL);
        iqRouter.process(iq);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof IQ);
        IQ resultIQ = (IQ) argumentCaptor.getValue();
        Assert.assertSame(resultIQ.getType(), IQ.Type.ERROR);
        Assert.assertNotNull(resultIQ.getError());
        Assert.assertSame(resultIQ.getError().getCondition(), Condition.SERVICE_UNAVAILABLE);
    }

    /**
     * Tests, that unknown IQ namespaces and IQ with no 'to' attribute are responded to with service-unavailable.
     * <p>
     * If the IQ stanza is of type "get" or "set" and the server does not understand the namespace that qualifies the
     * payload, the server MUST return an error to the sending entity, which MUST be {@code <service-unavailable/>}.
     *
     * @see <a href="https://xmpp.org/rfcs/rfc6120.html#rules-noto-IQ">10.3.3.  IQ, §2</a>
     */
    @Test
    public void testNoAttributeAndUnknownNamespace() {
        IQ iq = IQ.get("");
        iq.setFrom(JID_1_FULL);
        iqRouter.process(iq);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof IQ);
        IQ resultIQ = (IQ) argumentCaptor.getValue();
        Assert.assertSame(resultIQ.getType(), IQ.Type.ERROR);
        Assert.assertNotNull(resultIQ.getError());
        Assert.assertSame(resultIQ.getError().getCondition(), Condition.SERVICE_UNAVAILABLE);
    }

    /**
     * Tests, if the service returns service-unavailable if there's no such user.
     *
     * @see <a href="https://xmpp.org/rfcs/rfc6120.html#rules-local-barejid-nosuchuser">10.5.3.1.  No Such User</a>
     */
    @Test
    public void testNoSuchUser() {
        IQ iq = IQ.get(Jid.of("nosuchuser@domain"), new DummyIQ());
        iq.setFrom(JID_1_FULL);
        iqRouter.process(iq);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof IQ);
        IQ resultIQ = (IQ) argumentCaptor.getValue();
        Assert.assertSame(resultIQ.getType(), IQ.Type.ERROR);
        Assert.assertNotNull(resultIQ.getError());
        Assert.assertSame(resultIQ.getError().getCondition(), Condition.SERVICE_UNAVAILABLE);
    }

    /**
     * If recipient is full JID and is available, the IQ should be routed to that session.
     */
    @Test
    public void testRouteToKnownSession() {
        IQ iq = IQ.get(JID_2_FULL, "");
        iq.setFrom(JID_1_FULL);
        iqRouter.process(iq);
        Mockito.verify(testSession2).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof IQ);
        IQ resultIQ = (IQ) argumentCaptor.getValue();
        Assert.assertSame(resultIQ, iq);
    }

    @Test
    public void testRouteToUnknownSession() {
        IQ iq = IQ.get(JID_2_UNAVAILABLE, new DummyIQ());
        iq.setFrom(JID_1_FULL);
        iqRouter.process(iq);
        Mockito.verify(dummyIQHandler, Mockito.times(1)).handleRequest((IQ) argumentCaptor.capture());
        Assert.assertSame(argumentCaptor.getValue(), iq);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof IQ);
        IQ resultIQ = (IQ) argumentCaptor.getValue();
        Assert.assertTrue(resultIQ.isResponse());
        Assert.assertEquals(resultIQ.getFrom(), JID_2_UNAVAILABLE.asBareJid());
    }

    private static class DummyIQ {

    }

    private static class DummyIQHandler implements IQHandler {

        @Override
        public Class<?> getPayloadClass() {
            return DummyIQ.class;
        }

        @Override
        public IQ handleRequest(IQ iq) {
            return iq.createResult();
        }
    }

    private static class NullIQ {

    }

    private static class NullIQHandler implements IQHandler {

        @Override
        public Class<?> getPayloadClass() {
            return NullIQ.class;
        }

        @Override
        public IQ handleRequest(IQ iq) {
            return null;
        }
    }

    private static class ExceptionIQ {

    }

    private static class ExceptionIQHandler implements IQHandler {

        @Override
        public Class<?> getPayloadClass() {
            return ExceptionIQ.class;
        }

        @Override
        public IQ handleRequest(IQ iq) {
            throw new IllegalArgumentException();
        }
    }
}
