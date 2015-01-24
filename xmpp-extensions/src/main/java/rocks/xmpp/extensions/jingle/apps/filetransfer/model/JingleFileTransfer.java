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

package rocks.xmpp.extensions.jingle.apps.filetransfer.model;

import rocks.xmpp.extensions.filetransfer.FileTransferOffer;
import rocks.xmpp.extensions.filetransfer.Range;
import rocks.xmpp.extensions.hashes.model.Hash;
import rocks.xmpp.extensions.jingle.apps.model.ApplicationFormat;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "description")
@XmlSeeAlso({JingleFileTransfer.Checksum.class})
public final class JingleFileTransfer extends ApplicationFormat {

    /**
     * urn:xmpp:jingle:apps:file-transfer:4
     */
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:file-transfer:4";

    @XmlElement(name = "file")
    private File file;

    private JingleFileTransfer() {
    }

    public JingleFileTransfer(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public static class File implements FileTransferOffer {

        @XmlElementRef
        private final List<Hash> hashes = new ArrayList<>();

        @XmlElement(name = "date")
        private Date date;

        @XmlElement(name = "desc")
        private String description;

        @XmlElement(name = "media-type")
        private String mediaType;

        @XmlElement(name = "name")
        private String name;

        @XmlElement(name = "size")
        private long size;

        private File() {
        }

        public File(String name, long size) {
            this.name = name;
            this.size = size;
        }

        public File(String name, long size, Date lastModified, String hash, String description) {
            this.name = name;
            this.size = size;
            this.date = lastModified;
            this.description = description;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Date getDate() {
            return date;
        }

        @Override
        public List<Hash> getHashes() {
            return Collections.unmodifiableList(hashes);
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Range getRange() {
            return null;
        }

        public String getMediaType() {
            return mediaType;
        }
    }

    @XmlRootElement(name = "checksum")
    public static final class Checksum {

        @XmlElement(name = "file")
        private File file;

        private Checksum() {
        }

        public Checksum(File file) {
            this.file = file;
        }

        /**
         * Gets the file.
         *
         * @return The file.
         */
        public File getFile() {
            return file;
        }
    }
}
