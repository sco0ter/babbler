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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "file")
public final class FileTransfer {

    static final String PROFILE = "http://jabber.org/protocol/si/profile/file-transfer";

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

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public String getHash() {
        return hash;
    }

    public String getDescription() {
        return description;
    }

    public Range getRange() {
        return range;
    }

    public static class Range {
        @XmlAttribute
        private long offset;

        @XmlAttribute
        private long length;
    }
}
