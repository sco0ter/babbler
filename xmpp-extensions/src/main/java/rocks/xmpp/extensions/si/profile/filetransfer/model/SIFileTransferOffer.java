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

package rocks.xmpp.extensions.si.profile.filetransfer.model;

import rocks.xmpp.extensions.filetransfer.FileTransferOffer;
import rocks.xmpp.extensions.filetransfer.Range;
import rocks.xmpp.extensions.hashes.model.Hash;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * The implementation of the file transfer profile, i.e. the {@code <file/>} element.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0096.html">XEP-0096: SI File Transfer</a>
 * @see <a href="http://xmpp.org/extensions/xep-0096.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "file")
public final class SIFileTransferOffer implements FileTransferOffer {

    public static final String NAMESPACE = "http://jabber.org/protocol/si/profile/file-transfer";

    @XmlAttribute(name = "size")
    private Long size;

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "date")
    private Date date;

    @XmlAttribute(name = "hash")
    private String hash;

    @XmlElement(name = "desc")
    private String description;

    @XmlElement(name = "range")
    private SIRange range;

    private SIFileTransferOffer() {
    }

    public SIFileTransferOffer(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public SIFileTransferOffer(String name, long size, Date lastModified, String hash, String description, SIRange range) {
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
        return size != null ? size : 0;
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
    public List<Hash> getHashes() {
        if (hash != null) {
            // XEP-0096 seem to be hex encoded, while XEP-300 are base64 encoded. Convert from hex to byte array.
            return Arrays.asList(new Hash(new BigInteger(hash, 16).toByteArray(), "md5"));
        } else {
            return Collections.emptyList();
        }
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
    public static final class SIRange implements Range {
        @XmlAttribute
        private long offset;

        @XmlAttribute
        private long length;

        private SIRange() {
        }

        /**
         * @param offset Specifies the position, in bytes, to start transferring the file data from. This defaults to zero (0) if not specified.
         * @param length Specifies the number of bytes to retrieve starting at offset. This defaults to the length of the file from offset to the end.
         */
        public SIRange(long offset, long length) {
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
