package org.xmpp.extension.registration;

import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class Registration {

    private String registered;

    @XmlElement
    private String instructions;

    @XmlElement
    private String username;

    @XmlElement
    private String nick;

    @XmlElement
    private String password;

    @XmlElement
    private String name;

    @XmlElement
    private String first;

    @XmlElement
    private String last;

    @XmlElement
    private String email;

    @XmlElement
    private String address;

    @XmlElement
    private String city;

    @XmlElement
    private String state;

    @XmlElement
    private String zip;

    @XmlElement
    private String phone;

    @XmlElement
    private String url;

    @XmlElement
    private String date;

    @XmlElement
    private String misc;

    @XmlElement
    private String text;

    @XmlElement
    private String key;

    @XmlElement
    private String remove;

    @XmlElement
    private DataForm dataForm;

    public Registration() {

    }

    public Registration(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
