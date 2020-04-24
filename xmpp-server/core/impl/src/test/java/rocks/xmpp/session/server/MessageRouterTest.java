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

import java.util.stream.Stream;

/**
 * Tests the RFC 6121 Message Delivery Rules.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#rules-local-message">8.5.4.  Summary of Message Delivery Rules</a>
 */
public class MessageRouterTest extends XmlTest {

    private static final Jid JID_1_FULL = Jid.of("juliet@im.example.com/balcony");

    private static final Jid JID_2_FULL_A = Jid.of("romeo@example.net/orchard");

    private static final Jid JID_2_FULL_B = Jid.of("romeo@example.net/balcony");

    private static final Jid JID_2_FULL_C_NEGATIVE = Jid.of("romeo@example.net/negative");

    private static final Jid JID_3_FULL_NO_RESOURCES = Jid.of("jid3@example.net/orchard");

    private static final Jid JID_4_FULL_ONLY_NEGATIVE = Jid.of("jid4@example.net/orchard");

    private static final Jid NO_SUCH_USER_FULL = Jid.of("nosuchuser@domain/resource");

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
    private InboundClientSession testSession2a;

    @Mock
    private InboundClientSession testSession2b;

    @Mock
    private InboundClientSession testSession2cNegative;

    @Mock
    private InboundClientSession testSession4Negative;

    @Captor
    private ArgumentCaptor<StreamElement> argumentCaptor;

    protected MessageRouterTest() {
        super("jabber:client", ClientIQ.class);
    }

    @BeforeClass
    public void before() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(testSession1.getPresence()).thenReturn(new Presence());
        Mockito.when(testSession2a.getPresence()).thenReturn(new Presence());
        Mockito.when(testSession2b.getPresence()).thenReturn(new Presence());
        Mockito.when(testSession2cNegative.getPresence()).thenReturn(new Presence((byte) -1));

