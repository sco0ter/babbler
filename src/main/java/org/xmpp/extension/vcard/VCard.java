package org.xmpp.extension.vcard;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "vCard")
public final class VCard {

    @XmlElement(name = "VERSION")
    private String version;

    @XmlElement(name = "FN")
    private String formattedName;

    @XmlElement(name = "N")
    private Name name;

    @XmlElement(name = "NICKNAME")
    private String nickName;

    @XmlElement(name = "PHOTO")
    private String photo;

    @XmlElement(name = "BDAY")
    private Date birthday;

    @XmlElement(name = "URL")
    private String url;

    @XmlElement(name = "ORGANIZATION")
    private Organization organization;

    @XmlElement(name = "ADR")
    private Address address;

    @XmlElement(name = "JABBERID")
    private Jid jid;

    @XmlElement(name = "MAILER")
    private String mailer;

    @XmlElement(name = "TZ")
    private String timezone;

    @XmlElement(name = "GEO")
    private Geo geo;

    @XmlElement(name = "TITLE")
    private String title;

    @XmlElement(name = "ROLE")
    private String role;

    @XmlElement(name = "AGENT")
    private String agent;

    @XmlElement(name = "LOGO")
    private String logo;

    @XmlElement(name = "DESC")
    private String desc;

    public static class Geo {

        @XmlElement(name = "LAT")
        private String latitude;

        @XmlElement(name = "LON")
        private String longitude;
    }

    public static class Address {

        @XmlElement(name = "HOME")
        private String home;

        @XmlElement(name = "WORK")
        private String work;

        @XmlElement(name = "POSTAL")
        private String postal;

        @XmlElement(name = "PARCEL")
        private String parcel;

        // TODO DOM|INTL

        @XmlElement(name = "PREF")
        private String pref;

        @XmlElement(name = "POBOX")
        private String poBox;

        @XmlElement(name = "EXTADD")
        private String extAdd;

        @XmlElement(name = "STREET")
        private String street;

        @XmlElement(name = "LOCALITY")
        private String locality;

        @XmlElement(name = "REGION")
        private String region;

        @XmlElement(name = "PCODE")
        private String postalCode;

        @XmlElement(name = "CTRY")
        private String country;
    }

    public static class Label {
        @XmlElement(name = "HOME")
        private String home;

        @XmlElement(name = "WORK")
        private String work;

        @XmlElement(name = "POSTAL")
        private String postal;

        @XmlElement(name = "PARCEL")
        private String parcel;

        // TODO DOM|INTL

        @XmlElement(name = "PREF")
        private String pref;

        @XmlElement(name = "LINE")
        private String line;
    }

    public static class TelephoneNumber {
        //        HOME?,
        //        WORK?,
        //        VOICE?,
        //        FAX?,
        //        PAGER?,
        //        MSG?,
        //        CELL?,
        //        VIDEO?,
        //        BBS?,
        //        MODEM?,
        //        ISDN?,
        //        PCS?,

        @XmlElement(name = "PREF")
        private String pref;

        @XmlElement(name = "NUMBER")
        private String number;

    }

    public static class Email {
        @XmlElement(name = "HOME")
        private String home;

        @XmlElement(name = "WORK")
        private String work;

        //        INTERNET?,
        //        PREF?,
        //        X400?,
        //        USERID
    }

    public static class Organization {
        @XmlElement(name = "ORGNAME")
        private String organizationName;

        @XmlElement(name = "ORGUNIT")
        private String orgUnit;
    }

    public static class Name {
        @XmlElement(name = "FAMILY")
        private String familyName;

        @XmlElement(name = "GIVEN")
        private String givenName;

        @XmlElement(name = "MIDDLE")
        private String middleName;

        @XmlElement(name = "PREFIX")
        private String prefix;

        @XmlElement(name = "SUFFIX")
        private String suffix;
    }
}
