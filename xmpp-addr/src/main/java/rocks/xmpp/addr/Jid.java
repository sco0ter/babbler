/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

import rocks.xmpp.precis.PrecisProfile;
import rocks.xmpp.precis.PrecisProfiles;
import rocks.xmpp.util.cache.LruCache;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.net.IDN;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The implementation of the JID as described in <a href="https://tools.ietf.org/html/rfc7622">Extensible Messaging and Presence Protocol (XMPP): Address Format</a>.
 * <p>
 * A JID consists of three parts:
 * <p>
 * [ localpart "@" ] domainpart [ "/" resourcepart ]
 * </p>
 * The easiest way to create a JID is to use the {@link #of(CharSequence)} method:
 * <pre><code>
 * Jid jid = Jid.of("juliet@capulet.lit/balcony");
 * </code></pre>
 * You can then get the parts from it via the respective methods:
 * <pre><code>
 * String local = jid.getLocal(); // juliet
 * String domain = jid.getDomain(); // capulet.lit
 * String resource = jid.getResource(); // balcony
 * </code></pre>
 * This class overrides <code>equals()</code> and <code>hashCode()</code>, so that different instances with the same value are equal:
 * <pre><code>
 * Jid.of("romeo@capulet.lit/balcony").equals(Jid.of("romeo@capulet.lit/balcony")); // true
 * </code></pre>
 * This class also supports <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>, i.e.
 * <pre><code>
 * Jid.of("d'artagnan@musketeers.lit")
 * </code></pre>
 * is escaped as <code>d\\27artagnan@musketeers.lit</code>.
 * <p>
 * This class is thread-safe and immutable.
 *
 * @author Christian Schudt
 * @see <a href="https://tools.ietf.org/html/rfc7622">RFC 7622 - Extensible Messaging and Presence Protocol (XMPP): Address Format</a>
 */
@XmlJavaTypeAdapter(JidAdapter.class)
public final class Jid implements Comparable<Jid>, Serializable, CharSequence {

    /**
     * The service discovery feature used for determining support of JID escaping (<code>jid\20escaping</code>).
     */
    public static final String ESCAPING_FEATURE = "jid\\20escaping";

    /**
     * Escapes all disallowed characters and also backslash, when followed by a defined hex code for escaping. See 4. Business Rules.
     */
    private static final Pattern ESCAPE_PATTERN = Pattern.compile("[ \"&'/:<>@]|\\\\(?=20|22|26|27|2f|3a|3c|3e|40|5c)");

    private static final Pattern UNESCAPE_PATTERN = Pattern.compile("\\\\(20|22|26|27|2f|3a|3c|3e|40|5c)");

    private static final String DOMAIN_PART = "((?:(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))+)";

    private static final Pattern JID = Pattern.compile("^((.*?)@)?" + DOMAIN_PART + "(/(.*))?$");

    private static final IDNProfile IDN_PROFILE = new IDNProfile(false);

    /**
     * Label separators for domain labels, which should be mapped to "." (dot): IDEOGRAPHIC FULL STOP character (U+3002)
     */
    private static final Pattern LABEL_SEPARATOR = Pattern.compile("\u3002");

    /**
     * Caches the escaped JIDs.
     */
    private static final Map<CharSequence, Jid> ESCAPED_CACHE = new LruCache<>(5000);

    /**
     * Caches the unescaped JIDs.
     */
    private static final Map<CharSequence, Jid> UNESCAPED_CACHE = new LruCache<>(5000);

    private static final long serialVersionUID = -3824234106101731424L;

    private final String escapedLocal;

    private final String local;

    private final String domain;

    private final String resource;

    /**
     * Creates a full JID with local, domain and resource part.
     *
     * @param local    The local part.
     * @param domain   The domain part.
     * @param resource The resource part.
     */
    private Jid(CharSequence local, CharSequence domain, CharSequence resource) {
        this(local, domain, resource, false, true);
    }

    private Jid(final CharSequence local, final CharSequence domain, final CharSequence resource, final boolean doUnescape, final boolean enforceAndValidate) {
        final String enforcedLocalPart;
        final String enforcedDomainPart;
        final String enforcedResource;

        // If the domainpart includes a final character considered to be a label
        // separator (dot) by [RFC1034], this character MUST be stripped from
        // the domainpart before the JID of which it is a part is used for the
        // purpose of routing an XML stanza, comparing against another JID, or
        // constructing an XMPP URI or IRI [RFC5122].  In particular, such a
        // character MUST be stripped before any other canonicalization steps
        // are taken.
        final String strDomain = Objects.requireNonNull(domain).toString().replaceAll("\\.$", "");
        final String unescapedLocalPart;

        if (doUnescape) {
            unescapedLocalPart = unescape(local);
        } else {
            unescapedLocalPart = local != null ? local.toString() : null;
        }

        // Escape the local part, so that disallowed characters like the space characters pass the UsernameCaseMapped profile.
        final String escapedLocalPart = escape(unescapedLocalPart);
        if (enforceAndValidate) {
            enforcedLocalPart = escapedLocalPart != null ? PrecisProfiles.USERNAME_CASE_MAPPED.enforce(escapedLocalPart) : null;
            enforcedResource = resource != null ? PrecisProfiles.OPAQUE_STRING.enforce(resource) : null;
            // See https://tools.ietf.org/html/rfc5895#section-2
            enforcedDomainPart = IDN_PROFILE.enforce(strDomain);

            validateLength(enforcedLocalPart, "local");
            validateLength(enforcedResource, "resource");
            validateDomain(strDomain);
        } else {
            enforcedLocalPart = escapedLocalPart != null ? escapedLocalPart : null;
            enforcedResource = resource != null ? resource.toString() : null;
            enforcedDomainPart = strDomain;
        }

        this.local = unescape(enforcedLocalPart);
        this.escapedLocal = enforcedLocalPart;
        this.domain = enforcedDomainPart;
        this.resource = enforcedResource;
    }

    /**
     * Returns a full JID with a domain and resource part, e.g. <code>capulet.com/balcony</code>
     *
     * @param local    The local part.
     * @param domain   The domain.
     * @param resource The resource part.
     * @return The JID.
     * @throws NullPointerException     If the domain is null.
     * @throws IllegalArgumentException If the domain, local or resource part are not valid.
     */
    public static Jid of(CharSequence local, CharSequence domain, CharSequence resource) {
        return new Jid(local, domain, resource);
    }

    /**
     * Creates a bare JID with only the domain part, e.g. <code>capulet.com</code>
     *
     * @param domain The domain.
     * @return The JID.
     * @throws NullPointerException     If the domain is null.
     * @throws IllegalArgumentException If the domain or local part are not valid.
     */
    public static Jid ofDomain(CharSequence domain) {
        return new Jid(null, domain, null);
    }

    /**
     * Creates a bare JID with a local and domain part, e.g. <code>juliet@capulet.com</code>
     *
     * @param local  The local part.
     * @param domain The domain.
     * @return The JID.
     * @throws NullPointerException     If the domain is null.
     * @throws IllegalArgumentException If the domain or local part are not valid.
     */
    public static Jid ofLocalAndDomain(CharSequence local, CharSequence domain) {
        return new Jid(local, domain, null);
    }

    /**
     * Creates a full JID with a domain and resource part, e.g. <code>capulet.com/balcony</code>
     *
     * @param domain   The domain.
     * @param resource The resource part.
     * @return The JID.
     * @throws NullPointerException     If the domain is null.
     * @throws IllegalArgumentException If the domain or resource are not valid.
     */
    public static Jid ofDomainAndResource(CharSequence domain, CharSequence resource) {
        return new Jid(null, domain, resource);
    }

    /**
     * Creates a JID from an unescaped string. The format must be
     * <blockquote><p>[ localpart "@" ] domainpart [ "/" resourcepart ]</p></blockquote>
     * The input string will be escaped.
     *
     * @param jid The JID.
     * @return The JID.
     * @throws NullPointerException     If the jid is null.
     * @throws IllegalArgumentException If the jid could not be parsed or is not valid.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    public static Jid of(CharSequence jid) {
        return of(jid.toString(), false);
    }

    /**
     * Creates a JID from a escaped JID string. The format must be
     * <blockquote><p>[ localpart "@" ] domainpart [ "/" resourcepart ]</p></blockquote>
     *
     * @param jid The JID.
     * @return The JID.
     * @throws NullPointerException     If the jid is null.
     * @throws IllegalArgumentException If the jid could not be parsed or is not valid.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    public static Jid ofEscaped(CharSequence jid) {
        return of(jid.toString(), true);
    }

    /**
     * Creates a JID from a string. The format must be
     * <blockquote><p>[ localpart "@" ] domainpart [ "/" resourcepart ]</p></blockquote>
     *
     * @param jid        The JID.
     * @param doUnescape If the jid parameter will be unescaped.
     * @return The JID.
     * @throws NullPointerException     If the jid is null.
     * @throws IllegalArgumentException If the jid could not be parsed or is not valid.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    private static Jid of(String jid, final boolean doUnescape) {
        Objects.requireNonNull(jid, "jid must not be null.");

        jid = jid.trim();

        if (jid.isEmpty()) {
            throw new IllegalArgumentException("jid must not be empty.");
        }

        Jid result;
        if (doUnescape) {
            result = UNESCAPED_CACHE.get(jid);
        } else {
            result = ESCAPED_CACHE.get(jid);
        }

        if (result != null) {
            return result;
        }

        Matcher matcher = JID.matcher(jid);
        if (matcher.matches()) {
            Jid jidValue = new Jid(matcher.group(2), matcher.group(3), matcher.group(8), doUnescape, true);
            if (doUnescape) {
                UNESCAPED_CACHE.put(jid, jidValue);
            } else {
                ESCAPED_CACHE.put(jid, jidValue);
            }
            return jidValue;
        } else {
            throw new IllegalArgumentException("Could not parse JID: " + jid);
        }
    }

    /**
     * Escapes a local part. The characters {@code "&'/:<>@} (+ whitespace) are replaced with their respective escape characters.
     *
     * @param localPart The local part.
     * @return The escaped local part or null.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    private static String escape(CharSequence localPart) {
        if (localPart != null) {
            Matcher matcher = ESCAPE_PATTERN.matcher(localPart);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String match = matcher.group();
                matcher.appendReplacement(sb, String.format("\\\\%x", match.getBytes(StandardCharsets.UTF_8)[0]));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
        return null;
    }

    private static String unescape(CharSequence localPart) {
        if (localPart != null) {
            Matcher matcher = UNESCAPE_PATTERN.matcher(localPart);
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

    private static void validateDomain(String domain) {
        Objects.requireNonNull(domain, "domain must not be null.");
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
    private static void validateLength(CharSequence value, CharSequence part) {
        if (value != null) {
            if (value.length() == 0) {
                throw new IllegalArgumentException(part + " must not be empty.");
            }
            if (value.length() > 1023) {
                throw new IllegalArgumentException(part + " must not be greater than 1023 characters.");
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
    public final boolean isFullJid() {
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
    public final boolean isBareJid() {
        return resource == null;
    }

    /**
     * Converts this JID into a bare JID, i.e. removes the resource part.
     * <blockquote>
     * <p>The term "bare JID" refers to an XMPP address of the form &lt;localpart@domainpart&gt; (for an account at a server) or of the form &lt;domainpart&gt; (for a server).</p>
     * </blockquote>
     *
     * @return The bare JID.
     * @see #withResource(CharSequence)
     */
    public final Jid asBareJid() {
        return new Jid(local, domain, null, false, false);
    }

    /**
     * Creates a new JID with a new local part and the same domain and resource part of the current JID.
     *
     * @param local The local part.
     * @return The JID with a new local part.
     * @throws IllegalArgumentException If the local is not a valid local part.
     * @see #withResource(CharSequence)
     */
    public final Jid withLocal(CharSequence local) {
        return new Jid(local, domain, resource, false, true);
    }

    /**
     * Creates a new full JID with a resource and the same local and domain part of the current JID.
     *
     * @param resource The resource.
     * @return The full JID with a resource.
     * @throws IllegalArgumentException If the resource is not a valid resource part.
     * @see #asBareJid()
     * @see #withLocal(CharSequence)
     */
    public final Jid withResource(CharSequence resource) {
        return new Jid(local, domain, resource, false, true);
    }

    /**
     * Creates a new JID at a subdomain and at the same domain as this JID.
     *
     * @param subdomain The subdomain.
     * @return The JID at a subdomain.
     * @throws NullPointerException     If subdomain is null.
     * @throws IllegalArgumentException If subdomain is not a valid subdomain name.
     */
    public final Jid atSubdomain(CharSequence subdomain) {
        return new Jid(local, Objects.requireNonNull(subdomain) + "." + domain, resource, false, true);
    }

    /**
     * Gets the local part of the JID, also known as the name or node.
     * <blockquote>
     * <p><cite><a href="https://tools.ietf.org/html/rfc7622#section-3.3">3.3.  Localpart</a></cite></p>
     * <p>The localpart of a JID is an optional identifier placed before the
     * domainpart and separated from the latter by the '@' character.
     * Typically, a localpart uniquely identifies the entity requesting and
     * using network access provided by a server (i.e., a local account),
     * although it can also represent other kinds of entities (e.g., a
     * chatroom associated with a multi-user chat service [XEP-0045]).  The
     * entity represented by an XMPP localpart is addressed within the
     * context of a specific domain (i.e., &lt;localpart@domainpart&gt;).</p>
     * </blockquote>
     *
     * @return The local part or null.
     */
    public final String getLocal() {
        return local;
    }

    /**
     * Gets the domain part.
     * <blockquote>
     * <p><cite><a href="https://tools.ietf.org/html/rfc7622#section-3.2">3.2.  Domainpart</a></cite></p>
     * <p>The domainpart is the primary identifier and is the only REQUIRED
     * element of a JID (a mere domainpart is a valid JID).  Typically,
     * a domainpart identifies the "home" server to which clients connect
     * for XML routing and data management functionality.</p>
     * </blockquote>
     *
     * @return The domain part.
     */
    public final String getDomain() {
        return domain;
    }

    /**
     * Gets the resource part.
     * <blockquote>
     * <p><cite><a href="https://tools.ietf.org/html/rfc7622#section-3.4">3.4.  Resourcepart</a></cite></p>
     * <p>The resourcepart of a JID is an optional identifier placed after the
     * domainpart and separated from the latter by the '/' character.  A
     * resourcepart can modify either a &lt;localpart@domainpart&gt; address or a
     * mere &lt;domainpart&gt; address.  Typically, a resourcepart uniquely
     * identifies a specific connection (e.g., a device or location) or
     * object (e.g., an occupant in a multi-user chatroom [XEP-0045])
     * belonging to the entity associated with an XMPP localpart at a domain
     * (i.e., &lt;localpart@domainpart/resourcepart&gt;).</p>
     * </blockquote>
     *
     * @return The resource part or null.
     */
    public final String getResource() {
        return resource;
    }

    /**
     * Returns the JID in escaped form as described in <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>.
     *
     * @return The escaped JID.
     * @see #toString()
     */
    public final String toEscapedString() {
        return toString(escapedLocal, domain, resource);
    }

    @Override
    public final int length() {
        return toString().length();
    }

    @Override
    public final char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public final CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    /**
     * Returns the JID in its string representation, i.e. [ localpart "@" ] domainpart [ "/" resourcepart ].
     *
     * @return The JID.
     * @see #toEscapedString()
     */
    @Override
    public final String toString() {
        return toString(local, domain, resource);
    }

    private static String toString(String local, String domain, String resource) {
        StringBuilder sb = new StringBuilder();
        if (local != null) {
            sb.append(local).append('@');
        }
        sb.append(domain);
        if (resource != null) {
            sb.append('/').append(resource);
        }
        return sb.toString();
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Jid)) {
            return false;
        }
        Jid other = (Jid) o;

        return Objects.equals(local, other.local)
                && Objects.equals(domain, other.domain)
                && Objects.equals(resource, other.resource);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(local, domain, resource);
    }

    /**
     * Compares this JID with another JID. First domain parts are compared. If these are equal, local parts are compared
     * and if these are equal, too, resource parts are compared.
     *
     * @param o The other JID.
     * @return The comparison result.
     */
    @Override
    public final int compareTo(Jid o) {
        if (this == o) {
            return 0;
        }

        if (o != null) {
            int result;
            // First compare domain parts.
            if (domain != null) {
                result = o.domain != null ? domain.compareTo(o.domain) : -1;
            } else {
                result = o.domain != null ? 1 : 0;
            }
            // If the domains are equal, compare local parts.
            if (result == 0) {
                if (local != null) {
                    // If this local part is not null, but the other is null, move this down (1).
                    result = o.local != null ? local.compareTo(o.local) : 1;
                } else {
                    // If this local part is null, but the other is not, move this up (-1).
                    result = o.local != null ? -1 : 0;
                }
            }
            // If the local parts are equal, compare resource parts.
            if (result == 0) {
                if (resource != null) {
                    // If this resource part is not null, but the other is null, move this down (1).
                    return o.resource != null ? resource.compareTo(o.resource) : 1;
                } else {
                    // If this resource part is null, but the other is not, move this up (-1).
                    return o.resource != null ? -1 : 0;
                }
            }
            return result;
        } else {
            return -1;
        }
    }

    /**
     * A profile for applying the rules for IDN as in RFC 5895. Although IDN doesn't use Precis, it's still very similar so that we can use the base class.
     *
     * @see <a href="https://tools.ietf.org/html/rfc5895#section-2">RFC 5895</a>
     */
    private static final class IDNProfile extends PrecisProfile {

        private IDNProfile(boolean identifierClass) {
            super(identifierClass);
        }

        @Override
        public String prepare(CharSequence input) {
            return IDN.toUnicode(input.toString(), IDN.USE_STD3_ASCII_RULES);
        }

        @Override
        public String enforce(CharSequence input) {
            // 4. Map IDEOGRAPHIC FULL STOP character (U+3002) to dot.
            return applyAdditionalMappingRule(
                    // 3.  All characters are mapped using Unicode Normalization Form C (NFC).
                    applyNormalizationRule(
                            // 2. Fullwidth and halfwidth characters (those defined with
                            // Decomposition Types <wide> and <narrow>) are mapped to their
                            // decomposition mappings
                            applyWidthMappingRule(
                                    // 1. Uppercase characters are mapped to their lowercase equivalents
                                    applyCaseMappingRule(prepare(input))))).toString();
        }

        @Override
        protected CharSequence applyWidthMappingRule(CharSequence charSequence) {
            return widthMap(charSequence);
        }

        @Override
        protected CharSequence applyAdditionalMappingRule(CharSequence charSequence) {
            return LABEL_SEPARATOR.matcher(charSequence).replaceAll(".");
        }

        @Override
        protected CharSequence applyCaseMappingRule(CharSequence charSequence) {
            return charSequence.toString().toLowerCase();
        }

        @Override
        protected CharSequence applyNormalizationRule(CharSequence charSequence) {
            return Normalizer.normalize(charSequence, Normalizer.Form.NFC);
        }

        @Override
        protected CharSequence applyDirectionalityRule(CharSequence charSequence) {
            return charSequence;
        }
    }
}
