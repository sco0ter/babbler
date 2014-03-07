package org.xmpp.extension.stream.bytestreams;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Christian Schudt
 */
public class StreamHostUsed {

    @XmlAttribute(name = "jid")
    private Jid jid;
}