        Mockito.when(userManager.userExists(Mockito.anyString())).thenAnswer(invocationOnMock -> !invocationOnMock.getArgument(0, String.class).equals("nosuchuser"));
        Mockito.when(sessionManager.getSession(JID_1_FULL)).thenReturn(testSession1);
        Mockito.when(sessionManager.getSession(JID_2_FULL_A)).thenReturn(testSession2a);
        Mockito.when(sessionManager.getSession(JID_2_FULL_B)).thenReturn(testSession2b);
        Mockito.when(sessionManager.getSession(JID_2_FULL_C_NEGATIVE)).thenReturn(testSession2cNegative);
        Mockito.when(sessionManager.getSession(JID_4_FULL_ONLY_NEGATIVE)).thenReturn(testSession4Negative);

    }

    @BeforeMethod
    public void clear() {
        Mockito.clearInvocations(testSession1);
        Mockito.clearInvocations(testSession2a);
        Mockito.clearInvocations(testSession2b);
        Mockito.clearInvocations(messageRouter);
        Mockito.when(sessionManager.getUserSessions(JID_1_FULL.asBareJid())).thenReturn(Stream.of(testSession1));
        Mockito.when(sessionManager.getUserSessions(JID_2_FULL_A.asBareJid())).thenReturn(Stream.of(testSession2a, testSession2b, testSession2cNegative));
    }

    @Test
    public void testAccountDoesNotExistBareNormalMessage() {
        Message message = new Message(NO_SUCH_USER_FULL.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
        Mockito.verifyNoInteractions(testSession1, testSession2a);
    }

    @Test
    public void testAccountDoesNotExistBareChatMessage() {
        Message message = new Message(NO_SUCH_USER_FULL.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
        Mockito.verifyNoInteractions(testSession1, testSession2a);
    }

    @Test
    public void testAccountDoesNotExistBareGroupChatMessage() {
        Message message = new Message(NO_SUCH_USER_FULL.asBareJid(), Message.Type.GROUPCHAT);
        message.setFrom(JID_1_FULL);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof Message);
        Message message1 = (Message) argumentCaptor.getValue();
        Assert.assertNotNull(message1.getError());
        Assert.assertSame(message1.getError().getCondition(), Condition.SERVICE_UNAVAILABLE);
    }

    @Test
    public void testAccountDoesNotExistBareHeadlineMessage() {
        Message message = new Message(NO_SUCH_USER_FULL.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verifyNoInteractions(testSession1);
        Mockito.verifyNoInteractions(testSession2a);
        Mockito.verify(messageRouter, Mockito.times(0)).storeOfflineOrReturnError(Mockito.any());
        Mockito.verify(messageRouter, Mockito.times(0)).ignoreOrReturnError(Mockito.any());
        Mockito.verifyNoInteractions(testSession1, testSession2a);
    }

    @Test
    public void testAccountDoesNotExistFullNormalMessage() {
        Message message = new Message(NO_SUCH_USER_FULL);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
        Mockito.verifyNoInteractions(testSession1, testSession2a);
    }

    @Test
    public void testAccountDoesNotExistFullChatMessage() {
        Message message = new Message(NO_SUCH_USER_FULL);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
        Mockito.verifyNoInteractions(testSession1, testSession2a);
    }

    @Test
    public void testAccountDoesNotExistFullGroupchatMessage() {
        Message message = new Message(NO_SUCH_USER_FULL);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.GROUPCHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testAccountDoesNotExistFullHeadlineMessage() {
        Message message = new Message(NO_SUCH_USER_FULL);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
        Mockito.verifyNoInteractions(testSession1, testSession2a);
    }

    @Test
    public void testAccountExistsButNoActiveResourcesBareNormalMessage() {
        Message message = new Message(JID_3_FULL_NO_RESOURCES.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(messageRouter).storeOfflineOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testAccountExistsButNoActiveResourcesBareChatMessage() {
        Message message = new Message(JID_3_FULL_NO_RESOURCES.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).storeOfflineOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testAccountExistsButNoActiveResourcesBareGroupchatMessage() {
        Message message = new Message(JID_3_FULL_NO_RESOURCES.asBareJid(), Message.Type.GROUPCHAT);
        message.setFrom(JID_1_FULL);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof Message);
        Message message1 = (Message) argumentCaptor.getValue();
        Assert.assertNotNull(message1.getError());
        Assert.assertSame(message1.getError().getCondition(), Condition.SERVICE_UNAVAILABLE);
    }

    @Test
    public void testAccountExistsButNoActiveResourcesBareHeadlineMessage() {
        Message message = new Message(JID_3_FULL_NO_RESOURCES.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verifyNoInteractions(testSession1);
        Mockito.verifyNoInteractions(testSession2a);
        Mockito.verify(messageRouter, Mockito.times(0)).storeOfflineOrReturnError(Mockito.any());
        Mockito.verify(messageRouter, Mockito.times(0)).ignoreOrReturnError(Mockito.any());
    }

    @Test
    public void testAccountExistsButNoActiveResourcesFullNormalMessage() {
        Message message = new Message(JID_3_FULL_NO_RESOURCES);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(messageRouter, Mockito.times(0)).storeOfflineOrReturnError(Mockito.eq(message));
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testAccountExistsButNoActiveResourcesFullChatMessage() {
        Message message = new Message(JID_3_FULL_NO_RESOURCES);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).storeOfflineOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testAccountExistsButNoActiveResourcesFullGroupchatMessage() {
        Message message = new Message(JID_3_FULL_NO_RESOURCES, Message.Type.GROUPCHAT);
        message.setFrom(JID_1_FULL);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testAccountExistsButNoActiveResourcesFullHeadlineMessage() {
        Message message = new Message(JID_3_FULL_NO_RESOURCES);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testOnlyNegativeResourcesBareNormalMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(messageRouter).storeOfflineOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testOnlyNegativeResourcesBareChatMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).storeOfflineOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testOnlyNegativeResourcesBareGroupchatMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.GROUPCHAT);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof Message);
        Message message1 = (Message) argumentCaptor.getValue();
        Assert.assertNotNull(message1.getError());
        Assert.assertSame(message1.getError().getCondition(), Condition.SERVICE_UNAVAILABLE);
    }

    @Test
    public void testOnlyNegativeResourcesBareHeadlineMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verifyNoInteractions(testSession1);
        Mockito.verifyNoInteractions(testSession2a);
        Mockito.verify(messageRouter, Mockito.times(0)).storeOfflineOrReturnError(Mockito.any());
        Mockito.verify(messageRouter, Mockito.times(0)).ignoreOrReturnError(Mockito.any());
    }

    @Test
    public void testOnlyNegativeResourcesFullMatchNormalMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(testSession4Negative).send(Mockito.eq(message));
    }

    @Test
    public void testOnlyNegativeResourcesFullMatchChatMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(testSession4Negative).send(Mockito.eq(message));
    }

    @Test
    public void testOnlyNegativeResourcesFullMatchGroupchatMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.GROUPCHAT);
        messageRouter.process(message);
        Mockito.verify(testSession4Negative).send(Mockito.eq(message));
    }

    @Test
    public void testOnlyNegativeResourcesFullMatchHeadlineMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verify(testSession4Negative).send(Mockito.eq(message));
    }

    @Test
    public void testOnlyNegativeResourcesFullNoMatchNormalMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testOnlyNegativeResourcesFullNoMatchChatMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).storeOfflineOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testOnlyNegativeResourcesFullNoMatchGroupchatMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.GROUPCHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testOnlyNegativeResourcesFullNoMatchHeadlineMessage() {
        Message message = new Message(JID_4_FULL_ONLY_NEGATIVE.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourceBareNormalMessage() {
        Message message = new Message(JID_1_FULL.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourceBareChatMessage() {
        Message message = new Message(JID_1_FULL.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourceBareGroupchatMessage() {
        Message message = new Message(JID_1_FULL.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.GROUPCHAT);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof Message);
        Message message1 = (Message) argumentCaptor.getValue();
        Assert.assertNotNull(message1.getError());
        Assert.assertSame(message1.getError().getCondition(), Condition.SERVICE_UNAVAILABLE);
    }

    @Test
    public void testOneNonNegativeResourceBareHeadlineMessage() {
        Message message = new Message(JID_1_FULL.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourceFullMatchNormalMessage() {
        Message message = new Message(JID_1_FULL);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourceFullMatchChatMessage() {
        Message message = new Message(JID_1_FULL);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourceFullMatchGroupchatMessage() {
        Message message = new Message(JID_1_FULL);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.GROUPCHAT);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourceFullMatchHeadlineMessage() {
        Message message = new Message(JID_1_FULL);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourceNoMatchNormalMessage() {
        Message message = new Message(JID_1_FULL.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourceNoMatchChatMessage() {
        Message message = new Message(JID_1_FULL.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourceNoMatchGroupchatMessage() {
        Message message = new Message(JID_1_FULL.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.GROUPCHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testOneNonNegativeResourcesNoMatchHeadlineMessage() {
        Message message = new Message(JID_1_FULL.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testMultipleNonNegativeResourceBareNormalMessage() {
        Message message = new Message(JID_2_FULL_A.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(messageRouter).deliverToMostAvailableResources(Mockito.any(), Mockito.eq(message));
    }

    @Test
    public void testMultipleNonNegativeResourceBareChatMessage() {
        Message message = new Message(JID_2_FULL_A.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).deliverToMostAvailableResources(Mockito.any(), Mockito.eq(message));
    }

    @Test
    public void testMultipleNonNegativeResourceBareGroupchatMessage() {
        Message message = new Message(JID_2_FULL_A.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.GROUPCHAT);
        messageRouter.process(message);
        Mockito.verify(testSession1).send(argumentCaptor.capture());
        Assert.assertTrue(argumentCaptor.getValue() instanceof Message);
        Message message1 = (Message) argumentCaptor.getValue();
        Assert.assertNotNull(message1.getError());
        Assert.assertSame(message1.getError().getCondition(), Condition.SERVICE_UNAVAILABLE);
    }

    @Test
    public void testMultipleNonNegativeResourcesBareHeadlineMessage() {
        Message message = new Message(JID_2_FULL_A.asBareJid());
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verify(testSession2a).send(Mockito.eq(message));
        Mockito.verify(testSession2b).send(Mockito.eq(message));
        Mockito.verify(messageRouter, Mockito.times(0)).deliverToMostAvailableResources(Mockito.any(), Mockito.any());
    }

    @Test
    public void testMultipleNonNegativeResourceFullMatchNormalMessage() {
        Message message = new Message(JID_2_FULL_A);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(testSession2a).send(Mockito.eq(message));
        Mockito.verify(testSession2b, Mockito.times(0)).send(Mockito.eq(message));
        Mockito.verify(messageRouter, Mockito.times(0)).deliverToMostAvailableResources(Mockito.any(), Mockito.any());
    }

    @Test
    public void testMultipleNonNegativeResourceFullMatchChatMessage() {
        Message message = new Message(JID_2_FULL_A);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(testSession2a).send(Mockito.eq(message));
        Mockito.verify(testSession2b, Mockito.times(0)).send(Mockito.eq(message));
        Mockito.verify(messageRouter, Mockito.times(0)).deliverToMostAvailableResources(Mockito.any(), Mockito.any());
    }

    @Test
    public void testMultipleNonNegativeResourceFullMatchGroupchatMessage() {
        Message message = new Message(JID_2_FULL_A);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.GROUPCHAT);
        messageRouter.process(message);
        Mockito.verify(testSession2a).send(Mockito.eq(message));
        Mockito.verify(testSession2b, Mockito.times(0)).send(Mockito.eq(message));
        Mockito.verify(messageRouter, Mockito.times(0)).deliverToMostAvailableResources(Mockito.any(), Mockito.any());
    }

    @Test
    public void testMultipleNonNegativeResourcesFullMatchHeadlineMessage() {
        Message message = new Message(JID_2_FULL_A);
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verify(testSession2a).send(Mockito.eq(message));
        Mockito.verify(testSession2b, Mockito.times(0)).send(Mockito.eq(message));
        Mockito.verify(messageRouter, Mockito.times(0)).deliverToMostAvailableResources(Mockito.any(), Mockito.any());
    }

    @Test
    public void testMultipleNonNegativeResourceFullNoMatchNormalMessage() {
        Message message = new Message(JID_2_FULL_A.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.NORMAL);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testMultipleNonNegativeResourceFullNoMatchChatMessage() {
        Message message = new Message(JID_2_FULL_A.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.CHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).deliverToMostAvailableResources(Mockito.any(), Mockito.eq(message));
    }

    @Test
    public void testMultipleNonNegativeResourceFullNoMatchGroupchatMessage() {
        Message message = new Message(JID_2_FULL_A.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.GROUPCHAT);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
    }

    @Test
    public void testMultipleNonNegativeResourcesFullNoMatchHeadlineMessage() {
        Message message = new Message(JID_2_FULL_A.withResource("nomatch"));
        message.setFrom(JID_1_FULL);
        message.setType(Message.Type.HEADLINE);
        messageRouter.process(message);
        Mockito.verify(messageRouter).ignoreOrReturnError(Mockito.eq(message));
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
        Mockito.verifyNoInteractions(testSession2a);
    }
}
