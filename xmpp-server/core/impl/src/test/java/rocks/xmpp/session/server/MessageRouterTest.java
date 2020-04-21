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
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.model.StreamElement;

import javax.xml.bind.JAXBException;
import java.util.stream.Stream;

/**
 * @author Christian Schudt
 */
public class MessageRouterTest extends XmlTest {

    private static final Jid JID_1_FULL = Jid.of("juliet@im.example.com/balcony");

    private static final Jid JID_2_FULL = Jid.of("romeo@example.net/orchard");

    private static final Jid JID_2_UNAVAILABLE = Jid.of("romeo@example.net/balcony");

    @Spy
    @InjectMocks
    private MessageRouter messageRouter;

    @Mock
    private UserManager userManager;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private InboundClientSession testSession1;

    @Mock
    private InboundClientSession testSession2;

    @Captor
    private ArgumentCaptor<StreamElement> argumentCaptor;

    protected MessageRouterTest() throws JAXBException {
        super("jabber:client", ClientIQ.class);
    }

    @BeforeClass
    public void before() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(testSession1.getPresence()).thenReturn(new Presence());
        Mockito.when(testSession2.getPresence()).thenReturn(new Presence());

        Mockito.when(userManager.userExists(Mockito.anyString())).thenAnswer(invocationOnMock -> !invocationOnMock.getArgument(0, String.class).equals("nosuchuser"));
        Mockito.when(sessionManager.getSession(JID_1_FULL)).thenReturn(testSession1);
        Mockito.when(sessionManager.getSession(JID_2_FULL)).thenReturn(testSession2);

    }

    @BeforeMethod
    public void clear() {
        Mockito.clearInvocations(testSession1);
        Mockito.clearInvocations(testSession2);
        Mockito.when(sessionManager.getUserSessions(JID_1_FULL.asBareJid())).thenReturn(Stream.of(testSession1));
        Mockito.when(sessionManager.getUserSessions(JID_2_FULL.asBareJid())).thenReturn(Stream.of(testSession2));
    }

    /**
     * Tests that a message with no 'to' attribute is sent to the bare JID of the sender.
     *
     * @see <a href="https://xmpp.org/rfcs/rfc6120.html#rules-noto-message">10.3.1.  Message</a>
     */
    @Test
    public void testMessageWithNoToAttribute() {
        Message message = new Message();
        message.setFrom(JID_1_FULL);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof Message);
        Message message1 = (Message) argumentCaptor.getValue();
        Assert.assertSame(message1, message);
        Mockito.verifyNoInteractions(testSession2);
    }

    /**
     * Message to unknown user should be silently ignored.
     */
    @Test
    public void testMessageToUnknownUser() {
        Message message = new Message(Jid.of("nosuchuser@domain"));
        message.setFrom(JID_1_FULL);
        messageRouter.process(message);
        Mockito.verifyNoInteractions(testSession1, testSession2);
    }

    /**
     * Tests the routing to an available resource.
     */
    @Test
    public void testMessageToKnownUserAvailableResource() {
        Message message = new Message(JID_2_FULL);
        message.setFrom(JID_1_FULL);
        messageRouter.process(message);
        Mockito.verify(testSession2).send(argumentCaptor.capture());
        Message message1 = (Message) argumentCaptor.getValue();
        Assert.assertSame(message1, message);
        Mockito.verifyNoInteractions(testSession1);
    }

    /**
     * RFC 6121 8.5.2.1.1.  Message
     * <p>
     * For a message stanza of type "groupchat",
     * the server MUST NOT deliver the stanza to any of the available resources but instead
     * MUST return a stanza error to the sender, which SHOULD be {@code <service-unavailable/>}.
     */
    @Test
    public void testGroupChatMessageToBareJidShouldReturnServiceUnavailable() {
        Message message = new Message(JID_1_FULL.asBareJid(), Message.Type.GROUPCHAT);
        message.setFrom(JID_1_FULL);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof Message);
        Message message1 = (Message) argumentCaptor.getValue();
        Assert.assertNotNull(message1.getError());
        Assert.assertSame(message1.getError().getCondition(), Condition.SERVICE_UNAVAILABLE);
    }
}
