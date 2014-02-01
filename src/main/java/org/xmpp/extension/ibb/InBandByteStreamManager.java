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

package org.xmpp.extension.ibb;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.Stanza;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public class InBandByteStreamManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(InBandByteStreamManager.class.getName());

    final Set<IbbListener> ibbListeners = new CopyOnWriteArraySet<>();

    private Map<String, IbbSession> ibbSessionMap = new ConcurrentHashMap<>();

    private InBandByteStreamManager(final Connection connection) {
        super(connection, "http://jabber.org/protocol/ibb");

        connection.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                if (e.isIncoming()) {
                    IQ iq = e.getIQ();
                    // Check, if the IQ carries some IBB related payload.
                    if (iq.getType() == IQ.Type.SET) {
                        Data data = iq.getExtension(Data.class);
                        if (data != null) {
                            if (isEnabled()) {
                                IbbSession ibbSession = getIbbSession(iq, data.getSessionId());
                                if (ibbSession != null) {
                                    if (ibbSession.handleData(data)) {
                                        connection.send(iq.createResult());
                                    } else {
                                        // 2. Because the sequence number has already been used, the recipient returns an <unexpected-request/> error with a type of 'cancel'.
                                        connection.send(iq.createError(new Stanza.Error(Stanza.Error.Type.CANCEL, new Stanza.Error.UnexpectedRequest())));
                                    }
                                }
                            } else {
                                sendServiceUnavailable(iq);
                            }
                            System.out.println("Data");
                            return;
                        }
                        Open open = iq.getExtension(Open.class);
                        if (open != null) {
                            if (isEnabled()) {
                                // Somebody wants to create a IBB session with me.
                                // Notify the listeners.
                                for (IbbListener ibbListener : ibbListeners) {
                                    try {
                                        ibbListener.streamRequested(new IbbEvent(InBandByteStreamManager.this, connection, iq, iq.getExtension(Open.class)));
                                    } catch (Exception exc) {
                                        logger.log(Level.WARNING, exc.getMessage(), exc);
                                    }
                                }
                            } else {
                                sendServiceUnavailable(iq);
                            }
                            return;
                        }
                        Close close = iq.getExtension(Close.class);
                        if (close != null) {
                            if (isEnabled()) {
                                IbbSession ibbSession = getIbbSession(iq, close.getSessionId());
                                if (ibbSession != null) {
                                    try {
                                        ibbSessionMap.remove(close.getSessionId());
                                        ibbSession.closedByPeer();
                                    } catch (IOException e1) {
                                        // logger.warning(e1.);
                                    } finally {
                                        connection.send(iq.createResult());
                                    }
                                }
                            } else {
                                sendServiceUnavailable(iq);
                            }
                            System.out.println("Close");
                        }
                    }
                }
            }
        });
        setEnabled(true);
    }

    private IbbSession getIbbSession(IQ iq, String sessionId) {
        // Check, if the session id is known.
        IbbSession ibbSession = ibbSessionMap.get(sessionId);
        if (ibbSession == null) {
            // 1. Because the session ID is unknown, the recipient returns an <item-not-found/> error with a type of 'cancel'.
            connection.send(iq.createError(new Stanza.Error(Stanza.Error.Type.CANCEL, new Stanza.Error.ItemNotFound())));
        }
        return ibbSession;
    }

    public IbbSession createInBandByteStream(Jid jid, int blockSize) {
        IbbSession ibbSession = new IbbSession(connection, jid, blockSize);
        ibbSessionMap.put(ibbSession.getSessionId(), ibbSession);
        return ibbSession;
    }

    IbbSession createInBandByteStream(Jid jid, int blockSize, String sessionId) {
        IbbSession ibbSession = new IbbSession(connection, jid, blockSize, sessionId);
        ibbSessionMap.put(ibbSession.getSessionId(), ibbSession);
        return ibbSession;
    }

    /**
     * Adds a IBB listener, which allows to listen for incoming IBB requests.
     *
     * @param ibbListener The listener.
     * @see #removeIbbListener(IbbListener)
     */
    public void addIbbListener(IbbListener ibbListener) {
        ibbListeners.add(ibbListener);
    }

    /**
     * Removes a previously added IBB listener.
     *
     * @param ibbListener The listener.
     * @see #addIbbListener(IbbListener)
     */
    public void removeIbbListener(IbbListener ibbListener) {
        ibbListeners.remove(ibbListener);
    }
}
