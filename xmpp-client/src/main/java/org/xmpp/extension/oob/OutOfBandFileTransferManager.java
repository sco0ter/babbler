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

package org.xmpp.extension.oob;

import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.filetransfer.*;
import org.xmpp.extension.oob.iq.OobIQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.errors.ItemNotFound;
import org.xmpp.stanza.errors.NotAcceptable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Date;

/**
 * @author Christian Schudt
 */
public final class OutOfBandFileTransferManager extends ExtensionManager implements FileTransferNegotiator {

    private OutOfBandFileTransferManager(final XmppSession xmppSession) {
        super(xmppSession, "jabber:iq:oob", " jabber:x:oob");

        final FileTransferManager fileTransferManager = xmppSession.getExtensionManager(FileTransferManager.class);

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {

                    OobIQ oobIQ = iq.getExtension(OobIQ.class);
                    if (oobIQ != null) {
                        final URL url = oobIQ.getUrl();
                        final String description = oobIQ.getDescription();
                        try {

                            HttpURLConnection connection = null;
                            try {
                                URLConnection urlConnection = url.openConnection();

                                // Get the header information like file length and content type.
                                if (urlConnection instanceof HttpURLConnection) {
                                    String mimeType;
                                    final long length;
                                    long lastModified;

                                    connection = (HttpURLConnection) urlConnection;
                                    connection.setRequestMethod("HEAD");
                                    connection.connect();

                                    mimeType = connection.getContentType();
                                    length = connection.getContentLengthLong();
                                    lastModified = connection.getLastModified();

                                    final Date date = lastModified > 0 ? new Date(lastModified) : null;
                                    final String name = url.toString();
                                    fileTransferManager.fileTransferOffered(iq, null, mimeType, null, new FileTransferOffer() {
                                        @Override
                                        public long getSize() {
                                            return length;
                                        }

                                        @Override
                                        public String getName() {
                                            return name;
                                        }

                                        @Override
                                        public Date getDate() {
                                            return date;
                                        }

                                        @Override
                                        public String getHash() {
                                            return null;
                                        }

                                        @Override
                                        public String getDescription() {
                                            return description;
                                        }

                                        @Override
                                        public Range getRange() {
                                            return null;
                                        }
                                    }, OutOfBandFileTransferManager.this);
                                } else {
                                    // If the URL is no HTTP URL, return a stanza error.
                                    xmppSession.send(iq.createError(new StanzaError(new NotAcceptable())));
                                }
                            } catch (ProtocolException e1) {
                                throw new IOException(e1);
                            } finally {
                                if (connection != null) {
                                    connection.disconnect();
                                }
                            }
                        } catch (IOException e1) {
                            // If the recipient attempts to retrieve the file but is unable to do so, the receiving application MUST return an <iq/> of type 'error' to the sender specifying a Not Found condition:
                            xmppSession.send(iq.createError(new StanzaError(new ItemNotFound())));
                        }
                        e.consume();
                    }
                }
            }
        });
        setEnabled(true);
    }

    @Override
    public FileTransfer accept(final IQ iq, String sessionId, FileTransferOffer fileTransferOffer, Object protocol, OutputStream outputStream) throws IOException {
        try {
            URL url = new URL(fileTransferOffer.getName());
            URLConnection urlConnection = url.openConnection();
            final FileTransfer fileTransfer = new FileTransfer(urlConnection.getInputStream(), outputStream, fileTransferOffer.getSize());
            fileTransfer.addFileTransferStatusListener(new FileTransferStatusListener() {
                @Override
                public void fileTransferStatusChanged(FileTransferStatusEvent e) {
                    if (e.getStatus() == FileTransfer.Status.COMPLETED) {
                        xmppSession.send(iq.createResult());
                        fileTransfer.removeFileTransferStatusListener(this);
                    } else if (e.getStatus() == FileTransfer.Status.CANCELED ||
                            e.getStatus() == FileTransfer.Status.FAILED) {
                        xmppSession.send(iq.createError(new StanzaError(new ItemNotFound())));
                        fileTransfer.removeFileTransferStatusListener(this);
                    }
                }
            });
            return fileTransfer;
        } catch (MalformedURLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void reject(IQ iq) {
        // If the recipient rejects the request outright, the receiving application MUST return an <iq/> of type 'error' to the sender specifying a Not Acceptable condition:
        xmppSession.send(iq.createError(new StanzaError(new NotAcceptable())));
    }
}
