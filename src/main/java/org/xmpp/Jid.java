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

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The implementation of the JID as described in <a href="http://xmpp.org/rfcs/rfc6122.html">Extensible Messaging and Presence Protocol (XMPP): Address Format</a>.
 * <p>
 * A JID consists of three parts:
 * <p>
 * [ localpart "@" ] domainpart [ "/" resourcepart ]
 * </p>
 * The easiest way to create a JID is to use the {@link #valueOf(String)} method:
 * <pre><code>
 * Jid jid = Jid.valueOf("juliet@capulet.lit/balcony");
 * </code></pre>
 * You can then get the parts from it via the respective methods:
 * <pre><code>
 * String local = jid.getLocal(); // juliet
 * String domain = jid.getDomain(); // capulet.lit
 * String resource = jid.getResource(); // balcony
 * </code></pre>
 * This class overrides <code>equals()</code> and <code>hashCode()</code>, so that different instances with the same value are equal:
 * <pre><code>
 * Jid.valueOf("romeo@capulet.lit/balcony").equals(Jid.valueOf("romeo@capulet.lit/balcony")); // true
 * </code></pre>
 * This class also supports <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>, i.e.
 * <pre><code>
 * Jid.valueOf("d'artagnan@musketeers.lit")
 * </code></pre>
 * is escaped as <code>d\\27artagnan@musketeers.lit</code>.
 *
 * @author Christian Schudt
 */
public final class Jid {

    /**
     * Escapes all disallowed characters and also backslash, when followed by a defined hex code for escaping. See 4. Business Rules.
     */
    private static final Pattern ESCAPE_PATTERN = Pattern.compile("[ \"&'/:<>@]|\\\\(?=20|22|26|27|2f|3a|3c|3e|40|5c)");

    private static final Pattern UNESCAPE_PATTERN = Pattern.compile("\\\\(20|22|26|27|2f|3a|3c|3e|40|5c)");

    /**
     * Every character, which is not a letter, number, punctuation, symbol character, marker character or space.
     */
    private static final Pattern PROHIBITED_CHARACTERS = Pattern.compile("[^\\p{L}\\p{N}\\p{P}\\p{S}\\p{M}\\s]");

    private static final String DOMAIN_PART = "((?:(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))+)";

    private static final Pattern JID = Pattern.compile("^((.*?)@)?" + DOMAIN_PART + "(/(.*))?$");

    // B.1 Commonly mapped to nothing
    // Every space, except white-space (\u0020)
    private static final Pattern MAP_TO_NOTHING = Pattern.compile("([\u00AD\u034F\u1806\u180B\u180C\u180D\u200B\u200C\u200D\u2060\uFE00\uFE01\uFE0F\uFEFF])");

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
        this(null, domain, null);
    }

    /**
     * Creates a bare JID with only the local and domain part.
     *
     * @param local  The local part.
     * @param domain The domain part.
     */
    public Jid(String local, String domain) {
        this(local, domain, null);
    }

    /**
     * Creates a full JID with local, domain and resource part.
     *
     * @param local    The local part.
     * @param domain   The domain part.
     * @param resource The resource part.
     */
    public Jid(String local, String domain, String resource) {
        this(local, domain, resource, false, true);
    }

    private Jid(String local, String domain, String resource, boolean doUnescape, boolean prepareAndValidate) {
        String preparedNode;
        if (prepareAndValidate) {
            preparedNode = prepare(local, true);
            validateDomain(domain);
            validateLength(preparedNode, "local");
        } else {
            preparedNode = local;
        }
        String preparedResource = prepare(resource, false);
        validateLength(preparedResource, "resource");

        if (doUnescape) {
            this.local = unescape(preparedNode);
        } else {
            this.local = preparedNode;
        }
        this.escapedLocal = escape(this.local);
        this.domain = domain.toLowerCase();
        this.resource = preparedResource;
    }

    /**
     * Creates a JID from a string. The format must be
     * <blockquote><p>[ localpart "@" ] domainpart [ "/" resourcepart ]</p></blockquote>.
     * The input string will be escaped.
     *
     * @param jid The JID.
     * @return The JID.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    public static Jid valueOf(String jid) {
        return valueOf(jid, false);
    }

    /**
     * Creates a JID from a string. The format must be
     * <blockquote><p>[ localpart "@" ] domainpart [ "/" resourcepart ]</p></blockquote>
     *
     * @param jid        The JID.
     * @param doUnescape If the jid parameter will be unescaped.
     * @return The JID.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    public static Jid valueOf(String jid, boolean doUnescape) {
        if (jid == null) {
            throw new IllegalArgumentException("jid must not be null.");
        }
        jid = jid.trim();

        if (jid.isEmpty()) {
            throw new IllegalArgumentException("jid must not be empty.");
        }

        Matcher matcher = JID.matcher(jid);
        if (matcher.matches()) {
            return new Jid(matcher.group(2), matcher.group(3), matcher.group(8), doUnescape, true);
        } else {
            throw new IllegalArgumentException("Could not parse JID.");
        }
    }

    /**
     * Escapes a JID. The characters <code>"&'/:<>@</code> (+ whitespace) are replaced with
     *
     * @param jid The JID.
     * @return The escaped JID.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    private static String escape(String jid) {
        if (jid != null) {
            Matcher matcher = ESCAPE_PATTERN.matcher(jid);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String match = matcher.group();
                matcher.appendReplacement(sb, String.format("\\\\%x", match.getBytes()[0]));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
        return null;
    }

    private static String unescape(String jid) {
        if (jid != null) {
            Matcher matcher = UNESCAPE_PATTERN.matcher(jid);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String match = matcher.group(1);
                int num = Integer.parseInt(match, 16);
                String value = String.valueOf((char) num);
                if (value.equals("\\")) {
                    matcher.appendReplacement(sb, "\\\\");
                } else {
                    matcher.appendReplacement(sb, value);
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
        return null;
    }

    static String prepare(String input, boolean caseFold) {
        if (input != null) {
            String mappedToNothing = MAP_TO_NOTHING.matcher(input).replaceAll("");
            String normalized = Normalizer.normalize(mappedToNothing, Normalizer.Form.NFKC);
            if (caseFold) {
                normalized = normalized.toUpperCase(Locale.ENGLISH).toLowerCase(Locale.ENGLISH);
            }
            // A.7. Notes
            // Because the additional characters prohibited by Nodeprep are
            // prohibited after normalization
            Matcher matcher = PROHIBITED_CHARACTERS.matcher(normalized);
            if (matcher.find()) {
                throw new IllegalArgumentException("Local or resource part contains prohibited characters.");
            }
            return normalized;
        }
        return null;
    }

    private void validateDomain(String domain) {
        if (domain == null) {
            throw new IllegalArgumentException("domain must not be null.");
        }
        if (domain.contains("@")) {
            // Prevent misuse of API.
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
     * @see #withResource(String)
     */
    public Jid asBareJid() {
        return new Jid(local, domain, null, false, false);
    }

    /**
     * Returns a new full JID with a resource and the same local and domain part of the current JID.
     *
     * @param resource The resource.
     * @return The full JID with a resource.
     * @see #asBareJid()
     */
    public Jid withResource(String resource) {
        return new Jid(local, domain, resource, false, false);
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
     * Gets the resource part.
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
