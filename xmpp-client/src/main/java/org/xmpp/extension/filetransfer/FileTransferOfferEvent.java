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
import org.xmpp.stanza.client.IQ;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.EventObject;

/**
 * @author Christian Schudt
 */
public final class FileTransferOfferEvent extends EventObject implements FileTransferOffer {

    private final FileTransferOffer fileTransferOffer;

    private final IQ iq;

    private final String mimeType;

    private final String sessionId;

    private final FileTransferNegotiator fileTransferNegotiator;

    private final Object protocol;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    FileTransferOfferEvent(Object source, IQ iq, String sessionId, String mimeType, FileTransferOffer fileTransferOffer, Object protocol, FileTransferNegotiator fileTransferNegotiator) {
        super(source);
        this.sessionId = sessionId;
        this.iq = iq;
        this.fileTransferOffer = fileTransferOffer;
        this.mimeType = mimeType;
        this.fileTransferNegotiator = fileTransferNegotiator;
        this.protocol = protocol;
    }

    /**
     * Gets the mime type of the file.
     *
     * @return The mime type.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Gets the initiator.
     *
     * @return The initiator.
     */
    public Jid getInitiator() {
        return iq.getFrom();
    }

    /**
     * Accepts the incoming file transfer request.
     * After accepting the file transfer you should call {@link FileTransfer#transfer()} in order to start the transfer.
     *
     * @param outputStream The output stream, to which the file will be written.
     * @return The file transfer object.
     * @throws IOException If the byte stream session could not be established.
     */
    public FileTransfer accept(OutputStream outputStream) throws IOException {
        return fileTransferNegotiator.accept(iq, sessionId, fileTransferOffer, protocol, outputStream);
    }

    /**
     * Rejects the incoming file transfer request.
     */
    public void reject() {
        fileTransferNegotiator.reject(iq);
    }

    @Override
    public long getSize() {
        return fileTransferOffer.getSize();
    }

    @Override
    public String getName() {
        return fileTransferOffer.getName();
    }

    @Override
    public Date getDate() {
        return fileTransferOffer.getDate();
    }

    @Override
    public String getHash() {
        return fileTransferOffer.getHash();
    }

    @Override
    public String getDescription() {
        return fileTransferOffer.getDescription();
    }

    @Override
    public Range getRange() {
        return fileTransferOffer.getRange();
    }

    public String getSessionId() {
        return sessionId;
    }
}
