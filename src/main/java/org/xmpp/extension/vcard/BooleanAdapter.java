package org.xmpp.extension.vcard;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Christian Schudt
 */
final class BooleanAdapter extends XmlAdapter<String, Boolean> {

    @Override
    public Boolean unmarshal(String v) throws Exception {
        return v != null;
    }

    @Override
    public String marshal(Boolean v) throws Exception {
        return (v != null && v) ? "" : null;
    }
}
