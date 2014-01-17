package org.xmpp.extension.featurenegotiation;

import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "feature")
public final class FeatureNegotiation {

    @XmlElement
    private DataForm dataForm;

}
