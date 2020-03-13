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
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.StanzaErrorException;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.extensions.vcard.avatar.model.AvatarUpdate;
import rocks.xmpp.extensions.vcard.temp.VCardManager;
import rocks.xmpp.extensions.vcard.temp.model.VCard;
import rocks.xmpp.im.subscription.PresenceManager;
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
    public void beforeClass() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(xmppSession.getLocalXmppAddress()).thenReturn(Jid.of("user"));
        Mockito.when(xmppSession.getManager(Mockito.eq(PresenceManager.class))).thenReturn(Mockito.mock(PresenceManager.class));
        Mockito.when(xmppSession.getConfiguration()).thenReturn(Mockito.mock(XmppSessionConfiguration.class));
        Mockito.when(xmppSession.getManager(Mockito.eq(VCardManager.class))).thenReturn(vCardManager);

        VCard vCard = new VCard();
        vCard.setPhoto(new VCard.Image("image/png", IMAGE));

        Mockito.when(vCardManager.getVCard(Mockito.any())).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(vCard)));
        Mockito.when(vCardManager.getVCard()).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(vCard)));
        Mockito.when(vCardManager.setVCard(Mockito.any())).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(null)));
    }

    @BeforeMethod
    public void reset() {
        Mockito.clearInvocations(vCardManager, xmppSession);
    }

    @Test
    public void getAvatarWithEmptyCache() throws XmppException {

        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession));
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
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession));
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

    @Test
    public void shouldNotPublishSameAvatarTwice() throws XmppException {

        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        vCardBasedAvatarsProtocol.publishAvatar(IMAGE).getResult();
        Mockito.verify(vCardManager, Mockito.times(0)).setVCard(Mockito.any());
    }

    @Test
    public void shouldPublishAvatarIfNotYetSet() throws XmppException {
        VCardManager vCardManager = Mockito.mock(VCardManager.class);
        Mockito.when(vCardManager.getVCard()).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(new VCard())));
        Mockito.when(vCardManager.setVCard(Mockito.any())).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(null)));
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        vCardBasedAvatarsProtocol.publishAvatar(IMAGE).getResult();
        Mockito.verify(vCardManager, Mockito.times(1)).setVCard(Mockito.any());
        Assert.assertEquals(vCardBasedAvatarsProtocol.userHashes.get(xmppSession.getLocalXmppAddress().asBareJid()), IMAGE_HASH);
    }

    @Test
    public void shouldPublishNewAvatar() throws XmppException {
        VCardManager vCardManager = Mockito.mock(VCardManager.class);
        Mockito.when(vCardManager.getVCard()).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(new VCard())));
        Mockito.when(vCardManager.setVCard(Mockito.any())).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(null)));
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        vCardBasedAvatarsProtocol.publishAvatar(new byte[]{5, 6, 7, 8}).getResult();
        ArgumentCaptor<VCard> vCardCaptor = ArgumentCaptor.forClass(VCard.class);
        Mockito.verify(vCardManager, Mockito.times(1)).setVCard(vCardCaptor.capture());
        Assert.assertEquals(vCardCaptor.getValue().getPhoto().getValue(), new byte[]{5, 6, 7, 8});
        Assert.assertEquals(vCardBasedAvatarsProtocol.userHashes.get(xmppSession.getLocalXmppAddress().asBareJid()), XmppUtils.hash(new byte[]{5, 6, 7, 8}));
    }

    @Test
    public void shouldResetAvatar() throws XmppException {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        vCardBasedAvatarsProtocol.publishAvatar(null).getResult();
        ArgumentCaptor<VCard> vCardCaptor = ArgumentCaptor.forClass(VCard.class);
        Mockito.verify(vCardManager, Mockito.times(1)).setVCard(vCardCaptor.capture());
        Assert.assertEquals(vCardBasedAvatarsProtocol.userHashes.get(xmppSession.getLocalXmppAddress().asBareJid()), "");
        Assert.assertNull(vCardCaptor.getValue().getPhoto());
    }

    /**
     * If a client supports the protocol defined herein, it MUST include the update child element in every presence broadcast it sends and SHOULD also include the update child in directed presence stanzas (e.g., directed presence sent when joining Multi-User Chat (XEP-0045) [5] rooms).
     *
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#bizrules-presence">4.1 Inclusion of Update Data in Presence</a>>
     */
    @Test
    public void shouldIncludeAvatarUpdateInOutboundPresence() {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>());
        Presence presence = new Presence();
        vCardBasedAvatarsProtocol.handleOutboundPresence(new PresenceEvent(Mockito.mock(XmppSession.class), presence, false));
        Assert.assertTrue(presence.hasExtension(AvatarUpdate.class));
    }

    /**
     * If a client is not yet ready to advertise an image, it MUST send an empty update child element.
     *
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#bizrules-presence">4.1 Inclusion of Update Data in Presence</a>>
     */
    @Test
    public void shouldIncludeEmptyAvatarUpdateIfNotReady() {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>());
        Presence presence = new Presence();
        vCardBasedAvatarsProtocol.handleOutboundPresence(new PresenceEvent(Mockito.mock(XmppSession.class), presence, false));
        AvatarUpdate avatarUpdate = presence.getExtension(AvatarUpdate.class);
        Assert.assertNotNull(avatarUpdate);
        Assert.assertNull(avatarUpdate.getHash());
    }

    /**
     * If there is no avatar image to be advertised, the photo element MUST be empty.
     *
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#bizrules-presence">4.1 Inclusion of Update Data in Presence</a>>
     */
    @Test
    public void shouldIncludeEmptyPhotoIfNoAvatar() {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>());
        // Empty hash == no avatar
        vCardBasedAvatarsProtocol.userHashes.put(xmppSession.getLocalXmppAddress().asBareJid(), "");
        Presence presence = new Presence();
        vCardBasedAvatarsProtocol.handleOutboundPresence(new PresenceEvent(xmppSession, presence, false));
        AvatarUpdate avatarUpdate = presence.getExtension(AvatarUpdate.class);
        Assert.assertNotNull(avatarUpdate);
        Assert.assertNotNull(avatarUpdate.getHash());
        Assert.assertEquals(avatarUpdate.getHash(), "");
    }

    /**
     * If the presence stanza received from the other resource does not contain the update child element,
     * then the other resource does not support vCard-based avatars.
     * That resource could modify the contents of the vCard (including the photo element);
     * because polling for vCard updates is not allowed, the client MUST stop advertising the avatar image hash.
     *
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#bizrules-resources">4.3 Multiple Resources</a>
     */
    @Test
    public void presenceOfOtherResourceDoesNotConform() {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>());
        Presence presence = new Presence();
        presence.setFrom(xmppSession.getLocalXmppAddress().withResource("other"));
        vCardBasedAvatarsProtocol.handleInboundPresence(new PresenceEvent(xmppSession, presence, true));

        Presence presenceOut = new Presence();
        vCardBasedAvatarsProtocol.handleOutboundPresence(new PresenceEvent(xmppSession, presenceOut, false));
        Assert.assertFalse(presence.hasExtension(AvatarUpdate.class));
    }

    /**
     * If the update child element is empty, then the other resource supports the protocol but does not have its own avatar image.
     * Therefore the client can ignore the other resource and continue to broadcast the existing image hash.
     *
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#bizrules-resources">4.3 Multiple Resources</a>
     */
    @Test
    public void presenceOfOtherResourceSendsEmptyAvatarUpdate() {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        Presence presence = new Presence();
        presence.setFrom(xmppSession.getLocalXmppAddress().withResource("other"));
        presence.addExtension(new AvatarUpdate());
        vCardBasedAvatarsProtocol.handleInboundPresence(new PresenceEvent(xmppSession, presence, true));
        Assert.assertTrue(vCardBasedAvatarsProtocol.nonConformingResources.isEmpty());
        Mockito.verify(vCardBasedAvatarsProtocol, Mockito.times(0)).resetHash();
    }

    /**
     * If the update child element contains an empty photo element,
     * then the other resource has updated the vCard with an empty BINVAL.
     * Therefore the client MUST retrieve the vCard.
     * If the retrieved vCard contains a photo element with an empty BINVAL, then the client MUST stop advertising the old image.
     *
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#bizrules-resources">4.3 Multiple Resources</a>
     */
    @Test
    public void presenceOfOtherResourceSendsEmptyPhoto() {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        Presence presence = new Presence();
        presence.setFrom(xmppSession.getLocalXmppAddress().withResource("other"));
        presence.addExtension(new AvatarUpdate(""));
        vCardBasedAvatarsProtocol.handleInboundPresence(new PresenceEvent(xmppSession, presence, true));
        Assert.assertTrue(vCardBasedAvatarsProtocol.nonConformingResources.isEmpty());
        Mockito.verify(vCardBasedAvatarsProtocol).resetHash();
    }

    /**
     * If the update child element contains a non-empty photo element, then the client MUST compare the image hashes. If the hashes are identical,
     * then the client can ignore the other resource and continue to broadcast the existing image hash.
     *
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#bizrules-resources">4.3 Multiple Resources</a>
     */
    @Test
    public void presenceOfOtherResourceSendsIdenticalImageHash() {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        vCardBasedAvatarsProtocol.userHashes.put(xmppSession.getLocalXmppAddress().asBareJid(), IMAGE_HASH);
        Presence presence = new Presence();
        presence.setFrom(xmppSession.getLocalXmppAddress().withResource("other"));
        presence.addExtension(new AvatarUpdate(IMAGE_HASH));
        vCardBasedAvatarsProtocol.handleInboundPresence(new PresenceEvent(xmppSession, presence, true));
        Assert.assertTrue(vCardBasedAvatarsProtocol.nonConformingResources.isEmpty());
        Mockito.verify(vCardBasedAvatarsProtocol, Mockito.times(0)).resetHash();
    }

    /**
     * If the hashes are different, then the client MUST NOT attempt to resolve the conflict by uploading its avatar image again.
     * Instead, it MUST defer to the content of the retrieved vCard by resetting its image hash (see below) and providing that hash in future presence broadcasts.
     *
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#bizrules-resources">4.3 Multiple Resources</a>
     */
    @Test
    public void presenceOfOtherResourceSendsDifferentImageHash() {
        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        vCardBasedAvatarsProtocol.userHashes.put(xmppSession.getLocalXmppAddress().asBareJid(), IMAGE_HASH);
        Presence presence = new Presence();
        presence.setFrom(xmppSession.getLocalXmppAddress().withResource("other"));
        presence.addExtension(new AvatarUpdate("differentHash"));
        vCardBasedAvatarsProtocol.handleInboundPresence(new PresenceEvent(xmppSession, presence, true));
        Assert.assertTrue(vCardBasedAvatarsProtocol.nonConformingResources.isEmpty());
        Mockito.verify(vCardBasedAvatarsProtocol).resetHash();
    }

    /**
     * If the hashes are different, then the client MUST NOT attempt to resolve the conflict by uploading its avatar image again.
     * Instead, it MUST defer to the content of the retrieved vCard by resetting its image hash (see below) and providing that hash in future presence broadcasts.
     *
     * @see <a href="https://xmpp.org/extensions/xep-0153.html#bizrules-reset">4.4 Resetting the Image Hash</a>
     */
    @Test
    public void testResetHash() {
        VCard vCard = new VCard();
        vCard.setPhoto(new VCard.Image("image/png", IMAGE));

        VCardManager vCardManager = Mockito.mock(VCardManager.class);
        Mockito.when(vCardManager.getVCard(Mockito.any())).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(vCard)));
        Mockito.when(vCardManager.getVCard()).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(vCard)));
        Mockito.when(vCardManager.setVCard(Mockito.any())).thenReturn(new AsyncResult<>(CompletableFuture.completedFuture(null)));

        VCardBasedAvatarsProtocol vCardBasedAvatarsProtocol = Mockito.spy(new VCardBasedAvatarsProtocol(xmppSession, vCardManager, new HashMap<>()));
        Mockito.when(xmppSession.send(Mockito.any())).then(invocation -> {
            vCardBasedAvatarsProtocol.handleOutboundPresence(new PresenceEvent(xmppSession, invocation.getArgument(0), false));
            return new AsyncResult<>(CompletableFuture.completedFuture(null));
        });
        vCardBasedAvatarsProtocol.resetHash();
        ArgumentCaptor<StreamElement> argumentCaptorSend = ArgumentCaptor.forClass(StreamElement.class);
        InOrder inOrder = Mockito.inOrder(xmppSession, vCardManager);
        // 1. Immediately send out a presence element with an empty update child element (containing no photo element).
        inOrder.verify(xmppSession, Mockito.times(1)).send(argumentCaptorSend.capture());
        Assert.assertTrue(argumentCaptorSend.getValue() instanceof Presence);
        AvatarUpdate avatarUpdate = ((Presence) argumentCaptorSend.getValue()).getExtension(AvatarUpdate.class);
        Assert.assertNotNull(avatarUpdate);
        Assert.assertNull(avatarUpdate.getHash());

        // 2. Download the vCard from the server.
        inOrder.verify(vCardManager).getVCard(xmppSession.getLocalXmppAddress().asBareJid());

        // 4. calculate the hash of image and advertise that hash in future presence broadcasts.
        ArgumentCaptor<StreamElement> argumentCaptorSend2 = ArgumentCaptor.forClass(StreamElement.class);
        inOrder.verify(xmppSession, Mockito.times(1)).send(argumentCaptorSend2.capture());
        Assert.assertTrue(argumentCaptorSend2.getValue() instanceof Presence);
        avatarUpdate = ((Presence) argumentCaptorSend2.getValue()).getExtension(AvatarUpdate.class);
        Assert.assertNotNull(avatarUpdate);
        Assert.assertEquals(avatarUpdate.getHash(), IMAGE_HASH);
    }
}
