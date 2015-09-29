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
import rocks.xmpp.extensions.jingle.apps.filetransfer.model.errors.FileTransferError;
import rocks.xmpp.extensions.jingle.apps.model.ApplicationFormat;
import rocks.xmpp.extensions.jingle.model.Jingle;
import rocks.xmpp.util.adapters.InstantAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "description")
@XmlSeeAlso({JingleFileTransfer.Received.class, JingleFileTransfer.Checksum.class, FileTransferError.class})
public final class JingleFileTransfer extends ApplicationFormat {

    /**
     * urn:xmpp:jingle:apps:file-transfer:4
     */
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:file-transfer:4";

    private File file;

    private JingleFileTransfer() {
    }

    public JingleFileTransfer(File file) {
        this.file = file;
    }

    public final File getFile() {
        return file;
    }

    public static final class File implements FileTransferOffer {

        @XmlElementRef
        private final List<Hash> hashes = new ArrayList<>();

        @XmlJavaTypeAdapter(InstantAdapter.class)
        private Instant date;

        private String desc;

        @XmlElement(name = "media-type")
        private String mediaType;

        private String name;

        private long size;

        private File() {
        }

        public File(String name, long size) {
            this.name = name;
            this.size = size;
        }

        public File(String name, long size, Instant lastModified, String hash, String description) {
            this.name = name;
            this.size = size;
            this.date = lastModified;
            this.desc = description;
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
        public Instant getDate() {
            return date;
        }

        @Override
        public List<Hash> getHashes() {
            return Collections.unmodifiableList(hashes);
        }

        @Override
        public String getDescription() {
            return desc;
        }

        @Override
        public Range getRange() {
            return null;
        }

        public String getMediaType() {
            return mediaType;
        }
    }

    @XmlRootElement
    public static final class Received extends InformationalMessage {

        private Received() {
            super(null, null);
        }

        public Received(Jingle.Content.Creator creator, String name) {
            super(creator, name);
        }
    }

    @XmlRootElement
    public static final class Checksum extends InformationalMessage {

        private File file;

        private Checksum() {
            super(null, null);
            this.file = null;
        }

        public Checksum(Jingle.Content.Creator creator, String name, File file) {
            super(creator, name);
            this.file = file;
        }

        /**
         * Gets the file.
         *
         * @return The file.
         */
        public final File getFile() {
            return file;
        }
    }

    @XmlTransient
    private static abstract class InformationalMessage {

        @XmlAttribute
        private final Jingle.Content.Creator creator;

        @XmlAttribute
        private final String name;

        private InformationalMessage(Jingle.Content.Creator creator, String name) {
            this.creator = creator;
            this.name = name;
        }

        /**
         * Gets the creator.
         *
         * @return The creator.
         */
        public final Jingle.Content.Creator getCreator() {
            return creator;
        }

        /**
         * Gets the name.
         *
         * @return The name.
         */
        public final String getName() {
            return name;
        }
    }
}
