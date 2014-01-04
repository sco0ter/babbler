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

package org.xmpp;

/**
 * The implementation of the JID as described in <a href="http://xmpp.org/rfcs/rfc6122.html">Extensible Messaging and Presence Protocol (XMPP): Address Format</a>.
 *
 * @author Christian Schudt
 */
public final class Jid {

    private final String local;

    private final String domain;

    private final String resource;

    /**
     * Creates a bare JID with only the domain part.
     *
     * @param domain The domain.
     */
    public Jid(String domain) {
        validateDomain(domain);
        this.domain = domain;
        this.local = null;
        this.resource = null;
    }

    /**
     * Creates a bare JID with only the local and domain part.
     *
     * @param local  The local part.
     * @param domain The domain part.
     */
    public Jid(String local, String domain) {
        validateDomain(domain);
        validateLength(local, "local");
        this.domain = domain;
        this.local = local;
        this.resource = null;
    }

    /**
     * Creates a full JID with local, domain and resource part.
     *
     * @param local    The local part.
     * @param domain   The domain part.
     * @param resource The resource part.
     */
    public Jid(String local, String domain, String resource) {
        validateDomain(domain);
        validateLength(local, "local");
        validateLength(resource, "resource");
        this.local = local;
        this.domain = domain;
        this.resource = resource;
    }

    /**
     * Creates a JID from a string. The format must be
     * <blockquote><p>[ localpart "@" ] domainpart [ "/" resourcepart ]</p></blockquote>
     *
     * @param jid The jid.
     * @return The JID.
     */
    public static Jid fromString(String jid) {

        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null.");
        }
        jid = jid.trim();

        if (jid.isEmpty()) {
            throw new IllegalArgumentException("jid must not be empty.");
        }

        String local = null;
        String domain;
        String resource = null;

        int indexOfAt = jid.indexOf("@");
        int indexOfSlash = jid.indexOf("/");
        if (indexOfAt > -1) {
            local = jid.substring(0, indexOfAt);
            if (indexOfSlash > -1) {
                domain = jid.substring(indexOfAt + 1, indexOfSlash);
            } else {
                domain = jid.substring(indexOfAt + 1);
            }
        } else {
            if (indexOfSlash > -1) {
                domain = jid.substring(0, indexOfSlash);
            } else {
                domain = jid.substring(0);
            }
        }
        if (indexOfSlash > -1) {
            resource = jid.substring(indexOfSlash + 1);
        }

        return new Jid(local, domain, resource);
    }

    private void validateDomain(String domain) {
        if (domain == null) {
            throw new IllegalArgumentException("domain must not be null.");
        }
        if (domain.contains("@")) {
            throw new IllegalArgumentException("domain must not contain a '@' sign");
        }
        validateLength(domain, "domain");
    }

    /**
     * Validates that the length of a local, domain or resource part is not longer than 1023 characters.
     *
     * @param value The value.
     * @param part  The part, only used to produce an exception message.
     */
    private void validateLength(String value, String part) {
        if (value != null) {
            if (value.isEmpty()) {
                throw new IllegalArgumentException(String.format("%s must not be empty.", part));
            }
            if (value.length() > 1023) {
                throw new IllegalArgumentException(String.format("%s must not be greater than 1023 characters.", part));
            }
        }
    }

    /**
     * Checks if the JID is a full JID.
     * <blockquote>
     * <p>The term "full JID" refers to an XMPP address of the form &lt;localpart@domainpart/resourcepart&gt; (for a particular authorized client or device associated with an account) or of the form &lt;domainpart/resourcepart&gt; (for a particular resource or script associated with a server).</p>
     * </blockquote>
     *
     * @return True, if the JID is a full JID; otherwise false.
     */
    public boolean isFullJid() {
        return resource != null;
    }

    /**
     * Checks if the JID is a bare JID.
     * <blockquote>
     * <p>The term "bare JID" refers to an XMPP address of the form &lt;localpart@domainpart&gt; (for an account at a server) or of the form &lt;domainpart&gt; (for a server).</p>
     * </blockquote>
     *
     * @return True, if the JID is a bare JID; otherwise false.
     */
    public boolean isBareJid() {
        return resource == null;
    }

    /**
     * Converts this JID into a bare JID, i.e. removes the resource.
     * <blockquote>
     * <p>The term "bare JID" refers to an XMPP address of the form &lt;localpart@domainpart&gt; (for an account at a server) or of the form &lt;domainpart&gt; (for a server).</p>
     * </blockquote>
     *
     * @return The bare JID.
     */
    public Jid toBareJid() {
        if (local != null) {
            return new Jid(local, domain);
        } else {
            return new Jid(domain);
        }
    }

    /**
     * Gets the local part of the JID, also known as the name or node.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6122.html#addressing-localpart">2.3.  Localpart</a></cite></p>
     * <p>The localpart of a JID is an optional identifier placed before the domainpart and separated from the latter by the '@' character. Typically a localpart uniquely identifies the entity requesting and using network access provided by a server (i.e., a local account), although it can also represent other kinds of entities (e.g., a chat room associated with a multi-user chat service). The entity represented by an XMPP localpart is addressed within the context of a specific domain (i.e., {@code <localpart@domainpart>}).</p>
     * </blockquote>
     *
     * @return The local part.
     */
    public String getLocal() {
        return local;
    }

    /**
     * Gets the domain part.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6122.html#addressing-domain">2.2.  Domainpart</a></cite></p>
     * <p>The domainpart of a JID is that portion after the '@' character (if any) and before the '/' character (if any); it is the primary identifier and is the only REQUIRED element of a JID (a mere domainpart is a valid JID).</p>
     * </blockquote>
     *
     * @return The domain part.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6122.html#addressing-resource">2.4.  Resourcepart</a></cite></p>
     * <p>The resourcepart of a JID is an optional identifier placed after the domainpart and separated from the latter by the '/' character. A resourcepart can modify either a {@code <localpart@domainpart>} address or a mere {@code <domainpart>} address. Typically a resourcepart uniquely identifies a specific connection (e.g., a device or location) or object (e.g., an occupant in a multi-user chat room) belonging to the entity associated with an XMPP localpart at a domain (i.e., {@code <localpart@domainpart/resourcepart>}).</p>
     * </blockquote>
     *
     * @return The resource part.
     */
    public String getResource() {
        return resource;
    }

    /**
     * Converts the JID into its string representation, i.e. [ localpart "@" ] domainpart [ "/" resourcepart ].
     *
     * @return The JID.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (local != null) {
            sb.append(local);
            sb.append("@");
        }
        sb.append(domain);
        if (resource != null) {
            sb.append("/");
            sb.append(resource);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Jid)) {
            return false;
        }
        Jid other = (Jid) o;

        return (local == null ? other.local == null : local.equals(other.local))
                && (domain == null ? other.domain == null : domain.equals(other.domain))
                && (resource == null ? other.resource == null : resource.equals(other.resource));

    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ((local == null) ? 0 : local.hashCode());
        result = 31 * result + ((domain == null) ? 0 : domain.hashCode());
        result = 31 * result + ((resource == null) ? 0 : resource.hashCode());
        return result;
    }
}
