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

package org.xmpp.extension.filetransfer;

import java.util.Date;

/**
 * An interface for file transfers, which covers both XEP-0096 and XEP-0234.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0096.html">XEP-0096: SI File Transferr</a>
 * @see <a href="http://xmpp.org/extensions/xep-0234.html">XEP-0234: Jingle File Transfer</a>
 */
public interface FileTransfer {

    /**
     * Gets the size, in bytes, of the data to be sent.
     *
     * @return The size.
     */
    long getSize();

    /**
     * Gets the name of the file that the Sender wishes to send.
     *
     * @return The file name.
     */
    String getName();

    /**
     * Gets the last modification time of the file.
     *
     * @return The date.
     */
    Date getDate();

    /**
     * Gets the MD5 sum of the file contents.
     *
     * @return The MD5 sum.
     */
    // TODO XEP-0300 !
    String getHash();

    /**
     * Gets a sender-generated description of the file.
     *
     * @return The description.
     */
    String getDescription();

    /**
     * Gets the range.
     *
     * @return The range.
     */
    Range getRange();
}
