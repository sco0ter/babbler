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

package org.xmpp.stanza.server;

import org.xmpp.stanza.AbstractPresence;
import org.xmpp.stanza.StanzaError;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
@XmlRootElement(name = "presence")
@XmlType(propOrder = {"from", "id", "to", "type", "status", "show", "priority", "extensions", "error"})
public final class Presence extends AbstractPresence {
    /**
     * Constructs an empty presence.
     */
    public Presence() {
    }

    /**
     * Constructs a presence of a specific type.
     *
     * @param type The type.
     */
    public Presence(Type type) {
        super(type);
    }

    /**
     * Constructs a presence with a specific 'show' attribute.
     *
     * @param show The 'show' attribute.
     */
    public Presence(Show show) {
        super(show);
    }


    @Override
    public Presence createError(StanzaError error) {
        Presence presence = new Presence(Type.ERROR);
        createError(presence, error);
        return presence;
    }
}
