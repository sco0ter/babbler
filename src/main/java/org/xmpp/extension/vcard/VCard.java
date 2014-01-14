package org.xmpp.extension.vcard;

import org.xmpp.Jid;
import org.xmpp.util.JidAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "vCard")
public final class VCard {

    /**
     * To specify the version of the vCard specification used to format this vCard.
     * Some Jabber implementations add a 'version' attribute to the {@code <vCard/>} element, with the value set at "2.0" or "3.0". The DTD is incorrect, and the examples in draft-dawson-vcard-xml-dtd-01 clearly show that version information is to be included by means of a 'version' attribute, not the {@code <VERSION/>} element as defined in the DTD. However, to conform to draft-dawson-vcard-xml-dtd-01, the value should be "3.0", not "2.0".
     */
    @XmlAttribute(name = "version")
    private String version = "3.0";

    /**
     * To specify the formatted text corresponding to the name of the object the vCard represents.
     */
    @XmlElement(name = "FN")
    private String formattedName;

    /**
     * To specify the components of the name of the object the vCard represents.
     */
    @XmlElement(name = "N")
    private Name name;

    /**
     * To specify the text corresponding to the nickname of the object the vCard represents.
     */
    @XmlElement(name = "NICKNAME")
    private String nickName;

    /**
     * To specify an image or photograph information that annotates some aspect of the object the vCard represents.
     */
    @XmlElement(name = "PHOTO")
    private String photo;

    /**
     * To specify the birth date of the object the vCard represents.
     */
    @XmlElement(name = "BDAY")
    private Date birthday;

    /**
     * To specify a uniform resource locator associated with the object that the vCard refers to.
     */
    @XmlElement(name = "URL")
    private String url;

    /**
     * To specify the organizational name and units associated with the vCard.
     */
    @XmlElement(name = "ORG")
    private Organization organization;

    /**
     * To specify the components of the delivery address for the vCard object.
     */
    @XmlElement(name = "ADR")
    private List<Address> address;

    /**
     * To specify the telephone number for telephony
     * communication with the object the vCard represents.
     */
    @XmlElement(name = "TEL")
    private List<CommunicationData> communicationData;

    /**
     * To specify the electronic mail address for
     * communication with the object the vCard represents.
     */
    @XmlElement(name = "EMAIL")
    private List<Email> emails;

    /**
     * To specify the formatted text corresponding to delivery address of the object the vCard represents.
     */
    @XmlElement(name = "LABEL")
    private Label label;

    @XmlJavaTypeAdapter(JidAdapter.class)
    @XmlElement(name = "JABBERID")
    private Jid jid;

    /**
     * To specify the type of electronic mail software that is used by the individual associated with the vCard.
     */
    @XmlElement(name = "MAILER")
    private String mailer;

    /**
     * To specify information related to the time zone of the object the vCard represents.
     */
    @XmlElement(name = "TZ")
    private String timezone;

    /**
     * To specify information related to the global positioning of the object the vCard represents.
     */
    @XmlElement(name = "GEO")
    private Geo geo;

    /**
     * To specify the job title, functional position or function of the object the vCard represents.
     */
    @XmlElement(name = "TITLE")
    private String title;

    /**
     * To specify information concerning the role, occupation, or business category of the object the vCard represents.
     */
    @XmlElement(name = "ROLE")
    private String role;

    /**
     * To specify information about another person who will act on behalf of the individual or resource associated with the vCard.
     */
    @XmlElement(name = "AGENT")
    private String agent;

    /**
     * To specify a graphic image of a logo associated with the object the vCard represents.
     */
    @XmlElement(name = "LOGO")
    private String logo;

    /**
     * To specify application category information about the vCard.
     */
    @XmlElement(name = "CATEGORIES")
    private List<String> categories;

    /**
     * To specify supplemental information or a comment that is associated with the vCard.
     */
    @XmlElement(name = "NOTE")
    private String note;

    /**
     * To specify the identifier for the product that created the vCard object.
     */
    @XmlElement(name = "PRODID")
    private String productId;

    /**
     * To specify revision information about the current vCard.
     */
    @XmlElement(name = "REV")
    private Date revision;

    /**
     * To specify the family name or given name text to be used for national-language-specific sorting of the FN and N types.
     */
    @XmlElement(name = "SORT-STRING")
    private String sortString;

    /**
     * To specify a digital sound content information that annotates some aspect of the vCard. By default this type is used to
     * specify the proper pronunciation of the name type value of the vCard.
     */
    @XmlElement(name = "SOUND")
    private String sound;

