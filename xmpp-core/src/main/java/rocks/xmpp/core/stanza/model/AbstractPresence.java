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

package rocks.xmpp.core.stanza.model;

import rocks.xmpp.core.Jid;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The implementation of the {@code <presence/>} element.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#presence-fundamentals">4.1.  Presence Fundamentals</a></cite></p>
 * <p>The concept of presence refers to an entity's availability for communication over a network. At the most basic level, presence is a boolean "on/off" variable that signals whether an entity is available or unavailable for communication (the terms "online" and "offline" are also used). In XMPP, an entity's availability is signaled when its client generates a {@code <presence/>} stanza with no 'type' attribute, and an entity's lack of availability is signaled when its client generates a {@code <presence/>} stanza whose 'type' attribute has a value of "unavailable".</p>
 * <p>XMPP presence typically follows a "publish-subscribe" or "observer" pattern, wherein an entity sends presence to its server, and its server then broadcasts that information to all of the entity's contacts who have a subscription to the entity's presence (in the terminology of [IMP-MODEL], an entity that generates presence is a "presentity" and the entities that receive presence are "subscribers"). A client generates presence for broadcast to all subscribed entities by sending a presence stanza to its server with no 'to' address, where the presence stanza has either no 'type' attribute or a 'type' attribute whose value is "unavailable". This kind of presence is called "broadcast presence".</p>
 * <p>After a client completes the preconditions specified in [XMPP-CORE], it can establish a "presence session" at its server by sending initial presence, where the presence session is terminated by sending unavailable presence. For the duration of its presence session, a connected resource (in the terminology of [XMPP-CORE]) is said to be an "available resource".</p>
 * <p>In XMPP, applications that combine messaging and presence functionality, the default type of communication for which presence signals availability is messaging; however, it is not necessary for XMPP applications to combine messaging and presence functionality, and they can provide standalone presence features without messaging (in addition, XMPP servers do not require information about network availability in order to successfully route message and IQ stanzas).</p>
 * </blockquote>
 * Please also refer to <a href="http://xmpp.org/rfcs/rfc6121.html#presence-syntax">4.7.  Presence Syntax</a>.
 *
 * @author Christian Schudt
 */
@XmlTransient
public abstract class AbstractPresence extends Stanza implements Comparable<AbstractPresence> {

    @XmlAnyElement(lax = true)
    private final List<Object> extensions = new CopyOnWriteArrayList<>();

    @XmlElement
    private final List<Status> status = new CopyOnWriteArrayList<>();

    @XmlElement
    private Byte priority;

    @XmlElement
    private Show show;

    @XmlAttribute
    private Type type;

    /**
     * Constructs an empty presence.
     */
    protected AbstractPresence() {
    }

    /**
     * Constructs an full presence with all possible values.
     */
    protected AbstractPresence(Type type, Show show, Jid to, Collection<Status> status, Byte priority, String id, Jid from, String language, StanzaError error) {
        super(to, from, id, language, error);
        this.show = show;
        this.type = type;
        this.show = show;
        if (status != null) {
            this.status.addAll(status);
        }
        this.priority = priority;
    }

    /**
     * Indicates, whether an entity is available.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#presence-fundamentals">4.1.  Presence Fundamentals</a></cite></p>
     * <p>In XMPP, an entity's availability is signaled when its client generates a {@code <presence/>} stanza with no 'type' attribute, and an entity's lack of availability is signaled when its client generates a {@code <presence/>} stanza whose 'type' attribute has a value of "unavailable".</p>
     * </blockquote>
     *
     * @return True, if the presence is available.
     */
    public final boolean isAvailable() {
        return type == null;
    }

    /**
     * Gets the {@code <show/>} element.
     *
     * @return The {@code <show/>} element.
     * @see Show
     * @see #setShow(Show)
     */
    public final Show getShow() {
        return show;
    }

    /**
     * Sets the {@code <show/>} element.
     *
     * @param show The {@code <show/>} element.
     * @see #getShow()
     * @deprecated Use constructor.
     */
    @Deprecated
    public final void setShow(Show show) {
        this.show = show;
    }

    /**
     * Gets the priority.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#presence-syntax-children-priority">4.7.2.3.  Priority Element</a></cite></p>
     * <p>The OPTIONAL {@code <priority/>} element contains non-human-readable XML character data that specifies the priority level of the resource. The value MUST be an integer between -128 and +127.</p>
     * </blockquote>
     *
     * @return The priority.
     * @see #setPriority(byte)
     */
    public final byte getPriority() {
        return priority != null ? priority : 0;
    }

