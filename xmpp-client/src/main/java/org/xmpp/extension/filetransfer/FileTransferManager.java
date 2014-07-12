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

package org.xmpp.extension.filetransfer;

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.bytestreams.ByteStreamSession;
import org.xmpp.extension.caps.EntityCapabilitiesManager;
import org.xmpp.extension.si.StreamInitiation;
import org.xmpp.extension.si.StreamInitiationManager;
import org.xmpp.extension.si.profile.filetransfer.SIFileTransfer;
import org.xmpp.stanza.client.IQ;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class FileTransferManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(FileTransferManager.class.getName());

    private final StreamInitiationManager streamInitiationManager;

    private final EntityCapabilitiesManager entityCapabilitiesManager;

    private final Set<FileTransferListener> fileTransferListeners = new CopyOnWriteArraySet<>();

    private FileTransferManager(XmppSession xmppSession) {
        super(xmppSession);
        this.streamInitiationManager = xmppSession.getExtensionManager(StreamInitiationManager.class);
        this.entityCapabilitiesManager = xmppSession.getExtensionManager(EntityCapabilitiesManager.class);
    }

    public void sendFile(File file, Jid jid, long timeout) throws XmppException, IOException {

        if (file == null) {
            throw new IllegalArgumentException("file must not be null.");
        }
        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null.");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException(new NoSuchFileException(file.getName()));
        }

        // Before a Stream Initiation is attempted the Sender should be sure that the Receiver supports both Stream Initiation and the specific profile that they wish to use.
        if (entityCapabilitiesManager.isSupported(StreamInitiation.NAMESPACE, jid) && entityCapabilitiesManager.isSupported(SIFileTransfer.NAMESPACE, jid)) {

            SIFileTransfer fileTransfer = new SIFileTransfer(file.getName(), file.length());
            String mimeType;

            try {
                mimeType = Files.probeContentType(file.toPath());
            } catch (IOException e) {
                mimeType = null;
            }

            ByteStreamSession byteStreamSession = streamInitiationManager.initiateStream(jid, fileTransfer, mimeType, timeout);
            OutputStream os = byteStreamSession.getOutputStream();
            InputStream inputStream;

            inputStream = new FileInputStream(file);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            inputStream.close();
            os.close();

        } else {
            throw new UnsupportedOperationException("Feature not supported"); // TODO other exception!?
        }
    }

    public void notifyIncomingFileTransferRequest(IQ iq, String sessionId, String mimeType, FileTransfer fileTransfer, FileTransferNegotiator fileTransferNegotiator) {
        FileTransferEvent fileTransferEvent = new FileTransferEvent(this, xmppSession, iq, sessionId, mimeType, fileTransfer, fileTransferNegotiator);
        for (FileTransferListener fileTransferListener : fileTransferListeners) {
            try {
                fileTransferListener.fileTransferRequested(fileTransferEvent);
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
}
