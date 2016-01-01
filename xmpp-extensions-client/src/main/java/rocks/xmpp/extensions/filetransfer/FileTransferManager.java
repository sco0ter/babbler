/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Manager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;
import rocks.xmpp.extensions.oob.model.iq.OobIQ;
import rocks.xmpp.extensions.si.StreamInitiationManager;
import rocks.xmpp.extensions.si.model.StreamInitiation;
import rocks.xmpp.extensions.si.profile.filetransfer.model.SIFileTransferOffer;
import rocks.xmpp.util.XmppUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * @author Christian Schudt
 */
public final class FileTransferManager extends Manager {

    private final StreamInitiationManager streamInitiationManager;

    private final EntityCapabilitiesManager entityCapabilitiesManager;

    private final Set<Consumer<FileTransferOfferEvent>> fileTransferOfferListeners = new CopyOnWriteArraySet<>();

    private final ExecutorService fileTransferOfferExecutor = Executors.newCachedThreadPool(XmppUtils.createNamedThreadFactory("File Transfer Offer Thread"));

    private FileTransferManager(final XmppSession xmppSession) {
        super(xmppSession, true);
        this.streamInitiationManager = xmppSession.getManager(StreamInitiationManager.class);
        this.entityCapabilitiesManager = xmppSession.getManager(EntityCapabilitiesManager.class);
    }

