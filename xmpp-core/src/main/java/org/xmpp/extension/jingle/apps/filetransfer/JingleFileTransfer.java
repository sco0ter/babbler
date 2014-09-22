package org.xmpp.extension.jingle.apps.filetransfer;

import org.xmpp.extension.filetransfer.FileTransferOffer;
import org.xmpp.extension.filetransfer.Range;
import org.xmpp.extension.hashes.Hash;
import org.xmpp.extension.jingle.apps.ApplicationFormat;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "description")
@XmlSeeAlso({JingleFileTransfer.Checksum.class})
public final class JingleFileTransfer extends ApplicationFormat {

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
            return hashes;
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
