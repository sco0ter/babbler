package rocks.xmpp.addr;

import java.text.Collator;
import java.util.Objects;

/**
 * @author Christian Schudt
 */
abstract class AbstractJid implements Jid {

    /**
     * Checks if the JID is a full JID.
     * <blockquote>
     * <p>The term "full JID" refers to an XMPP address of the form &lt;localpart@domainpart/resourcepart&gt; (for a particular authorized client or device associated with an account) or of the form &lt;domainpart/resourcepart&gt; (for a particular resource or script associated with a server).</p>
     * </blockquote>
     *
     * @return True, if the JID is a full JID; otherwise false.
     */
    @Override
    public final boolean isFullJid() {
        return getResource() != null;
    }

    /**
     * Checks if the JID is a bare JID.
     * <blockquote>
     * <p>The term "bare JID" refers to an XMPP address of the form &lt;localpart@domainpart&gt; (for an account at a server) or of the form &lt;domainpart&gt; (for a server).</p>
     * </blockquote>
     *
     * @return True, if the JID is a bare JID; otherwise false.
     */
    @Override
    public final boolean isBareJid() {
        return getResource() == null;
    }

    /**
     * Creates a new JID with a new local part and the same domain and resource part of the current JID.
     *
     * @param local The local part.
     * @return The JID with a new local part.
     * @throws IllegalArgumentException If the local is not a valid local part.
     * @see #withResource(CharSequence)
     */
    @Override
    public final Jid withLocal(CharSequence local) {
        if (Objects.equals(local, this.getLocal())) {
            return this;
        }
        return new FullJid(local, getDomain(), getResource(), false, true, null);
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
    @Override
    public final Jid withResource(CharSequence resource) {
        if (Objects.equals(resource, this.getResource())) {
            return this;
        }
        return new FullJid(getLocal(), getDomain(), resource, false, true, asBareJid());
    }

    /**
     * Creates a new JID at a subdomain and at the same domain as this JID.
     *
     * @param subdomain The subdomain.
     * @return The JID at a subdomain.
     * @throws NullPointerException     If subdomain is null.
     * @throws IllegalArgumentException If subdomain is not a valid subdomain name.
     */
    @Override
    public final Jid atSubdomain(CharSequence subdomain) {
        return new FullJid(getLocal(), Objects.requireNonNull(subdomain) + "." + getDomain(), getResource(), false, true, null);
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

        return Objects.equals(getLocal(), other.getLocal())
                && Objects.equals(getDomain(), other.getDomain())
                && Objects.equals(getResource(), other.getResource());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getLocal(), getDomain(), getResource());
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
            final Collator collator = Collator.getInstance();
            int result;
            // First compare domain parts.
            if (getDomain() != null) {
                result = o.getDomain() != null ? collator.compare(getDomain(), o.getDomain()) : -1;
            } else {
                result = o.getDomain() != null ? 1 : 0;
            }
            // If the domains are equal, compare local parts.
            if (result == 0) {
                if (getLocal() != null) {
                    // If this local part is not null, but the other is null, move this down (1).
                    result = o.getLocal() != null ? collator.compare(getLocal(), o.getLocal()) : 1;
                } else {
                    // If this local part is null, but the other is not, move this up (-1).
                    result = o.getLocal() != null ? -1 : 0;
                }
            }
            // If the local parts are equal, compare resource parts.
            if (result == 0) {
                if (getResource() != null) {
                    // If this resource part is not null, but the other is null, move this down (1).
                    return o.getResource() != null ? collator.compare(getResource(), o.getResource()) : 1;
                } else {
                    // If this resource part is null, but the other is not, move this up (-1).
                    return o.getResource() != null ? -1 : 0;
                }
            }
            return result;
        } else {
            return -1;
        }
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
        return toString(getLocal(), getDomain(), getResource());
    }

    static String toString(String local, String domain, String resource) {
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
}
