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

package rocks.xmpp.addr;

/**
 * Represents a malformed JID in order to handle the <code>jid-malformed</code> error.
 * <p>
 * This class is not intended to be publicly instantiable, but is used for malformed JIDs during parsing automatically.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-jid-malformed">RFC 6120, 8.3.3.8.  jid-malformed</a>
 */
public final class MalformedJid implements Jid {

    private static final long serialVersionUID = -1563072620243158894L;

    private final String malformedJid;

    private final Throwable cause;

    MalformedJid(final String jid, final Throwable cause) {
        this.malformedJid = jid;
        this.cause = cause;
    }

    @Override
    public final boolean isFullJid() {
        return false;
    }

    @Override
    public final boolean isBareJid() {
        return false;
    }

    @Override
    public final Jid asBareJid() {
        return this;
    }

    @Override
    public final Jid withLocal(CharSequence local) {
        return this;
    }

    @Override
    public final Jid withResource(CharSequence resource) {
        return this;
    }

    @Override
    public final Jid atSubdomain(CharSequence subdomain) {
        return this;
    }

    @Override
    public final String getLocal() {
        return null;
    }

    @Override
    public final String getEscapedLocal() {
        return null;
    }

    @Override
    public final String getDomain() {
        return null;
    }

    @Override
    public final String getResource() {
        return null;
    }

    @Override
    public final String toEscapedString() {
        return toString();
    }

    @Override
    public final int length() {
        return malformedJid.length();
    }

    @Override
    public final char charAt(int index) {
        return malformedJid.charAt(index);
    }

    @Override
    public final CharSequence subSequence(int start, int end) {
        return malformedJid.subSequence(start, end);
    }

    @Override
    public final int compareTo(Jid o) {
        if (o instanceof MalformedJid) {
            return malformedJid.compareTo(((MalformedJid) o).malformedJid);
        }
        return 1;
    }

    @Override
    public final String toString() {
        return malformedJid;
    }

    /**
     * Gets the cause why the JID is malformed.
     *
     * @return The cause.
     */
    public final Throwable getCause() {
        return cause;
    }
}
