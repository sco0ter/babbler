package org.xmpp.extension.muc.owner;

import org.xmpp.extension.data.DataForm;

import javax.xml.bind.annotation.XmlElementRef;

/**
 * @author Christian Schudt
 */
public final class Query {

    @XmlElementRef
    private DataForm dataForm;

    @XmlElementRef(name = "destroy")
    private Destroy destroy;
}
