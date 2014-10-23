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

package rocks.xmpp.extensions.compress;

import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.StreamFeatureListener;
import rocks.xmpp.core.stream.StreamFeatureNegotiator;
import rocks.xmpp.extensions.compress.model.Compress;
import rocks.xmpp.extensions.compress.model.Compressed;
import rocks.xmpp.extensions.compress.model.CompressionMethod;
import rocks.xmpp.extensions.compress.model.Failure;
import rocks.xmpp.extensions.compress.model.feature.Compression;

/**
 * Manages stream compression as described in <a href="http://xmpp.org/extensions/xep-0138.html">XEP-0138: Stream Compression</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0138.html#intro">1. Introduction</a></cite></p>
 * <p>XMPP Core [1] specifies the use of Transport Layer Security (TLS; see RFC 5246 [2]) for encryption of XML streams, and TLS includes the ability to compress encrypted traffic (see RFC 3749 [3]). However, not all computing platforms are able to implement TLS, and traffic compression may be desirable for communication by applications on such computing platforms. This document defines a mechanism for negotiating the compression of XML streams outside the context of TLS.</p>
 * </blockquote>
 * If you enable this manager, stream compression will be used, if available. Note that stream compression should not be used, when you use TLS.
 *
 * @author Christian Schudt
 */
public final class CompressionManager extends StreamFeatureNegotiator {

    private final XmppSession xmppSession;

    // Currently only support zlib compression.
    private final CompressionMethod method;

    public CompressionManager(XmppSession xmppSession, StreamFeatureListener streamFeatureListener, CompressionMethod compressionMethod) {
        super(Compression.class);
        addFeatureListener(streamFeatureListener);
        this.xmppSession = xmppSession;
        this.method = compressionMethod;
    }

    @Override
    public Status processNegotiation(Object element) throws Exception {
        Status status = Status.INCOMPLETE;
        try {
            if (element instanceof Compression) {
                if (method != null) {
                    xmppSession.send(new Compress(method));
                    status = Status.INCOMPLETE;
                } else {
                    status = Status.IGNORE;
                }
            } else if (element instanceof Compressed) {
                status = Status.SUCCESS;
            } else if (element instanceof Failure) {
                status = Status.FAILURE;
                throw new Exception("Failure during compression negotiation: " + ((Failure) element).getCondition());
            }
        } finally {
            notifyFeatureNegotiated(status, element);
        }
        return status;
    }

    /**
     * {@inheritDoc}
     *
     * @return True, because compression needs a stream restart after feature negotiation.
     */
    public boolean needsRestart() {
        return true;
    }

    @Override
    public boolean canProcess(Object element) {
        return element instanceof Compressed || element instanceof Failure;
    }
}
