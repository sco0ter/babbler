package org.xmpp.extension.rpc;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
final class ArrayType {

    @XmlElementWrapper(name = "data")
    @XmlElement(name = "value")
    List<Value> values = new ArrayList<>();

}
