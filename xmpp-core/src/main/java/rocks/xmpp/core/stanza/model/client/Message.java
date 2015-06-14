/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.core.stanza.model.client;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.AbstractMessage;
import rocks.xmpp.core.stanza.model.StanzaError;
import rocks.xmpp.core.stanza.model.Text;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.model.StreamElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.Collections;

/**
 * The implementation of the {@code <message/>} element for the client namespace ('jabber:client').
 *
 * @author Christian Schudt
 */
@XmlRootElement
@XmlType(propOrder = {"from", "id", "to", "type", "lang", "subject", "body", "thread", "extensions", "error"})
public final class Message extends AbstractMessage implements StreamElement {

    public Message() {
        this(null);
    }

    /**
     * Constructs an empty message.
     *
     * @param to The recipient.
     */
    public Message(Jid to) {
        this(to, null);
    }

    /**
     * Constructs a message with a type.
     *
     * @param to   The recipient.
     * @param type The message type.
     */
    public Message(Jid to, Type type) {
        this(to, type, null);
    }

    /**
     * Constructs a message with body and type.
     *
     * @param to   The recipient.
     * @param body The message body.
     * @param type The message type.
     */
    public Message(Jid to, Type type, String body) {
        this(to, type, body, null);
    }

    /**
     * Constructs a message with body and type.
     *
     * @param to      The recipient.
     * @param body    The message body.
     * @param type    The message type.
     * @param subject The subject.
     */
    public Message(Jid to, Type type, String body, String subject) {
        this(to, type, body, subject, null);
    }

    /**
     * Constructs a message with body and type.
     *
     * @param to      The recipient.
     * @param body    The message body.
     * @param type    The message type.
     * @param subject The subject.
     * @param thread  The thread.
     */
    public Message(Jid to, Type type, String body, String subject, String thread) {
        this(to, type, body != null ? Collections.singleton(new Text(body)) : Collections.emptyList(), subject != null ? Collections.singleton(new Text(subject)) : Collections.emptyList(), thread, null, null, null, null, null, null);
    }

    /**
     * Constructs a message with body and type.
     *
     * @param to           The recipient.
     * @param body         The message body.
     * @param type         The message type.
     * @param subject      The subject.
     * @param thread       The thread.
     * @param parentThread The parent thread.
     * @param from         The sender.
     * @param id           The id.
     * @param language     The language.
     * @param extensions   The extensions.
     * @param error        The error.
     */
    public Message(Jid to, Type type, String body, String subject, String thread, String parentThread, String id, Jid from, String language, Collection<?> extensions, StanzaError error) {
        this(to, type, body != null ? Collections.singleton(new Text(body)) : Collections.emptyList(), subject != null ? Collections.singleton(new Text(subject)) : Collections.emptyList(), thread, parentThread, id, from, language, extensions, error);
    }

    /**
     * Constructs a message with body and type.
     *
     * @param to           The recipient.
     * @param bodies       The message bodies.
     * @param type         The message type.
     * @param subjects     The subjects.
     * @param thread       The thread.
     * @param parentThread The parent thread.
     * @param from         The sender.
     * @param id           The id.
     * @param language     The language.
     * @param extensions   The extensions.
     * @param error        The error.
     */
    public Message(Jid to, Type type, Collection<Text> bodies, Collection<Text> subjects, String thread, String parentThread, String id, Jid from, String language, Collection<?> extensions, StanzaError error) {
        super(to, type, bodies, subjects, thread, parentThread, from, id, language, extensions, error);
    }

    @Override
    public final Message createError(StanzaError error) {
        return new Message(getFrom(), Type.ERROR, getBodies(), getSubjects(), getThread(), getParentThread(), getId(), getTo(), getLanguage(), getExtensions(), error);
    }

    @Override
    public final Message createError(Condition condition) {
        return createError(new StanzaError(condition));
    }

    @Override
    public final Message withFrom(Jid from) {
        return new Message(getTo(), getType(), getBodies(), getSubjects(), getThread(), getParentThread(), getId(), from, getLanguage(), getExtensions(), getError());
    }
}
