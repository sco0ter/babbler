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

package rocks.xmpp.extensions.muc;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.session.TestXmppSession;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
public class ChatServiceTest {

    @Test
    public void testComparable() {
        XmppSession xmppSession = new TestXmppSession();
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
        ChatService chatService1 = new ChatService(Jid.valueOf("aaa"), "aaa", xmppSession, serviceDiscoveryManager);
        ChatService chatService2 = new ChatService(Jid.valueOf("bbb"), "bbb", xmppSession, serviceDiscoveryManager);
        ChatService chatService3 = new ChatService(Jid.valueOf("ccc"), "aaa", xmppSession, serviceDiscoveryManager);
        ChatService chatService4 = new ChatService(Jid.valueOf("ddd"), "bbb", xmppSession, serviceDiscoveryManager);
        ChatService chatService5 = new ChatService(Jid.valueOf("ddd"), null, xmppSession, serviceDiscoveryManager);
        ChatService chatService6 = new ChatService(null, "aaa", xmppSession, serviceDiscoveryManager);

        List<ChatService> chatServices = new ArrayList<>();
        chatServices.add(chatService1);
        chatServices.add(chatService2);
        chatServices.add(chatService3);
        chatServices.add(chatService4);
        chatServices.add(chatService5);
        chatServices.add(chatService6);

        Collections.shuffle(chatServices);
        Collections.sort(chatServices);

        Assert.assertEquals(chatServices.get(0), chatService1);
        Assert.assertEquals(chatServices.get(1), chatService3);
        Assert.assertEquals(chatServices.get(2), chatService6);
        Assert.assertEquals(chatServices.get(3), chatService2);
        Assert.assertEquals(chatServices.get(4), chatService4);
        Assert.assertEquals(chatServices.get(5), chatService5);
    }

    @Test
    public void testChatRoomsComparable() {
        XmppSession xmppSession = new TestXmppSession();
        ServiceDiscoveryManager serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
        ChatRoom chatRoom1 = new ChatRoom(Jid.valueOf("aaa"), "aaa", xmppSession, serviceDiscoveryManager);
        ChatRoom chatRoom2 = new ChatRoom(Jid.valueOf("bbb"), "bbb", xmppSession, serviceDiscoveryManager);
        ChatRoom chatRoom3 = new ChatRoom(Jid.valueOf("ccc"), "aaa", xmppSession, serviceDiscoveryManager);
        ChatRoom chatRoom4 = new ChatRoom(Jid.valueOf("ddd"), "bbb", xmppSession, serviceDiscoveryManager);
        ChatRoom chatRoom5 = new ChatRoom(Jid.valueOf("ddd"), null, xmppSession, serviceDiscoveryManager);
        ChatRoom chatRoom6 = new ChatRoom(null, "aaa", xmppSession, serviceDiscoveryManager);

        List<ChatRoom> chatRooms = new ArrayList<>();
        chatRooms.add(chatRoom1);
        chatRooms.add(chatRoom2);
        chatRooms.add(chatRoom3);
        chatRooms.add(chatRoom4);
        chatRooms.add(chatRoom5);
        chatRooms.add(chatRoom6);

        Collections.shuffle(chatRooms);
        Collections.sort(chatRooms);

        Assert.assertEquals(chatRooms.get(0), chatRoom1);
        Assert.assertEquals(chatRooms.get(1), chatRoom3);
        Assert.assertEquals(chatRooms.get(2), chatRoom6);
        Assert.assertEquals(chatRooms.get(3), chatRoom2);
        Assert.assertEquals(chatRooms.get(4), chatRoom4);
        Assert.assertEquals(chatRooms.get(5), chatRoom5);
    }
}
