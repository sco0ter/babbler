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

package org.xmpp.extension.compression;

import org.xmpp.Connection;
import org.xmpp.stream.FeatureListener;
import org.xmpp.stream.FeatureNegotiator;

/**
 * @author Christian Schudt
 */
public class CompressionNegotiator extends FeatureNegotiator {

    private final Connection connection;

    public CompressionNegotiator(Connection connection, FeatureListener featureListener) {
        super(Compression.class);
        addFeatureListener(featureListener);
        this.connection = connection;
    }

    @Override
    public Status processNegotiation(Object element) throws Exception {
        Status status = Status.INCOMPLETE;
        try {
            if (element instanceof Compression) {
                //connection.send(new Compress(new Compression.Method("zlib")));
                status = Status.SUCCESS;
            } else if (element instanceof Compressed) {
                status = Status.SUCCESS;
            } else if (element instanceof Failure) {
                status = Status.FAILURE;
            }
        } finally {
            notifyFeatureNegotiated(status, element);
        }
        return status;
    }

    /**
     * @return True, if the stream requires a restart after feature negotiation.
     */
    public boolean needsRestart() {
        return true;
    }

    @Override
    public boolean canProcess(Object element) {
        return element instanceof Compressed || element instanceof Failure;
    }
}
