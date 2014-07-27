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

import org.xmpp.*;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.caps.EntityCapabilitiesManager;
import org.xmpp.extension.oob.iq.OobIQ;
import org.xmpp.extension.si.StreamInitiation;
import org.xmpp.extension.si.StreamInitiationManager;
import org.xmpp.extension.si.profile.filetransfer.SIFileTransferOffer;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.errors.Forbidden;
import org.xmpp.stanza.errors.NotAcceptable;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Date;
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

    private final Set<FileTransferOfferListener> fileTransferOfferListeners = new CopyOnWriteArraySet<>();

    private FileTransferManager(final XmppSession xmppSession) {
        super(xmppSession);
        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    fileTransferOfferListeners.clear();
                }
            }
        });
        this.streamInitiationManager = xmppSession.getExtensionManager(StreamInitiationManager.class);
        this.entityCapabilitiesManager = xmppSession.getExtensionManager(EntityCapabilitiesManager.class);
    }

    /**
     * Offers a file to another user in form of an URL. The file can be downloaded by the recipient via an HTTP GET request.
     * If this method returns without exception you can assume, that the file has been successfully downloaded by the recipient.
     *
     * @param url         The URL of the file.
     * @param description The description of the file.
     * @param recipient   The recipient's JID (must be a full JID).
     * @param timeout     The timeout (indicates how long to wait until the file has been downloaded).
     * @throws FileTransferRejectedException If the recipient rejected the file.
     * @throws StanzaException               If the user is unavailable or failed to download the file.
     * @throws NoResponseException           If the recipient did not downloaded the file within the timeout.
     * @see <a href="http://xmpp.org/extensions/xep-0066.html">XEP-0066: Out of Band Data</a>
     */
    public void offerFile(URL url, String description, Jid recipient, long timeout) throws XmppException {
        try {
            xmppSession.query(new IQ(recipient, IQ.Type.SET, new OobIQ(url, description)), timeout);
        } catch (StanzaException e) {
            if (e.getStanza().getError().getCondition() instanceof NotAcceptable) {
                throw new FileTransferRejectedException();
            } else {
                throw e;
            }
        }
    }

    /**
     * Offers a file to another user. If this method returns successfully, the recipient has accepted the offer, the stream method has been negotiated and the file is ready to be transferred.
     * Call {@link FileTransfer#transfer()} to start the file transfer.
     *
     * @param file        The file.
     * @param description The description of the file.
     * @param recipient   The recipient's JID (must be a full JID).
     * @param timeout     The timeout (indicates how long to wait until the file offer has either been accepted or rejected).
     * @return The file transfer object.
     * @throws FileTransferRejectedException If the recipient rejected the file.
     * @throws StanzaException               If the user is unavailable or failed to download the file.
     * @throws NoResponseException           If the recipient did not downloaded the file within the timeout.
     * @throws IOException                   If the file could not be read.
     */
    public FileTransfer offerFile(File file, String description, Jid recipient, long timeout) throws XmppException, IOException {

        if (file == null) {
            throw new IllegalArgumentException("file must not be null.");
        }
        if (recipient == null) {
            throw new IllegalArgumentException("jid must not be null.");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException(new NoSuchFileException(file.getName()));
        }

        // Before a Stream Initiation is attempted the Sender should be sure that the Receiver supports both Stream Initiation and the specific profile that they wish to use.
        if (entityCapabilitiesManager.isSupported(StreamInitiation.NAMESPACE, recipient) && entityCapabilitiesManager.isSupported(SIFileTransferOffer.NAMESPACE, recipient)) {

            SIFileTransferOffer fileTransfer = new SIFileTransferOffer(file.getName(), file.length(), new Date(file.lastModified()), null, description, null);
            String mimeType;

            try {
                mimeType = Files.probeContentType(file.toPath());
            } catch (IOException e) {
                mimeType = null;
            }
            try {
                OutputStream outputStream = streamInitiationManager.initiateStream(recipient, fileTransfer, mimeType, timeout);
                InputStream inputStream = new FileInputStream(file);
                return new FileTransfer(inputStream, outputStream, file.length());
            } catch (StanzaException e) {
                if (e.getStanza().getError().getCondition() instanceof Forbidden) {
                    throw new FileTransferRejectedException();
                } else {
                    throw e;
                }
            }
        } else {

        }
        throw new UnsupportedOperationException("Feature not supported"); // TODO other exception!?
    }

    public void fileTransferOffered(IQ iq, String sessionId, String mimeType, FileTransferOffer fileTransferOffer, Object protocol, FileTransferNegotiator fileTransferNegotiator) {
        FileTransferOfferEvent fileTransferRequestEvent = new FileTransferOfferEvent(this, iq, sessionId, mimeType, fileTransferOffer, protocol, fileTransferNegotiator);
        for (FileTransferOfferListener fileTransferOfferListener : fileTransferOfferListeners) {
            try {
                fileTransferOfferListener.fileTransferOffered(fileTransferRequestEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Adds a file transfer listener, which allows to listen for incoming file transfer requests.
     *
     * @param fileTransferOfferListener The listener.
     * @see #removeFileTransferOfferListener(FileTransferOfferListener)
     */
    public void addFileTransferOfferListener(FileTransferOfferListener fileTransferOfferListener) {
        fileTransferOfferListeners.add(fileTransferOfferListener);
    }

    /**
     * Removes a previously added file transfer listener.
     *
     * @param fileTransferOfferListener The listener.
     * @see #addFileTransferOfferListener(FileTransferOfferListener)
     */
    public void removeFileTransferOfferListener(FileTransferOfferListener fileTransferOfferListener) {
        fileTransferOfferListeners.remove(fileTransferOfferListener);
    }
}
