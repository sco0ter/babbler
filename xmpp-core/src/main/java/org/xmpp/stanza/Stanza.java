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

package org.xmpp.stanza;

import org.xmpp.Jid;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The abstract base class for a XML stanza.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas">8.  XML Stanzas</a></cite>
 * <p>After a client and a server (or two servers) have completed stream negotiation, either party can send XML stanzas. Three kinds of XML stanza are defined for the 'jabber:client' and 'jabber:server' namespaces: {@code <message/>}, {@code <presence/>}, and {@code <iq/>}.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlTransient
public abstract class Stanza {

    @XmlAttribute
    protected Jid from;

    @XmlAttribute
    protected String id;

    @XmlAttribute
    protected Jid to;

    @XmlAttribute(name = "lang", namespace = XMLConstants.XML_NS_URI)
    protected String language;

    @XmlElement
    private StanzaError error;

    /**
     * Gets the stanza's 'to' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-to">8.1.1.  to</a></cite></p>
     * <p>The 'to' attribute specifies the JID of the intended recipient for the stanza.</p>
     * </blockquote>
     *
     * @return The JID.
     * @see #setTo(org.xmpp.Jid)
     */
    public final Jid getTo() {
        return to;
    }

    /**
     * Sets the stanza's 'to' attribute.
     *
     * @param to The JID.
     * @see #getTo()
     */
    public final void setTo(Jid to) {
        this.to = to;
    }

    /**
     * Gets the stanza's 'id' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-id">8.1.3.  id</a></cite></p>
     * <p>The 'id' attribute is used by the originating entity to track any response or error stanza that it might receive in relation to the generated stanza from another entity (such as an intermediate server or the intended recipient).</p>
     * <p>It is up to the originating entity whether the value of the 'id' attribute is unique only within its current stream or unique globally.</p>
     * <p>For {@code <message/>} and {@code <presence/>} stanzas, it is RECOMMENDED for the originating entity to include an 'id' attribute; for {@code <iq/>} stanzas, it is REQUIRED.</p>
     * <p>If the generated stanza includes an 'id' attribute then it is REQUIRED for the response or error stanza to also include an 'id' attribute, where the value of the 'id' attribute MUST match that of the generated stanza.</p>
     * </blockquote>
     *
     * @return The id.
     * @see #setId(String)
     */
    public final String getId() {
        return id;
    }

    /**
     * Sets the stanza's 'id' attribute.
     *
     * @param id The id.
     * @see #getId()
     */
    public final void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the stanza's 'from' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-from">8.1.2.  from</a></cite></p>
     * <p>The 'from' attribute specifies the JID of the sender.</p>
     * </blockquote>
     *
     * @return The JID.
     * @see #setFrom(org.xmpp.Jid)
     */
    public final Jid getFrom() {
        return from;
    }

    /**
     * Sets the stanza's 'from' attribute.
     *
     * @param from The JID.
     * @see #getFrom()
     */
    public final void setFrom(Jid from) {
        this.from = from;
    }

    /**
     * Gets the stanza's 'xml:lang' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-lang">8.1.5.  xml:lang</a></cite></p>
     * <p>A stanza SHOULD possess an 'xml:lang' attribute (as defined in Section 2.12 of [XML]) if the stanza contains XML character data that is intended to be presented to a human user (as explained in [CHARSETS], "internationalization is for humans"). The value of the 'xml:lang' attribute specifies the default language of any such human-readable XML character data.</p>
     * </blockquote>
     *
     * @return The language.
     * @see #setLanguage(String)
     */
    public final String getLanguage() {
        return language;
    }

    /**
     * Sets the stanza's 'xml:lang' attribute.
     *
     * @param language The language.
     * @see #getLanguage()
     */
    public final void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets the stanza's 'error' element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error">8.3.  Stanza Errors</a></cite></p>
     * <div>
     * Stanza-related errors are handled in a manner similar to stream errors. Unlike stream errors, stanza errors are recoverable; therefore, they do not result in termination of the XML stream and underlying TCP connection. Instead, the entity that discovers the error condition returns an error stanza, which is a stanza that:
     * <ul>
     * <li>is of the same kind (message, presence, or IQ) as the generated stanza that triggered the error</li>
     * <li>has a 'type' attribute set to a value of "error"</li>
     * <li>typically swaps the 'from' and 'to' addresses of the generated stanza</li>
     * <li>mirrors the 'id' attribute (if any) of the generated stanza that triggered the error</li>
     * <li>contains an {@code <error/>} child element that specifies the error condition and therefore provides a hint regarding actions that the sender might be able to take in an effort to remedy the error (however, it is not always possible to remedy the error)</li>
     * </ul>
     * </div>
     * </blockquote>
     *
     * @return The stanza error.
     * @see #setError(StanzaError)
     */
    public final StanzaError getError() {
        return error;
    }

    /**
     * Sets the stanza's 'error' element.
     *
     * @param stanzaError The stanza error.
     * @see #getError()
     */
    public final void setError(StanzaError stanzaError) {
        this.error = stanzaError;
    }

    /**
     * Creates an error response for this stanza.
     *
     * @param error The error which is appended to the stanza.
     * @return The error response.
     * @see #getError()
     * @see #createError(Stanza, StanzaError)
     */
    public abstract Stanza createError(StanzaError error);

    /**
     * Swaps the 'from' and 'to' addresses from the generated stanza and adds a 'by' attribute to the error.
     *
     * @param stanza The stanza to create the error for.
     * @param error  The error.
     * @see #createError(StanzaError)
     */
    protected void createError(Stanza stanza, StanzaError error) {
        stanza.setId(id);
        stanza.setError(error);
        // The entity that returns an error stanza MAY pass along its JID to the sender of the generated stanza (e.g., for diagnostic or tracking purposes) through the addition of a 'by' attribute to the <error/> child element.
        error.setBy(to);
        stanza.setFrom(to);
        stanza.setTo(from);
    }

    /**
     * Gets an extension by type.
     *
     * @param type The class.
     * @param <T>  The type.
     * @return The extension or null.
     */
    public abstract <T> T getExtension(Class<T> type);
}
