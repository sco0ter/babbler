package org.xmpp.extension.stream.bytestreams;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public class Query {

    @XmlElement(name = "streamhost")
    private List<StreamHost> streamHosts;

    @XmlElement(name = "streamhost-used")
    private StreamHostUsed streamHostUsed;

    @XmlAttribute(name = "dstaddr")
    private String dstaddr;

    @XmlElement(name = "activate")
    private String activate;

    @XmlAttribute(name = "mode")
    private Mode mode;

    @XmlAttribute(name = "sid")
    private String sid;

    public enum Mode {
        @XmlEnumValue("tcp")
        TCP,
        @XmlEnumValue("udp")
        UDP
    }
}
