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

package org.xmpp.extension.stream.si.filetransfer;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * The implementation of the file transfer profile, i.e. the {@code <file/>} element.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "file")
public final class FileTransfer {

    @XmlAttribute(name = "size")
    private long size;

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "date")
    private Date date;

    @XmlAttribute(name = "hash")
    private String hash;

    @XmlElement(name = "desc")
    private String description;

    @XmlElement(name = "range")
    private Range range;

    private FileTransfer() {
    }

    public FileTransfer(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public FileTransfer(String name, long size, Date lastModified, String hash, String description, Range range) {
        this.name = name;
        this.size = size;
        this.date = lastModified;
        this.hash = hash;
        this.description = description;
        this.range = range;
    }

    /**
     * Gets the size, in bytes, of the data to be sent.
     *
     * @return The size.
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the name of the file that the Sender wishes to send.
     *
     * @return The file name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the last modification time of the file.
     *
     * @return The date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the MD5 sum of the file contents.
     *
     * @return The MD5 sum.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Gets a sender-generated description of the file.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the range.
     *
     * @return The range.
     */
    public Range getRange() {
        return range;
    }

    /**
     * Allows to do ranged transfers.
     */
    public static final class Range {
        @XmlAttribute
        private long offset;

        @XmlAttribute
        private long length;

        private Range() {
        }

        /**
         * @param offset Specifies the position, in bytes, to start transferring the file data from. This defaults to zero (0) if not specified.
         * @param length Specifies the number of bytes to retrieve starting at offset. This defaults to the length of the file from offset to the end.
         */
        public Range(long offset, long length) {
            this.offset = offset;
            this.length = length;
        }

        /**
         * Gets the position, in bytes, to start transferring the file data from. This defaults to zero (0) if not specified.
         *
         * @return The offset.
         */
        public long getOffset() {
            return offset;
        }

        /**
         * Gets the number of bytes to retrieve starting at offset. This defaults to the length of the file from offset to the end.
         *
         * @return The length.
         */
        public long getLength() {
            return length;
        }
    }
}
