package org.xmpp.extension.jingle.apps.filetransfer;

import org.xmpp.extension.filetransfer.FileTransferOffer;
import org.xmpp.extension.filetransfer.Range;
import org.xmpp.extension.jingle.apps.ApplicationFormat;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "description")
public final class JingleFileTransfer extends ApplicationFormat {

    @XmlElementWrapper(name = "offer")
    @XmlElement(name = "file")
    private List<File> offeredFiles = new ArrayList<>();

    public List<File> getOffers() {
        return offeredFiles;
    }

    public static final class File implements FileTransferOffer {

        @XmlElement(name = "date")
        private Date date;

        @XmlElement(name = "desc")
        private String description;

        @XmlElement(name = "name")
        private String name;

        @XmlElement(name = "size")
        private long size;

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
        public String getHash() {
            return null;
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
}
