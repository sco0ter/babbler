/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.core.sasl.server;

import rocks.xmpp.core.sasl.model.Failure;

import javax.security.sasl.SaslException;
import java.util.Objects;

/**
 * A {@link SaslException} for usage in {@link javax.security.sasl.SaslServer} implementations to report XMPP specific SASL failures.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/rfcs/rfc6120.html#sasl-errors">6.5.  SASL Errors</a>
 */
public final class SaslFailureException extends SaslException {

    private static final long serialVersionUID = -6404705871024919394L;

    private final Failure failure;

    public SaslFailureException(Failure failure) {
        this.failure = Objects.requireNonNull(failure);
    }

    public SaslFailureException(Failure failure, String detail) {
        super(detail);
        this.failure = Objects.requireNonNull(failure);
    }

    public SaslFailureException(Failure failure, String detail, Throwable ex) {
        super(detail, ex);
        this.failure = Objects.requireNonNull(failure);
    }

    /**
     * The specific SASL failure.
     *
     * @return The SASL failure.
     */
    public final Failure getFailure() {
        return failure;
    }
}
