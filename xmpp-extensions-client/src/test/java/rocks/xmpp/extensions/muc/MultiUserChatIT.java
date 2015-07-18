/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.muc;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rocks.xmpp.core.IntegrationTest;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.MessageEvent;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Christian Schudt
 */
public class MultiUserChatIT extends IntegrationTest {

    private ChatRoom[] chatRoom = new ChatRoom[2];

    private XmppClient[] xmppSession = new XmppClient[2];

    private MultiUserChatManager[] multiUserChatManager = new MultiUserChatManager[2];

    @BeforeClass
    public void before() throws XmppException {
        XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                //.debugger(ConsoleDebugger.class)
                .build();
        xmppSession[0] = new XmppClient(DOMAIN, configuration, TcpConnectionConfiguration.getDefault());
        xmppSession[0].connect();
        xmppSession[0].login(USER_1, PASSWORD_1);

        xmppSession[1] = new XmppClient(DOMAIN, TcpConnectionConfiguration.getDefault());
        xmppSession[1].connect();
        xmppSession[1].login(USER_2, PASSWORD_2);

        multiUserChatManager[0] = xmppSession[0].getManager(MultiUserChatManager.class);
        multiUserChatManager[1] = xmppSession[1].getManager(MultiUserChatManager.class);
        Collection<ChatService> chatServices0 = multiUserChatManager[0].discoverChatServices();
        Collection<ChatService> chatServices1 = multiUserChatManager[1].discoverChatServices();
        Assert.assertFalse(chatServices0.isEmpty());

        ChatService chatService0 = chatServices0.iterator().next();
        ChatService chatService1 = chatServices1.iterator().next();

        chatRoom[0] = chatService0.createRoom("test");
        chatRoom[1] = chatService1.createRoom("test");
        Assert.assertEquals(chatRoom[0].getAddress(), chatService0.getAddress().withLocal("test"));
    }

    @Test
    public void testMessageListener() throws XmppException, InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Consumer<MessageEvent> messageListener = e -> {
            if (e.getMessage().getBody().equals("Hello")) {
                countDownLatch.countDown();
            }
        };
        chatRoom[0].addInboundMessageListener(messageListener);

        try {
            chatRoom[0].enter("test");
            chatRoom[0].sendMessage("Hello");
            if (!countDownLatch.await(3, TimeUnit.SECONDS)) {
                Assert.fail("Timeout reached while waiting on message.");
            }
        } finally {
            chatRoom[0].removeInboundMessageListener(messageListener);
            chatRoom[0].exit();
        }
    }

    @Test(dependsOnMethods = "testMessageListener")
    public void testInvitation() throws XmppException, InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        multiUserChatManager[1].addInvitationListener(e -> {
            System.out.println(e);
            if (e.getReason().equals("join!")) {
                countDownLatch.countDown();
                //ChatRoom chatRoom = multiUserChatManager[0].createChatService(Jid.of(e.getRoomAddress().getDomain())).createRoom(e.getRoomAddress().getLocal());
            }
        });
        try {
            chatRoom[0].enter("test");
            chatRoom[0].invite(xmppSession[1].getConnectedResource().asBareJid(), "join!");
            if (!countDownLatch.await(3, TimeUnit.SECONDS)) {
                Assert.fail("Timeout reached while waiting on invitation.");
            }
        } finally {
            chatRoom[0].exit();
        }
    }

    @Test
    public void testUserEnters() throws XmppException, InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        chatRoom[0].addOccupantListener(e -> {
            if (e.getType() == OccupantEvent.Type.ENTERED) {
                countDownLatch.countDown();
            }
        });
        try {
            chatRoom[0].enter("test");
            chatRoom[1].enter("nick");
            if (!countDownLatch.await(6, TimeUnit.SECONDS)) {
                Assert.fail("Timeout reached while waiting on user entering.");
            }
        } finally {
            chatRoom[0].exit();
            chatRoom[1].exit();
        }
    }
}
