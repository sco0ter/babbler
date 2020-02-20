/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.hashes;

import rocks.xmpp.core.ExtensionProtocol;
import rocks.xmpp.extensions.hashes.model.Hash;

import java.security.Security;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents the XEP-0300: Use of Cryptographic Hash Functions in XMPP.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0300.html">XEP-0300: Use of Cryptographic Hash Functions in XMPP</a>
 */
public final class CryptographicHashFunctionsProtocol implements ExtensionProtocol {

    private static final Set<String> FEATURES = new LinkedHashSet<>();

    static {
        FEATURES.add(Hash.NAMESPACE);
        for (String algorithm : Security.getAlgorithms("MessageDigest")) {
            // Replace "SHA" algorithm with "SHA-1".
            FEATURES.add("urn:xmpp:hash-function-text-names:" + algorithm.replaceAll("^SHA$", "SHA-1").toLowerCase());
        }
    }

    @Override
    public final Set<String> getFeatures() {
        return Collections.unmodifiableSet(FEATURES);
    }
}
