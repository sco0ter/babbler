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

package rocks.xmpp.extensions.vcard.avatar;

import org.mockito.ArgumentCaptor;
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
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.vcard.temp.VCardManager;
import rocks.xmpp.extensions.vcard.temp.model.VCard;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.AsyncResult;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * @author Christian Schudt
 */
public class VCardBasedAvatarsProtocolTest {

    private static final byte[] IMAGE = new byte[]{1, 2, 3, 4};

    private static final String IMAGE_HASH = XmppUtils.hash(IMAGE);

    @Mock
    private XmppSession xmppSession;

    @Mock
    private VCardManager vCardManager;

    @BeforeClass
    private void beforeClass() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(xmppSession.getLocalXmppAddress()).thenReturn(Jid.of("user"));
        Mockito.when(xmppSession.getConfiguration()).thenReturn(Mockito.mock(XmppSessionConfiguration.class));
        VCard vCard = new VCard();
        vCard.setPhoto(new VCard.Image("image/png", IMAGE));

        Mockito.when(vCardManager.getVCard(Mockito.any())).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(vCard)));
    }

    @BeforeMethod
    private void reset() {
        Mockito.clearInvocations(vCardManager);
    }

    @Test
    public void getAvatarWithEmptyCache() throws XmppException {

        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        byte[] avatar = vCardBasedAvatarsProtocol.getAvatar(Jid.of("contact")).getResult();
        Assert.assertEquals(avatar, IMAGE);
        Assert.assertTrue(vCardBasedAvatarsProtocol.userHashes.containsKey(Jid.of("contact")));
        Assert.assertEquals(vCardBasedAvatarsProtocol.userHashes.get(Jid.of("contact")), IMAGE_HASH);

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> avatarCaptor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(vCardBasedAvatarsProtocol).storeToCache(hashCaptor.capture(), avatarCaptor.capture());
        Assert.assertEquals(hashCaptor.getValue(), IMAGE_HASH);
        Assert.assertEquals(avatarCaptor.getValue(), IMAGE);
        Mockito.verify(vCardManager).getVCard(Mockito.eq(Jid.of("contact")));
    }

    @Test
    public void getAvatarWithCacheHit() throws XmppException {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        vCardBasedAvatarsProtocol.userHashes.put(Jid.of("contact"), IMAGE_HASH);

        Mockito.when(vCardBasedAvatarsProtocol.loadFromCache(Mockito.anyString())).thenReturn(IMAGE);

        byte[] avatar = vCardBasedAvatarsProtocol.getAvatar(Jid.of("contact")).getResult();

        Assert.assertEquals(avatar, IMAGE);

        Mockito.verifyNoInteractions(vCardManager);

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(vCardBasedAvatarsProtocol).loadFromCache(hashCaptor.capture());
        Assert.assertEquals(hashCaptor.getValue(), IMAGE_HASH);
    }

    @Test
    public void getAvatarWithUserHashOnly() throws XmppException {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        vCardBasedAvatarsProtocol.userHashes.put(Jid.of("contact"), IMAGE_HASH);

        Mockito.when(vCardBasedAvatarsProtocol.loadFromCache(Mockito.anyString())).thenReturn(null);

        byte[] avatar = vCardBasedAvatarsProtocol.getAvatar(Jid.of("contact")).getResult();

        Assert.assertEquals(avatar, IMAGE);

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> avatarCaptor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(vCardBasedAvatarsProtocol).storeToCache(hashCaptor.capture(), avatarCaptor.capture());
        Assert.assertEquals(hashCaptor.getValue(), IMAGE_HASH);
        Assert.assertEquals(avatarCaptor.getValue(), IMAGE);
        Mockito.verify(vCardManager).getVCard(Mockito.eq(Jid.of("contact")));
    }

    @Test
    public void getAvatarWithEmptyVCard() throws XmppException {
        VCardManager vCardManager = Mockito.mock(VCardManager.class);
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));


        Mockito.when(vCardManager.getVCard(Mockito.any())).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(new VCard())));
        Mockito.when(vCardBasedAvatarsProtocol.loadFromCache(Mockito.anyString())).thenReturn(null);

        byte[] avatar = vCardBasedAvatarsProtocol.getAvatar(Jid.of("contact")).getResult();

        Assert.assertEquals(avatar, new byte[0]);

        Mockito.verify(vCardBasedAvatarsProtocol, Mockito.times(0)).storeToCache(Mockito.anyString(), Mockito.any());
        Mockito.verify(vCardManager).getVCard(Mockito.eq(Jid.of("contact")));
    }

    @Test
    public void getAvatarWithNoVCardAtAll() throws XmppException {

        VCardManager vCardManager = Mockito.mock(VCardManager.class);
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));

        Mockito.when(vCardManager.getVCard(Mockito.any())).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(null)));
        Mockito.when(vCardBasedAvatarsProtocol.loadFromCache(Mockito.anyString())).thenReturn(null);

        byte[] avatar = vCardBasedAvatarsProtocol.getAvatar(Jid.of("contact")).getResult();

        Assert.assertEquals(avatar, new byte[0]);

        Mockito.verify(vCardBasedAvatarsProtocol, Mockito.times(0)).storeToCache(Mockito.any(), Mockito.any());
        Mockito.verify(vCardManager).getVCard(Mockito.eq(Jid.of("contact")));
    }

    @Test
    public void getAvatarWithVCardReturnedStanzaError() throws XmppException {

        VCardManager vCardManager = Mockito.mock(VCardManager.class);
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));

        CompletableFuture<VCard> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(new StanzaErrorException(new IQ(IQ.Type.GET, null).createError(Condition.ITEM_NOT_FOUND)));
        Mockito.when(vCardManager.getVCard(Mockito.any())).thenReturn(new AsyncResult<>(completableFuture));
        Mockito.when(vCardBasedAvatarsProtocol.loadFromCache(Mockito.anyString())).thenReturn(null);

        byte[] avatar = vCardBasedAvatarsProtocol.getAvatar(Jid.of("contact")).getResult();

        Assert.assertEquals(avatar, new byte[0]);

        Mockito.verify(vCardBasedAvatarsProtocol, Mockito.times(0)).storeToCache(Mockito.any(), Mockito.any());
        Mockito.verify(vCardManager).getVCard(Mockito.eq(Jid.of("contact")));
    }

    @Test(expectedExceptions = Exception.class)
    public void getAvatarWithVCardRetrievalThrew() throws XmppException {

        VCardManager vCardManager = Mockito.mock(VCardManager.class);
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        vCardBasedAvatarsProtocol.getAvatar(Jid.of("contact")).getResult();

        CompletableFuture<VCard> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(new Exception());
        Mockito.when(vCardManager.getVCard(Mockito.any())).thenReturn(new AsyncResult<>(completableFuture));
        Mockito.when(vCardBasedAvatarsProtocol.loadFromCache(Mockito.anyString())).thenReturn(null);
    }
}
