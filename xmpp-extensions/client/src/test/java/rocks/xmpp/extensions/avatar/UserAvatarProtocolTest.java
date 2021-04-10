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

package rocks.xmpp.extensions.avatar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.extensions.avatar.model.data.AvatarData;
import rocks.xmpp.extensions.avatar.model.metadata.AvatarMetadata;
import rocks.xmpp.extensions.pubsub.PubSubManager;
import rocks.xmpp.extensions.pubsub.PubSubNode;
import rocks.xmpp.extensions.pubsub.PubSubService;
import rocks.xmpp.extensions.pubsub.model.Item;
import rocks.xmpp.extensions.pubsub.model.event.Event;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.AsyncResult;

/**
 * @author Christian Schudt
 */
public class UserAvatarProtocolTest {

    private static final byte[] IMAGE;

    private static final String IMAGE_HASH;

    static {
        try {
            InputStream is = UserAvatarProtocolTest.class.getResourceAsStream("/xmpp.png");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            IMAGE = buffer.toByteArray();
            IMAGE_HASH = XmppUtils.hash(IMAGE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Mock
    private XmppSession xmppSession;

    @Mock
    private PubSubManager pubSubManager;

    @Mock
    private PubSubNode metaDataNode;

    @Mock
    private PubSubNode dataNode;

    @BeforeClass
    public void init() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(xmppSession.getLocalXmppAddress()).thenReturn(Jid.of("user"));
        Mockito.when(xmppSession.getManager(Mockito.eq(PubSubManager.class))).thenReturn(pubSubManager);
        Mockito.when(xmppSession.getConfiguration()).thenReturn(Mockito.mock(XmppSessionConfiguration.class));

        PubSubService pepService = Mockito.mock(PubSubService.class);
        Mockito.when(pubSubManager.createPersonalEventingService()).thenReturn(pepService);
        Mockito.when(pubSubManager.createPubSubService(Mockito.any())).thenReturn(pepService);

        Mockito.when(pepService.node(Mockito.eq(AvatarMetadata.NAMESPACE))).thenReturn(metaDataNode);
        Mockito.when(pepService.node(Mockito.eq(AvatarData.NAMESPACE))).thenReturn(dataNode);
        Mockito.when(metaDataNode.publish(Mockito.any(), Mockito.any(Object.class)))
                .thenReturn(new AsyncResult<>(CompletableFuture.completedFuture("")));
        Mockito.when(metaDataNode.publish(Mockito.any(Object.class)))
                .thenReturn(new AsyncResult<>(CompletableFuture.completedFuture("")));
        Mockito.when(dataNode.publish(Mockito.anyString(), Mockito.any(Object.class)))
                .thenReturn(new AsyncResult<>(CompletableFuture.completedFuture("")));
        Mockito.when(dataNode.getItems(Mockito.eq(IMAGE_HASH)))
                .thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(Collections.singletonList(
                        new Item() {
                            @Override
                            public Object getPayload() {
                                return new AvatarData(IMAGE);
                            }

                            @Override
                            public String getId() {
                                return IMAGE_HASH;
                            }

                            @Override
                            public Jid getPublisher() {
                                return null;
                            }
                        }
                ))));
        Mockito.when(metaDataNode.getItems(Mockito.anyInt())).thenReturn(new AsyncResult<>(
                CompletableFuture.completedFuture(Collections.singletonList(
                        new Item() {
                            @Override
                            public Object getPayload() {
                                return new AvatarMetadata(
                                        new AvatarMetadata.Info(IMAGE.length, IMAGE_HASH, "image/png"));
                            }

                            @Override
                            public String getId() {
                                return IMAGE_HASH;
                            }

                            @Override
                            public Jid getPublisher() {
                                return null;
                            }
                        }
                ))));
    }

    @BeforeMethod
    public void reset() {
        Mockito.clearInvocations(dataNode, metaDataNode);
    }

