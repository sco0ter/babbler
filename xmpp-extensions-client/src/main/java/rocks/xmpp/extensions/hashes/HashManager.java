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

package rocks.xmpp.extensions.hashes;

import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.hashes.model.Hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
public final class HashManager extends ExtensionManager {

    private static final String[] REGISTERED_HASH_ALGORITHMS = new String[]{"md5", "sha-1", "sha-224", "sha-256", "sha-384", "sha-512"};

    private static final List<String> FEATURES = new ArrayList<>();

    static {
        FEATURES.add(Hash.NAMESPACE);
        for (String algorithm : REGISTERED_HASH_ALGORITHMS) {
            try {
                MessageDigest.getInstance(algorithm);
                FEATURES.add("urn:xmpp:hash-function-text-names:" + algorithm);
            } catch (NoSuchAlgorithmException e) {
                // ignore
            }
        }
    }

    private HashManager(XmppSession xmppSession) {
        super(xmppSession, false);
    }
}
