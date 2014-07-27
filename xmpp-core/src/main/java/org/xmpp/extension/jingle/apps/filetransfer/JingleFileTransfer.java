package org.xmpp.extension.jingle.apps.filetransfer;

import org.xmpp.extension.filetransfer.FileTransferOffer;
import org.xmpp.extension.filetransfer.Range;
import org.xmpp.extension.hashes.Hash;
import org.xmpp.extension.jingle.apps.ApplicationFormat;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "description")
@XmlSeeAlso({JingleFileTransfer.Abort.class, JingleFileTransfer.Received.class, JingleFileTransfer.Checksum.class})
public final class JingleFileTransfer extends ApplicationFormat {

    @XmlElementWrapper(name = "request")
    @XmlElement(name = "file")
    private final List<File> requestedFiles = new ArrayList<>();

    @XmlElementWrapper(name = "offer")
    @XmlElement(name = "file")
    private final List<File> offeredFiles = new ArrayList<>();

    public static JingleFileTransfer offer(File... files) {
        JingleFileTransfer jingleFileTransfer = new JingleFileTransfer();
        jingleFileTransfer.offeredFiles.addAll(Arrays.asList(files));
        return jingleFileTransfer;
    }

    public static JingleFileTransfer request(File... files) {
        JingleFileTransfer jingleFileTransfer = new JingleFileTransfer();
        jingleFileTransfer.requestedFiles.addAll(Arrays.asList(files));
        return jingleFileTransfer;
    }

    public List<File> getOffers() {
        return offeredFiles;
    }

    public static class File implements FileTransferOffer {

        @XmlElementRef
        private final List<Hash> hashes = new ArrayList<>();

        @XmlElement(name = "date")
        private Date date;

        @XmlElement(name = "desc")
        private String description;

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
    }

    @XmlRootElement(name = "abort")
    public static final class Abort {
        @XmlElement(name = "file")
        private File file;

        private Abort() {
        }

        public Abort(File file) {
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

    @XmlRootElement(name = "received")
    public static final class Received {

        @XmlElement(name = "file")
        private File file;

        private Received() {
        }

        public Received(File file) {
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
