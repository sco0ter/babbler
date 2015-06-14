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

package rocks.xmpp.extensions.si;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.AbstractIQHandler;
import rocks.xmpp.core.stanza.IQHandler;
import rocks.xmpp.core.stanza.model.AbstractIQ;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.extensions.bytestreams.ByteStreamEvent;
import rocks.xmpp.extensions.bytestreams.ByteStreamSession;
import rocks.xmpp.extensions.bytestreams.ibb.InBandByteStreamManager;
import rocks.xmpp.extensions.bytestreams.ibb.model.InBandByteStream;
import rocks.xmpp.extensions.bytestreams.s5b.Socks5ByteStreamManager;
import rocks.xmpp.extensions.bytestreams.s5b.model.Socks5ByteStream;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.featureneg.model.FeatureNegotiation;
import rocks.xmpp.extensions.filetransfer.FileTransfer;
import rocks.xmpp.extensions.filetransfer.FileTransferManager;
import rocks.xmpp.extensions.filetransfer.FileTransferNegotiator;
import rocks.xmpp.extensions.filetransfer.FileTransferOffer;
import rocks.xmpp.extensions.si.model.StreamInitiation;
import rocks.xmpp.extensions.si.profile.filetransfer.model.SIFileTransferOffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Christian Schudt
 */
public final class StreamInitiationManager extends Manager implements FileTransferNegotiator {

    private static final String STREAM_METHOD = "stream-method";

    private final Map<String, ProfileManager> profileManagers = new ConcurrentHashMap<>();

    private final InBandByteStreamManager inBandByteStreamManager;

    private final Socks5ByteStreamManager socks5ByteStreamManager;

    private final IQHandler iqHandler;

    private StreamInitiationManager(final XmppSession xmppSession) {
        super(xmppSession);

        inBandByteStreamManager = xmppSession.getManager(InBandByteStreamManager.class);
        socks5ByteStreamManager = xmppSession.getManager(Socks5ByteStreamManager.class);

        // Currently, there's only one profile in XMPP, namely XEP-0096 SI File Transfer.
        profileManagers.put(SIFileTransferOffer.NAMESPACE, (iq, streamInitiation) -> {
            FileTransferManager fileTransferManager = xmppSession.getManager(FileTransferManager.class);
            fileTransferManager.fileTransferOffered(iq, streamInitiation.getId(), streamInitiation.getMimeType(), (FileTransferOffer) streamInitiation.getProfileElement(), streamInitiation, StreamInitiationManager.this);
        });

        iqHandler = new AbstractIQHandler(AbstractIQ.Type.SET) {
            @Override
            protected AbstractIQ processRequest(AbstractIQ iq) {
                StreamInitiation streamInitiation = iq.getExtension(StreamInitiation.class);

                FeatureNegotiation featureNegotiation = streamInitiation.getFeatureNegotiation();
                // Assume no valid streams by default, unless valid streams are found.
                boolean noValidStreams = true;
                if (featureNegotiation != null) {
                    DataForm dataForm = featureNegotiation.getDataForm();
                    if (dataForm != null) {
                        DataForm.Field field = dataForm.findField(STREAM_METHOD);
                        if (field != null) {
                            List<String> streamMethods = field.getOptions().stream().map(DataForm.Option::getValue).collect(Collectors.toList());
                            if (!Collections.disjoint(streamMethods, getSupportedStreamMethods())) {
                                // Request contains valid streams
                                noValidStreams = false;
                            }
                        }
                    }
                }
                if (noValidStreams) {
                    return iq.createError(new StanzaError(rocks.xmpp.core.stanza.model.errors.Condition.BAD_REQUEST, StreamInitiation.NO_VALID_STREAMS));
                } else {
                    ProfileManager profileManager = profileManagers.get(streamInitiation.getProfile());

                    if (profileManager == null) {
                        return iq.createError(new StanzaError(rocks.xmpp.core.stanza.model.errors.Condition.BAD_REQUEST, StreamInitiation.BAD_PROFILE));
                    } else {
                        profileManager.handle(iq, streamInitiation);
                        return null;
                    }
                }
            }
        };
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        xmppSession.addIQHandler(StreamInitiation.class, iqHandler);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        xmppSession.removeIQHandler(StreamInitiation.class);
    }

