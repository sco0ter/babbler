package org.xmpp.extension.rpc;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
final class StructType {

    @XmlElement(name = "member")
    List<MemberType> values = new ArrayList<>();

    static final class MemberType {
        @XmlElement(name = "name")
        String name;

        @XmlElement(name = "value")
        Value value;

        private MemberType() {
        }

        MemberType(String name, Value value) {
            this.name = name;
            this.value = value;
        }
    }
}