    @Test
    public void getAvatarWithEmptyCache() throws XmppException {
        UserAvatarProtocol userAvatarProtocol = Mockito.spy(new UserAvatarProtocol(xmppSession));
        byte[] avatar = userAvatarProtocol.getAvatar(Jid.of("contact")).getResult();
        Assert.assertEquals(avatar, IMAGE);

        InOrder inOrder = Mockito.inOrder(metaDataNode, dataNode, userAvatarProtocol);
        inOrder.verify(metaDataNode).getItems(1);
        inOrder.verify(userAvatarProtocol).loadFromCache(IMAGE_HASH);
        inOrder.verify(dataNode).getItems(IMAGE_HASH);
        inOrder.verify(userAvatarProtocol).storeToCache(IMAGE_HASH, IMAGE);
    }

    @Test
    public void getAvatarWithCacheHit() throws XmppException {
        UserAvatarProtocol userAvatarProtocol = Mockito.spy(new UserAvatarProtocol(xmppSession));
        Mockito.when(userAvatarProtocol.loadFromCache(Mockito.eq(IMAGE_HASH))).thenReturn(IMAGE);
        byte[] avatar = userAvatarProtocol.getAvatar(Jid.of("contact")).getResult();
        Assert.assertEquals(avatar, IMAGE);

        InOrder inOrder = Mockito.inOrder(metaDataNode, dataNode, userAvatarProtocol);
        inOrder.verify(metaDataNode).getItems(1);
        inOrder.verify(userAvatarProtocol).loadFromCache(IMAGE_HASH);
        Mockito.verifyNoMoreInteractions(dataNode);
        Mockito.verify(userAvatarProtocol, Mockito.times(0)).storeToCache(IMAGE_HASH, IMAGE);
        Mockito.verify(userAvatarProtocol, Mockito.times(0)).notifyListeners(Mockito.any(), Mockito.any());
    }