    /**
     * To specify a value that represents a globally unique identifier corresponding to the individual or resource associated
     * with the vCard.
     */
    @XmlElement(name = "UID")
    private String uid;

    /**
     * Type purpose: To specify a public key or authentication certificate
     * associated with the object that the vCard represents.
     */
    @XmlElement(name = "KEY")
    private String key;

    @XmlElement(name = "DESC")
    private String desc;

    public static final class Geo {

        @XmlElement(name = "LAT")
        private String latitude;

        @XmlElement(name = "LON")
        private String longitude;
    }

    public static final class Address extends AbstractAddress {
        // The default is "TYPE=intl,postal,parcel,work".

        @XmlElement(name = "POBOX")
        private String postOfficeBox;

        @XmlElement(name = "EXTADD")
        private String extendedAddress;

        @XmlElement(name = "STREET")
        private String street;

        @XmlElement(name = "LOCALITY")
        private String city;

        @XmlElement(name = "REGION")
        private String region;

        @XmlElement(name = "PCODE")
        private String postalCode = "";

        @XmlElement(name = "CTRY")
        private String country;

        public Address() {
            super(false, false, true, true, true, true);
        }

        public Address(boolean preferred, boolean home, boolean work, boolean postal, boolean parcel, boolean international, String postOfficeBox, String extendedAddress, String street, String city, String region, String postalCode, String country) {
            super(preferred, home, work, postal, parcel, international);
            this.postOfficeBox = postOfficeBox;
            this.extendedAddress = extendedAddress;
            this.street = street;
            this.city = city;
            this.region = region;
            this.postalCode = postalCode;
            this.country = country;
        }

        public Address(boolean preferred, boolean home, boolean work, boolean postal, boolean parcel, boolean international) {
            super(preferred, home, work, postal, parcel, international);
        }
    }

    public static class Label extends AbstractAddress {
        @XmlElement(name = "LINE")
        private List<String> line;

        public Label(boolean preferred, boolean home, boolean work, boolean postal, boolean parcel, boolean international) {
            super(preferred, home, work, postal, parcel, international);
        }
    }

    @XmlTransient
    private static abstract class AbstractAddress extends ContactData {

        @XmlElement(name = "POSTAL")
        private Boolean postal;

        @XmlElement(name = "PARCEL")
        private Boolean parcel;

        @XmlElement(name = "INTL")
        private Boolean international;

        @XmlElement(name = "DOM")
        private Boolean domestic;

        public AbstractAddress(boolean preferred, boolean home, boolean work, boolean postal, boolean parcel, boolean international) {
            super(preferred, home, work);
            this.postal = postal;
            this.parcel = parcel;
            setInternational(international);
        }

        /**
         * Indicates whether this address is domestic or international.
         *
         * @return
         */
        public boolean isInternational() {
            return international != null && international && domestic != null && !domestic;
        }

        public void setInternational(boolean international) {
            this.international = international;
            this.domestic = !international;
        }

        /**
         * Indicates a postal delivery address.
         *
         * @return
         */
        public boolean isPostal() {
            return postal != null && postal;
        }

        public void setPostal(boolean postal) {
            this.postal = postal;
        }

        /**
         * Indicates a parcel delivery address.
         *
         * @return
         */
        public boolean isParcel() {
            return parcel != null && parcel;
        }

        public void setParcel(boolean parcel) {
            this.parcel = parcel;
        }
    }

    @XmlTransient
    private static abstract class ContactData extends Preferable {
        @XmlElement(name = "HOME")
        private Boolean home;

        @XmlElement(name = "WORK")
        private Boolean work;

        public ContactData(boolean preferred, boolean home, boolean work) {
            super(preferred);
            this.home = home;
            this.work = work;
        }

        /**
         * Indicates a delivery address for a residence.
         *
         * @return
         */
        public boolean isHome() {
            return home != null && home;
        }

        public void setHome(boolean home) {
            this.home = home;
        }

        /**
         * Indicate delivery address for a place of work.
         *
         * @return
         */
        public boolean isWork() {
            return work != null && work;
        }

        public void setWork(boolean work) {
            this.work = work;
        }
    }

    @XmlTransient
    private static abstract class Preferable {
        @XmlElement(name = "PREF")
        private Boolean pref;

        protected Preferable(boolean preferred) {
            this.pref = preferred;
        }

        /**
         * Indicates the preferred delivery address when more than one address is specified.
         *
         * @return
         */
        public boolean isPreferred() {
            return pref != null && pref;
        }

