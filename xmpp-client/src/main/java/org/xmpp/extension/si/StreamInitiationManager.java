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

package org.xmpp.extension.si;

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.bytestreams.ByteStreamEvent;
import org.xmpp.extension.bytestreams.ByteStreamListener;
import org.xmpp.extension.bytestreams.ByteStreamSession;
import org.xmpp.extension.bytestreams.ibb.InBandByteStreamManager;
import org.xmpp.extension.bytestreams.s5b.Socks5ByteStreamManager;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.featureneg.FeatureNegotiation;
import org.xmpp.extension.filetransfer.FileTransfer;
import org.xmpp.extension.filetransfer.FileTransferManager;
import org.xmpp.extension.filetransfer.FileTransferNegotiator;
import org.xmpp.extension.filetransfer.FileTransferOffer;
import org.xmpp.extension.si.profile.filetransfer.SIFileTransferOffer;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.errors.BadRequest;
import org.xmpp.stanza.errors.Forbidden;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Christian Schudt
 */
public final class StreamInitiationManager extends ExtensionManager implements FileTransferNegotiator {

    private static final String STREAM_METHOD = "stream-method";

    private final Collection<String> supportedStreamMethod = new ArrayList<>(Arrays.asList(Socks5ByteStreamManager.NAMESPACE, InBandByteStreamManager.NAMESPACE));

    private final Map<String, ProfileManager> profileManagers = new ConcurrentHashMap<>();