    /**
     * Sets the priority.
     *
     * @param priority The priority.
     * @see #getPriority()
     * @deprecated Use constructor.
     */
    @Deprecated
    public final void setPriority(byte priority) {
        this.priority = priority;
    }

    /**
     * Gets the type of the presence.
     *
     * @return The type.
     * @see Type
     * @see #setType(Type)
     */
    public final Type getType() {
        return type;
    }

    /**
     * Sets the type of the presence.
     *
     * @param type The type.
     * @see #getType()
     * @deprecated Use constructor.
     */
    @Deprecated
    public final void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets the statuses.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#presence-syntax-children-status">4.7.2.2.  Status Element</a></cite></p>
     * <p>Multiple instances of the {@code <status/>} element MAY be included, but only if each instance possesses an 'xml:lang' attribute with a distinct language value.</p>
     * </blockquote>
     *
     * @return The statuses.
     * @see #getStatus()
     */
    public final List<Status> getStatuses() {
        return Collections.unmodifiableList(status);
    }

    /**
     * Gets the default body (which has no language attribute).
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#presence-syntax-children-status">4.7.2.2.  Status Element</a></cite></p>
     * <p>The OPTIONAL {@code <status/>} element contains human-readable XML character data specifying a natural-language description of an entity's availability. It is normally used in conjunction with the show element to provide a detailed description of an availability state (e.g., "In a meeting") when the presence stanza has no 'type' attribute.</p>
     * </blockquote>
     * <p>If there's no default status, but only statuses with a language attribute, the first status is returned.</p>
     * <p>If there are no statuses at all, null is returned.</p>
     *
     * @return The status or null.
     * @see #getStatuses()
     * @see #setStatus(String)
     */
    public final String getStatus() {
        for (Status status : this.status) {
            if (status.getLanguage() == null || status.getLanguage().isEmpty()) {
                return status.getText();
            }
        }
        if (!status.isEmpty()) {
            return status.get(0).getText();
        }
        return null;
    }

    /**
     * Sets the default status element.
     *
     * @param text The status text.
     * @see #getStatus() ()
     * @deprecated Use constructor.
     */
    @Deprecated
    public final void setStatus(String text) {
        if (text != null) {
            for (Status s : status) {
                if (s.getLanguage() == null || s.getLanguage().isEmpty()) {
                    s.setText(text);
                    return;
                }
            }
            this.status.add(new Status(text));
        } else {
            this.status.clear();
        }
    }

