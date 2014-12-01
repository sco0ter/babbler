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

package rocks.xmpp.extensions.chatstates.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0085.html">XEP-0085: Chat State Notifications</a>
 * @see <a href="http://xmpp.org/extensions/xep-0085.html#schema">XML Schema</a>
 */
@XmlTransient
public abstract class ChatState {

    /**
     * User is actively participating in the chat session.
     */
    public static final ChatState ACTIVE = new Active();

    /**
     * User is composing a message.
     */
    public static final ChatState COMPOSING = new Composing();

    /**
     * User has effectively ended their participation in the chat session.
     */
    public static final ChatState GONE = new Gone();

    /**
     * User has not been actively participating in the chat session.
     */
    public static final ChatState INACTIVE = new Inactive();

    /**
     * User had been composing but now has stopped.
     */
    public static final ChatState PAUSED = new Paused();

    /**
     * http://jabber.org/protocol/chatstates
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/chatstates";

    /**
     * The implementation of the {@code <active/>} element in the {@code http://jabber.org/protocol/chatstates} namespace, which represents an active chat state.
     */
    @XmlRootElement
    public static final class Active extends ChatState {
        private Active() {
        }
    }

    /**
     * The implementation of the {@code <composing/>} element in the {@code http://jabber.org/protocol/chatstates} namespace, which represents a composing chat state.
     */
    @XmlRootElement
    public static final class Composing extends ChatState {
        private Composing() {
        }
    }

    /**
     * The implementation of the {@code <gone/>} element in the {@code http://jabber.org/protocol/chatstates} namespace, which represents a gone chat state.
     */
    @XmlRootElement
    public static final class Gone extends ChatState {
        private Gone() {
        }
    }

    /**
     * The implementation of the {@code <inactive/>} element in the {@code http://jabber.org/protocol/chatstates} namespace, which represents an inactive chat state.
     */
    @XmlRootElement
    public static final class Inactive extends ChatState {
        private Inactive() {
        }
    }

    /**
     * The implementation of the {@code <paused/>} element in the {@code http://jabber.org/protocol/chatstates} namespace, which represents a paused chat state.
     */
    @XmlRootElement
    public static final class Paused extends ChatState {
        private Paused() {
        }
    }
}
