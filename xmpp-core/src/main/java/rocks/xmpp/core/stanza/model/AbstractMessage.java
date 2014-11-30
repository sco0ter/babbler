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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The implementation of the {@code <message/>} element.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message">5.  Exchanging Messages</a></cite></p>
 * <p>Once a client has authenticated with a server and bound a resource to an XML stream as described in [XMPP-CORE], an XMPP server will route XML stanzas to and from that client. One kind of stanza that can be exchanged is {@code <message/>} (if, that is, messaging functionality is enabled on the server). Exchanging messages is a basic use of XMPP and occurs when a user generates a message stanza that is addressed to another entity. As defined under Section 8, the sender's server is responsible for delivering the message to the intended recipient (if the recipient is on the same local server) or for routing the message to the recipient's server (if the recipient is on a remote server). Thus a message stanza is used to "push" information to another entity.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlTransient
public abstract class AbstractMessage extends Stanza {

    @XmlElement(name = "body")
    private final List<Body> body = new CopyOnWriteArrayList<>();

    @XmlElement(name = "subject")
    private final List<Subject> subject = new CopyOnWriteArrayList<>();

    @XmlAnyElement(lax = true)
    private final List<Object> extensions = new CopyOnWriteArrayList<>();

    @XmlAttribute
    private Type type;

    @XmlElement
    private Thread thread;

    @SuppressWarnings("unused")
    protected AbstractMessage() {
    }

    /**
     * Constructs an empty message.
     *
     * @param to The recipient.
     */
    protected AbstractMessage(Jid to) {
        this.to = to;
    }

    /**
     * Constructs a message with a type.
     *
     * @param to   The recipient.
     * @param type The message type.
     */
    protected AbstractMessage(Jid to, Type type) {
        this.to = to;
        this.type = type;
    }

    /**
     * Constructs a message with body and type.
     *
     * @param to   The recipient.
     * @param body The message body.
     * @param type The message type.
     */
    protected AbstractMessage(Jid to, Type type, String body) {
        this.to = to;
        this.body.add(new Body(body));
        this.type = type;
    }

    /**
     * Gets the bodies.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-body">5.2.3.  Body Element</a></cite></p>
     * <p>Multiple instances of the {@code <body/>} element MAY be included in a message stanza for the purpose of providing alternate versions of the same body, but only if each instance possesses an 'xml:lang' attribute with a distinct language value.</p>
     * </blockquote>
     *
     * @return The bodies.
     * @see #getBody()
     */
    public final List<Body> getBodies() {
        return body;
    }

    /**
     * Gets the default body (which has no language attribute).
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-body">5.2.3.  Body Element</a></cite></p>
     * <p>The {@code <body/>} element contains human-readable XML character data that specifies the textual contents of the message; this child element is normally included but is OPTIONAL.</p>
     * </blockquote>
     * If there's no default body, but only bodies with a language attribute, the first body is returned.
     * <p>
     * If there are no bodies at all, null is returned.
     * </p>
     *
     * @return The body or null.
     * @see #getBodies()
     * @see #setBody(String)
     */
    public final String getBody() {
        for (Body body : this.body) {
            if (body.getLanguage() == null || body.getLanguage().isEmpty()) {
                return body.getText();
            }
        }
        if (!body.isEmpty()) {
            return body.get(0).getText();
        }
        return null;
    }

    /**
     * Sets the default body element.
     *
     * @param body The body text.
     * @see #getBody()
     */
    public final void setBody(String body) {
        if (body != null) {
            for (Body b : this.body) {
                if (b.getLanguage() == null || b.getLanguage().isEmpty()) {
                    b.setText(body);
                    return;
                }
            }
            this.body.add(new Body(body));
        } else {
            this.body.clear();
        }
    }

    /**
     * Gets the subjects.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-subject">5.2.4.  Subject Element</a></cite></p>
     * <p>Multiple instances of the {@code <subject/>} element MAY be included for the purpose of providing alternate versions of the same subject, but only if each instance possesses an 'xml:lang' attribute with a distinct language value.</p>
     * </blockquote>
     *
     * @return The subjects.
     * @see #getSubject()
     */
    public final List<Subject> getSubjects() {
        return subject;
    }

    /**
     * Gets the default subject (which has no language attribute).
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-subject">5.2.4.  Subject Element</a></cite></p>
     * <p>The {@code <subject/>} element contains human-readable XML character data that specifies the topic of the message.</p>
     * </blockquote>
     * <p>If there's no default subject, but only subjects with a language attribute, the first subject is returned.</p>
     * <p>If there are no subjects at all, null is returned.</p>
     *
     * @return The subject or null.
     * @see #getSubjects()
     * @see #setSubject(String)
     */
    public final String getSubject() {
        for (Subject subject : this.subject) {
            if (subject.getLanguage() == null || subject.getLanguage().isEmpty()) {
                return subject.getText();
            }
        }
        if (!subject.isEmpty()) {
            return subject.get(0).getText();
        }
        return null;
    }

