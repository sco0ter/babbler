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

package rocks.xmpp.extensions.compress;

import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stream.StreamFeatureNegotiator;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.extensions.compress.model.StreamCompression;
import rocks.xmpp.extensions.compress.model.feature.CompressionFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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
    /**
     * The "zlib" compression method.
     */
    public static final CompressionMethod ZLIB;

    /**
     * The "gzip" compression method.
     */
    public static final CompressionMethod GZIP;

    /**
     * The "deflate" compression method.
     */
    public static final CompressionMethod DEFLATE;

    private static final Logger logger = Logger.getLogger(CompressionManager.class.getName());

    static {
        ZLIB = new CompressionMethod() {
            @Override
            public String getName() {
                return "zlib";
            }

            @Override
            public InputStream decompress(InputStream inputStream) throws IOException {
                return new InflaterInputStream(inputStream);
            }

            @Override
            public OutputStream compress(OutputStream outputStream) throws IOException {
                return new DeflaterOutputStream(outputStream, true);
            }
        };
        GZIP = new CompressionMethod() {
            public String getName() {
                return "gzip";
            }

            @Override
            public InputStream decompress(InputStream inputStream) throws IOException {
                return new GZIPInputStream(inputStream);
            }

            @Override
            public OutputStream compress(OutputStream outputStream) throws IOException {
                return new GZIPOutputStream(outputStream);
            }
        };
        DEFLATE = new CompressionMethod() {
            @Override
            public String getName() {
                return "deflate";
            }

            @Override
            public InputStream decompress(InputStream inputStream) throws IOException {
                // See http://stackoverflow.com/a/3932260
                // Seems like most (web)server implement deflate in a wrong way.
                return new InflaterInputStream(inputStream, new Inflater(true));
            }

            @Override
            public OutputStream compress(OutputStream outputStream) throws IOException {
                return new DeflaterOutputStream(outputStream, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
            }
        };
    }

    private final XmppSession xmppSession;

    private final List<CompressionMethod> compressionMethods = new CopyOnWriteArrayList<>();

    private CompressionMethod negotiatedCompressionMethod;

    private CompressionManager(XmppSession xmppSession) {
        super(CompressionFeature.class);
        this.xmppSession = xmppSession;
    }

    @Override
    public final Status processNegotiation(Object element) throws StreamNegotiationException {
        if (!isEnabled() || compressionMethods.isEmpty()) {
            return Status.IGNORE;
        }

        if (element instanceof CompressionFeature) {
            List<String> advertisedCompressionMethods = ((CompressionFeature) element).getMethods();
            Map<String, CompressionMethod> clientMethods = new LinkedHashMap<>();
            for (CompressionMethod compressionMethod : compressionMethods) {
                clientMethods.put(compressionMethod.getName(), compressionMethod);
            }
            clientMethods.keySet().retainAll(advertisedCompressionMethods);
            if (!clientMethods.isEmpty()) {
                // Use the first configured compression method, which is also advertised by the server.
                CompressionMethod compressionMethod = clientMethods.values().iterator().next();
                xmppSession.send(new StreamCompression.Compress(compressionMethod.getName()));
                negotiatedCompressionMethod = compressionMethod;
                return Status.INCOMPLETE;
            } else {
                return Status.IGNORE;
            }
        } else if (element == StreamCompression.COMPRESSED) {
            notifyFeatureNegotiated();
            logger.fine("Stream is now compressed.");
            return Status.SUCCESS;
        } else if (element instanceof StreamCompression.Failure) {
            negotiatedCompressionMethod = null;
            // Failure of the negotiation SHOULD NOT be treated as an unrecoverable error
            logger.warning("Failure during compression negotiation: " + ((StreamCompression.Failure) element).getCondition());
            return Status.IGNORE;
        }
        return Status.IGNORE;
    }

    /**
     * {@inheritDoc}
     *
     * @return True, because compression needs a stream restart after feature negotiation.
     */
    public final boolean needsRestart() {
        return true;
    }

    @Override
    public final boolean canProcess(Object element) {
        return element instanceof StreamCompression;
    }

    /**
     * Gets the negotiated compression method.
     *
     * @return The negotiated compression method.
     */
    public final CompressionMethod getNegotiatedCompressionMethod() {
        return negotiatedCompressionMethod;
    }

    /**
     * Gets the configured compression methods.
     *
     * @return The configured compression method.
     */
    public final List<CompressionMethod> getConfiguredCompressionMethods() {
        return compressionMethods;
    }
}
