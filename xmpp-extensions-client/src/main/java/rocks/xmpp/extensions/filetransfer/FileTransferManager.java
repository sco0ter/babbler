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

package rocks.xmpp.extensions.filetransfer;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.SessionStatusListener;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.model.StanzaException;
import rocks.xmpp.core.stanza.model.client.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;
import rocks.xmpp.extensions.oob.model.iq.OobIQ;
import rocks.xmpp.extensions.si.StreamInitiationManager;
import rocks.xmpp.extensions.si.model.StreamInitiation;
import rocks.xmpp.extensions.si.profile.filetransfer.model.SIFileTransferOffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public final class FileTransferManager extends ExtensionManager implements SessionStatusListener {

    private static final Logger logger = Logger.getLogger(FileTransferManager.class.getName());

    private final StreamInitiationManager streamInitiationManager;

    private final EntityCapabilitiesManager entityCapabilitiesManager;

    private final Set<FileTransferOfferListener> fileTransferOfferListeners = new CopyOnWriteArraySet<>();

    private final ExecutorService fileTransferOfferExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "File Transfer Offer Thread");
            thread.setDaemon(true);
            return thread;
        }
    });

    private FileTransferManager(final XmppSession xmppSession) {
        super(xmppSession);
        xmppSession.addSessionStatusListener(this);
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
     * @throws FileTransferRejectedException                If the recipient rejected the file.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the user is unavailable or failed to download the file.
     * @throws rocks.xmpp.core.session.NoResponseException  If the recipient did not downloaded the file within the timeout.
     * @see <a href="http://xmpp.org/extensions/xep-0066.html">XEP-0066: Out of Band Data</a>
     */
    public void offerFile(URL url, String description, Jid recipient, long timeout) throws XmppException {
        try {
            xmppSession.query(new IQ(recipient, IQ.Type.SET, new OobIQ(url, description)), timeout);
        } catch (StanzaException e) {
            if (e.getStanza().getError().getCondition() == Condition.NOT_ACCEPTABLE) {
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
     * @param recipient   The recipient's JID (must be a <em>full</em> JID, i. e. including {@code resource}).
     * @param timeout     The timeout (indicates how long to wait until the file offer has either been accepted or rejected).
     * @return The file transfer object.
     * @throws FileTransferRejectedException                If the recipient rejected the file.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the user is unavailable or failed to download the file.
     * @throws rocks.xmpp.core.session.NoResponseException  If the recipient did not downloaded the file within the timeout.
     * @throws java.io.IOException                          If the file could not be read.
     */
    public FileTransfer offerFile(File file, String description, Jid recipient, long timeout) throws XmppException, IOException {
        Objects.requireNonNull(file, "file must not be null. ");
        Objects.requireNonNull(recipient, "jid must not be null.");

        if (!recipient.isFullJid())
            throw new IllegalArgumentException("recipient must be a full JID (including resource)");

        if (!file.exists()) {
            throw new FileNotFoundException(file.getName());
        }

        // Before a Stream Initiation is attempted the Sender should be sure that the Receiver supports both Stream Initiation and the specific profile that they wish to use.
        if (entityCapabilitiesManager.isSupported(StreamInitiation.NAMESPACE, recipient) && entityCapabilitiesManager.isSupported(SIFileTransferOffer.NAMESPACE, recipient)) {

            SIFileTransferOffer fileTransfer = new SIFileTransferOffer(file.getName(), file.length(), new Date(file.lastModified()), null, description, null);
            String mimeType;

            try {
                mimeType = URLConnection.guessContentTypeFromStream(new BufferedInputStream(new FileInputStream(file))); //Files.probeContentType(file.toPath());
            } catch (IOException e) {
                mimeType = null;
            }
            try {
                OutputStream outputStream = streamInitiationManager.initiateStream(recipient, fileTransfer, mimeType, timeout);
                InputStream inputStream = new FileInputStream(file);
                return new FileTransfer(inputStream, outputStream, file.length());
            } catch (StanzaException e) {
                if (e.getStanza().getError().getCondition() == Condition.FORBIDDEN) {
                    throw new FileTransferRejectedException();
                } else {
                    throw e;
                }
            }
        }
        throw new UnsupportedOperationException("Feature not supported"); // TODO other exception!?
    }

    public void fileTransferOffered(final IQ iq, final String sessionId, final String mimeType, final FileTransferOffer fileTransferOffer, final Object protocol, final FileTransferNegotiator fileTransferNegotiator) {
        fileTransferOfferExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FileTransferOfferEvent fileTransferRequestEvent = new FileTransferOfferEvent(this, iq, sessionId, mimeType, fileTransferOffer, protocol, fileTransferNegotiator);
                for (FileTransferOfferListener fileTransferOfferListener : fileTransferOfferListeners) {
                    try {
                        fileTransferOfferListener.fileTransferOffered(fileTransferRequestEvent);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        });
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

    @Override
    public void sessionStatusChanged(SessionStatusEvent e) {
        if (e.getStatus() == XmppSession.Status.CLOSED) {
            fileTransferOfferListeners.clear();
            fileTransferOfferExecutor.shutdown();
        }
    }
}
