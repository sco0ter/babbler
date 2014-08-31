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

package org.xmpp.extension.rtt;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * The implementation of the {@code <rtt/>} element in the {@code urn:xmpp:rtt:0} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0301.html">XEP-0301: In-Band Real Time Text</a>
 * @see <a href="http://xmpp.org/extensions/xep-0301.html#xml_schema">XML Schema</a>
 */
@XmlRootElement(name = "rtt")
public final class RealTimeText {
    static final String NAMESPACE = "urn:xmpp:rtt:0";

    @XmlElements({
            @XmlElement(name = "t", type = InsertText.class),
            @XmlElement(name = "e", type = EraseText.class),
            @XmlElement(name = "w", type = WaitInterval.class),
    })
    private List<Action> actions;

    @XmlAttribute(name = "seq")
    private Long sequence;

    @XmlAttribute(name = "event")
    private Event event;

    @XmlAttribute(name = "id")
    private String id;

    public RealTimeText() {

    }

    public RealTimeText(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public Long getSequence() {
        return sequence;
    }

    public List<Action> getActions() {
        return actions;
    }

    public enum Event {
        /**
         * Begin a new real-time message.
         */
        @XmlEnumValue(value = "new")
        NEW,
        /**
         * Re-initialize the real-time message.
         */
        @XmlEnumValue(value = "reset")
        RESET,
        /**
         * Modify existing real-time message.
         */
        @XmlEnumValue(value = "edit")
        EDIT,
        /**
         * Signals activation of real-time text.
         */
        @XmlEnumValue(value = "init")
        INIT,
        /**
         * Signals deactivation of real-time text.
         */
        @XmlEnumValue(value = "cancel")
        CANCEL
    }

    public static abstract class Action {
        @XmlValue
        protected String value;
    }

    /**
     * The implementation of the {@code <e/>} element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0301.html#element_t_insert_text">4.6.3.1 Element {@code <t/>} - Insert Text</a></cite></p>
     * <p>Supports the transmission of text, including key presses, and text block inserts.</p>
     * </blockquote>
     */
    public static final class InsertText extends Action {
        @XmlAttribute(name = "p")
        private Long position;

        public String getValue() {
            return value;
        }

        public Long getPosition() {
            return position;
        }
    }

    /**
     * The implementation of the {@code <e/>} element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0301.html#element_e_erase_text">4.6.3.2 Element {@code <e/>} - Erase Text</a></cite></p>
     * <p>Supports the behavior of backspace key presses. Text is removed towards beginning of the message. This element is also used for all delete operations, including the backspace key, the delete key, and text block deletes.</p>
     * </blockquote>
     */
    public static final class EraseText extends Action {

        @XmlAttribute(name = "p")
        private Long position;

        @XmlAttribute(name = "n")
        private Long numberOfCharacters;

        /**
         * Gets the position until text is erased.
         *
         * @return The position.
         */
        public Long getPosition() {
            return position;
        }

        /**
         * Indicates, how many characters are removed before the position.
         *
         * @return The number of characters to be removed.
         * @see #getPosition()
         */
        public Long getNumberOfCharacters() {
            return numberOfCharacters;
        }
    }

    /**
     * The implementation of the {@code <w/>} element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/extensions/xep-0301.html#element_w_wait_interval">4.6.3.3 Element {@code <w/>} - Wait Interval</a></cite></p>
     * <p>Allow for the transmission of intervals, between real-time text actions, to recreate the pauses between key presses.</p>
     * </blockquote>
     */
    public static final class WaitInterval extends Action {
        @XmlAttribute(name = "n")
        private Long milliSeconds;

        public Long getMilliSeconds() {
            return milliSeconds;
        }
    }
}
