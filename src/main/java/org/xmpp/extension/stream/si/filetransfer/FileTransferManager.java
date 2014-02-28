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

package org.xmpp.extension.stream.si.filetransfer;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.featureneg.FeatureNegotiation;
import org.xmpp.extension.stream.ibb.IbbSession;
import org.xmpp.extension.stream.ibb.InBandBytestreamManager;
import org.xmpp.extension.stream.si.BadProfile;
import org.xmpp.extension.stream.si.StreamInitiation;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.Stanza;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class FileTransferManager extends ExtensionManager {

    static final String PROFILE = "http://jabber.org/protocol/si/profile/file-transfer";

    private static final Logger logger = Logger.getLogger(FileTransferManager.class.getName());

    private final Set<FileTransferListener> fileTransferListeners = new CopyOnWriteArraySet<>();

    private FileTransferManager(final Connection connection) {
        super(connection, "http://jabber.org/protocol/si", PROFILE);

        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && iq.getType() == IQ.Type.SET) {
                    StreamInitiation streamInitiation = iq.getExtension(StreamInitiation.class);
                    if (streamInitiation != null && streamInitiation.getProfile() != null && streamInitiation.getProfile().equals(PROFILE)) {
                        Object profileElement = streamInitiation.getProfileElement();
                        if (profileElement instanceof FileTransfer) {
                            if (fileTransferListeners.isEmpty()) {
                                sendServiceUnavailable(iq);
                            } else {
                                FileTransfer fileTransfer = (FileTransfer) profileElement;
                                notifyIncomingFileTransferRequest(iq, fileTransfer);
                            }
                        } else {
                            Stanza.Error error = new Stanza.Error(Stanza.Error.Type.MODIFY, new Stanza.Error.BadRequest());
                            error.setExtension(new BadProfile());
                            IQ result = iq.createError(error);
                            connection.send(result);
                        }
                    }
                }
            }
        });
    }

    private void notifyIncomingFileTransferRequest(IQ iq, FileTransfer fileTransfer) {
        FileTransferEvent fileTransferEvent = new FileTransferEvent(this, connection, iq, fileTransfer);
        for (FileTransferListener fileTransferListener : fileTransferListeners) {
            try {
                fileTransferListener.fileTransferRequest(fileTransferEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Adds a file transfer listener, which allows to listen for incoming file transfer requests.
     *
     * @param fileTransferListener The listener.
     * @see #removeFileTransferListener(FileTransferListener)
     */
    public void addFileTransferListener(FileTransferListener fileTransferListener) {
        fileTransferListeners.add(fileTransferListener);
    }

    /**
     * Removes a previously added file transfer listener.
     *
     * @param fileTransferListener The listener.
     * @see #addFileTransferListener(FileTransferListener)
     */
    public void removeFileTransferListener(FileTransferListener fileTransferListener) {
        fileTransferListeners.remove(fileTransferListener);
    }

    public void sendFile(File file, Jid jid, long timeout) throws XmppException {

        if (file == null) {
            throw new IllegalArgumentException("file must not be null.");
        }
        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null.");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException(new NoSuchFileException(file.getName()));
        }

        FileTransfer fileTransfer = new FileTransfer(file.getName(), file.length());

        DataForm dataForm = new DataForm(DataForm.Type.FORM);
        DataForm.Field field = new DataForm.Field(DataForm.Field.Type.LIST_SINGLE, "stream-method");
        field.getOptions().add(new DataForm.Option(InBandBytestreamManager.NAMESPACE));
        dataForm.getFields().add(field);

        FeatureNegotiation featureNegotiation = new FeatureNegotiation(dataForm);

        String mimeType;

        String id = UUID.randomUUID().toString();

        try {
            mimeType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            mimeType = null;
        }
        StreamInitiation streamInitiation = new StreamInitiation(id, PROFILE, mimeType, fileTransfer, featureNegotiation);

        IQ iq = new IQ(jid, IQ.Type.SET, streamInitiation);

        IQ result = connection.query(iq, timeout);

        InBandBytestreamManager ibbManager = connection.getExtensionManager(InBandBytestreamManager.class);
        IbbSession ibbSession = ibbManager.createInBandByteStream(jid, 4096);
        ibbSession.open();

        OutputStream os = ibbSession.getOutputStream();
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(file);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            inputStream.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