    /**
     * Initiates a stream with another entity.
     *
     * @param receiver The receiver, i.e. the XMPP entity you want to negotiate a stream.
     * @param profile  The profile. Currently there's only the {@link rocks.xmpp.extensions.si.profile.filetransfer.model.SIFileTransferOffer} profile.
     * @param mimeType The mime type of the stream.
     * @param timeout  The timeout, which wait until the stream has been negotiated.
     * @return The byte stream session which has been negotiated.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the entity returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException If the entity did not respond.
     * @throws java.io.IOException                         If an I/O error occurred during byte session establishment.
     */
    public OutputStream initiateStream(Jid receiver, SIFileTransferOffer profile, String mimeType, long timeout) throws XmppException, IOException {

        // Create a random id for the stream session.
        String sessionId = UUID.randomUUID().toString();

        // Offer stream methods.
        List<DataForm.Option> options = getSupportedStreamMethods().stream().map(DataForm.Option::new).collect(Collectors.toList());
        DataForm.Field field = DataForm.Field.builder().var(STREAM_METHOD).type(DataForm.Field.Type.LIST_SINGLE).options(options).build();
        DataForm dataForm = new DataForm(DataForm.Type.FORM, Collections.singletonList(field));
        // Offer the file to the recipient and wait until it's accepted.
        AbstractIQ result = xmppSession.query(new IQ(receiver, IQ.Type.SET, new StreamInitiation(sessionId, SIFileTransferOffer.NAMESPACE, mimeType, profile, new FeatureNegotiation(dataForm))), timeout);

        // The recipient must response with a stream initiation.
        StreamInitiation streamInitiation = result.getExtension(StreamInitiation.class);

        FeatureNegotiation featureNegotiation = streamInitiation.getFeatureNegotiation();
        // Get the stream method which has been chosen by the recipient.
        String streamMethod = featureNegotiation.getDataForm().findField(STREAM_METHOD).getValues().get(0);

        ByteStreamSession byteStreamSession;
        // Choose the stream method to be used based on the recipient's choice.
        switch (streamMethod) {
            case Socks5ByteStream.NAMESPACE:
                try {
                    byteStreamSession = socks5ByteStreamManager.initiateSession(receiver, sessionId);
                } catch (Exception e) {
                    // As fallback, if SOCKS5 negotiation failed, try IBB.
                    byteStreamSession = inBandByteStreamManager.initiateSession(receiver, sessionId, 4096);
                }
                break;
            case InBandByteStream.NAMESPACE:
                byteStreamSession = inBandByteStreamManager.initiateSession(receiver, sessionId, 4096);
                break;
            default:
                throw new IOException("Receiver returned unsupported stream method.");
        }
        return byteStreamSession.getOutputStream();
    }

    @Override
    public FileTransfer accept(AbstractIQ iq, final String sessionId, FileTransferOffer fileTransferOffer, Object protocol, OutputStream outputStream) throws IOException {
        StreamInitiation streamInitiation = (StreamInitiation) protocol;
        DataForm.Field field = streamInitiation.getFeatureNegotiation().getDataForm().findField(STREAM_METHOD);
        final List<String> offeredStreamMethods = field.getOptions().stream().map(DataForm.Option::getValue).collect(Collectors.toList());
        offeredStreamMethods.retainAll(getSupportedStreamMethods());
        DataForm.Field fieldReply = DataForm.Field.builder().var(STREAM_METHOD).values(offeredStreamMethods).type(DataForm.Field.Type.LIST_SINGLE).build();
        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT, Collections.singleton(fieldReply));
        StreamInitiation siResponse = new StreamInitiation(new FeatureNegotiation(dataForm));

        final Lock lock = new ReentrantLock();
        final Condition byteStreamOpened = lock.newCondition();
        final ByteStreamSession[] byteStreamSessions = new ByteStreamSession[1];

        final List<Exception> negotiationExceptions = new ArrayList<>();
        // Before we reply with the chosen stream method, we
        // register a byte stream listener, because we expect the initiator to open a byte stream with us.
        Consumer<ByteStreamEvent> byteStreamListener = e -> {
            if (sessionId.equals(e.getSessionId())) {
                lock.lock();
                try {
                    // Auto-accept the inbound stream
                    byteStreamSessions[0] = e.accept();
                    // If no exception occurred during stream method negotiation, notify the waiting thread.
                    byteStreamOpened.signal();
                } catch (Exception e1) {
                    negotiationExceptions.add(e1);
                } finally {
                    lock.unlock();
                }
            }
        };

        try {
            socks5ByteStreamManager.addByteStreamListener(byteStreamListener);
            inBandByteStreamManager.addByteStreamListener(byteStreamListener);

            // Send the stream initiation result.
            xmppSession.send(iq.createResult(siResponse));

            // And then wait until the peer opens the stream.
            lock.lock();
            try {
                if (!byteStreamOpened.await(xmppSession.getConfiguration().getDefaultResponseTimeout(), TimeUnit.MILLISECONDS)) {
                    throw new IOException("No byte stream could be negotiated in time.", negotiationExceptions.isEmpty() ? null : negotiationExceptions.get(0));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
            byteStreamSessions[0].setReadTimeout(xmppSession.getConfiguration().getDefaultResponseTimeout());
            return new FileTransfer(byteStreamSessions[0].getInputStream(), outputStream, fileTransferOffer.getSize());
        } finally {
            inBandByteStreamManager.removeByteStreamListener(byteStreamListener);
            socks5ByteStreamManager.removeByteStreamListener(byteStreamListener);
        }
    }

    @Override
    public void reject(AbstractIQ iq) {
        xmppSession.send(iq.createError(rocks.xmpp.core.stanza.model.errors.Condition.FORBIDDEN));
    }

    Collection<String> getSupportedStreamMethods() {
        Collection<String> allStreamMethods = new ArrayList<>(Arrays.asList(Socks5ByteStream.NAMESPACE, InBandByteStream.NAMESPACE));
        allStreamMethods.retainAll(xmppSession.getEnabledFeatures());
        return allStreamMethods;
    }

    private interface ProfileManager {
        void handle(AbstractIQ iq, StreamInitiation streamInitiation);
    }
}
