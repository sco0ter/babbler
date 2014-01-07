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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The implementation of the JID as described in <a href="http://xmpp.org/rfcs/rfc6122.html">Extensible Messaging and Presence Protocol (XMPP): Address Format</a>.
 * <p>
 * <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a> is also supported.
 * </p>
 *
 * @author Christian Schudt
 */
public final class Jid {

    private static final Pattern ESCAPE_PATTERN = Pattern.compile("[ \"&'/:<>@\\\\]");

    private static final Pattern UNESCAPE_PATTERN = Pattern.compile("\\\\([0-9a-fA-F]{2})");

    private static final Pattern JID = Pattern.compile("((?<local>.{0,1023}?)@)?(?<domain>(?:(?!\\d|-)[a-zA-Z0-9\\-]{1,63}(?<!-)\\.?)+(?:[a-zA-Z]{2,}))(/(?<resource>.{0,1023}))?");

    private final String escapedLocal;

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
        this.escapedLocal = null;
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
        this.escapedLocal = local != null ? escape(local) : null;
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
        this.escapedLocal = local != null ? escape(local) : null;
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
        return parseJid(jid, false);
    }

    public static Jid fromEscapedString(String jid) {
        return parseJid(jid, true);
    }

    private static Jid parseJid(String jid, boolean unescape) {
        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null.");
        }
        jid = jid.trim();

        if (jid.isEmpty()) {
            throw new IllegalArgumentException("jid must not be empty.");
        }

        Matcher matcher = JID.matcher(jid);
        if (matcher.find()) {
            String local = matcher.group("local");
            return new Jid(unescape && local != null ? unescape(local) : local, matcher.group("domain"), matcher.group("resource"));
        } else {
            throw new IllegalArgumentException("Could not parse JID.");
        }
    }

    private static String escape(String string) {
        Matcher matcher = ESCAPE_PATTERN.matcher(string);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            matcher.appendReplacement(sb, String.format("\\\\%x", match.getBytes()[0]));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String unescape(String string) {
        Matcher matcher = UNESCAPE_PATTERN.matcher(string);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group(1);
            int num = Integer.parseInt(match, 16);
            String value = String.valueOf((char) num);
            matcher.appendReplacement(sb, value);
        }
        matcher.appendTail(sb);
        return sb.toString();
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
     * Gets the JID in escaped form as described in <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>.
     *
     * @return The escaped JID.
     * @see #toString()
     */
    public String toEscapedString() {
        return toString(escapedLocal, domain, resource);
    }

    /**
     * Converts the JID into its string representation, i.e. [ localpart "@" ] domainpart [ "/" resourcepart ].
     *
     * @return The JID.
     * @see #toEscapedString()
     */
    @Override
    public String toString() {
        return toString(local, domain, resource);
    }

    private String toString(String local, String domain, String resource) {
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
