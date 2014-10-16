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

package rocks.xmpp.extensions.muc.model;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Date;

/**
 * The discussion history.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#enter-history">7.2.14 Discussion History</a>
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#enter-managehistory">7.2.15 Managing Discussion History</a>
 */
public final class History {
    @XmlAttribute(name = "maxchars")
    private Integer maxChars;

    @XmlAttribute(name = "maxstanzas")
    private Integer maxStanzas;

    @XmlAttribute(name = "seconds")
    private Integer seconds;

    @XmlAttribute(name = "since")
    private Date since;

    private History() {
    }

    /**
     * Limit the total number of characters in the history to "X" (where the character count is the characters of the complete XML stanzas, not only their XML character data).
     *
     * @param maxChars The maximal character count.
     * @return The history.
     */
    public static History forMaxChars(int maxChars) {
        History history = new History();
        history.maxChars = maxChars;
        return history;
    }

    /**
     * Limit the total number of messages in the history to "X".
     *
     * @param maxMessages The maximal number of messages.
     * @return The history.
     */
    public static History forMaxMessages(int maxMessages) {
        History history = new History();
        history.maxStanzas = maxMessages;
        return history;
    }

    /**
     * Send only the messages received in the last "X" seconds.
     *
     * @param seconds The seconds.
     * @return The history.
     */
    public static History forSeconds(int seconds) {
        History history = new History();
        history.seconds = seconds;
        return history;
    }

    /**
     * Send only the messages received since the date.
     *
     * @param date The date.
     * @return The history.
     */
    public static History since(Date date) {
        History history = new History();
        history.since = date;
        return history;
    }

    /**
     * Send no history.
     *
     * @return The history.
     */
    public static History none() {
        History history = new History();
        history.maxChars = 0;
        return history;
    }
}
