/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Christian Schudt
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

package rocks.xmpp.extensions.chatmarkers.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * Represents a chat marker for marking the last received, displayed and acknowledged message in a chat.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0333.html">XEP-0333: Chat Markers</a>
 * @see <a href="http://xmpp.org/extensions/xep-0333.html#schema">XML Schema</a>
 */
@XmlTransient
@XmlSeeAlso({ChatMarker.Markable.class, ChatMarker.Received.class, ChatMarker.Displayed.class, ChatMarker.Acknowledged.class})
public abstract class ChatMarker {

    /**
     * Indicates that a message can be marked with a Chat Marker and is therefore a "markable message".
     */
    public static final ChatMarker MARKABLE = new Markable();

    /**
     * urn:xmpp:chat-markers:0
     */
    public static final String NAMESPACE = "urn:xmpp:chat-markers:0";

    private ChatMarker() {
    }

    @Override
    public final String toString() {
        return "Chat marker: " + getClass().getSimpleName().toLowerCase();
    }

    @XmlTransient
    private abstract static class IdChatMarker extends ChatMarker {
        @XmlAttribute
        private final String id;

        private IdChatMarker(final String id) {
            this.id = id;
        }

        /**
         * Gets the message id, which this chat marker is referring to.
         *
         * @return The message id.
         */
        public final String getId() {
            return id;
        }
    }

    /**
     * The implementation of the {@code <markable/>} element.
     */
    @XmlRootElement
    @XmlType(factoryMethod = "create")
    public static final class Markable extends ChatMarker {

        private Markable() {
        }

        private static Markable create() {
            return (Markable) ChatMarker.MARKABLE;
        }
    }

    /**
     * The implementation of the {@code <received/>} element.
     * Indicates that the message has been received by a client.
     */
    @XmlRootElement
    public static final class Received extends IdChatMarker {

        private Received() {
            super(null);
        }

        public Received(final String id) {
            super(Objects.requireNonNull(id));
        }
    }

    /**
     * The implementation of the {@code <displayed/>} element.
     * Indicates that the message has been displayed to a user in a active chat and not in a system notification.
     */
    @XmlRootElement
    public static final class Displayed extends IdChatMarker {
        private Displayed() {
            super(null);
        }

        public Displayed(final String id) {
            super(Objects.requireNonNull(id));
        }
    }

    /**
     * The implementation of the {@code <acknowledged/>} element.
     * Indicates the message has been acknowledged by some user interaction e.g. pressing an acknowledgement button.
     */
    @XmlRootElement
    public static final class Acknowledged extends IdChatMarker {
        private Acknowledged() {
            super(null);
        }

        public Acknowledged(final String id) {
            super(Objects.requireNonNull(id));
        }
    }
}