    private StreamInitiationManager(final XmppSession xmppSession) {
        super(xmppSession, StreamInitiation.NAMESPACE, SIFileTransferOffer.NAMESPACE);

        // Currently, there's only one profile in XMPP, namely XEP-0096 SI File Transfer.
        profileManagers.put(SIFileTransferOffer.NAMESPACE, new ProfileManager() {
            @Override
            public void handle(IQ iq, StreamInitiation streamInitiation) {
                FileTransferManager fileTransferManager = xmppSession.getExtensionManager(FileTransferManager.class);
                fileTransferManager.fileTransferOffered(iq, streamInitiation.getId(), streamInitiation.getMimeType(), (FileTransferOffer) streamInitiation.getProfileElement(), StreamInitiationManager.this);
            }
        });

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {
                    StreamInitiation streamInitiation = iq.getExtension(StreamInitiation.class);
                    if (streamInitiation != null) {
                        FeatureNegotiation featureNegotiation = streamInitiation.getFeatureNegotiation();
                        // Assume no valid streams by default, unless valid streams are found.
                        boolean noValidStreams = true;
                        if (featureNegotiation != null) {
                            DataForm dataForm = featureNegotiation.getDataForm();
                            if (dataForm != null) {
                                DataForm.Field field = dataForm.findField(STREAM_METHOD);
                                if (field != null) {
                                    List<String> streamMethods = new ArrayList<>();
                                    for (DataForm.Option option : field.getOptions()) {
                                        streamMethods.add(option.getValue());
                                    }
                                    if (!Collections.disjoint(streamMethods, supportedStreamMethod)) {
                                        // Request contains valid streams
                                        noValidStreams = false;
                                    }
                                }
                            }
                        }
                        if (noValidStreams) {
                            StanzaError error = new StanzaError(new BadRequest());
                            error.setExtension(new NoValidStreams());
                            xmppSession.send(iq.createError(error));
                        } else {
                            ProfileManager profileManager = profileManagers.get(streamInitiation.getProfile());

                            if (profileManager == null) {
                                StanzaError error = new StanzaError(new BadRequest());
                                error.setExtension(new BadProfile());
                                xmppSession.send(iq.createError(error));
                            } else {
                                profileManager.handle(iq, streamInitiation);
                            }
                        }
                        e.consume();
                    }
                }
            }
        });
        setEnabled(true);
    }

    /**
     * Initiates a stream with another entity.
     *
     * @param receiver The receiver, i.e. the XMPP entity you want to negotiate a stream.
     * @param profile  The profile. Currently there's only the {@link org.xmpp.extension.si.profile.filetransfer.SIFileTransferOffer} profile.
     * @param mimeType The mime type of the stream.
     * @param timeout  The timeout, which wait until the stream has been negotiated.
     * @return The byte stream session which has been negotiated.
     * @throws XmppException
     */
    public OutputStream initiateStream(Jid receiver, SIFileTransferOffer profile, String mimeType, long timeout) throws XmppException, IOException {

        // Create a random id for the stream session.
        String sessionId = UUID.randomUUID().toString();

        // Offer stream methods.
        DataForm dataForm = new DataForm(DataForm.Type.FORM);
        DataForm.Field field = new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, STREAM_METHOD);
        for (String streamMethod : supportedStreamMethod) {
            field.getOptions().add(new DataForm.Option(streamMethod));
        }
        dataForm.getFields().add(field);

        // Offer the file to the recipient and wait until it's accepted.
        IQ result = xmppSession.query(new IQ(receiver, IQ.Type.SET, new StreamInitiation(sessionId, SIFileTransferOffer.NAMESPACE, mimeType, profile, new FeatureNegotiation(dataForm))), timeout);

        // The recipient must response with a stream initiation.
        StreamInitiation streamInitiation = result.getExtension(StreamInitiation.class);

        FeatureNegotiation featureNegotiation = streamInitiation.getFeatureNegotiation();
        // Get the stream method which has been chosen by the recipient.
        String streamMethod = featureNegotiation.getDataForm().findField(STREAM_METHOD).getValues().get(0);

        ByteStreamSession byteStreamSession;
        // Choose the stream method to be used based on the recipient's choice.
        if (streamMethod.equals(InBandByteStreamManager.NAMESPACE)) {
            InBandByteStreamManager inBandBytestreamManager = xmppSession.getExtensionManager(InBandByteStreamManager.class);
            byteStreamSession = inBandBytestreamManager.initiateSession(receiver, sessionId, 4096);
        } else if (streamMethod.equals(Socks5ByteStreamManager.NAMESPACE)) {
            Socks5ByteStreamManager socks5ByteStreamManager = xmppSession.getExtensionManager(Socks5ByteStreamManager.class);
            byteStreamSession = socks5ByteStreamManager.initiateSession(receiver, sessionId, socks5ByteStreamManager.discoverProxies());
        } else {
            throw new IOException("Receiver returned unsupported stream method.");
        }
        return byteStreamSession.getOutputStream();
    }

    @Override
    public FileTransfer accept(IQ iq, final String sessionId, FileTransferOffer fileTransferOffer, OutputStream outputStream) throws IOException {
        DataForm dataForm = new DataForm(DataForm.Type.SUBMIT);
        DataForm.Field field = new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, STREAM_METHOD);
        field.getValues().add(InBandByteStreamManager.NAMESPACE);
        dataForm.getFields().add(field);
        StreamInitiation streamInitiation = new StreamInitiation(new FeatureNegotiation(dataForm));

        final InBandByteStreamManager inBandBytestreamManager = xmppSession.getExtensionManager(InBandByteStreamManager.class);
        final ByteStreamSession[] byteStreamSessions = new ByteStreamSession[1];
        final Lock lock = new ReentrantLock();
        final Condition byteStreamOpened = lock.newCondition();
        inBandBytestreamManager.addByteStreamListener(new ByteStreamListener() {
            @Override
            public void byteStreamRequested(ByteStreamEvent e) {
                if (sessionId.equals(e.getSessionId())) {
                    lock.lock();
                    try {
                        byteStreamSessions[0] = e.accept();
                        byteStreamOpened.signalAll();
                    } finally {
                        inBandBytestreamManager.removeByteStreamListener(this);
                        lock.unlock();
                    }
                }
            }
        });

        // Send the stream initiation result.
        IQ result = iq.createResult();
        result.setExtension(streamInitiation);
        xmppSession.send(result);

        // And then wait until the peer opens the stream.
        lock.lock();
        try {
            if (!byteStreamOpened.await(5, TimeUnit.SECONDS)) {
                throw new IOException("No byte stream was initiated in time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
        return new FileTransfer(byteStreamSessions[0].getInputStream(), outputStream, fileTransferOffer.getSize());
    }

    @Override
    public void reject(IQ iq) {
        xmppSession.send(iq.createError(new StanzaError(new Forbidden())));
    }

    private interface ProfileManager {
        void handle(IQ iq, StreamInitiation streamInitiation);
    }
}