    @Test
    public void getAvatarWithEmptyAvatarMetaData() throws XmppException {
        XmppSession xmppSession = Mockito.mock(XmppSession.class);
        Mockito.when(xmppSession.getConfiguration()).thenReturn(Mockito.mock(XmppSessionConfiguration.class));

        PubSubManager pubSubManager = Mockito.mock(PubSubManager.class);
        Mockito.when(xmppSession.getManager(Mockito.eq(PubSubManager.class))).thenReturn(pubSubManager);
        PubSubService pepService = Mockito.mock(PubSubService.class);
        Mockito.when(pubSubManager.createPubSubService(Mockito.any())).thenReturn(pepService);
        UserAvatarProtocol userAvatarProtocol = Mockito.spy(new UserAvatarProtocol(xmppSession));

        PubSubNode metaNode = Mockito.mock(PubSubNode.class);
        Mockito.when(pepService.node(Mockito.eq(AvatarMetadata.NAMESPACE))).thenReturn(metaNode);
        Mockito.when(metaNode.getItems(1))
                .thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(Collections.singletonList(new Item() {
                    @Override
                    public Object getPayload() {
                        // Empty meta data.
                        return new AvatarMetadata();
                    }

                    @Override
                    public String getId() {
                        return null;
                    }

                    @Override
                    public Jid getPublisher() {
                        return null;
                    }
                }))));

        byte[] avatar = userAvatarProtocol.getAvatar(Jid.of("contact")).getResult();

        Assert.assertEquals(avatar, new byte[0]);

        Mockito.verify(pepService, Mockito.times(0)).node(Mockito.eq(AvatarData.NAMESPACE));
        Mockito.verify(userAvatarProtocol, Mockito.times(0)).loadFromCache(Mockito.any());
        Mockito.verify(userAvatarProtocol, Mockito.times(0)).notifyListeners(Mockito.any(), Mockito.any());
    }

    /**
     * @see <a href="https://xmpp.org/extensions/xep-0084.html#process-pubdata">3.1 User Publishes Data¶</a>
     */
    @Test
    public void publishAvatar() throws XmppException {

        UserAvatarProtocol userAvatarProtocol = Mockito.spy(new UserAvatarProtocol(xmppSession));
        userAvatarProtocol.publishAvatar(IMAGE).getResult();

        InOrder inOrder = Mockito.inOrder(dataNode, metaDataNode);
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> itemCaptor = ArgumentCaptor.forClass(Object.class);

        // First the image data should have been published to the "urn:xmpp:avatar:data" node.
        inOrder.verify(dataNode).publish(hashCaptor.capture(), itemCaptor.capture());
        Assert.assertEquals(hashCaptor.getValue(), IMAGE_HASH);
        Assert.assertTrue(itemCaptor.getValue() instanceof AvatarData);
        Assert.assertEquals(((AvatarData) itemCaptor.getValue()).getData(), IMAGE);

        ArgumentCaptor<String> hashCaptorMetaData = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> itemCaptorMetaData = ArgumentCaptor.forClass(Object.class);

        // Second the image data meta data should have been published to the "urn:xmpp:avatar:metadata" node.
        inOrder.verify(metaDataNode).publish(hashCaptorMetaData.capture(), itemCaptorMetaData.capture());
        Assert.assertEquals(hashCaptorMetaData.getValue(), IMAGE_HASH);
        Assert.assertTrue(itemCaptorMetaData.getValue() instanceof AvatarMetadata);
        List<AvatarMetadata.Info> infoList = ((AvatarMetadata) itemCaptorMetaData.getValue()).getInfoList();
        Assert.assertFalse(infoList.isEmpty());
        Assert.assertEquals(infoList.get(0).getBytes(), IMAGE.length);
        Assert.assertEquals(infoList.get(0).getType(), "image/png");
        Assert.assertEquals(infoList.get(0).getId(), IMAGE_HASH);

        Mockito.verify(userAvatarProtocol, Mockito.times(0)).notifyListeners(Mockito.any(), Mockito.any());
    }

    /**
     * @see <a href="https://xmpp.org/extensions/xep-0084.html#pub-disable">3.5 Publisher Disables Avatar Publishing</a>
     */
    @Test
    public void resetAvatar() throws XmppException {

        UserAvatarProtocol userAvatarProtocol = Mockito.spy(new UserAvatarProtocol(xmppSession));
        userAvatarProtocol.publishAvatar(null).getResult();

        Mockito.verifyNoInteractions(dataNode);

        ArgumentCaptor<Object> itemCaptorMetaData = ArgumentCaptor.forClass(Object.class);

        // Second the image data meta data should have been published to the "urn:xmpp:avatar:metadata" node.
        Mockito.verify(metaDataNode).publish(itemCaptorMetaData.capture());
        Assert.assertTrue(itemCaptorMetaData.getValue() instanceof AvatarMetadata);
        Assert.assertTrue(((AvatarMetadata) itemCaptorMetaData.getValue()).getInfoList().isEmpty());
        Mockito.verify(userAvatarProtocol, Mockito.times(0)).notifyListeners(Mockito.any(), Mockito.any());
    }

    @Test
    public void handlePubSubNotification() {
        UserAvatarProtocol userAvatarProtocol = Mockito.spy(new UserAvatarProtocol(xmppSession));

        Message message = new Message();
        message.setFrom(Jid.of("contact@avatar/test"));
        message.addExtensions(Event.withItem(AvatarMetadata.NAMESPACE,
                new AvatarMetadata(new AvatarMetadata.Info(IMAGE.length, IMAGE_HASH, "image/png")), IMAGE_HASH, null));

        MessageEvent messageEvent = new MessageEvent(xmppSession, message, true);
        userAvatarProtocol.handleInboundMessage(messageEvent);

        ArgumentCaptor<Jid> contactCaptor = ArgumentCaptor.forClass(Jid.class);
        ArgumentCaptor<byte[]> avatarCaptor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(userAvatarProtocol).notifyListeners(contactCaptor.capture(), avatarCaptor.capture());
        Assert.assertEquals(contactCaptor.getValue(), message.getFrom().asBareJid());
        Assert.assertEquals(avatarCaptor.getValue(), IMAGE);
        Mockito.verify(userAvatarProtocol).storeToCache(IMAGE_HASH, IMAGE);
    }
}