    /**
     * Sets the default subject.
     *
     * @param subject The subject text.
     * @see #getSubject()
     */
    public final void setSubject(String subject) {
        if (subject != null) {
            for (Subject s : this.subject) {
                if (s.getLanguage() == null || s.getLanguage().isEmpty()) {
                    s.setText(subject);
                    return;
                }
            }
            this.subject.add(new Subject(subject));
        } else {
            this.subject.clear();
        }
    }

    /**
     * Gets the message type.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-type">5.2.2.  Type Attribute</a></cite></p>
     * <p>Common uses of the message stanza in instant messaging applications include: single messages; messages sent in the context of a one-to-one chat session; messages sent in the context of a multi-user chat room; alerts, notifications, or other information to which no reply is expected; and errors. These uses are differentiated via the 'type' attribute.</p>
     * </blockquote>
     *
     * @return The message type.
     * @see #setType(Type)
     */
    public final Type getType() {
        return type;
    }

    /**
     * Sets the message type.
     *
     * @param type The message type.
     * @see #getType()
     */
    public final void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets the thread.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-thread">5.2.5.  Thread Element</a></cite></p>
     * <p>The primary use of the XMPP {@code <thread/>} element is to uniquely identify a conversation thread or "chat session" between two entities instantiated by {@code <message/>} stanzas of type 'chat'. However, the XMPP {@code <thread/>} element MAY also be used to uniquely identify an analogous thread between two entities instantiated by {@code <message/>} stanzas of type 'headline' or 'normal', or among multiple entities in the context of a multi-user chat room instantiated by {@code <message/>} stanzas of type 'groupchat'. It MAY also be used for {@code <message/>} stanzas not related to a human conversation, such as a game session or an interaction between plugins. The {@code <thread/>} element is not used to identify individual messages, only conversations or messaging sessions.</p>
     * </blockquote>
     *
     * @return The thread.
     * @see #setThread(String)
     */
    public final String getThread() {
        if (thread != null) {
            return thread.value;
        }
        return null;
    }

    /**
     * Sets the thread.
     *
     * @param thread The thread.
     * @see #getThread()
     */
    public final void setThread(String thread) {
        if (this.thread == null) {
            this.thread = new Thread();
        }
        this.thread.value = thread;
    }

    /**
     * Gets the parent thread.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-thread">5.2.5.  Thread Element</a></cite></p>
     * <p>The {@code <thread/>} element MAY possess a 'parent' attribute that identifies another thread of which the current thread is an offshoot or child. The 'parent' attribute MUST conform to the syntax of the {@code <thread/>} element itself and its value MUST be different from the XML character data of the {@code <thread/>} element on which the 'parent' attribute is included.</p>
     * </blockquote>
     *
     * @return The parent thread.
     * @see #setParentThread(String)
     */
    public final String getParentThread() {
        if (thread != null) {
            return thread.parent;
        }
        return null;
    }

