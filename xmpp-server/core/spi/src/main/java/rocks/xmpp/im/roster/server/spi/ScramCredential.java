/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.im.roster.server.spi;

import javax.security.enterprise.credential.Credential;

/**
 * @author Christian Schudt
 */
public final class ScramCredential implements Credential {
    private final String caller;

    private final int iterationCount;

    private final byte[] salt;

    private final byte[] storedKey;

    private final byte[] serverKey;

    public ScramCredential(String caller, int iterationCount, byte[] salt, byte[] storedKey, byte[] serverKey) {
        this.caller = caller;
        this.iterationCount = iterationCount;
        this.salt = salt;
        this.storedKey = storedKey;
        this.serverKey = serverKey;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getStoredKey() {
        return storedKey;
    }

    public byte[] getServerKey() {
        return serverKey;
    }

    public String getCaller() {
        return caller;
    }
}
