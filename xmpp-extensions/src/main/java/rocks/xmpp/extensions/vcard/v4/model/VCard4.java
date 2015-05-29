/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package rocks.xmpp.extensions.vcard.v4.model;

import rocks.xmpp.core.util.adapters.LocalDateAdapter;
import rocks.xmpp.extensions.vcard.VCard;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "vcard")
public final class VCard4 implements VCard {

    /**
     * urn:ietf:params:xml:ns:vcard-4.0
     */
    public static final String NAMESPACE = "urn:ietf:params:xml:ns:vcard-4.0";

    /*
     +-------------+--------------------------------------------------+
     | Cardinality | Meaning                                          |
     +-------------+--------------------------------------------------+
     |      1      | Exactly one instance per vCard MUST be present.  |
     |      *1     | Exactly one instance per vCard MAY be present.   |
     |      1*     | One or more instances per vCard MUST be present. |
     |      *      | One or more instances per vCard MAY be present.  |
     +-------------+--------------------------------------------------+
     */

    /**
     * Cardinality:  1*
     */
    @XmlElement(name = "fn")
    @XmlJavaTypeAdapter(TextAdapter.class)
    private String formattedName;

    /**
     * Cardinality:  *1
     */
    @XmlElement(name = "n")
    private Name name;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "nickname")
    @XmlJavaTypeAdapter(TextAdapter.class)
    private String nickname;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "photo")
    private List<Photo> photos;

    /**
     * Cardinality:  *1
     */
    @XmlElement(name = "bday")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private LocalDate birthday;

    /**
     * Cardinality:  *1
     */
    private Date anniversary;

    /**
     * Cardinality:  *1
     */
    private Gender gender;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "adr")
    private final List<Address> addresses = new ArrayList<>();

    /**
     * Cardinality:  *
     */
    private List<String> telephone;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "email")
    private List<EmailAddress> email;

    /**
     * Cardinality:  *
     */
    private List<URI> impp;

    /**
     * Cardinality:  *
     */
    private List<String> language;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "tz")
    @XmlJavaTypeAdapter(ZoneIdAdapter.class)
    private ZoneId timeZone;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "geo")
    private URI geo;

    /**
     * Cardinality:  *
     */
    private String title;

    /**
     * Cardinality:  *
     */
    private String role;

    /**
     * Cardinality:  *
     */
    private List<URI> logo;

    /**
     * Cardinality:  *
     */
    private Organization org;

    /**
     * Cardinality:  *
     */
    private List<String> member;

    /**
     * Cardinality:  *
     */
    private List<URI> related;

    /**
     * Cardinality:  *
     */
    private List<String> categories;

    /**
     * Cardinality:  *
     */
    @XmlElement(name = "note")
    @XmlJavaTypeAdapter(TextAdapter.class)
    private String note;

    /**
     * Cardinality:  *1
     */
    private String prodId;

    /**
     * Cardinality:  *1
     */
    private String revision;

    /**
     * Cardinality:  *
     */
    private List<URI> sound;

    /**
     * Cardinality:  *1
     */
    private String uid;

    /**
     * Cardinality:  *
     */
    private List<String> clientidmap;

    /**
     * Cardinality:  *
     */
    @XmlJavaTypeAdapter(UriAdapter.class)
    private URL url;

    /**
     * Cardinality:  1
     */
    private String version = "4.0";

    /**
     * Cardinality:  *
     */
    private URI key;

    /**
     * Cardinality:  *
     */
    private List<URI> fbUrl;

    /**
     * Cardinality:  *
     */
    private List<URI> caladrUri;

    /**
     * Cardinality:  *
     */
    private List<URI> calUri;

    @Override
    public List<EmailAddress> getEmailAddresses() {
        return email;
    }

    @Override
    public String getFormattedName() {
        return formattedName;
    }

    @Override
    public Name getName() {
        return name;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    @Override
    public LocalDate getBirthday() {
        return birthday;
    }

    @Override
    public List<Address> getAddresses() {
        return addresses;
    }

    @Override
    public List<TelephoneNumber> getTelephoneNumbers() {
        return null;
    }

    @Override
    public ZoneId getTimeZone() {
        return timeZone;
    }

    @Override
    public URI getGeo() {
        return geo;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public Organization getOrganization() {
        return org;
    }

    @Override
    public List<String> getCategories() {
        return categories;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public String getProductId() {
        return prodId;
    }

    @Override
    public String getRevision() {
        return revision;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public Gender getGender() {
        return gender;
    }

    public enum Type {
        @XmlEnumValue("work")
        WORK,
        @XmlEnumValue("home")
        HOME
    }

    public static final class Gender {

        private Sex sex;

        private String identity;

        public Sex getSex() {
            return sex;
        }
    }

    public enum Sex {
        @XmlEnumValue("M")
        MALE,
        @XmlEnumValue("F")
        FEMALE,
        @XmlEnumValue("O")
        OTHER,
        @XmlEnumValue("N")
        NONE,
        @XmlEnumValue("U")
        UNKNOWN
    }

    public enum CommunicationType {
        @XmlEnumValue("work")
        WORK,
        @XmlEnumValue("home")
        HOME,
        @XmlEnumValue("text")
        TEXT,
        @XmlEnumValue("voice")
        VOICE,
        @XmlEnumValue("fax")
        FAX,
        @XmlEnumValue("cell")
        CELL,
        @XmlEnumValue("video")
        VIDEO,
        @XmlEnumValue("pager")
        PAGER,
        @XmlEnumValue("textphone")
        TEXTPHONE
    }

    public static final class Name implements VCard.Name {
        @XmlElement(name = "surname")
        private String surname;

        @XmlElement(name = "given")
        private String givenName;

        @XmlElement(name = "additional")
        private String additionalName;

        @XmlElement(name = "prefix")
        private String prefix;

        @XmlElement(name = "suffix")
        private String suffix;

        @Override
        public String getFamilyName() {
            return surname;
        }

        @Override
        public String getGivenName() {
            return givenName;
        }

        @Override
        public String getMiddleName() {
            return additionalName;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public String getSuffix() {
            return suffix;
        }
    }

    public static final class Photo extends Parameterizable {

        private URI uri;

        public URI getUri() {
            return uri;
        }
    }

    private static final class Address implements VCard.Address {

        @XmlElement(name = "pobox")
        private String postBox;

        @XmlElement(name = "ext")
        private String ext;

        @XmlElement(name = "street")
        private String street;

        @XmlElement(name = "locality")
        private String locality;

        @XmlElement(name = "region")
        private String region;

        @XmlElement(name = "code")
        private String postalCode;

        @XmlElement(name = "country")
        private String country;

        @Override
        public String getPostOfficeBox() {
            return postBox;
        }

        @Override
        public String getExtendedAddress() {
            return ext;
        }

        @Override
        public String getStreet() {
            return street;
        }

        @Override
        public String getCity() {
            return locality;
        }

        @Override
        public String getRegion() {
            return region;
        }

        @Override
        public String getPostalCode() {
            return postalCode;
        }

        @Override
        public String getCountry() {
            return country;
        }
    }

    @XmlTransient
    private static abstract class Parameterizable {
        @XmlElementWrapper(name = "parameters")
        @XmlElements({
                @XmlElement(name = "language", type = Parameter.LanguageParameter.class),
                @XmlElement(name = "type", type = Parameter.TypeParameter.class),
                @XmlElement(name = "label", type = Parameter.LabelParameter.class)})
        private List<Parameter> parameters;

        public List<Parameter> getParameters() {
            return parameters;
        }
    }

    public static final class EmailAddress extends Parameterizable implements VCard.Email {
        @XmlElement(name = "text")
        private String text;

        @Override
        public String getEmail() {
            return text;
        }

    }

    public static final class Organization implements VCard.Organization {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public List<String> getUnits() {
            return null;
        }
    }

    @XmlTransient
    public static abstract class Parameter {

        public static final class LanguageParameter extends Parameter {

        }

        public static final class PreferenceParameter extends Parameter {

        }

        public static final class AlternativeId extends Parameter {
            private String text;
        }

        public static final class PidParameter extends Parameter {

        }

        public static final class TypeParameter extends Parameter {

            @XmlElement(name = "text")
            private List<Type> types;

            public List<Type> getTypes() {
                return types;
            }
        }

        public static final class MediaTypeParameter extends Parameter {
            private String text;
        }

        public static final class CalendarScaleParameter extends Parameter {
            private String text;
        }

        public static final class SortAsParameter extends Parameter {
            private String text;
        }

        public static final class GeoParameter extends Parameter {
            private URI uri;
        }

        public static final class TimeZoneParameter extends Parameter {
            private String text;
        }

        public static final class LabelParameter extends Parameter {
            @XmlElement(name = "text")
            private String text;
        }
    }

    private static class TextValue {
        @XmlElement(name = "text")
        private String text;

        private TextValue() {
        }

        private TextValue(String text) {
            this.text = text;
        }
    }

    private static class DateValue {
        @XmlElement(name = "date")
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate date;

        private DateValue() {
        }

        private DateValue(LocalDate date) {
            this.date = date;
        }
    }

    private static class UriValue {
        @XmlElement(name = "uri")
        private URL uri;

        private UriValue() {
        }

        private UriValue(URL uri) {
            this.uri = uri;
        }
    }

    private static final class TextAdapter extends XmlAdapter<TextValue, String> {

        @Override
        public String unmarshal(TextValue v) throws Exception {
            return v != null ? v.text : null;
        }

        @Override
        public TextValue marshal(String v) throws Exception {
            return new TextValue(v);
        }
    }

    private static final class DateAdapter extends XmlAdapter<DateValue, LocalDate> {

        @Override
        public LocalDate unmarshal(DateValue v) throws Exception {
            return v != null ? v.date : null;
        }

        @Override
        public DateValue marshal(LocalDate v) throws Exception {
            return new DateValue(v);
        }
    }

    private static final class ZoneIdAdapter extends XmlAdapter<TextValue, ZoneId> {

        @Override
        public ZoneId unmarshal(TextValue v) throws Exception {
            return v != null ? ZoneId.of(v.text) : null;
        }

        @Override
        public TextValue marshal(ZoneId v) throws Exception {
            return new TextValue(v.toString());
        }
    }

    private static final class UriAdapter extends XmlAdapter<UriValue, URL> {

        @Override
        public URL unmarshal(UriValue v) throws Exception {
            return v != null ? v.uri : null;
        }

        @Override
        public UriValue marshal(URL v) throws Exception {
            return new UriValue(v);
        }
    }
}