    /**
     * Sets the parent thread.
     *
     * @param parent The parent thread.
     * @see #getParentThread()
     */
    public final void setParentThread(String parent) {
        if (this.thread == null) {
            this.thread = new Thread();
        }
        this.thread.parent = parent;
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
            if (extension.getClass() == type) {
                return (T) extension;
            }
        }
        return null;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        if (type != null) {
            String sType = type.name();
            sb.append(sType.substring(0, 1)).append(sType.substring(1).toLowerCase()).append(" ");
        }
        sb.append("Message");
        sb.append(super.toString());
        String body = getBody();
        if (body != null) {
            sb.append(": ").append(body);
        }
        return sb.toString();
    }

    /**
     * Represents a {@code <message/>} 'type' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-type">5.2.2.  Type Attribute</a></cite></p>
     * <p>Common uses of the message stanza in instant messaging applications include: single messages; messages sent in the context of a one-to-one chat session; messages sent in the context of a multi-user chat room; alerts, notifications, or other information to which no reply is expected; and errors. These uses are differentiated via the 'type' attribute.</p>
     * </blockquote>
     */
    @XmlType(name = "messageType")
    public enum Type {
        /**
         * The message is a standalone message that is sent outside the context of a one-to-one conversation or groupchat, and to which it is expected that the recipient will reply. Typically a receiving client will present a message of type "normal" in an interface that enables the recipient to reply, but without a conversation history. The default value of the 'type' attribute is "normal".
         */
        @XmlEnumValue(value = "normal")
        NORMAL,
        /**
         * The message is sent in the context of a one-to-one chat session. Typically an interactive client will present a message of type "chat" in an interface that enables one-to-one chat between the two parties, including an appropriate conversation history.
         */
        @XmlEnumValue(value = "chat")
        CHAT,
        /**
         * The message provides an alert, a notification, or other transient information to which no reply is expected (e.g., news headlines, sports updates, near-real-time market data, or syndicated content). Because no reply to the message is expected, typically a receiving client will present a message of type "headline" in an interface that appropriately differentiates the message from standalone messages, chat messages, and groupchat messages (e.g., by not providing the recipient with the ability to reply). If the 'to' address is the bare JID, the receiving server SHOULD deliver the message to all of the recipient's available resources with non-negative presence priority and MUST deliver the message to at least one of those resources; if the 'to' address is a full JID and there is a matching resource, the server MUST deliver the message to that resource; otherwise the server MUST either silently ignore the message or return an error.
         */
        @XmlEnumValue(value = "headline")
        HEADLINE,
        /**
         * The message is sent in the context of a multi-user chat environment (similar to that of [IRC]). Typically a receiving client will present a message of type "groupchat" in an interface that enables many-to-many chat between the parties, including a roster of parties in the chatroom and an appropriate conversation history. For detailed information about XMPP-based groupchat, refer to [XEP-0045].
         */
        @XmlEnumValue(value = "groupchat")
        GROUPCHAT,
        /**
         * The message is generated by an entity that experiences an error when processing a message received from another entity. A client that receives a message of type "error" SHOULD present an appropriate interface informing the original sender regarding the nature of the error.
         */
        @XmlEnumValue(value = "error")
        ERROR
    }

    /**
     * The implementation of a message's {@code <body/>} element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-body">5.2.3.  Body Element</a></cite></p>
     * <p>The {@code <body/>} element contains human-readable XML character data that specifies the textual contents of the message; this child element is normally included but is OPTIONAL.</p>
     * <p>There are no attributes defined for the {@code <body/>} element, with the exception of the 'xml:lang' attribute. Multiple instances of the {@code <body/>} element MAY be included in a message stanza for the purpose of providing alternate versions of the same body, but only if each instance possesses an 'xml:lang' attribute with a distinct language value.</p>
     * </blockquote>
     */
    public static final class Body {
        @XmlAttribute(name = "lang", namespace = XMLConstants.XML_NS_URI)
        private String language;

        @XmlValue
        private String text;

        /**
         * Private default constructor, needed for unmarshalling.
         */
        @SuppressWarnings("unused")
        private Body() {
        }

        /**
         * Constructs a default body.
         *
         * @param text The text.
         */
        public Body(String text) {
            this.text = text;
        }

        /**
         * Constructs a body with a language attribute.
         *
         * @param text     The text
         * @param language The language.
         */
        public Body(String text, String language) {
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
         */
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
         */
        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    /**
     * The implementation of a message's {@code <subject/>} element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-subject">5.2.4.  Subject Element</a></cite></p>
     * <p>The {@code <subject/>} element contains human-readable XML character data that specifies the topic of the message.</p>
     * <p>There are no attributes defined for the {@code <subject/>} element, with the exception of the 'xml:lang' attribute inherited from [XML]. Multiple instances of the {@code <subject/>} element MAY be included for the purpose of providing alternate versions of the same subject, but only if each instance possesses an 'xml:lang' attribute with a distinct language value.</p>
     * </blockquote>
     */
    public static final class Subject {
        @XmlAttribute(name = "lang", namespace = XMLConstants.XML_NS_URI)
        private String language;

        @XmlValue
        private String text;

        /**
         * Private default constructor, needed for unmarshalling.
         */
        @SuppressWarnings("unused")
        private Subject() {

        }

        /**
         * Constructs a default subject.
         *
         * @param text The text.
         */
        public Subject(String text) {
            this.text = text;
        }

        /**
         * Constructs a subject with a language attribute.
         *
         * @param text     The text
         * @param language The language.
         */
        public Subject(String text, String language) {
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
         */
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
         */
        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    /**
     * The implementation of the message's {@code <thread/>} element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-syntax-thread">5.2.5.  Thread Element</a></cite></p>
     * <p>The primary use of the XMPP {@code <thread/>} element is to uniquely identify a conversation thread or "chat session" between two entities instantiated by {@code <message/>} stanzas of type 'chat'. However, the XMPP {@code <thread/>} element MAY also be used to uniquely identify an analogous thread between two entities instantiated by {@code <message/>} stanzas of type 'headline' or 'normal', or among multiple entities in the context of a multi-user chat room instantiated by {@code <message/>} stanzas of type 'groupchat'. It MAY also be used for {@code <message/>} stanzas not related to a human conversation, such as a game session or an interaction between plugins. The {@code <thread/>} element is not used to identify individual messages, only conversations or messaging sessions.</p>
     * </blockquote>
     */
    private static final class Thread {

        @XmlAttribute(name = "parent")
        private String parent;

        @XmlValue
        private String value;

        private Thread() {
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