        public void setPreferred(boolean preferred) {
            this.pref = preferred;
        }
    }

    public static class CommunicationData extends ContactData {
        /**
         * to indicate the telephone number has voice messaging support
         */
        @XmlElement(name = "MSG")
        private Boolean msg;

        /**
         * to indicate a voice telephone number
         */
        @XmlElement(name = "VOICE")
        private Boolean voice;

        /**
         * to indicate a facsimile telephone number
         */
        @XmlElement(name = "FAX")
        private Boolean fax;

        /**
         * to indicate a cellular telephone number
         */
        @XmlElement(name = "CELL")
        private Boolean cell;

        /**
         * to indicate a video conferencing telephone number
         */
        @XmlElement(name = "VIDEO")
        private Boolean video;

        /**
         * to indicate a paging device telephone number
         */
        @XmlElement(name = "PAGER")
        private Boolean pager;

        /**
         * to indicate a bulletin board system telephone number
         */
        @XmlElement(name = "BBS")
        private Boolean bbs;

        /**
         * to indicate a MODEM connected telephone number
         */
        @XmlElement(name = "MODEM")
        private Boolean modem;

        /**
         * to indicate a personal communication services telephone number
         */
        @XmlElement(name = "PCS")
        private Boolean pcs;

        /**
         * to indicate an ISDN service telephone number
         */
        @XmlElement(name = "ISDN")
        private Boolean isdn;

        /**
         *
         */
        @XmlElement(name = "NUMBER")
        private String number;

        public CommunicationData() {
            super(false, false, true);
        }

        public CommunicationData(String number, boolean preferred) {
            this(number, preferred, false, true);
        }

        public CommunicationData(String number, boolean preferred, boolean home, boolean work) {
            this(number, preferred, home, work, false, true, false, false, false, false, false, false, false, false);
        }

        public CommunicationData(String number, boolean preferred, boolean home, boolean work, boolean message, boolean voice, boolean fax, boolean cell, boolean video, boolean pager, boolean bbs, boolean modem, boolean pcs, boolean isdn) {
            super(preferred, home, work);
            // If no telephone number is included in a <TEL/> element, an empty <NUMBER/> child MUST be included.
            this.number = number == null ? "" : number;
            this.voice = voice;
            this.msg = message;
            this.fax = fax;
            this.cell = cell;
            this.video = video;
            this.pager = pager;
            this.bbs = bbs;
            this.modem = modem;
            this.pcs = pcs;
            this.isdn = isdn;
        }
    }

    public static final class Email extends Preferable {

        /**
         * to indicate an Internet addressing type
         */
        @XmlElement(name = "INTERNET")
        private Boolean internet;

        /**
         * to indicate a X.400 addressing type
         */
        @XmlElement(name = "X400")
        private Boolean x400;

        /**
         *
         */
        @XmlElement(name = "USERID")
        private String userId;

        private Email() {
            super(false);
        }

        public Email(String email, boolean preferred) {
            this(email, preferred, true, false);
        }

        public Email(String email, boolean preferred, boolean internet, boolean x400) {
            super(preferred);
            this.userId = email;
            this.internet = internet;
            this.x400 = x400;
        }

        public boolean isInternet() {
            return internet != null && internet;
        }

        public void setInternet(boolean internet) {
            this.internet = internet;
        }

        public boolean isX400() {
            return x400 != null && x400;
        }

        public void setX400(boolean x400) {
            this.x400 = x400;
        }

        public String getEmail() {
            return userId;
        }

        public void setEmail(String email) {
            this.userId = email;
        }
    }

    public static final class Organization {

        @XmlElement(name = "ORGNAME")
        private String organizationName;

        @XmlElement(name = "ORGUNIT")
        private List<String> orgUnits = new ArrayList<>();

        private Organization() {
        }

        public Organization(String organizationName, String... orgUnit) {
            this.organizationName = organizationName;
            this.orgUnits = Arrays.asList(orgUnit);
        }

        public String getOrganizationName() {
            return organizationName;
        }

        public void setOrganizationName(String organizationName) {
            this.organizationName = organizationName;
        }

        public List<String> getOrgUnits() {
            return orgUnits;
        }
    }

    public static final class Name {
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

        private Name() {
        }

        public Name(String familyName, String givenName, String middleName) {
            this(familyName, givenName, middleName, null, null);
        }

        public Name(String familyName, String givenName, String middleName, String prefix, String suffix) {
            this.familyName = familyName;
            this.givenName = givenName;
            this.middleName = middleName;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }
    }
}
