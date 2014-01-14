package org.xmpp.extension.registration;

import org.xmpp.stream.Feature;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "register", namespace = "http://jabber.org/features/iq-register")
public final class Register extends Feature {
    private Register() {
    }
}