    /**
     * Offers a file to another user in form of an URL. The file can be downloaded by the recipient via an HTTP GET request.
     * If this method returns without exception you can assume, that the file has been successfully downloaded by the recipient.
     *
     * @param url         The URL of the file.
     * @param description The description of the file.
     * @param recipient   The recipient's JID (must be a full JID).
     * @param timeout     The timeout (indicates how long to wait until the file has been downloaded).
     * @throws FileTransferRejectedException               If the recipient rejected the file.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the user is unavailable or failed to download the file.
     * @throws rocks.xmpp.core.session.NoResponseException If the recipient did not downloaded the file within the timeout.
     * @see <a href="http://xmpp.org/extensions/xep-0066.html">XEP-0066: Out of Band Data</a>
     */
    public void offerFile(URL url, String description, Jid recipient, long timeout) throws XmppException {
        try {
            xmppSession.query(IQ.set(recipient, new OobIQ(url, description)), timeout);
        } catch (StanzaException e) {
            if (e.getCondition() == Condition.NOT_ACCEPTABLE) {
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
     * @throws FileTransferRejectedException               If the recipient rejected the file.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the user is unavailable or failed to download the file.
     * @throws rocks.xmpp.core.session.NoResponseException If the recipient did not downloaded the file within the timeout.
     * @throws java.io.IOException                         If the file could not be read.
     */
    public final FileTransfer offerFile(final File file, final String description, final Jid recipient, final long timeout) throws XmppException, IOException {
        return offerFile(requireNonNull(file, "file must not be null.").toPath(), description, recipient, timeout);
    }

    /**
     * Offers a file to another user. If this method returns successfully, the recipient has accepted the offer, the stream method has been negotiated and the file is ready to be transferred.
     * Call {@link FileTransfer#transfer()} to start the file transfer.
     *
     * @param source      The file.
     * @param description The description of the file.
     * @param recipient   The recipient's JID (must be a <em>full</em> JID, i. e. including {@code resource}).
     * @param timeout     The timeout (indicates how long to wait until the file offer has either been accepted or rejected).
     * @return The file transfer object.
     * @throws FileTransferRejectedException               If the recipient rejected the file.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the user is unavailable or failed to download the file.
     * @throws rocks.xmpp.core.session.NoResponseException If the recipient did not downloaded the file within the timeout.
     * @throws java.io.IOException                         If the file could not be read.
     */
    public final FileTransfer offerFile(final Path source, final String description, final Jid recipient, final long timeout) throws XmppException, IOException {
        if (Files.notExists(requireNonNull(source, "source must not be null."))) {
            throw new NoSuchFileException(source.getFileName().toString());
        }
        return offerFile(Files.newInputStream(source), source.getFileName().toString(), Files.size(source), Files.getLastModifiedTime(source).toInstant(), description, recipient, timeout);
    }

    /**
     * Offers a stream to another user. If this method returns successfully, the recipient has accepted the offer, the stream method has been negotiated and the file is ready to be transferred.
     * Call {@link FileTransfer#transfer()} to start the file transfer.
     *
     * @param source       The stream.
     * @param fileName     The file name.
     * @param fileSize     The file size.
     * @param lastModified The last modified date.
     * @param description  The description of the file.
     * @param recipient    The recipient's JID (must be a <em>full</em> JID, i. e. including {@code resource}).
     * @param timeout      The timeout (indicates how long to wait until the file offer has either been accepted or rejected).
     * @return The file transfer object.
     * @throws FileTransferRejectedException               If the recipient rejected the file.
     * @throws rocks.xmpp.core.stanza.StanzaException      If the user is unavailable or failed to download the file.
     * @throws rocks.xmpp.core.session.NoResponseException If the recipient did not downloaded the file within the timeout.
     * @throws java.io.IOException                         If the file could not be read.
     */
    public final FileTransfer offerFile(final InputStream source, final String fileName, final long fileSize, Instant lastModified, final String description, final Jid recipient, final long timeout) throws XmppException, IOException {
        if (!requireNonNull(recipient, "jid must not be null.").isFullJid()) {
            throw new IllegalArgumentException("recipient must be a full JID (including resource)");
        }

        // Before a Stream Initiation is attempted the Sender should be sure that the Receiver supports both Stream Initiation and the specific profile that they wish to use.
        if (!(this.entityCapabilitiesManager.isSupported(StreamInitiation.NAMESPACE, recipient) && this.entityCapabilitiesManager.isSupported(SIFileTransferOffer.NAMESPACE, recipient))) {
            throw new XmppException("Feature not supported");
        }

        final SIFileTransferOffer fileTransfer = new SIFileTransferOffer(fileName, fileSize, lastModified, null, description, null);
        final String mimeType = URLConnection.guessContentTypeFromStream(source); // Files.probeContentType(file.toPath());

        try {
            final OutputStream outputStream = this.streamInitiationManager.initiateStream(recipient, fileTransfer, mimeType, timeout);
            return new FileTransfer(source, outputStream, fileSize);
        } catch (final StanzaException e) {
            if (e.getCondition() == Condition.FORBIDDEN) {
                throw new FileTransferRejectedException();
            } else {
                throw e;
            }
        }
    }

    public void fileTransferOffered(final IQ iq, final String sessionId, final String mimeType, final FileTransferOffer fileTransferOffer, final Object protocol, final FileTransferNegotiator fileTransferNegotiator) {
        fileTransferOfferExecutor.execute(() -> XmppUtils.notifyEventListeners(fileTransferOfferListeners, new FileTransferOfferEvent(this, iq, sessionId, mimeType, fileTransferOffer, protocol, fileTransferNegotiator)));
    }

    /**
     * Adds a file transfer listener, which allows to listen for inbound file transfer requests.
     *
     * @param fileTransferOfferListener The listener.
     * @see #removeFileTransferOfferListener(Consumer)
     */
    public void addFileTransferOfferListener(Consumer<FileTransferOfferEvent> fileTransferOfferListener) {
        fileTransferOfferListeners.add(fileTransferOfferListener);
    }

    /**
     * Removes a previously added file transfer listener.
     *
     * @param fileTransferOfferListener The listener.
     * @see #addFileTransferOfferListener(Consumer)
     */
    public void removeFileTransferOfferListener(Consumer<FileTransferOfferEvent> fileTransferOfferListener) {
        fileTransferOfferListeners.remove(fileTransferOfferListener);
    }

    @Override
    protected void dispose() {
        fileTransferOfferListeners.clear();
        fileTransferOfferExecutor.shutdown();
    }
}