    /**
     * Gets all extensions.
     *
     * @return The extensions.
     */
    public final List<Object> getExtensions() {
        return extensions;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> T getExtension(Class<T> type) {
        for (Object extension : extensions) {
            if (type.isAssignableFrom(extension.getClass())) {
                return (T) extension;
            }
        }
        return null;
    }

    @Override
    public final int compareTo(AbstractPresence o) {
        if (o == null) {
            return -1;
        }
        if (isAvailable() && !o.isAvailable()) {
            return -1;
        } else if (!isAvailable() && o.isAvailable()) {
            return 1;
        }

        // First compare the priority.
        int result = Byte.compare(getPriority(), o.getPriority());
        // If priority is equal, compare the show element.
        if (result == 0) {
            // If we have no show attribute, but the other one has, we are available and are better than the other.
            if (getShow() == null && o.getShow() != null) {
                return -1;
            }
            // If both have no show element, presences are equal.
            else if (getShow() == null) {
                return 0;
            }
            // If we have a show element, but the other not, the other has higher priority.
            else if (o.getShow() == null) {
                return 1;
            } else {
                return getShow().compareTo(o.getShow());
            }
        }
        return result;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        if (type != null) {
            String sType = type.name();
            sb.append(sType.substring(0, 1)).append(sType.substring(1).toLowerCase()).append(" ");
        }
        sb.append("Presence");
        if (show != null) {
            sb.append(" ").append(show.name());
        }
        sb.append(super.toString());

        String status = getStatus();
        if (status != null) {
            sb.append(": ").append(status);
        }
        return sb.toString();
    }

    /**
     * Represents a {@code <presence/>} {@code <show/>} element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#presence-syntax-children-show">4.7.2.1.  Show Element</a></cite></p>
     * <p>The OPTIONAL {@code <show/>} element specifies the particular availability sub-state of an entity or a specific resource thereof.</p>
     * <p>If no {@code <show/>} element is provided, the entity is assumed to be online and available.</p>
     * </blockquote>
     */
    public enum Show {
        /**
         * The entity or resource is actively interested in chatting.
         */
        @XmlEnumValue(value = "chat")
        CHAT,
        /**
         * The entity or resource is temporarily away.
         */
        @XmlEnumValue(value = "away")
        AWAY,
        /**
         * The entity or resource is away for an extended period (xa = "eXtended Away").
         */
        @XmlEnumValue(value = "xa")
        XA,
        /**
         * The entity or resource is busy (dnd = "Do Not Disturb").
         */
        @XmlEnumValue(value = "dnd")
        DND
    }

    /**
     * Represents a {@code <presence/>} 'type' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#presence-syntax-type">4.7.1.  Type Attribute</a></cite></p>
     * <p>The absence of a 'type' attribute signals that the relevant entity is available for communication.</p>
     * <p>A 'type' attribute with a value of "unavailable" signals that the relevant entity is not available for communication.</p>
     * <p>The XMPP presence stanza is also used to negotiate and manage subscriptions to the presence of other entities. These tasks are completed via presence stanzas of type "subscribe", "unsubscribe", "subscribed", and "unsubscribed".</p>
     * <p>If a user and contact are associated with different XMPP servers, those servers also use a special presence stanza of type "probe" in order to determine the availability of the entity on the peer server.</p>
     * </blockquote>
     */
    @XmlType(name = "presenceType")
    public enum Type {
        /**
         * An error has occurred regarding processing of a previously sent presence stanza; if the presence stanza is of type "error", it MUST include an {@code <error/>} child element.
         */
        @XmlEnumValue(value = "error")
        ERROR,
        /**
         * A request for an entity's current presence; SHOULD be generated only by a server on behalf of a user.
         */
        @XmlEnumValue(value = "probe")
        PROBE,
        /**
         * The sender wishes to subscribe to the recipient's presence.
         */
        @XmlEnumValue(value = "subscribe")
        SUBSCRIBE,
        /**
         * The sender has allowed the recipient to receive their presence.
         */
        @XmlEnumValue(value = "subscribed")
        SUBSCRIBED,
        /**
         * The sender is no longer available for communication.
         */
        @XmlEnumValue(value = "unavailable")
        UNAVAILABLE,
        /**
         * The sender is unsubscribing from the receiver's presence.
         */
        @XmlEnumValue(value = "unsubscribe")
        UNSUBSCRIBE,
        /**
         * The subscription request has been denied or a previously granted subscription has been canceled.
         */
        @XmlEnumValue(value = "unsubscribed")
        UNSUBSCRIBED
    }

    /**
     * The implementation of a presence's {@code <status/>} element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#presence-syntax-children-status">4.7.2.2.  Status Element</a></cite></p>
     * <p>The OPTIONAL {@code <status/>} element contains human-readable XML character data specifying a natural-language description of an entity's availability. It is normally used in conjunction with the show element to provide a detailed description of an availability state (e.g., "In a meeting") when the presence stanza has no 'type' attribute.</p>
     * </blockquote>
     */
    public static final class Status {
        @XmlAttribute(name = "lang", namespace = XMLConstants.XML_NS_URI)
        private String language;

        @XmlValue
        private String text;

        /**
         * Private default constructor, needed for unmarshalling.
         */
        @SuppressWarnings("unused")
        private Status() {
        }

        /**
         * Constructs a default status.
         *
         * @param text The text.
         */
        public Status(String text) {
            this.text = text;
        }

        /**
         * Constructs a status with a language attribute.
         *
         * @param text     The text
         * @param language The language.
         */
        public Status(String text, String language) {
            this.text = text;
            this.language = language;
        }

        /**
         * Gets the language.
         *
         * @return The language.
         */
        public String getLanguage() {
            return language;
        }

        /**
         * Sets the language.
         *
         * @param language The language.
         * @deprecated Use constructor.
         */
        @Deprecated
        public void setLanguage(String language) {
            this.language = language;
        }

        /**
         * Gets the text.
         *
         * @return The text.
         */
        public String getText() {
            return text;
        }

        /**
         * Sets the text.
         *
         * @param text The text.
         * @deprecated Use constructor.
         */
        @Deprecated
        public void setText(String text) {
            this.text = text;
        }
    }
}
